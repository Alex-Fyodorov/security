package com.globus.modul26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtBlacklistServiceTest {

    private JwtBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new JwtBlacklistService();
    }

    @Test
    void blacklist() {
        // Добавляем jti в черный список и проверяем, что он там есть
        String jti = "abc123";
        assertFalse(service.isBlacklisted(jti), "Токен не должен быть черным до добавления");
        service.blacklist(jti);
        assertTrue(service.isBlacklisted(jti), "Токен должен быть в черном списке после добавления");
    }

    @Test
    void isBlacklisted() {
        // Проверяем новый jti
        String jti1 = "jti1";
        String jti2 = "jti2";

        assertFalse(service.isBlacklisted(jti1));
        assertFalse(service.isBlacklisted(jti2));

        service.blacklist(jti1);

        assertTrue(service.isBlacklisted(jti1));
        assertFalse(service.isBlacklisted(jti2));
    }
}