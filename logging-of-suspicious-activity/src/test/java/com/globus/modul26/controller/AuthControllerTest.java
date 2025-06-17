package com.globus.modul26.controller;

import com.globus.modul26.model.SecurityLog;
import com.globus.modul26.model.User;
import com.globus.modul26.repository.UserRepository;
import com.globus.modul26.service.SecurityLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock(lenient = true)
    private UserRepository userRepository;

    @Mock(lenient = true)
    private PasswordEncoder passwordEncoder;

    @Mock(lenient = true)
    private SecurityLogService securityLogService;

    @Mock(lenient = true)
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // Настраиваем общие моки, если нужно
    }

    @Test
    void testSuccessfulLogin() {
        // Подготовка данных
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword(passwordEncoder.encode("password"));
        mockUser.setRole(com.globus.modul26.model.Role.USER);
        mockUser.setLocked(false);

        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");
        loginRequest.setBiometryUsed(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password", mockUser.getPassword())).thenReturn(true);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");  // Хотя этот стаббинг теперь не нужен, оставляем для полноты

        // Вызов метода
        ResponseEntity<?> response = authController.login(loginRequest, "TestAgent", "CustomHeader", request);

        // Проверки
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(((AuthController.JwtResponse) response.getBody()).getToken());

        // Верификация вызовов (удалена verify для getHeader)
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password", mockUser.getPassword());
        verify(securityLogService).saveLog(any(SecurityLog.class));
        verify(request).getRemoteAddr();
    }

    @Test
    void testFailedLogin() {
        // Подготовка данных
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword(passwordEncoder.encode("correctPassword"));
        mockUser.setLocked(false);

        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongPassword");
        loginRequest.setBiometryUsed(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPassword", mockUser.getPassword())).thenReturn(false);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        // Убрали when(request.getHeader("User-Agent")), так как оно не используется

        // Вызов метода
        ResponseEntity<?> response = authController.login(loginRequest, "TestAgent", "CustomHeader", request);

        // Проверки
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Неверный логин или пароль", response.getBody());

        // Верификация вызовов (удалена verify для getHeader)
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongPassword", mockUser.getPassword());
        verify(securityLogService).saveLog(any(SecurityLog.class));
        verify(request).getRemoteAddr();
    }

    @Test
    void testLockedUser() {
        // Подготовка данных
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword(passwordEncoder.encode("password"));
        mockUser.setLocked(true);

        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");
        loginRequest.setBiometryUsed(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        // Убрали when(request.getHeader("User-Agent")), так как оно не используется

        // Вызов метода
        ResponseEntity<?> response = authController.login(loginRequest, "TestAgent", "CustomHeader", request);

        // Проверки
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Пользователь заблокирован из-за многократных неудачных попыток входа", response.getBody());

        // Верификация вызовов (удалена verify для getHeader)
        verify(userRepository).findByUsername("testuser");
        verify(securityLogService).saveLog(any(SecurityLog.class));
        verify(request).getRemoteAddr();
    }
}