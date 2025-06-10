package com.globus.android_emulator.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<AppError> catchUserOrResourceNotFoundException(
            UserOrResourceNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new AppError(HttpStatus.NOT_FOUND.value(),
                exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<FieldsValidationError> catchValidationException(ValidationException exception) {
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new FieldsValidationError(HttpStatus.BAD_REQUEST.value(),
                exception.getErrorFieldsMessages()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<AppError> catchBadBiometricRequestException(
            BadBiometricRequestException exception) {
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(),
                exception.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
