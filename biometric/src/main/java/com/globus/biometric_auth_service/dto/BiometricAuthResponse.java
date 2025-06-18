package com.globus.biometric_auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Biometric authentication response")
public class BiometricAuthResponse {
    @Schema(
            description = "Response content message",
            example = "Authentication successful"
    )
    private String content;
    @Schema(
            description = "HTTP status code",
            example = "200"
    )
    private int status;
}
