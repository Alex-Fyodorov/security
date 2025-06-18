package com.globus.session_tracing.services;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.exceptions.SessionNotFoundException;
import com.globus.session_tracing.exceptions.SessionsOperationsException;
import com.globus.session_tracing.exceptions.TooManySessionsException;
import com.globus.session_tracing.repositiries.RedisRepository;
import com.globus.session_tracing.repositiries.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SessionTracingServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private SessionTracingService service;

    private final int pageSize = 10;
    private final int sessionLifeDays = 5;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        setField("pageSize", pageSize);
        setField("sessionLifeDays", sessionLifeDays);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = SessionTracingService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }

    @Test
    void findAll() {
        Integer userId = 1;
        LocalDateTime minLogin = LocalDateTime.now().minusDays(2);
        String method = "login";
        Boolean isActive = true;
        Integer page = 1;
        String sort = "loginTime";

        Page<Session> pageResult = new PageImpl<>(Collections.emptyList());

        // Исправлено!
        when(sessionRepository.findAll(
                ArgumentMatchers.<Specification<Session>>any(),
                any(PageRequest.class)))
                .thenReturn(pageResult);

        Page<Session> result = service.findAll(userId, minLogin, method, isActive, page, sort);

        assertNotNull(result);
        verify(sessionRepository, times(1)).findAll(
                ArgumentMatchers.<Specification<Session>>any(),
                any(PageRequest.class));
    }

    @Test
    void findBySessionId_found() {
        Session session = new Session();
        session.setId(1L);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        Session found = service.findBySessionId(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void findBySessionId_notFound() {
        when(sessionRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(SessionNotFoundException.class, () -> service.findBySessionId(2L));
    }

    @Test
    void save_whenUnderSessionLimit() {
        Session session = new Session();
        session.setUserId(10);
        when(sessionRepository.sessionCount(10)).thenReturn(2);
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> {
            Session s = invocation.getArgument(0);
            s.setId(42L);
            return s;
        });

        Session saved = service.save(session);

        assertNotNull(saved.getId());
        verify(redisRepository, times(1)).add(saved);
    }

    @Test
    void save_whenTooManySessions() {
        Session session = new Session();
        session.setUserId(11);
        when(sessionRepository.sessionCount(11)).thenReturn(3);

        assertThrows(TooManySessionsException.class, () -> service.save(session));
        verify(redisRepository, never()).add(any());
    }

    @Test
    void logout_success() {
        Session session = new Session();
        session.setId(44L);
        session.setIsActive(true);
        when(sessionRepository.findById(44L)).thenReturn(Optional.of(session));
        doNothing().when(sessionRepository).logout(44L);
        when(redisRepository.delete(44L)).thenReturn(true);

        assertDoesNotThrow(() -> service.logout(44L));
        verify(sessionRepository, times(1)).logout(44L);
        verify(redisRepository, times(1)).delete(44L);
    }

    @Test
    void logout_notFound() {
        when(sessionRepository.findById(77L)).thenReturn(Optional.empty());
        assertThrows(SessionNotFoundException.class, () -> service.logout(77L));
    }

    @Test
    void logout_closedSession() {
        Session session = new Session();
        session.setId(55L);
        session.setIsActive(false);
        when(sessionRepository.findById(55L)).thenReturn(Optional.of(session));

        assertThrows(SessionsOperationsException.class, () -> service.logout(55L));
        verify(sessionRepository, never()).logout(anyLong());
    }

    @Test
    void findFromRedis_found() {
        Session session = new Session();
        session.setId(100L);
        when(redisRepository.findBySessionId(100L)).thenReturn(Optional.of(session));
        Session found = service.findFromRedisById(100L);
        assertEquals(100L, found.getId());
    }

    @Test
    void findFromRedis_notFound() {
        when(redisRepository.findBySessionId(101L)).thenReturn(Optional.empty());
        assertThrows(SessionNotFoundException.class, () -> service.findFromRedisById(101L));
    }

    @Test
    void deleteOldSessions() {
        doNothing().when(sessionRepository).deleteOldSessions(any(LocalDateTime.class));
        assertDoesNotThrow(() -> service.deleteOldSessions());
        verify(sessionRepository, times(1)).deleteOldSessions(any(LocalDateTime.class));
    }

    @Test
    void findAllFromRedis() {
        List<Session> list = Arrays.asList(new Session(), new Session());
        when(redisRepository.findAll()).thenReturn(list);
        assertEquals(2, service.findAllFromRedis().size());
        verify(redisRepository, times(1)).findAll();
    }

    @Test
    void prolongSession() {
        doNothing().when(redisRepository).prolongSession(99L);
        service.prolongSession(99L);
        verify(redisRepository, times(1)).prolongSession(99L);
    }

    @Test
    void prolongSessionByUserId_found() {
        when(sessionRepository.findSessionIdByUserIdAndDeviceInfo(55, "android")).thenReturn(Optional.of(99L));
        doNothing().when(redisRepository).prolongSession(99L);
        service.prolongSessionByUserId(55, "android");
        verify(redisRepository, times(1)).prolongSession(99L);
    }

    @Test
    void prolongSessionByUserId_notFound() {
        when(sessionRepository.findSessionIdByUserIdAndDeviceInfo(55, "android")).thenReturn(Optional.empty());
        assertThrows(SessionNotFoundException.class, () -> service.prolongSessionByUserId(55, "android"));
    }

    @Test
    void deleteNotActiveSessions() {
        Set<String> keys = new HashSet<>(Arrays.asList("1", "2"));
        when(redisRepository.findAllKeys()).thenReturn(keys);
        doNothing().when(sessionRepository).closeNotActiveSessions(anyList());

        service.deleteNotActiveSessions();

        verify(redisRepository, times(1)).findAllKeys();
        verify(sessionRepository, times(1)).closeNotActiveSessions(Arrays.asList(1L, 2L));
    }
}