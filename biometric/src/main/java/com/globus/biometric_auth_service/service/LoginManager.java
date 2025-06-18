package com.globus.biometric_auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginManager {
    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MINUTES = 15;

    // Track failed attempts
    public void incrementAttempts(Integer id) {
        String key = "login_attempts:" + id;
        Long attempts = redisTemplate.opsForValue().increment(key, 1);

        // Set TTL on first attempt
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, BLOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }

    // Check if user is blocked
    public boolean isBlocked(Integer id) {
        String key = "login_attempts:" + id;
        String attemptsStr = redisTemplate.opsForValue().get(key);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        return attempts >= MAX_ATTEMPTS;
    }

    // Reset attempts on successful login
    public void resetAttempts(Integer id) {
        String key = "login_attempts:" + id;
        redisTemplate.delete(key);
    }
}
