package com.globus.modul26.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = new AuthResponse("tokentest", "my@mail.ru", true);
    }

    @Test
    void getToken() {
        assertEquals("tokentest", authResponse.getToken());
    }

    @Test
    void setToken() {
        authResponse.setToken("newtoken");
        assertEquals("newtoken", authResponse.getToken());
    }

    @Test
    void getEmail() {
        assertEquals("my@mail.ru", authResponse.getEmail());
    }

    @Test
    void setEmail() {
        authResponse.setEmail("other@mail.com");
        assertEquals("other@mail.com", authResponse.getEmail());
    }

    @Test
    void isBiometryUsed() {
        assertTrue(authResponse.isBiometryUsed());
    }

    @Test
    void setBiometryUsed() {
        authResponse.setBiometryUsed(false);
        assertFalse(authResponse.isBiometryUsed());
    }
}