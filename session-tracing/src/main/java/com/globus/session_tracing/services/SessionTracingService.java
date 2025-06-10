package com.globus.session_tracing.services;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.repositiries.SessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionTracingService {
    private final SessionRepository repository;

    public Session save(Session log) {
        log.setId(null);
        return repository.save(log);
    }

    public void logout(int id) {
        repository.logout(id);
    }

//    @Transactional
//    public void logout(int id) {
//        Session log = repository.findById(id).get();
//        log.setIsActive(false);
//        repository.save(log);
//    }

    public Session read(int id) {
        return repository.findById(id).get();
    }
}
