package com.globus.modul26.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityLogDTOTest {

    private SecurityLogDTO dto;

    @BeforeEach
    void setUp() {
        dto = new SecurityLogDTO();
    }

    @Test
    void getUserId() {
        dto.setUserId(123L);
        assertEquals(123L, dto.getUserId());
    }

    @Test
    void setUserId() {
        dto.setUserId(999L);
        assertEquals(999L, dto.getUserId());
    }

    @Test
    void getEventType() {
        dto.setEventType("LOGIN");
        assertEquals("LOGIN", dto.getEventType());
    }

    @Test
    void setEventType() {
        dto.setEventType("LOGOUT");
        assertEquals("LOGOUT", dto.getEventType());
    }

    @Test
    void getIpAddress() {
        dto.setIpAddress("127.0.0.1");
        assertEquals("127.0.0.1", dto.getIpAddress());
    }

    @Test
    void setIpAddress() {
        dto.setIpAddress("8.8.8.8");
        assertEquals("8.8.8.8", dto.getIpAddress());
    }

    @Test
    void getDeviceInfo() {
        dto.setDeviceInfo("Mozilla/5.0 (Windows NT 10.0)");
        assertEquals("Mozilla/5.0 (Windows NT 10.0)", dto.getDeviceInfo());
    }

    @Test
    void setDeviceInfo() {
        dto.setDeviceInfo("iPhone Safari");
        assertEquals("iPhone Safari", dto.getDeviceInfo());
    }

    @Test
    void getBiometryUsed() {
        dto.setBiometryUsed(Boolean.TRUE);
        assertTrue(dto.getBiometryUsed());
        dto.setBiometryUsed(null);
        assertNull(dto.getBiometryUsed());
    }

    @Test
    void setBiometryUsed() {
        dto.setBiometryUsed(Boolean.FALSE);
        assertFalse(dto.getBiometryUsed());
    }

    @Test
    void getMetadata() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("country", "Russia");
        meta.put("city", "Moscow");
        dto.setMetadata(meta);
        assertEquals(meta, dto.getMetadata());
    }

    @Test
    void setMetadata() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        dto.setMetadata(map);
        assertEquals(map, dto.getMetadata());
    }
}