package com.globus.biometric_auth_service.exception;

public class IllegalAuthStateException extends RuntimeException {
    public IllegalAuthStateException(String message) {
        super(message);
    }
}
