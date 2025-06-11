package com.globus.session_tracing.converters;

import com.globus.session_tracing.dtos.SessionDto;
import com.globus.session_tracing.entities.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionConverter {

    public SessionDto toDto(Session session) {
        return SessionDto.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .deviceInfo(session.getDeviceInfo())
                .method(session.getMethod())
                .ipAddress(session.getIpAddress())
                .loginTime(session.getLoginTime())
                .logoutTime(session.getLogoutTime())
                .isActive(session.getIsActive())
                .build();
    }

    public Session toEntity(SessionDto sessionDto) {
        return Session.builder()
                .id(sessionDto.getId())
                .userId(sessionDto.getUserId())
                .deviceInfo(sessionDto.getDeviceInfo())
                .method(sessionDto.getMethod())
                .ipAddress(sessionDto.getIpAddress())
                .loginTime(sessionDto.getLoginTime())
                .logoutTime(sessionDto.getLogoutTime())
                .isActive(sessionDto.getIsActive())
                .build();
    }
}
