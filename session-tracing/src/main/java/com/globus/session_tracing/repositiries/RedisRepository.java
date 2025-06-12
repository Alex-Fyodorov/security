package com.globus.session_tracing.repositiries;

import com.globus.session_tracing.entities.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepository {
    private final static int SESSION_LIFETIME_MINUTES = 30;
    private final RedisTemplate<String, Object> redisTemplate;

    public void add(Session session) {
        String key = String.valueOf(session.getId());
        redisTemplate.opsForValue().set(key, session);
        redisTemplate.expire(key, SESSION_LIFETIME_MINUTES, TimeUnit.MINUTES);
    }

    public Optional<Session> read(long id) {
        return Optional.ofNullable((Session) redisTemplate.opsForValue().get(String.valueOf(id)));
    }

    public boolean delete(long id) {
        return redisTemplate.delete(String.valueOf(id));
    }
}
