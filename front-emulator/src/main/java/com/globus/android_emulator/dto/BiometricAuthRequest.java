package com.globus.android_emulator.dto;

public record BiometricAuthRequest(String deviceInfo, int userId, boolean authenticated) {
}
