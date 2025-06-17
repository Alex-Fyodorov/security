package com.globus.modul26.controller;

import com.globus.modul26.dto.RegisterRequest;
import com.globus.modul26.dto.RegisterResponse;
import com.globus.modul26.model.Role;
import com.globus.modul26.model.User;
import com.globus.modul26.repository.UserRepository;
import com.globus.modul26.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationControllerTest {

    @Mock(lenient = true)
    private UserRepository userRepository;

    @Mock(lenient = true)
    private PasswordEncoder passwordEncoder;

    @Mock(lenient = true)
    private JwtService jwtService;

    @InjectMocks
    private RegistrationController registrationController;

    @BeforeEach
    void setUp() {
        // Моки настраиваются в отдельных тестах
    }

    @Test
    void testSuccessfulRegistration() {
        // Подготовка данных
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setRole("USER");
        request.setPhone("1234567890");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("newuser@example.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setRole(Role.USER);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setPhone("1234567890");

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        ResponseEntity<?> response = registrationController.registerUser(request, "TestAgent", "mobile");

        assertEquals(200, response.getStatusCodeValue());
        RegisterResponse responseBody = (RegisterResponse) response.getBody();
        assertNotNull(responseBody);
        assertEquals("newuser", responseBody.getUsername());
        assertTrue(responseBody.getEmail().startsWith("ne") && responseBody.getEmail().contains("@"));  // Проверка маскировки
        assertEquals("John", responseBody.getFirstName());
        assertEquals("Doe", responseBody.getLastName());
        assertEquals("USER", responseBody.getRole());
    }

    @Test
    void testRegistrationWithMissingFields() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");  // Только email

        ResponseEntity<?> response = registrationController.registerUser(request, "TestAgent", "mobile");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Заполните все обязательные поля", response.getBody());
    }

    @Test
    void testRegistrationWithExistingEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setRole("USER");
        request.setPhone("1234567890");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        ResponseEntity<?> response = registrationController.registerUser(request, "TestAgent", "mobile");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Email уже используется", response.getBody());
    }

    @Test
    void testRegistrationWithExistingUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setRole("USER");
        request.setPhone("1234567890");

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        ResponseEntity<?> response = registrationController.registerUser(request, "TestAgent", "mobile");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username уже используется", response.getBody());
    }

    @Test
    void testRegistrationWithInvalidRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setRole("INVALID_ROLE");
        request.setPhone("1234567890");

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        ResponseEntity<?> response = registrationController.registerUser(request, "TestAgent", "mobile");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Некорректная роль: INVALID_ROLE", response.getBody());
    }
}