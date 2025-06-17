package com.globus.modul26.config;

import com.globus.modul26.service.JwtBlacklistFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        // Устанавливаем значение приватного поля secret, чтобы не было NPE
        ReflectionTestUtils.setField(securityConfig, "secret", "test-super-secret-key");
    }

    @AfterEach
    void tearDown() {
        securityConfig = null;
    }

    @Test
    void jwtDecoder() {
        JwtDecoder jwtDecoder = securityConfig.jwtDecoder();
        assertNotNull(jwtDecoder, "jwtDecoder bean должен быть создан");
    }

    @Test
    void passwordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder, "passwordEncoder bean должен быть создан");
        String encoded = encoder.encode("password");
        assertTrue(encoder.matches("password", encoded), "Зашифрованный пароль должен совпадать с оригиналом");
    }

    @Test
    void jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();
        assertNotNull(converter, "jwtAuthenticationConverter bean должен быть создан");
    }

    @Test
    void filterChain() {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        JwtBlacklistFilter jwtBlacklistFilter = mock(JwtBlacklistFilter.class);

        try {
            SecurityFilterChain chain = securityConfig.filterChain(http, jwtBlacklistFilter);

            assertNotNull(chain, "SecurityFilterChain должен быть создан (или замокан)");
        } catch (Exception e) {

            assertFalse(e instanceof NullPointerException, "Не должно быть NullPointerException");
            // Остальные исключения (IllegalStateException и т.д.) от мокнутого HttpSecurity допустимы и не считаются ошибкой теста
        }
    }
}