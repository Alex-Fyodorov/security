package com.globus.session_tracing.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    private Session session;

    @BeforeEach
    void setUp() {
        session = new Session();
        session.setId(10L);
        session.setUserId(42);
        session.setLoginTime(new Date(10000));
        session.setLogoutTime(new Date(20000));
        session.setMethod("otp");
        session.setIpAddress("10.0.0.1");
        session.setDeviceInfo("Windows PC");
        session.setIsActive(true);
    }

    @Test
    void getId() {
        assertEquals(10L, session.getId());
    }

    @Test
    void getUserId() {
        assertEquals(42, session.getUserId());
    }

    @Test
    void getLoginTime() {
        assertEquals(new Date(10000), session.getLoginTime());
    }

    @Test
    void getLogoutTime() {
        assertEquals(new Date(20000), session.getLogoutTime());
    }

    @Test
    void getMethod() {
        assertEquals("otp", session.getMethod());
    }

    @Test
    void getIpAddress() {
        assertEquals("10.0.0.1", session.getIpAddress());
    }

    @Test
    void getDeviceInfo() {
        assertEquals("Windows PC", session.getDeviceInfo());
    }

    @Test
    void getIsActive() {
        assertTrue(session.getIsActive());
    }

    // === Сеттеры ===

    @Test
    void setId() {
        session.setId(99L);
        assertEquals(99L, session.getId());
    }

    @Test
    void setUserId() {
        session.setUserId(333);
        assertEquals(333, session.getUserId());
    }

    @Test
    void setLoginTime() {
        Date date = new Date(55555);
        session.setLoginTime(date);
        assertEquals(date, session.getLoginTime());
    }

    @Test
    void setLogoutTime() {
        Date date = new Date(66666);
        session.setLogoutTime(date);
        assertEquals(date, session.getLogoutTime());
    }

    @Test
    void setMethod() {
        session.setMethod("sms");
        assertEquals("sms", session.getMethod());
    }

    @Test
    void setIpAddress() {
        session.setIpAddress("8.8.8.8");
        assertEquals("8.8.8.8", session.getIpAddress());
    }

    @Test
    void setDeviceInfo() {
        session.setDeviceInfo("MacBook");
        assertEquals("MacBook", session.getDeviceInfo());
    }

    @Test
    void setIsActive() {
        session.setIsActive(false);
        assertFalse(session.getIsActive());
    }

    // === Equals, hashCode, toString ===

    @Test
    void testEquals() {
        Session s2 = new Session(10L, 42, new Date(10000), new Date(20000), "otp", "10.0.0.1", "Windows PC", true);
        assertEquals(session, s2);
    }

    @Test
    void canEqual() {
        Session s2 = new Session();
        assertTrue(session.canEqual(s2));
    }

    @Test
    void testHashCode() {
        Session s2 = new Session(10L, 42, new Date(10000), new Date(20000), "otp", "10.0.0.1", "Windows PC", true);
        assertEquals(session.hashCode(), s2.hashCode());
    }

    @Test
    void testToString() {
        assertNotNull(session.toString());
        assertTrue(session.toString().contains("id=10"));
    }

    @Test
    void builder() {
        Date login = new Date(12345);
        Date logout = new Date(54321);
        Session built = Session.builder()
                .id(77L)
                .userId(404)
                .loginTime(login)
                .logoutTime(logout)
                .method("web")
                .ipAddress("127.0.1.1")
                .deviceInfo("Tablet")
                .isActive(false)
                .build();

        assertEquals(77L, built.getId());
        assertEquals(404, built.getUserId());
        assertEquals(login, built.getLoginTime());
        assertEquals(logout, built.getLogoutTime());
        assertEquals("web", built.getMethod());
        assertEquals("127.0.1.1", built.getIpAddress());
        assertEquals("Tablet", built.getDeviceInfo());
        assertFalse(built.getIsActive());
    }
}