package com.globus.session_tracing.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<AppError> catchSessionNotFoundException(SessionNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new AppError(HttpStatus.NOT_FOUND.value(),
                exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<AppError> catchTooManySessionsException(TooManySessionsException exception) {
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new AppError(HttpStatus.NOT_ACCEPTABLE.value(),
                exception.getMessage()), HttpStatus.NOT_ACCEPTABLE);
    }
}
