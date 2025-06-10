package com.globus.android_emulator.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private List<String> errorFieldsMessages;

    public ValidationException(List<String> errorFieldsMessages) {
        super(String.join("; ", errorFieldsMessages));
        this.errorFieldsMessages = errorFieldsMessages;
    }
}
