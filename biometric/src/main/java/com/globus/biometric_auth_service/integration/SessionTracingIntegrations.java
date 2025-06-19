package com.globus.biometric_auth_service.integration;

import com.globus.biometric_auth_service.dto.SessionDto;
import com.globus.biometric_auth_service.exception.ResourceNotFoundException;
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
public class SessionTracingIntegrations {
    private final WebClient sessionTracingServiceWebClient;

    public SessionDto login(Integer userId, String deviceInfo, String method, String ipAddress) {
        SessionDto sessionDto = SessionDto.builder()
                .userId(userId)
                .deviceInfo(deviceInfo)
                .method(method)
                .ipAddress(ipAddress)
                .build();

                return sessionTracingServiceWebClient.post()
                .bodyValue(sessionDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .bodyToMono(SessionDto.class)
                .block();
    }

    public void prolongSession(Integer userId, String deviceInfo) {
        sessionTracingServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/prolong")
                        .queryParam("user_id", userId)
                        .queryParam("device_info", deviceInfo)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .toBodilessEntity()
                .block();
    }

    public void logout(Integer userId, String deviceInfo) {
        sessionTracingServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/logout")
                        .queryParam("user_id", userId)
                        .queryParam("device_info", deviceInfo)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .toBodilessEntity()
                .block();
    }

    private Mono<? extends Throwable> handleResponse(ClientResponse clientResponse) {
        return clientResponse
                .toEntity(Map.class)
                .map(response -> {
                    if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        log.info("not found");
                        return new ResourceNotFoundException(
                                response.getBody().get("message").toString());
                    }
                    if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                        log.info("bab request");
                        return new IllegalArgumentException(
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
