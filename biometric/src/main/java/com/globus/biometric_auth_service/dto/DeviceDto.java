package com.globus.biometric_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Biometric device information")
public class DeviceDto {
    @Schema(
            description = "Device registration ID",
            example = "456"
    )
    private Integer id;
    @Schema(
            description = "Associated account ID",
            example = "1001"
    )
    private Integer accountId;
    @Schema(
            description = "Device hardware/fingerprint",
            example = "Face ID: iPhone14,3",
            required = false
    )
    private String deviceInfo;
    @Schema(
            description = "Biometric activation status",
            example = "true"
    )
    private Boolean biometricEnabled;
    private String biometryType;
}
