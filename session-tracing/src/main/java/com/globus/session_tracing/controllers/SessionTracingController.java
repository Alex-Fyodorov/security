package com.globus.session_tracing.controllers;

import com.globus.session_tracing.converters.SessionConverter;
import com.globus.session_tracing.dtos.SessionDto;
import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.services.SessionTracingService;
import com.globus.session_tracing.validators.SessionValidator;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionTracingController {
    private final SessionTracingService sessionTracingService;
    private final SessionConverter sessionConverter;
    private final SessionValidator sessionValidator;

    @GetMapping
    public Page<SessionDto> findAll(
            @RequestParam(required = false, name = "user_id") @Parameter(
                    description = "Фильтр по id пользователя") Integer userId,
            @RequestParam(required = false, name = "min_login_time") @Parameter(
                    description = "Фильтр по минимальному времени вхождения в аккаунт") LocalDateTime minLoginTime,
            @RequestParam(required = false, name = "method") @Parameter(
                    description = "Фильтр по методу вхождения в аккаунт") String method,
            @RequestParam(required = false, name = "is_active") @Parameter(
                    description = "Фильтр по активности сессии") Boolean isActive,
            @RequestParam(defaultValue = "1", name = "page") @Parameter(
                    description = "Номер страницы") Integer page,
            @RequestParam(defaultValue = "id", name = "sort") @Parameter(description =
                    "Сортировка сессий по id, id пользователя, времени входа или методу") String sort) {
        return sessionTracingService.findAll(userId, minLoginTime, method, isActive, page, sort)
                .map(sessionConverter::toDto);
    }

    @GetMapping("/{id}")
    public SessionDto read(@PathVariable long id) {
        return sessionConverter.toDto(sessionTracingService.findBySessionId(id));
    }

    @GetMapping("/redis/{id}")
    public SessionDto readFromRedis(@PathVariable long id) {
        return sessionConverter.toDto(sessionTracingService.findFromRedis(id));
    }

    @PostMapping
    public SessionDto create(@RequestBody SessionDto sessionDto) {
        sessionValidator.validate(sessionDto);
        Session session = sessionConverter.toEntity(sessionDto);
        return sessionConverter.toDto(sessionTracingService.save(session));
    }

    @GetMapping("/logout/{id}")
    public void logout(@PathVariable long id) {
        sessionTracingService.logout(id);
    }

}

// TODO Дописать сваггер в SessionDto
// TODO Доделать Schedul

/*
Обсудить:
1. Метод делете
2. Валидатор
*/