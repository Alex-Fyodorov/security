package com.globus.session_tracing.repositiries;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.exceptions.SessionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisRepository redisRepository;

    private final int sessionLifeTimeMinutes = 10; // тестовое значение

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // injext @Value вручную через reflection
        Field field = RedisRepository.class.getDeclaredField("sessionLifeTimeMinutes");
        field.setAccessible(true);
        field.set(redisRepository, sessionLifeTimeMinutes);
    }

    @Test
    void add() {
        Session session = new Session();
        session.setId(1L);

        doNothing().when(valueOperations).set("1", session);
        when(redisTemplate.expire("1", sessionLifeTimeMinutes, TimeUnit.MINUTES)).thenReturn(true);

        redisRepository.add(session);

        verify(valueOperations, times(1)).set("1", session);
        verify(redisTemplate, times(1)).expire("1", sessionLifeTimeMinutes, TimeUnit.MINUTES);
    }

    @Test
    void prolongSession_ok() {
        when(redisTemplate.hasKey("2")).thenReturn(true);
        when(redisTemplate.expire("2", sessionLifeTimeMinutes, TimeUnit.MINUTES)).thenReturn(true);

        assertDoesNotThrow(() -> redisRepository.prolongSession(2L));

        verify(redisTemplate, times(1)).hasKey("2");
        verify(redisTemplate, times(1)).expire("2", sessionLifeTimeMinutes, TimeUnit.MINUTES);
    }

    @Test
    void prolongSession_notFound() {
        when(redisTemplate.hasKey("3")).thenReturn(false);

        Exception ex = assertThrows(SessionNotFoundException.class, () -> redisRepository.prolongSession(3L));
        assertEquals("Сессия не найдена или уже завершена", ex.getMessage());

        verify(redisTemplate, times(1)).hasKey("3");
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
    }

    @Test
    void findBySessionId_found() {
        Session session = new Session();
        session.setId(4L);

        when(valueOperations.get("4")).thenReturn(session);

        Optional<Session> result = redisRepository.findBySessionId(4L);

        assertTrue(result.isPresent());
        assertEquals(session, result.get());
    }

    @Test
    void findBySessionId_notFound() {
        when(valueOperations.get("5")).thenReturn(null);

        Optional<Session> result = redisRepository.findBySessionId(5L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll() {
        Set<String> keys = new HashSet<>(Arrays.asList("1", "2"));
        Session session1 = new Session(); session1.setId(1L);
        Session session2 = new Session(); session2.setId(2L);

        when(redisTemplate.keys("*")).thenReturn(keys);
        when(valueOperations.get("1")).thenReturn(session1);
        when(valueOperations.get("2")).thenReturn(session2);

        List<Session> result = redisRepository.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(session1));
        assertTrue(result.contains(session2));
    }

    @Test
    void findAllKeys() {
        Set<String> keys = new HashSet<>(Arrays.asList("10", "20"));
        when(redisTemplate.keys("*")).thenReturn(keys);

        Set<String> result = redisRepository.findAllKeys();

        assertEquals(keys, result);
    }

    @Test
    void delete() {
        when(redisTemplate.delete("15")).thenReturn(true);

        boolean res = redisRepository.delete(15L);

        assertTrue(res);
        verify(redisTemplate, times(1)).delete("15");
    }
}