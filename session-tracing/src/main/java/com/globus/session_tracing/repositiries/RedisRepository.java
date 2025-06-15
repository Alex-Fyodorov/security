package com.globus.session_tracing.repositiries;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.exceptions.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.data.redis.lifetime.minutes}")
    private int sessionLifeTimeMinutes;

    public void add(Session session) {
        String key = String.valueOf(session.getId());
        redisTemplate.opsForValue().set(key, session);
        redisTemplate.expire(key, sessionLifeTimeMinutes, TimeUnit.MINUTES);
    }

    public void prolongSession(Long id) {
        String key = String.valueOf(id);
        if (!redisTemplate.hasKey(key)) {
            throw new SessionNotFoundException("Сессия не найдена или уже завершена");
        }
        redisTemplate.expire(key, sessionLifeTimeMinutes, TimeUnit.MINUTES);
    }

    public Optional<Session> findBySessionId(long id) {
        return Optional.ofNullable((Session) redisTemplate.opsForValue().get(String.valueOf(id)));
    }

    public List<Session> findAll() {
        Set<String> redisKeys = redisTemplate.keys("*");
        List<Session> sessions = new ArrayList<>();
        for (String redisKey : redisKeys) {
            Session session = (Session) redisTemplate.opsForValue().get(redisKey);
            sessions.add(session);
        }
        return sessions;
    }

    public Set<String> findAllKeys() {
        return redisTemplate.keys("*");
    }

    public boolean delete(long id) {
        return redisTemplate.delete(String.valueOf(id));
    }
}
