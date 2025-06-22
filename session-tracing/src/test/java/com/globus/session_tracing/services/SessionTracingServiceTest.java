package com.globus.session_tracing.services;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.exceptions.SessionNotFoundException;
import com.globus.session_tracing.exceptions.SessionsOperationsException;
import com.globus.session_tracing.exceptions.TooManySessionsException;
import com.globus.session_tracing.repositiries.RedisRepository;
import com.globus.session_tracing.repositiries.SessionRepository;
import com.globus.session_tracing.utils.Base64Service;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    void save_success() {
        List<Session> sessions = new ArrayList<>();
        sessions.add(new Session(1L, 1, null, null, null, null, "YQ==", null));
        sessions.add(new Session(2L, 1, null, null, null, null, "Yw==", null));
        when(redisRepository.findAllByUserId(1)).thenReturn(sessions);
        Session session = new Session();
        session.setUserId(1);
        session.setDeviceInfo("android");
        session.setIpAddress("ipAddress");

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
        findAllByUserId();
        Session session = new Session();
        session.setUserId(1);
        session.setDeviceInfo("android");
        assertThrows(TooManySessionsException.class, () -> service.save(session));
        verify(redisRepository, never()).add(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void logout_success() {
        findAllByUserId();
        assertDoesNotThrow(() -> service.logout(1, "a"));
        verify(sessionRepository, times(2)).logout(anyLong());
        verify(redisRepository, times(2)).delete(anyLong());
    }

    @Test
    void logout_notFound() {
        when(redisRepository.findAllByUserId(1)).thenReturn(new ArrayList<>());
        assertThrows(SessionNotFoundException.class, () -> service.logout(1, "a"));
    }

    @Test
    void findAllSessionsByUserIdAndDeviceInfo() {
        findAllByUserId();
        try {
            Method thisMethod = SessionTracingService.class.getDeclaredMethod("findAllSessionsByUserIdAndDeviceInfo", Integer.class, String.class);
            thisMethod.setAccessible(true);
            List<Session> result = (List<Session>) thisMethod.invoke(service, 1, "a");
            assertEquals(2, result.size());
            verify(redisRepository, times(1)).findAllByUserId(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findAllFromRedis() {
        List<Session> list = Arrays.asList(new Session(), new Session());
        when(redisRepository.findAll()).thenReturn(list);
        assertEquals(2, service.findAllFromRedis().size());
        verify(redisRepository, times(1)).findAll();
    }

    @Test
    void findFromRedisById_found() {
        Session session = new Session();
        session.setId(100L);
        when(redisRepository.findBySessionId(100L)).thenReturn(Optional.of(session));
        Session found = service.findFromRedisById(100L);
        assertEquals(100L, found.getId());
    }

    @Test
    void findFromRedisById_notFound() {
        when(redisRepository.findBySessionId(101L)).thenReturn(Optional.empty());
        assertThrows(SessionNotFoundException.class, () -> service.findFromRedisById(101L));
    }

    @Test
    void prolongSession_found() {
        findAllByUserId();
        service.prolongSession(1, "a");
        verify(redisRepository, times(1)).prolongSession(1L);
        verify(redisRepository, times(1)).prolongSession(3L);
    }

    @Test
    void prolongSession_notFound() {
        when(redisRepository.findAllByUserId(1)).thenReturn(new ArrayList<>());
        assertThrows(SessionNotFoundException.class, () -> service.prolongSession(1, "a"));
    }

    @Test
    void deleteOldSessions() {
        doNothing().when(sessionRepository).deleteOldSessions(any(LocalDateTime.class));
        assertDoesNotThrow(() -> service.deleteOldSessions());
        verify(sessionRepository, times(1)).deleteOldSessions(any(LocalDateTime.class));
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

    @Test
    void maskIp() {
        try {
            Method thisMethod = SessionTracingService.class.getDeclaredMethod("maskIp", String.class);
            thisMethod.setAccessible(true);
            String result = (String) thisMethod.invoke(service, "111.111.111.111");
            assertEquals("111.1**.***.111", result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void decode() {
        Session session = new Session();
        session.setDeviceInfo(Base64Service.encode("android"));
        session.setIpAddress(Base64Service.encode("ipAddress"));
        try {
            Method thisMethod = SessionTracingService.class.getDeclaredMethod("decode", Session.class);
            thisMethod.setAccessible(true);
            Session result = (Session) thisMethod.invoke(service, session);
            assertEquals("android", result.getDeviceInfo());
            assertEquals("ipAddress", result.getIpAddress());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void findAllByUserId() {
        List<Session> sessions = new ArrayList<>();
        sessions.add(new Session(1L, 1, null, null, null, null, "YQ==", null));
        sessions.add(new Session(2L, 1, null, null, null, null, "Yw==", null));
        sessions.add(new Session(3L, 1, null, null, null, null, "YQ==", null));
        when(redisRepository.findAllByUserId(1)).thenReturn(sessions);
    }
}