package com.globus.android_emulator.exceptions;

public class TooManySessionsException extends RuntimeException {
    public TooManySessionsException(String message) {
        super(message);
    }
}
