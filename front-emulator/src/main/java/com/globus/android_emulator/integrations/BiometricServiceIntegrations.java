package com.globus.android_emulator.integrations;

import com.globus.android_emulator.dto.*;
import com.globus.android_emulator.exceptions.BadBiometricRequestException;
import com.globus.android_emulator.exceptions.UserOrResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class BiometricServiceIntegrations {
    private final WebClient biometricServiceWebClient;

    public OtpResponse registration(BiometricRegisterRequest request) {
        return biometricServiceWebClient.post()
                .uri("request-otp")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .bodyToMono(OtpResponse.class)
                .block();
    }

    public BiometricSettingsResponse sms(BiometricRegisterRequest request) {
        return biometricServiceWebClient.post()
                .uri("enable")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .bodyToMono(BiometricSettingsResponse.class)
                .block();
    }

    public JwtResponse auth(BiometricAuthRequest request) {
        return biometricServiceWebClient.post()
                .uri("login")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .bodyToMono(JwtResponse.class)
                .block();
    }

    public BiometricSettingsResponse check(int userId) {
        return biometricServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("status")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .bodyToMono(BiometricSettingsResponse.class)
                .block();
    }

    public DeviceDto changeStatus(DeviceStatusChangeRequest request) {
        return biometricServiceWebClient.post()
                .uri("device_enabled")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .bodyToMono(DeviceDto.class)
                .block();
    }

    private Mono<? extends Throwable> handleResponse(ClientResponse clientResponse) {
        return clientResponse
                .toEntity(Map.class)
                .map(response -> {
                    if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        log.info("not found");
                        return new UserOrResourceNotFoundException(
                                response.getBody().get("message").toString());
                    }
                    if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                        log.info("bab request");
                        return new BadBiometricRequestException(
                                response.getBody().get("message").toString());
                    }
//                    if (response.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
//                        return new (
//                                response.getBody().get("message").toString());
//                    }
                    if (response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                        return new InternalException(
                                response.getBody().get("message").toString());
                    }
                    return null;
                });
    }
}
