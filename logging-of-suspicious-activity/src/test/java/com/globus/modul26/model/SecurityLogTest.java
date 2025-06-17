package com.globus.modul26.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityLogTest {

    private SecurityLog log;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        log = SecurityLog.builder()
                .id(123)
                .userId(99L)
                .eventType("LOGIN")
                .ipAddress("1.2.3.4")
                .deviceInfo("Chrome")
                .createdAt(now)
                .metadata(new HashMap<>())
                .biometryUsed(true)
                .isSuspicious(true)
                .build();
    }

    @Test
    void prePersist_ipNullAndDefaults() {
        SecurityLog sl = new SecurityLog();
        sl.setIpAddress(null);
        sl.setCreatedAt(null);
        sl.setIsSuspicious(null);
        sl.prePersist();
        assertNotNull(sl.getCreatedAt());
        assertEquals("UNKNOWN", sl.getIpAddress());
        assertFalse(sl.getIsSuspicious());
    }

    @Test
    void prePersist_ipEmptyOrDefault() {
        SecurityLog sl1 = new SecurityLog();
        sl1.setIpAddress("");
        sl1.prePersist();
        assertEquals("UNKNOWN", sl1.getIpAddress());

        SecurityLog sl2 = new SecurityLog();
        sl2.setIpAddress("127.0.0.1");
        sl2.prePersist();
        assertEquals("UNKNOWN", sl2.getIpAddress());

        SecurityLog sl3 = new SecurityLog();
        sl3.setIpAddress("null");
        sl3.prePersist();
        assertEquals("UNKNOWN", sl3.getIpAddress());

        SecurityLog sl4 = new SecurityLog();
        sl4.setIpAddress("0:0:0:0:0:0:0:1");
        sl4.prePersist();
        assertEquals("UNKNOWN", sl4.getIpAddress());
    }

    @Test
    void getId() {
        assertEquals(123, log.getId());
    }

    @Test
    void getUserId() {
        assertEquals(99L, log.getUserId());
    }

    @Test
    void getEventType() {
        assertEquals("LOGIN", log.getEventType());
    }

    @Test
    void getIpAddress() {
        assertEquals("1.2.3.4", log.getIpAddress());
    }

    @Test
    void getDeviceInfo() {
        assertEquals("Chrome", log.getDeviceInfo());
    }

    @Test
    void getCreatedAt() {
        assertEquals(now, log.getCreatedAt());
    }

    @Test
    void getMetadata() {
        assertNotNull(log.getMetadata());
    }

    @Test
    void getBiometryUsed() {
        assertTrue(log.getBiometryUsed());
    }

    @Test
    void getIsSuspicious() {
        assertTrue(log.getIsSuspicious());
    }

    @Test
    void setId() {
        log.setId(5);
        assertEquals(5, log.getId());
    }

    @Test
    void setUserId() {
        log.setUserId(100L);
        assertEquals(100L, log.getUserId());
    }

    @Test
    void setEventType() {
        log.setEventType("NEW_EVENT");
        assertEquals("NEW_EVENT", log.getEventType());
    }

    @Test
    void setIpAddress() {
        log.setIpAddress("192.168.1.1");
        assertEquals("192.168.1.1", log.getIpAddress());
    }

    @Test
    void setDeviceInfo() {
        log.setDeviceInfo("Firefox");
        assertEquals("Firefox", log.getDeviceInfo());
    }

    @Test
    void setCreatedAt() {
        LocalDateTime dt = now.plusDays(1);
        log.setCreatedAt(dt);
        assertEquals(dt, log.getCreatedAt());
    }

    @Test
    void setMetadata() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("key", "val");
        log.setMetadata(meta);
        assertEquals(meta, log.getMetadata());
    }

    @Test
    void setBiometryUsed() {
        log.setBiometryUsed(false);
        assertFalse(log.getBiometryUsed());
    }
}