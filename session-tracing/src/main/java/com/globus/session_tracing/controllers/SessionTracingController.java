package com.globus.session_tracing.controllers;

import com.globus.session_tracing.converters.SessionConverter;
import com.globus.session_tracing.dtos.SessionDto;
import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.services.SessionTracingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionTracingController {
    private final SessionTracingService sessionTracingService;
    private final SessionConverter sessionConverter;

    @PostMapping
    public Session save(@RequestBody Session log) {
        return sessionTracingService.save(log);
    }

    @GetMapping("/logout/{id}")
    public void logout(@PathVariable int id) {
        sessionTracingService.logout(id);
    }

    @GetMapping("/read/{id}")
    public Session read(@PathVariable int id) {
        return sessionTracingService.read(id);
    }

    //public Page<SessionDto> findAll()
}

// TODO Дописать сваггер в SessionDto
