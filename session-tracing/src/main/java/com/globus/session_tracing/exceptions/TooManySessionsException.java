package com.globus.session_tracing.exceptions;

public class TooManySessionsException extends RuntimeException {
    public TooManySessionsException(String message) {
        super(message);
    }
}
