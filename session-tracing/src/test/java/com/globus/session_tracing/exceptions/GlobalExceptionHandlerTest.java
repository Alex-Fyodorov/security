package com.globus.session_tracing.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void catchSessionNotFoundException() {
        String message = "Session not found!";
        SessionNotFoundException ex = new SessionNotFoundException(message);

        ResponseEntity<AppError> response = handler.catchSessionNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatusCode()); // 👈 исправлено
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void catchTooManySessionsException() {
        String message = "Too many sessions!";
        TooManySessionsException ex = new TooManySessionsException(message);

        ResponseEntity<AppError> response = handler.catchTooManySessionsException(ex);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getBody().getStatusCode()); // 👈 исправлено
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void catchSessionsOperationsException() {
        String message = "Session operation failed!";
        SessionsOperationsException ex = new SessionsOperationsException(message);

        ResponseEntity<AppError> response = handler.catchSessionsOperationsException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatusCode()); // 👈 исправлено
        assertEquals(message, response.getBody().getMessage());
    }
}