package com.globus.modul26.controller;

import com.globus.modul26.model.SecurityLog;
import com.globus.modul26.model.User;
import com.globus.modul26.repository.UserRepository;
import com.globus.modul26.service.SecurityLogService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String SECRET = "super-secure-random-string-change-me-please-super-long";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityLogService securityLogService;

    @Autowired
    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          SecurityLogService securityLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityLogService = securityLogService;
    }

    @Operation(
            summary = "User login",
            description = "Авторизация пользователя с логином и паролем (и биометрией, если нужно)",
            parameters = {
                    @Parameter(
                            name = "User-Agent",
                            description = "Информация о устройстве, браузере клиента",
                            required = false,
                            in = ParameterIn.HEADER,
                            example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                    ),
                    @Parameter(
                            name = "X-Custom-Header",
                            description = "Любой дополнительный необязательный заголовок (пример)",
                            required = false,
                            in = ParameterIn.HEADER,
                            example = "some-value"
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest login,


            @Parameter(hidden = true)
            @RequestHeader(value = "User-Agent", required = false) String userAgent,

            @Parameter(hidden = true)
            @RequestHeader(value = "X-Custom-Header", required = false) String customHeader,

            HttpServletRequest request
    ) {
        Optional<User> userOpt = userRepository.findByUsername(login.getUsername());
        Long userId = userOpt.map(User::getId).orElse(null);

        // Проверка блокировки пользователя
        if (userOpt.isPresent() && Boolean.TRUE.equals(userOpt.get().getLocked())) {
            securityLogService.saveLog(SecurityLog.builder()
                    .userId(userId)
                    .eventType("LOGIN_ATTEMPT")
                    .ipAddress(request.getRemoteAddr())
                    .isSuspicious(true)
                    .deviceInfo(userAgent != null ? userAgent : request.getHeader("User-Agent"))
                    .biometryUsed(login.getBiometryUsed())
                    .createdAt(LocalDateTime.now())
                    .build()
            );
            return ResponseEntity.status(403).body("Пользователь заблокирован из-за многократных неудачных попыток входа");
        }

        boolean success = userOpt.isPresent()
                && passwordEncoder.matches(login.getPassword(), userOpt.get().getPassword());

        // Логирование попыткИ
        securityLogService.saveLog(SecurityLog.builder()
                .userId(userId)
                .eventType("LOGIN_ATTEMPT")
                .ipAddress(request.getRemoteAddr())
                .isSuspicious(!success)
                .deviceInfo(userAgent != null ? userAgent : request.getHeader("User-Agent"))
                .biometryUsed(login.getBiometryUsed())
                .createdAt(LocalDateTime.now())
                .build()
        );

        // Если не успех — блок
        if (!success) {
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // 3 последние попытки входа
                List<SecurityLog> lastAttempts = securityLogService.getLastLoginAttempts(user.getId(), 3);
                boolean lastThreeFailed = lastAttempts.size() == 3 &&
                        lastAttempts.stream().allMatch(SecurityLog::getIsSuspicious);

                if (lastThreeFailed) {
                    user.setLocked(true);
                    userRepository.save(user);
                    return ResponseEntity.status(403)
                            .body("Пользователь заблокирован из-за 3 неудачных попыток входа подряд");
                }
            }
            return ResponseEntity.status(401).body("Неверный логин или пароль");
        }

        // Всё ок — обновление времени последнего входа
        User user = userOpt.get();
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Генерация токена с JTI
        String token = Jwts.builder()
                .setId(java.util.UUID.randomUUID().toString())
                .setSubject(user.getId().toString())
                .claim("roles", List.of(user.getRole().name()))
                .claim("email", user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, SECRET.getBytes())
                .compact();

        return ResponseEntity.ok(new JwtResponse(token));
    }

    // DTO для логина
    public static class LoginRequest {
        private String username;
        private String password;

        @NotNull(message = "Поле biometryUsed обязательно")
        private Boolean biometryUsed;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public Boolean getBiometryUsed() { return biometryUsed; }
        public void setBiometryUsed(Boolean biometryUsed) { this.biometryUsed = biometryUsed; }
    }

    // DTO для ответа с токеном
    public static class JwtResponse {
        private String token;
        public JwtResponse(String token) { this.token = token; }
        public String getToken() { return token; }
    }
}