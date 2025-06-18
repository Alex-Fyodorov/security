package com.globus.biometric_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@ToString
@Schema(description = "Biometric settings configuration response")
public class BiometricSettingsResponse {
    @Schema(
            description = "Configuration ID",
            example = "789"
    )
    private Integer id;
    @Schema(
            description = "Associated user ID",
            example = "12345"
    )
    private Integer userId;
    @Schema(
            description = "Registered devices",
            implementation = DeviceDto.class,
            required = true
    )
    private List<DeviceDto> devices;
    @Schema(
            description = "Last successful authentication timestamp",
            example = "2025-05-15T14:30:00"
    )
    private LocalDateTime lastUsed;
}

