package com.globus.modul26.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterResponseTest {

    private RegisterResponse response;

    @BeforeEach
    void setUp() {
        response = new RegisterResponse();
    }

    @Test
    void getId() {
        response.setId(99L);
        assertEquals(99L, response.getId());
    }

    @Test
    void setId() {
        response.setId(101L);
        assertEquals(101L, response.getId());
    }

    @Test
    void getUsername() {
        response.setUsername("vasya");
        assertEquals("vasya", response.getUsername());
    }

    @Test
    void setUsername() {
        response.setUsername("petya");
        assertEquals("petya", response.getUsername());
    }

    @Test
    void getEmail() {
        response.setEmail("foo@bar.com");
        assertEquals("foo@bar.com", response.getEmail());
    }

    @Test
    void setEmail() {
        response.setEmail("mask@domain.ru");
        assertEquals("mask@domain.ru", response.getEmail());
    }

    @Test
    void getFirstName() {
        response.setFirstName("Ivan");
        assertEquals("Ivan", response.getFirstName());
    }

    @Test
    void setFirstName() {
        response.setFirstName("Sasha");
        assertEquals("Sasha", response.getFirstName());
    }

    @Test
    void getLastName() {
        response.setLastName("Ivanov");
        assertEquals("Ivanov", response.getLastName());
    }

    @Test
    void setLastName() {
        response.setLastName("Petrov");
        assertEquals("Petrov", response.getLastName());
    }

    @Test
    void getPhone() {
        response.setPhone("+79991112233");
        assertEquals("+79991112233", response.getPhone());
    }

    @Test
    void setPhone() {
        response.setPhone("81234567890");
        assertEquals("81234567890", response.getPhone());
    }

    @Test
    void getRole() {
        response.setRole("USER");
        assertEquals("USER", response.getRole());
    }

    @Test
    void setRole() {
        response.setRole("ADMIN");
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void testAllArgsConstructor() {
        RegisterResponse other = new RegisterResponse(
                7L, "user7", "u7@ya.ru", "Anna", "Kareva", "70000000007", "USER"
        );
        assertEquals(7L, other.getId());
        assertEquals("user7", other.getUsername());
        assertEquals("u7@ya.ru", other.getEmail());
        assertEquals("Anna", other.getFirstName());
        assertEquals("Kareva", other.getLastName());
        assertEquals("70000000007", other.getPhone());
        assertEquals("USER", other.getRole());
    }
}