package com.globus.android_emulator.exceptions;

public class BadBiometricRequestException extends RuntimeException{
    public BadBiometricRequestException(String message) {
        super(message);
    }
}
