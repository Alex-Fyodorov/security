package com.globus.android_emulator.dto;

public record BiometricRegisterRequest(String deviceInfo, Integer userId,
                                       String phoneNumber, String otp) {
}
