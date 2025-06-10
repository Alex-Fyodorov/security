package com.globus.android_emulator.dto;

public record DeviceStatusChangeRequest(String deviceInfo, int userId, boolean enabled) {
}
