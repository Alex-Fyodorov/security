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
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/active")
    public List<SessionDto> readAllFromRedis() {
        return sessionTracingService.findAllFromRedis()
                .stream().map(sessionConverter::toDto).collect(Collectors.toList());
    }

    @GetMapping("/prolong/{id}")
    public void prolongSession(@PathVariable Long id) {
        sessionTracingService.prolongSession(id);
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
// TODO Посмотреть по поводу операций с сессиями в сервисе и подумать, как их перекинуть на редис

/*
Обсудить:
1. Метод делете, код 204
2. Валидатор
3. Продление сессии
4. Удаление старых сессий
5. Ошибка 404 пользователь не найден
*/