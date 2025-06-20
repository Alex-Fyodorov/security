package com.globus.android_emulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SessionDto {
    private Long id;
    private Integer userId;
    private Date loginTime;
    private Date logoutTime;
    private String method;
    private String ipAddress;
    private String deviceInfo;
    private Boolean isActive;
}
