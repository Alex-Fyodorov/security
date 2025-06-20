package com.globus.android_emulator.controllers;

import com.globus.android_emulator.dto.PageDto;
import com.globus.android_emulator.dto.SessionDto;
import com.globus.android_emulator.integrations.SessionServiceIntegrations;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/session")
public class SessionController {
    private final SessionServiceIntegrations sessionServiceIntegrations;

    @GetMapping
    public PageDto findAll(
            @RequestParam(required = false, name = "user_id") Integer userId,
            @RequestParam(required = false, name = "min_login_time") LocalDateTime minLoginTime,
            @RequestParam(required = false, name = "method") String method,
            @RequestParam(required = false, name = "is_active") Boolean isActive,
            @RequestParam(defaultValue = "1", name = "page") Integer page,
            @RequestParam(defaultValue = "id", name = "sort") String sort) {
        return sessionServiceIntegrations.findAll(userId, minLoginTime, method, isActive, page, sort);
    }

    @GetMapping("/active")
    public List<SessionDto> readAllFromRedis() {
        return sessionServiceIntegrations.findAllFromRedis();
    }

    @PostMapping
    public SessionDto create(@RequestBody SessionDto sessionDto) {
        return sessionServiceIntegrations.login(sessionDto);
    }

    @GetMapping("/logout")
    public void logout(@RequestParam (name = "user_id") Integer userId,
                       @RequestParam (name = "device_info") String deviceInfo) {
        sessionServiceIntegrations.logout(userId, deviceInfo);
    }
}
