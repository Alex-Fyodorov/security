package com.globus.modul26.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterRequestTest {

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
    }

    @Test
    void getEmail() {
        request.setEmail("test@example.com");
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    void setEmail() {
        request.setEmail("user@email.com");
        assertEquals("user@email.com", request.getEmail());
    }

    @Test
    void getUsername() {
        request.setUsername("myuser");
        assertEquals("myuser", request.getUsername());
    }

    @Test
    void setUsername() {
        request.setUsername("another_user");
        assertEquals("another_user", request.getUsername());
    }

    @Test
    void getPassword() {
        request.setPassword("secret");
        assertEquals("secret", request.getPassword());
    }

    @Test
    void setPassword() {
        request.setPassword("qwerty");
        assertEquals("qwerty", request.getPassword());
    }

    @Test
    void getFirstName() {
        request.setFirstName("Vasya");
        assertEquals("Vasya", request.getFirstName());
    }

    @Test
    void setFirstName() {
        request.setFirstName("Petya");
        assertEquals("Petya", request.getFirstName());
    }

    @Test
    void getLastName() {
        request.setLastName("Ivanov");
        assertEquals("Ivanov", request.getLastName());
    }

    @Test
    void setLastName() {
        request.setLastName("Sidorov");
        assertEquals("Sidorov", request.getLastName());
    }

    @Test
    void getPhone() {
        request.setPhone("79991234567");
        assertEquals("79991234567", request.getPhone());
    }

    @Test
    void setPhone() {
        request.setPhone("+71234567890");
        assertEquals("+71234567890", request.getPhone());
    }

    @Test
    void getRole() {
        request.setRole("USER");
        assertEquals("USER", request.getRole());
    }

    @Test
    void setRole() {
        request.setRole("ADMIN");
        assertEquals("ADMIN", request.getRole());
    }
}