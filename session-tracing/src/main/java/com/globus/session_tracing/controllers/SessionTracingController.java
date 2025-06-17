package com.globus.session_tracing.controllers;

import com.globus.session_tracing.converters.SessionConverter;
import com.globus.session_tracing.dtos.SessionDto;
import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.exceptions.AppError;
import com.globus.session_tracing.services.SessionTracingService;
import com.globus.session_tracing.validators.SessionValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Сессии пользователей", description = "Отслеживание и методы работы с пользовательскими сессиями")
@RequiredArgsConstructor
public class SessionTracingController {
    private final SessionTracingService sessionTracingService;
    private final SessionConverter sessionConverter;
    private final SessionValidator sessionValidator;

    @GetMapping
    @Operation(summary = "Запрос на получение страницы с сессиями",
            responses = {
                    @ApiResponse(
                            description = "Успешный ответ", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = Page.class))
                    )
            })
    public Page<SessionDto> findAll(
            @RequestParam(required = false, name = "user_id") @Parameter(
                    description = "Фильтр по id пользователя") Integer userId,
            @RequestParam(required = false, name = "min_login_time") @Parameter(
                    description = "Фильтр по времени входа в аккаунт") LocalDateTime minLoginTime,
            @RequestParam(required = false, name = "method") @Parameter(
                    description = "Фильтр по методу входа в аккаунт") String method,
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
    @Operation(summary = "Запрос на получение сессии по id",
            responses = {
                    @ApiResponse(
                            description = "Успешный ответ", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SessionDto.class))
                    ),
                    @ApiResponse(
                            description = "Сессия не найдена", responseCode = "404",
                            content = @Content(schema = @Schema(implementation = AppError.class))
                    )
            })
    public SessionDto findDyId(@PathVariable @Parameter(
            description = "Идентификатор сессии", required = true) long id) {
        return sessionConverter.toDto(sessionTracingService.findBySessionId(id));
    }

    @GetMapping("/redis/{id}")
    @Operation(summary = "Запрос на получение активной сессии по id",
            description = "Получение сессии из кэша быстрого доступа, в котором хранятся активные сессии",
            responses = {
                    @ApiResponse(
                            description = "Успешный ответ", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SessionDto.class))
                    ),
                    @ApiResponse(
                            description = "Сессия не найдена", responseCode = "404",
                            content = @Content(schema = @Schema(implementation = AppError.class))
                    )
            })
    public SessionDto readFromRedis(@PathVariable @Parameter(
            description = "Идентификатор сессии", required = true) long id) {
        return sessionConverter.toDto(sessionTracingService.findFromRedis(id));
    }

    @GetMapping("/active")
    @Operation(summary = "Запрос на получение списка активных сессий",
            description = "Получение списка сессий из кэша быстрого доступа, в котором хранятся активные сессии",
            responses = {
                    @ApiResponse(
                            description = "Успешный ответ", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = Page.class))
                    )
            })
    public List<SessionDto> readAllFromRedis() {
        return sessionTracingService.findAllFromRedis()
                .stream().map(sessionConverter::toDto).collect(Collectors.toList());
    }

    @GetMapping("/prolong/{id}")
    @Operation(summary = "Продление срока жизни сессии по id",
            description = "При отсутствии активности сессия автоматически закрывается через 30 минут. " +
                    "Данный запрос подтверждает наличие активности и восстанавливает срок жизни сессии до начальных 30 минут.",
            responses = {
                    @ApiResponse(
                            description = "Успешный ответ", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SessionDto.class))
                    ),
                    @ApiResponse(
                            description = "Сессия не найдена или уже закрыта", responseCode = "404",
                            content = @Content(schema = @Schema(implementation = AppError.class))
                    )
            })
    public void prolongSession(@PathVariable @Parameter(
            description = "Идентификатор сессии", required = true) Long id) {
        sessionTracingService.prolongSession(id);
    }

    @PostMapping
    @Operation(summary = "Запрос на создание новой сессии",
            responses = {
                    @ApiResponse(
                            description = "Сессия успешно создана", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SessionDto.class))
                    ),
                    @ApiResponse(
                            description = "В запросе на создание сессии не заполнено поле userId", responseCode = "400",
                            content = @Content(schema = @Schema(implementation = AppError.class))
                    ),
                    @ApiResponse(
                            description = "Открыто слишком много сессий", responseCode = "406",
                            content = @Content(schema = @Schema(implementation = AppError.class))
                    )
            })
    public SessionDto create(@RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Дапнные, необходимые для создания новой сессии",
            required = true,
            content = @Content(schema = @Schema(implementation = SessionDto.class))) SessionDto sessionDto) {
        sessionValidator.validate(sessionDto);
        Session session = sessionConverter.toEntity(sessionDto);
        return sessionConverter.toDto(sessionTracingService.save(session));
    }

    @GetMapping("/logout/{id}")
    @Operation(summary = "Запрос на закрытие сессии",
            responses = {
                    @ApiResponse(
                            description = "Продукт успешно создан", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SessionDto.class))
                    ),
                    @ApiResponse(
                            description = "Сессия не найдена", responseCode = "404",
                            content = @Content(schema = @Schema(implementation = AppError.class))
                    ),
                    @ApiResponse(
                            description = "Невозможно выполнить операцию. Сессия с уже закрыта.", responseCode = "406",
                            content = @Content(schema = @Schema(implementation = AppError.class))
                    )
            })
    public void logout(@PathVariable @Parameter(
            description = "Идентификатор сессии", required = true) long id) {
        sessionTracingService.logout(id);
    }

}

// TODO Посмотреть по поводу операций с сессиями в сервисе и подумать, как их перекинуть на редис
// TODO Комменты
// TODO Отремонтировать кнопки страниц
// TODO Сделать маскировку ip
// TODO Тесты
