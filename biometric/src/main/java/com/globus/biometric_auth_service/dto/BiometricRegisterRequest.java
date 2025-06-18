package com.globus.biometric_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Biometric registration request payload")
public record BiometricRegisterRequest(
        @Schema(
        description = "Device identification information",
        example = "Google Pixel 7 Android 13",
        required = true
        )String deviceInfo,
        @Schema(
        description = "User ID for registration",
        example = "12345",
        required = true
        )Integer userId,
        @Schema(
        description = "Phone number for OTP verification",
        example = "+15551234567",
        required = true
        )String phoneNumber,
        @Schema(
        description = "One-time password for verification",
        example = "987654",
        required = true
        )String otp,
        @Schema(
        description = "Type of biometric registration",
        example = "FINGERPRINT",
        required = true
        )String biometricType) {
}
