package com.globus.biometric_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Biometric authentication request payload")
public record BiometricAuthRequest(@Schema(
        description = "Device identification information",
        example = "iPhone14,3 iOS 16.4.1",
        required = false
) String deviceInfo, @Schema(
        description = "User ID for authentication",
        example = "12345",
        required = true
)Integer userId,  @Schema(
        description = "Biometric authentication success flag",
        example = "true",
        required = true
)boolean authenticated) {
}
