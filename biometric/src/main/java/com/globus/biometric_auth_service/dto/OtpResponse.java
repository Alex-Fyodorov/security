package com.globus.biometric_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Otp response")
public record OtpResponse( @Schema(
        description = "Generated OTP",
        example = "123456",
        required = true
)String otp) {
}
