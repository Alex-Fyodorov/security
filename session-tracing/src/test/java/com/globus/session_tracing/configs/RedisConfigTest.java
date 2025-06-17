package com.globus.session_tracing.configs;

import io.lettuce.core.resource.ClientResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

class RedisConfigTest {

    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() {

        redisConfig = new RedisConfig();
        setField(redisConfig, "redisHost", "localhost");
        setField(redisConfig, "redisPort", 6379);
    }

    @Test
    void redisConnectionFactory() {

        ClientResources clientResources = Mockito.mock(ClientResources.class);

        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(clientResources);

        assertNotNull(factory, "LettuceConnectionFactory не должен быть null");
        assertEquals("localhost", factory.getStandaloneConfiguration().getHostName());
        assertEquals(6379, factory.getStandaloneConfiguration().getPort());
    }

    @Test
    void redisTemplate() {
        
        LettuceConnectionFactory connectionFactory = Mockito.mock(LettuceConnectionFactory.class);

        RedisTemplate<String, Object> template = redisConfig.redisTemplate(connectionFactory);

        assertNotNull(template, "RedisTemplate не должен быть null");
        assertNotNull(template.getConnectionFactory(), "ConnectionFactory должен быть установлен");
        assertTrue(template.getKeySerializer() instanceof org.springframework.data.redis.serializer.StringRedisSerializer);
        assertTrue(template.getValueSerializer() instanceof org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer);
    }

    // Вспомогательный метод для установки приватных полей через рефлексию
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}