package com.globus.session_tracing.dtos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SessionDtoTest {

    private SessionDto dto;

    @BeforeEach
    void setUp() {
        dto = new SessionDto();
        dto.setId(7L);
        dto.setUserId(123);
        dto.setLoginTime(new Date(100000));
        dto.setLogoutTime(new Date(200000));
        dto.setMethod("sms");
        dto.setIpAddress("192.168.0.1");
        dto.setDeviceInfo("iPhone");
        dto.setIsActive(true);
    }

    @Test
    void getId() {
        assertEquals(7L, dto.getId());
    }

    @Test
    void getUserId() {
        assertEquals(123, dto.getUserId());
    }

    @Test
    void getLoginTime() {
        assertEquals(new Date(100000), dto.getLoginTime());
    }

    @Test
    void getLogoutTime() {
        assertEquals(new Date(200000), dto.getLogoutTime());
    }

    @Test
    void getMethod() {
        assertEquals("sms", dto.getMethod());
    }

    @Test
    void getIpAddress() {
        assertEquals("192.168.0.1", dto.getIpAddress());
    }

    @Test
    void getDeviceInfo() {
        assertEquals("iPhone", dto.getDeviceInfo());
    }

    @Test
    void getIsActive() {
        assertTrue(dto.getIsActive());
    }

    // === Сеттеры ===

    @Test
    void setId() {
        dto.setId(99L);
        assertEquals(99L, dto.getId());
    }

    @Test
    void setUserId() {
        dto.setUserId(321);
        assertEquals(321, dto.getUserId());
    }

    @Test
    void setLoginTime() {
        Date newLogin = new Date(555);
        dto.setLoginTime(newLogin);
        assertEquals(newLogin, dto.getLoginTime());
    }

    @Test
    void setLogoutTime() {
        Date newLogout = new Date(777);
        dto.setLogoutTime(newLogout);
        assertEquals(newLogout, dto.getLogoutTime());
    }

    @Test
    void setMethod() {
        dto.setMethod("push");
        assertEquals("push", dto.getMethod());
    }

    @Test
    void setIpAddress() {
        dto.setIpAddress("8.8.4.4");
        assertEquals("8.8.4.4", dto.getIpAddress());
    }

    @Test
    void setDeviceInfo() {
        dto.setDeviceInfo("Android");
        assertEquals("Android", dto.getDeviceInfo());
    }

    @Test
    void setIsActive() {
        dto.setIsActive(false);
        assertFalse(dto.getIsActive());
    }

    // === Equals, hashCode, toString ===

    @Test
    void testEquals() {
        SessionDto dto2 = new SessionDto(7L, 123, new Date(100000), new Date(200000), "sms", "192.168.0.1", "iPhone", true);
        assertEquals(dto, dto2);
    }

    @Test
    void canEqual() {
        SessionDto dto2 = new SessionDto();
        assertTrue(dto.canEqual(dto2));
    }

    @Test
    void testHashCode() {
        SessionDto dto2 = new SessionDto(7L, 123, new Date(100000), new Date(200000), "sms", "192.168.0.1", "iPhone", true);
        assertEquals(dto.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        assertNotNull(dto.toString());
        assertTrue(dto.toString().contains("id=7"));
    }

    @Test
    void builder() {
        Date loginDate = new Date(12345);
        Date logoutDate = new Date(67890);
        SessionDto built = SessionDto.builder()
                .id(1L)
                .userId(99)
                .loginTime(loginDate)
                .logoutTime(logoutDate)
                .method("web")
                .ipAddress("127.0.0.1")
                .deviceInfo("PC")
                .isActive(false)
                .build();

        assertEquals(1L, built.getId());
        assertEquals(99, built.getUserId());
        assertEquals(loginDate, built.getLoginTime());
        assertEquals(logoutDate, built.getLogoutTime());
        assertEquals("web", built.getMethod());
        assertEquals("127.0.0.1", built.getIpAddress());
        assertEquals("PC", built.getDeviceInfo());
        assertFalse(built.getIsActive());
    }
}