package com.globus.modul26.controller;

import com.globus.modul26.model.User;
import com.globus.modul26.repository.UserRepository;
import com.globus.modul26.service.SecurityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityLogService securityLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //  Получение пользователя по ID
    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает публичные данные пользователя по его идентификатору.",
            parameters = {
                    @Parameter(
                            name = "User-Agent",
                            description = "Информация о клиентском устройстве",
                            in = ParameterIn.HEADER,
                            required = false,
                            example = "PostmanRuntime/7.32.0"
                    )
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long userId,
            @Parameter(hidden = true)
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return ResponseEntity.ok(user);
    }

    //  Частичное обновление пользователя (доступно с JWT токеном)
    @Operation(
            summary = "Частичное обновление пользователя",
            description = "Позволяет пользователю изменить свои данные. Требуется JWT токен.",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "JWT токен пользователя (Bearer ...)",
                            in = ParameterIn.HEADER,
                            required = true,
                            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    ),
                    @Parameter(
                            name = "User-Agent",
                            description = "Информация о клиентском устройстве",
                            in = ParameterIn.HEADER,
                            required = false,
                            example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                    )
            }
    )
    @PatchMapping("/{userId}")
    public ResponseEntity<?> patchUser(
            @PathVariable Long userId,
            @RequestBody UserPatchRequest patch,
            HttpServletRequest request,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true)
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Object principal = authentication.getPrincipal();
        Long jwtUserId = null;

        if (principal instanceof Jwt jwt) {
            //  userId — это claim sub
            Object claim = jwt.getClaim("sub");
            if (claim instanceof Integer) {
                jwtUserId = ((Integer) claim).longValue();
            } else if (claim instanceof Long) {
                jwtUserId = (Long) claim;
            } else if (claim instanceof String) {
                jwtUserId = Long.valueOf((String) claim);
            }
        }

        if (jwtUserId == null) {
            return ResponseEntity.status(401).body("Неавторизовано: идентификатор пользователя не найден в токене");
        }

        // Получение пользователя на основе userId из токена
        User currentUser = userRepository.findById(jwtUserId)
                .orElseThrow(() -> new UsernameNotFoundException("Аутентифицированный пользователь не найден"));

        // Получаем пользователя, чей профиль обновляем id из URL)
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден по id: " + userId));

        // Проверка что пользователь пытается изменить только свой профиль
        if (!currentUser.getId().equals(userToUpdate.getId())) {
            return ResponseEntity.status(403).body("Вы можете изменять только свой собственный профиль");
        }

        boolean passwordChanged = false;

        // ⚡ Обновление полей
        if (patch.getEmail() != null) userToUpdate.setEmail(patch.getEmail());
        if (patch.getPhone() != null) userToUpdate.setPhone(patch.getPhone());
        if (patch.getFirstName() != null) userToUpdate.setFirstName(patch.getFirstName());
        if (patch.getLastName() != null) userToUpdate.setLastName(patch.getLastName());

        //  Обработка смены пароля
        if (patch.getPassword() != null && !patch.getPassword().isBlank()) {
            userToUpdate.setPassword(passwordEncoder.encode(patch.getPassword()));
            passwordChanged = true;
        }

        userRepository.save(userToUpdate);

        //  Логирование сменф пароля
        if (passwordChanged) {
            securityLogService.logEvent(
                    userId,
                    "PASSWORD_CHANGE",
                    request.getRemoteAddr(),
                    userAgent != null ? userAgent : request.getHeader("User-Agent"),
                    null
            );
        }

        return ResponseEntity.ok(userToUpdate);
    }

    // DTO для PATCH-запроса
    public static class UserPatchRequest {
        private String email;
        private String phone;
        private String firstName;
        private String lastName;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}