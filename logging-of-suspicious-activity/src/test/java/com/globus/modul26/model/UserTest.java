package com.globus.modul26.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.of(2023, 1, 1, 12, 0);
        user = new User();
        user.setId(10L);
        user.setUsername("admin");
        user.setPassword("pass");
        user.setEmail("email@example.com");
        user.setFirstName("Ivan");
        user.setLastName("Petrov");
        user.setPhone("+79990001122");
        user.setRole(Role.ADMIN);
        user.setLocked(true);
        user.setCreatedAt(now);
        user.setLastLogin(now.plusDays(1));
        user.setAccountLockedUntil(now.plusDays(2));
        user.setFailedLoginAttempts(3);
    }

    @Test
    void getId() {
        assertEquals(10L, user.getId());
    }

    @Test
    void setId() {
        user.setId(15L);
        assertEquals(15L, user.getId());
    }

    @Test
    void getUsername() {
        assertEquals("admin", user.getUsername());
    }

    @Test
    void setUsername() {
        user.setUsername("user2");
        assertEquals("user2", user.getUsername());
    }

    @Test
    void getPassword() {
        assertEquals("pass", user.getPassword());
    }

    @Test
    void setPassword() {
        user.setPassword("12345");
        assertEquals("12345", user.getPassword());
    }

    @Test
    void getEmail() {
        assertEquals("email@example.com", user.getEmail());
    }

    @Test
    void setEmail() {
        user.setEmail("test@yandex.ru");
        assertEquals("test@yandex.ru", user.getEmail());
    }

    @Test
    void getFirstName() {
        assertEquals("Ivan", user.getFirstName());
    }

    @Test
    void setFirstName() {
        user.setFirstName("Petr");
        assertEquals("Petr", user.getFirstName());
    }

    @Test
    void getLastName() {
        assertEquals("Petrov", user.getLastName());
    }

    @Test
    void setLastName() {
        user.setLastName("Ivanov");
        assertEquals("Ivanov", user.getLastName());
    }

    @Test
    void getPhone() {
        assertEquals("+79990001122", user.getPhone());
    }

    @Test
    void setPhone() {
        user.setPhone("+79998889900");
        assertEquals("+79998889900", user.getPhone());
    }

    @Test
    void getRole() {
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void setRole() {
        user.setRole(Role.USER);
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void getLocked() {
        assertTrue(user.getLocked());
    }

    @Test
    void setLocked() {
        user.setLocked(false);
        assertFalse(user.getLocked());
    }

    @Test
    void getCreatedAt() {
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void setCreatedAt() {
        LocalDateTime t = now.minusDays(1);
        user.setCreatedAt(t);
        assertEquals(t, user.getCreatedAt());
    }

    @Test
    void getLastLogin() {
        assertEquals(now.plusDays(1), user.getLastLogin());
    }

    @Test
    void setLastLogin() {
        LocalDateTime t = now.plusWeeks(1);
        user.setLastLogin(t);
        assertEquals(t, user.getLastLogin());
    }

    @Test
    void getAccountLockedUntil() {
        assertEquals(now.plusDays(2), user.getAccountLockedUntil());
    }

    @Test
    void setAccountLockedUntil() {
        LocalDateTime t = now.plusMonths(1);
        user.setAccountLockedUntil(t);
        assertEquals(t, user.getAccountLockedUntil());
    }

    @Test
    void getFailedLoginAttempts() {
        assertEquals(3, user.getFailedLoginAttempts());
    }

    @Test
    void setFailedLoginAttempts() {
        user.setFailedLoginAttempts(7);
        assertEquals(7, user.getFailedLoginAttempts());
    }

    @Test
    void testToString() {
        String s = user.toString();
        assertTrue(s.contains("User{"));
        assertTrue(s.contains("username='admin'"));
        assertTrue(s.contains("role=ADMIN"));
    }
}