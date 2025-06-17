package com.globus.modul26.service;

import com.globus.modul26.model.Role;
import com.globus.modul26.model.User;
import com.globus.modul26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    private CustomUserDetailsService service;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new CustomUserDetailsService();
        // Внедрение зависимости через reflection
        java.lang.reflect.Field repoField;
        try {
            repoField = CustomUserDetailsService.class.getDeclaredField("userRepository");
            repoField.setAccessible(true);
            repoField.set(service, userRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void loadUserByUsername_success() {
        // Given
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("123");
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        // When
        UserDetails details = service.loadUserByUsername("test@test.com");

        // Then
        assertNotNull(details);
        assertEquals("test@test.com", details.getUsername());
        assertEquals("123", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(
                ga -> ga.getAuthority().equals("ROLE_ADMIN")
        ));
    }

    @Test
    void loadUserByUsername_userNotFound() {
        when(userRepository.findByEmail("nope@nope.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername("nope@nope.com");
        });
    }
}