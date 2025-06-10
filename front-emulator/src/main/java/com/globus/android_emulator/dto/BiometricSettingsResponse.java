package com.globus.android_emulator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@ToString
public class BiometricSettingsResponse {
    private Integer id;
    private Integer userId;
    private List<DeviceDto> devices;
    private LocalDateTime lastUsed;
}

