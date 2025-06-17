package com.globus.modul26.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globus.modul26.model.SecurityLog;
import com.globus.modul26.service.JwtBlacklistService;
import com.globus.modul26.service.JwtServiceImpl;
import com.globus.modul26.service.SecurityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Логи безопасности
@RestController
@RequestMapping("/api/logs")
public class SecurityLogController {

    private final SecurityLogService service;
    private final JwtBlacklistService jwtBlacklistService;
    private final JwtServiceImpl jwtService;

    public SecurityLogController(SecurityLogService service,
                                 JwtBlacklistService jwtBlacklistService,
                                 JwtServiceImpl jwtService) {
        this.service = service;
        this.jwtBlacklistService = jwtBlacklistService;
        this.jwtService = jwtService;
    }

    //  Запись события (универсальный метод для любого eventType)
    @Operation(
            summary = "Сохранить security event",
            description = "Логирование событий безопасности (LOGIN, LOGOUT, LOGIN_ATTEMPT и др.). Требуется JWT.",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "JWT токен пользователя в формате Bearer {token}",
                            required = true,
                            in = ParameterIn.HEADER,
                            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    ),
                    @Parameter(
                            name = "User-Agent",
                            description = "Информация о клиентском устройстве или софте",
                            required = false,
                            in = ParameterIn.HEADER,
                            example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                    )
            }
    )
    @PostMapping("/event")
    public ResponseEntity<SecurityLog> logEvent(
            @RequestBody SecurityLog log,
            Authentication authentication,
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true)
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        //  Подстановка userId из JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = extractUserIdFromJwt(jwt);
        log.setUserId(userId);

        //  Маскировка IP (основное поле)
        if (log.getIpAddress() != null) {
            log.setIpAddress(maskIp(log.getIpAddress()));
        }

        //  Special: если logout — blacklist jti токена
        if ("LOGOUT".equalsIgnoreCase(log.getEventType())) {
            tryBlacklistToken(authHeader);
        }

        //  Приведение metadata к Map, маскировка ipAddress в metadata, очистка isSuspicious
        log.setMetadata(normalizeAndMaskMetadataIp(log.getMetadata()));
        log.setIsSuspicious(null);

        //  Сохранет лог
        SecurityLog saved = service.saveLog(log);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    //  Получение только подозрительных логов по userId
    @Operation(
            summary = "Подозрительные события пользователя",
            description = "Получить список подозрительных (isSuspicious = true) событий по userId. Требуется JWT.",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "JWT токен пользователя (Bearer ...) для доступа",
                            required = true,
                            in = ParameterIn.HEADER,
                            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    )
            }
    )
    @GetMapping("/suspicious/{userId}")
    public ResponseEntity<List<SecurityLog>> getSuspiciousByUser(
            @PathVariable Long userId,
            Authentication authentication,
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        List<String> rolesList = jwt.getClaimAsStringList("roles");
        boolean isAdmin = rolesList != null && rolesList.contains("ADMIN");
        boolean isUser = rolesList != null && rolesList.contains("USER");
        Long jwtUserId = extractUserIdFromJwt(jwt);

        if (isAdmin) {
            return ResponseEntity.ok(service.findSuspiciousLogsByUserId(userId));
        }
        if (isUser && Objects.equals(jwtUserId, userId)) {
            return ResponseEntity.ok(service.findSuspiciousLogsByUserId(userId));
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    // Все логи по пользователчя
    @Operation(
            summary = "Все события пользователя",
            description = "Получить все security-события по userId. Требуется JWT.",
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "JWT токен пользователя (Bearer ...) для доступа",
                            required = true,
                            in = ParameterIn.HEADER,
                            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    )
            }
    )
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<SecurityLog>> getByUser(
            @PathVariable Long userId,
            Authentication authentication,
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        List<String> rolesList = jwt.getClaimAsStringList("roles");
        boolean isAdmin = rolesList != null && rolesList.contains("ADMIN");
        boolean isUser = rolesList != null && rolesList.contains("USER");
        Long jwtUserId = extractUserIdFromJwt(jwt);

        if (isAdmin) {
            return ResponseEntity.ok(service.findByUserId(userId));
        }
        if (isUser && Objects.equals(jwtUserId, userId)) {
            return ResponseEntity.ok(service.findByUserId(userId));
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    //  Маскировка IP для логов/metadata
    private static String maskIp(String ip) {
        if (ip == null) return null;
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return ip;
        String first = parts[0];
        String second = parts[1].isEmpty() ? "*" : parts[1].substring(0, 1);
        String fourth = parts[3];

        return String.format("%s.%s**.***.%s", first, second, fourth);
    }

    // Нормализация metadata (к Map) и маскировка ip внутри metadata
    private static Map<String, Object> normalizeAndMaskMetadataIp(Object metadataRaw) {
        Map<String, Object> metadataMap;
        if (metadataRaw == null) {
            metadataMap = new HashMap<>();
        } else if (metadataRaw instanceof Map) {
            //noinspection unchecked
            metadataMap = new HashMap<>((Map<String, Object>) metadataRaw);
        } else {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                metadataMap = objectMapper.readValue(metadataRaw.toString(), Map.class);
            } catch (Exception e) {
                metadataMap = new HashMap<>();
            }
        }
        if (metadataMap.containsKey("ipAddress") && metadataMap.get("ipAddress") instanceof String) {
            metadataMap.put("ipAddress", maskIp((String) metadataMap.get("ipAddress")));
        }
        return metadataMap;
    }

    // Извлечение userId (subject) из Jwt
    private static Long extractUserIdFromJwt(Jwt jwt) {
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    // Блэклистинг токена по jti (при logout)
    private void tryBlacklistToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String jti = jwtService.extractJti(token);
            if (jti != null) {
                jwtBlacklistService.blacklist(jti);
                System.out.println("токен заблокирован по jti: " + jti);
            } else {
                System.out.println("LOGOUT: jti токена не найден, добавить в blacklist не удалось!");
            }
        } else {
            System.out.println("LOGOUT: Authorization header не найден или формат неверный.");
        }
    }
}