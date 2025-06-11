package com.globus.session_tracing.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Модель записи в таблицу трассировки сессий")
public class SessionDto {

    @Schema(description = "ID записи", example = "1")
    private Long id;
    @Schema(description = "ID пользователя", example = "1")
    private Integer userId;
    @Schema(description = "Дата и время входа пользователя в аккаунт", example = "2025-06-10T18:15:39.697528")
    private LocalDateTime loginTime;
    @Schema(description = "Дата и время выхода пользователя из аккаунта", example = "2025-06-10T19:17:39.697528")
    private LocalDateTime logoutTime;
    @Schema(description = "Метод входа", example = "?")
    private String method;
    @Schema(description = "Маскированный ip-адрес", example = "192.168.***.**1")
    private String ipAddress;
    @Schema(description = "Информация об устройстве", example = "?")
    private String deviceInfo;
    @Schema(description = "Активность сессии", example = "true")
    private Boolean isActive;
}
