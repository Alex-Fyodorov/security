package com.globus.biometric_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Change device biometric status")
public record DeviceStatusChangeRequest(@Schema(
        description = "Device identification information",
        example = "iPhone14,3 iOS 16.4.1",
        required = true
) String deviceInfo, @Schema(
        description = "User ID for authentication",
        example = "12345",
        required = true
)int userId,  @Schema(
        description = "Device biometric status",
        example = "true",
        required = true
)boolean enabled) {
}
