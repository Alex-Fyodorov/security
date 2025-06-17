package com.globus.modul26.controller;

import com.globus.modul26.model.SecurityLog;
import com.globus.modul26.service.JwtBlacklistService;
import com.globus.modul26.service.JwtServiceImpl;
import com.globus.modul26.service.SecurityLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class SecurityLogControllerTest {

    @Mock
    private SecurityLogService service;
    @Mock
    private JwtBlacklistService jwtBlacklistService;
    @Mock
    private JwtServiceImpl jwtService;

    @Mock
    private Authentication authentication;
    @Mock
    private Jwt jwt;

    @InjectMocks
    private SecurityLogController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Заглушка для аутентификации - всегда возвращает jwt
        when(authentication.getPrincipal()).thenReturn(jwt);
    }

    @Test
    void logEvent() {
        // Заглушка данных
        SecurityLog inputLog = new SecurityLog();
        inputLog.setIpAddress("192.168.1.100");
        inputLog.setEventType("LOGIN");
        // Метадата пустая

        // Заглушка Jwt
        when(jwt.getSubject()).thenReturn("42");

        // Заглушка сервиса: возвращаем то же, что пришло (упрощённо)
        SecurityLog savedLog = new SecurityLog();
        when(service.saveLog(any(SecurityLog.class))).thenReturn(savedLog);

        // Вызов
        ResponseEntity<SecurityLog> response = controller.logEvent(
                inputLog,
                authentication,
                "Bearer test.jwt.token",
                "TestUserAgent/1.0"
        );

        // Проверки
        assertEquals(201, response.getStatusCodeValue());
        assertSame(savedLog, response.getBody());
    }

    @Test
    void getSuspiciousByUser_Admin() {
        Long userId = 123L;
        when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("ADMIN"));
        when(jwt.getSubject()).thenReturn(userId.toString());

        List<SecurityLog> mockLogs = List.of(new SecurityLog());
        when(service.findSuspiciousLogsByUserId(userId)).thenReturn(mockLogs);

        ResponseEntity<List<SecurityLog>> response = controller.getSuspiciousByUser(
                userId,
                authentication,
                "Bearer some.token"
        );

        assertEquals(200, response.getStatusCodeValue());
        assertSame(mockLogs, response.getBody());
    }

    @Test
    void getSuspiciousByUser_UserOwnLogs() {
        Long userId = 123L;
        when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("USER"));
        when(jwt.getSubject()).thenReturn(userId.toString());

        List<SecurityLog> mockLogs = List.of(new SecurityLog());
        when(service.findSuspiciousLogsByUserId(userId)).thenReturn(mockLogs);

        ResponseEntity<List<SecurityLog>> response = controller.getSuspiciousByUser(
                userId,
                authentication,
                "Bearer some.token"
        );

        assertEquals(200, response.getStatusCodeValue());
        assertSame(mockLogs, response.getBody());
    }

    @Test
    void getSuspiciousByUser_Forbidden() {
        Long userId = 123L;
        when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("USER"));
        when(jwt.getSubject()).thenReturn("999"); // id другой

        Exception exception = assertThrows(Exception.class, () -> controller.getSuspiciousByUser(
                userId,
                authentication,
                "Bearer some.token"
        ));
        assertTrue(exception.getMessage().contains("403"));
    }

    @Test
    void getByUser_Admin() {
        Long userId = 777L;
        when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("ADMIN"));
        when(jwt.getSubject()).thenReturn(userId.toString());

        List<SecurityLog> mockLogs = List.of(new SecurityLog());
        when(service.findByUserId(userId)).thenReturn(mockLogs);

        ResponseEntity<List<SecurityLog>> response = controller.getByUser(
                userId,
                authentication,
                "Bearer some.token"
        );

        assertEquals(200, response.getStatusCodeValue());
        assertSame(mockLogs, response.getBody());
    }

    @Test
    void getByUser_UserOwnLogs() {
        Long userId = 777L;
        when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("USER"));
        when(jwt.getSubject()).thenReturn(userId.toString());

        List<SecurityLog> mockLogs = List.of(new SecurityLog());
        when(service.findByUserId(userId)).thenReturn(mockLogs);

        ResponseEntity<List<SecurityLog>> response = controller.getByUser(
                userId,
                authentication,
                "Bearer some.token"
        );

        assertEquals(200, response.getStatusCodeValue());
        assertSame(mockLogs, response.getBody());
    }

    @Test
    void getByUser_Forbidden() {
        Long userId = 1L;
        when(jwt.getClaimAsStringList("roles")).thenReturn(List.of("USER"));
        when(jwt.getSubject()).thenReturn("100"); // чужой id

        Exception exception = assertThrows(Exception.class, () -> controller.getByUser(
                userId,
                authentication,
                "Bearer some.token"
        ));
        assertTrue(exception.getMessage().contains("403"));
    }
}