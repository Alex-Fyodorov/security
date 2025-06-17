package com.globus.modul26.service;

import com.globus.modul26.model.Role;
import com.globus.modul26.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtServiceImpl();
        // С помощью рефлексии устанавливаем приватные поля
        setPrivateField(jwtService, "jwtSecret", "test-jwt-secret-key-should-be-very-very-long-for-hmac-algorithm-abc");
        setPrivateField(jwtService, "jwtExpirationMs", 60000L); // 1 минута
        jwtService.init();
    }

    // Вспомогательный метод для установки приватных полей
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    // Вспомогательный метод для получения приватных полей
    private Object getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    @Test
    void init() throws Exception {
        // Проверяем через reflection, что signingKey установлен
        Object signingKey = getPrivateField(jwtService, "signingKey");
        assertNotNull(signingKey, "signingKey должен быть инициализирован после init()");
    }

    @Test
    void generateToken() {
        User user = new User();
        user.setId(123L);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "JWT должен состоять из 3 частей");

        String jti = jwtService.extractJti(token);
        assertNotNull(jti, "JTI должен быть не null");
        assertEquals(36, jti.length(), "JTI должен быть UUID");
    }

    @Test
    void extractJti() {
        User user = new User();
        user.setId(456L);
        user.setEmail("abc@def.com");
        user.setRole(Role.ADMIN);

        String token = jwtService.generateToken(user);
        String jti = jwtService.extractJti(token);

        assertNotNull(jti);
        assertEquals(36, jti.length());

        // Проверяем некорректный токен
        assertNull(jwtService.extractJti("invalid-token-value"), "Для некорректного токена должно быть null");
    }
}