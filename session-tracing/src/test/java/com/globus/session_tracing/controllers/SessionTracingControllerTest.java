package com.globus.session_tracing.controllers;

import com.globus.session_tracing.converters.SessionConverter;
import com.globus.session_tracing.dtos.SessionDto;
import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.services.SessionTracingService;
import com.globus.session_tracing.validators.SessionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SessionTracingControllerTest {

    private SessionTracingService sessionTracingService;
    private SessionConverter sessionConverter;
    private SessionValidator sessionValidator;
    private SessionTracingController controller;

    @BeforeEach
    void setUp() {
        sessionTracingService = mock(SessionTracingService.class);
        sessionConverter = mock(SessionConverter.class);
        sessionValidator = mock(SessionValidator.class);
        controller = new SessionTracingController(sessionTracingService, sessionConverter, sessionValidator);
    }

    @Test
    void findAll() {

        Session session = new Session();
        SessionDto sessionDto = new SessionDto();
        Page<Session> page = new PageImpl<>(List.of(session));
        when(sessionTracingService.findAll(any(), any(), any(), any(), anyInt(), any())).thenReturn(page);
        when(sessionConverter.toDto(session)).thenReturn(sessionDto);


        Page<SessionDto> result = controller.findAll(1, LocalDateTime.now(), "method", true, 1, "id");


        assertEquals(1, result.getTotalElements());
        verify(sessionTracingService, times(1)).findAll(any(), any(), any(), any(), anyInt(), any());
        verify(sessionConverter, times(1)).toDto(session);
    }

    @Test
    void findById() {
        long sessionId = 42L;
        Session session = new Session();
        SessionDto sessionDto = new SessionDto();
        when(sessionTracingService.findBySessionId(sessionId)).thenReturn(session);
        when(sessionConverter.toDto(session)).thenReturn(sessionDto);

        SessionDto result = controller.findById(sessionId);

        assertEquals(sessionDto, result);
        verify(sessionTracingService).findBySessionId(sessionId);
        verify(sessionConverter).toDto(session);
    }

    @Test
    void readFromRedis() {
        long sessionId = 123L;
        Session session = new Session();
        SessionDto sessionDto = new SessionDto();
        when(sessionTracingService.findFromRedisById(sessionId)).thenReturn(session);
        when(sessionConverter.toDto(session)).thenReturn(sessionDto);

        SessionDto result = controller.readFromRedis(sessionId);

        assertEquals(sessionDto, result);
        verify(sessionTracingService).findFromRedisById(sessionId);
        verify(sessionConverter).toDto(session);
    }

    @Test
    void readAllFromRedis() {
        Session session = new Session();
        SessionDto sessionDto = new SessionDto();
        List<Session> sessions = Collections.singletonList(session);
        when(sessionTracingService.findAllFromRedis()).thenReturn(sessions);
        when(sessionConverter.toDto(session)).thenReturn(sessionDto);

        List<SessionDto> result = controller.readAllFromRedis();

        assertEquals(1, result.size());
        assertEquals(sessionDto, result.get(0));
        verify(sessionTracingService).findAllFromRedis();
        verify(sessionConverter).toDto(session);
    }

    @Test
    void prolongSession() {
        Long id = 101L;
        controller.prolongSession(id);
        verify(sessionTracingService, times(1)).prolongSession(id);
    }

    @Test
    void create() {
        SessionDto sessionDto = new SessionDto();
        Session session = new Session();
        Session savedSession = new Session();
        SessionDto resultDto = new SessionDto();

        doNothing().when(sessionValidator).validate(sessionDto);
        when(sessionConverter.toEntity(sessionDto)).thenReturn(session);
        when(sessionTracingService.save(session)).thenReturn(savedSession);
        when(sessionConverter.toDto(savedSession)).thenReturn(resultDto);

        SessionDto result = controller.create(sessionDto);

        assertEquals(resultDto, result);
        verify(sessionValidator).validate(sessionDto);
        verify(sessionConverter).toEntity(sessionDto);
        verify(sessionTracingService).save(session);
        verify(sessionConverter).toDto(savedSession);
    }

    @Test
    void logout() {
        long id = 77L;
        controller.logout(id);
        verify(sessionTracingService, times(1)).logout(id);
    }
}