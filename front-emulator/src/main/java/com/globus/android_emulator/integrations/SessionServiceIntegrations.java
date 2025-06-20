package com.globus.android_emulator.integrations;

import com.globus.android_emulator.dto.PageDto;
import com.globus.android_emulator.dto.SessionDto;
import com.globus.android_emulator.exceptions.TooManySessionsException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionServiceIntegrations {
    private final WebClient sessionTracingServiceWebClient;

    public PageDto findAll(Integer userId, LocalDateTime minLoginTime,
                           String method, Boolean isActive, Integer page, String sort) {
        return sessionTracingServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("")
                        .queryParam("user_id", userId)
                        .queryParam("min_login_time", minLoginTime)
                        .queryParam("method", method)
                        .queryParam("is_active", isActive)
                        .queryParam("page", page)
                        .queryParam("sort", sort)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .bodyToMono(PageDto.class)
                .block();
    }

    public List<SessionDto> findAllFromRedis() {
        return sessionTracingServiceWebClient.get()
                .uri("/active")
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .bodyToFlux(SessionDto.class)
                .collectList()
                .block();
    }

    public SessionDto login(SessionDto sessionDto) {
        return sessionTracingServiceWebClient.post()
                .bodyValue(sessionDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleResponse)
                .bodyToMono(SessionDto.class)
                .block();
    }

//    public void prolongSession(Integer userId, String deviceInfo) {
//        sessionTracingServiceWebClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/prolong")
//                        .queryParam("user_id", userId)
//                        .queryParam("device_info", deviceInfo)
//                        .build())
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, this::handleResponse)
//                .toBodilessEntity()
//                .block();
//    }

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
                        return new UserOrResourceNotFoundException(
                                response.getBody().get("message").toString());
                    }
                    if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                        log.info("bab request");
                        return new IllegalArgumentException(
                                response.getBody().get("message").toString());
                    }
                    if (response.getStatusCode().equals(HttpStatus.NOT_ACCEPTABLE)) {
                        return new TooManySessionsException(
                                response.getBody().get("message").toString());
                    }
                    if (response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                        return new InternalException(
                                response.getBody().get("message").toString());
                    }
                    return null;
                });
    }
}
