package com.globus.session_tracing.controllers;

import com.globus.session_tracing.services.SessionTracingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SessionTracingController {
    private final SessionTracingService sessionTracingService;
}
