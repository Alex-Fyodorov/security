package com.globus.session_tracing.services;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.repositiries.SessionRepository;
import com.globus.session_tracing.repositiries.specifications.SessionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionTracingService {
    private final SessionRepository sessionRepository;
    private final static int SESSIONS_IN_PAGE = 50;

    public Page<Session> findAll(Integer userId, LocalDateTime minLoginTime, String method,
                                 Boolean isActive, Integer page, String sort) {
        Specification<Session> specification = (root, query, builder) -> null;
        if (userId != null) {
            specification = specification.and(SessionSpecification.userIdEqualTo(userId));
        }
        if (minLoginTime != null) {
            specification = specification.and(SessionSpecification.loginTimeGreaterOrEqualThan(minLoginTime));
        }
        if (method != null) {
            specification = specification.and(SessionSpecification.methodEqualTo(method));
        }
        if (isActive != null) {
            specification = specification.and(SessionSpecification.activityEqualTo(isActive));
        }
        if (page < 1) page = 1;
        return sessionRepository.findAll(specification, PageRequest.of(page - 1, SESSIONS_IN_PAGE, Sort.by(sort)));
    }

    public Session save(Session log) {
        log.setId(null);
        return sessionRepository.save(log);
    }

    public void logout(int id) {
        sessionRepository.logout(id);
    }


    public Session read(int id) {
        return sessionRepository.findById(id).get();
    }
}
