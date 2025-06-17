package com.globus.session_tracing.converters;

import com.globus.session_tracing.dtos.SessionDto;
import com.globus.session_tracing.entities.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SessionConverterTest {

    private SessionConverter converter;

    @BeforeEach
    void setUp() {
        converter = new SessionConverter();
    }

    @Test
    void toDto() {
        // Arrange
        LocalDateTime loginTime = LocalDateTime.now();
        LocalDateTime logoutTime = loginTime.plusHours(1);

        Date loginDate = Date.from(loginTime.atZone(ZoneId.systemDefault()).toInstant());
        Date logoutDate = Date.from(logoutTime.atZone(ZoneId.systemDefault()).toInstant());

        Session session = Session.builder()
                .id(1L)
                .userId(2)
                .deviceInfo("Android Phone")
                .method("sms")
                .ipAddress("192.168.1.10")
                .loginTime(loginDate)
                .logoutTime(logoutDate)
                .isActive(true)
                .build();

        // Act
        SessionDto dto = converter.toDto(session);

        // Assert
        assertNotNull(dto);
        assertEquals(session.getId(), dto.getId());
        assertEquals(session.getUserId(), dto.getUserId());
        assertEquals(session.getDeviceInfo(), dto.getDeviceInfo());
        assertEquals(session.getMethod(), dto.getMethod());
        assertEquals(session.getIpAddress(), dto.getIpAddress());
        assertEquals(session.getLoginTime(), dto.getLoginTime());
        assertEquals(session.getLogoutTime(), dto.getLogoutTime());
        assertEquals(session.getIsActive(), dto.getIsActive());
    }

    @Test
    void toEntity() {
        // Arrange
        LocalDateTime loginTime = LocalDateTime.now();
        LocalDateTime logoutTime = loginTime.plusMinutes(45);

        Date loginDate = Date.from(loginTime.atZone(ZoneId.systemDefault()).toInstant());
        Date logoutDate = Date.from(logoutTime.atZone(ZoneId.systemDefault()).toInstant());

        SessionDto dto = SessionDto.builder()
                .id(5L)
                .userId(10)
                .deviceInfo("iPhone 15")
                .method("push")
                .ipAddress("8.8.8.8")
                .loginTime(loginDate)
                .logoutTime(logoutDate)
                .isActive(false)
                .build();


        Session session = converter.toEntity(dto);


        assertNotNull(session);
        assertEquals(dto.getId(), session.getId());
        assertEquals(dto.getUserId(), session.getUserId());
        assertEquals(dto.getDeviceInfo(), session.getDeviceInfo());
        assertEquals(dto.getMethod(), session.getMethod());
        assertEquals(dto.getIpAddress(), session.getIpAddress());
        assertEquals(dto.getLoginTime(), session.getLoginTime());
        assertEquals(dto.getLogoutTime(), session.getLogoutTime());
        assertEquals(dto.getIsActive(), session.getIsActive());
    }
}