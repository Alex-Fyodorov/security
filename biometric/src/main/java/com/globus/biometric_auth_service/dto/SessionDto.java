package com.globus.biometric_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Модель записи в таблицу трассировки сессий")
public class SessionDto {

    @Schema(description = "Идентификатор сессии", example = "1")
    private Long id;
    @Schema(description = "Идентификатор пользователя", example = "1")
    private Integer userId;
    @Schema(description = "Дата и время входа пользователя в аккаунт", example = "2025-06-10T18:15:39.697528")
    private Date loginTime;
    @Schema(description = "Дата и время выхода пользователя из аккаунта", example = "2025-06-10T19:17:39.697528")
    private Date logoutTime;
    @Schema(description = "Метод входа", example = "password")
    private String method;
    @Schema(description = "Маскированный ip-адрес", example = "192.168.***.**1")
    private String ipAddress;
    @Schema(description = "Информация об устройстве")
    private String deviceInfo;
    @Schema(description = "Активность сессии", example = "true")
    private Boolean isActive;
}
