package com.globus.modul26.controller;

import com.globus.modul26.model.User;
import com.globus.modul26.repository.UserRepository;
import com.globus.modul26.service.SecurityLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityLogService securityLogService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserById() {
        // Arrange
        Long userId = 10L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<?> response = controller.getUserById(userId, "TestAgent/1.0");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertSame(user, response.getBody());
    }

    @Test
    void getUserById_notFound() {
        Long userId = 15L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () -> {
            controller.getUserById(userId, null);
        });
        assertTrue(exception.getMessage().contains(userId.toString()));
    }

    @Test
    void patchUser_success_ownProfile() {
        // Arrange
        Long userId = 1L;
        UserController.UserPatchRequest patch = new UserController.UserPatchRequest();
        patch.setEmail("new@mail.com");
        patch.setPassword("newPass123");

        User user = new User();
        user.setId(userId);

        // JWT имитирует sub = 1
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn("1");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Подставляем authentication в SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user)); // current
        when(userRepository.findById(userId)).thenReturn(Optional.of(user)); // userToUpdate
        when(passwordEncoder.encode("newPass123")).thenReturn("hashedPass");

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("127.0.0.1");
        when(req.getHeader("User-Agent")).thenReturn("JUnitTest/1.0");

        // Act
        ResponseEntity<?> response = controller.patchUser(
                userId, patch, req, "Bearer abc", "JUnitTest/1.0"
        );

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertSame(user, response.getBody());
        assertEquals("new@mail.com", user.getEmail());
        assertEquals("hashedPass", user.getPassword());

        verify(passwordEncoder).encode("newPass123");
        verify(securityLogService).logEvent(eq(userId), eq("PASSWORD_CHANGE"), eq("127.0.0.1"), anyString(), isNull());
        verify(userRepository, atLeastOnce()).save(user);
    }

    @Test
    void patchUser_forbidden_alienProfile() {
        // Arrange
        Long authUserId = 1L;
        Long patchUserId = 2L;

        UserController.UserPatchRequest patch = new UserController.UserPatchRequest();
        patch.setEmail("other@mail.com");

        User authUser = new User();
        authUser.setId(authUserId);
        User targetUser = new User();
        targetUser.setId(patchUserId);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn(authUserId.toString());

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findById(authUserId)).thenReturn(Optional.of(authUser));
        when(userRepository.findById(patchUserId)).thenReturn(Optional.of(targetUser));

        HttpServletRequest req = mock(HttpServletRequest.class);

        // Act
        ResponseEntity<?> response = controller.patchUser(
                patchUserId, patch, req, null, null
        );

        // Assert
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("только свой собственный"));
    }

    @Test
    void patchUser_unauthorized() {
        // arrange
        SecurityContextHolder.clearContext();
        UserController.UserPatchRequest patch = new UserController.UserPatchRequest();

        HttpServletRequest req = mock(HttpServletRequest.class);

        // act
        ResponseEntity<?> response = controller.patchUser(
                5L, patch, req, null, null
        );

        // assert
        assertEquals(401, response.getStatusCodeValue());
    }
}