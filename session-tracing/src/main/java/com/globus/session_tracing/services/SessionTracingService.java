package com.globus.session_tracing.services;

import com.globus.session_tracing.repositiries.SecurityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionTracingService {
    private final SecurityLogRepository repository;
}
