package com.globus.session_tracing.services;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.exceptions.SessionNotFoundException;
import com.globus.session_tracing.exceptions.SessionsOperationsException;
import com.globus.session_tracing.exceptions.TooManySessionsException;
import com.globus.session_tracing.repositiries.RedisRepository;
import com.globus.session_tracing.repositiries.SessionRepository;
import com.globus.session_tracing.repositiries.specifications.SessionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionTracingService {
    private final static int SESSIONS_IN_PAGE = 20;
    private final static int SESSIONS_LIFE_DAYS = 183;
    private final SessionRepository sessionRepository;
    private final RedisRepository redisRepository;

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
        return sessionRepository.findAll(specification, PageRequest.of(page - 1,
                SESSIONS_IN_PAGE, Sort.by(sort)));
    }

    public Session findBySessionId(long id) {
        return sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(
                String.format("Сессия с id: %d не найдена.", id)));
    }

    public Session save(Session session) {
        int count = sessionRepository.sessionCount(session.getUserId());
        if (count >= 3) {
            throw new TooManySessionsException("Открыто слишком много сессий.");
        }
        session.setId(null);
        session = sessionRepository.save(session);
        if (session.getId() != null) {
            redisRepository.add(session);
        }
        return session;
    }

    public void logout(long id) {
        Optional<Session> session = sessionRepository.findById(id);
        if (session.isEmpty()) {
            throw new SessionNotFoundException(
                    String.format("Сессия с id: %d не найдена.", id));
        }
        if (!session.get().getIsActive()) {
            throw new SessionsOperationsException(String.format(
                    "Невозможно выполнить операцию. Сессия с id: %d закрыта.", id));
        }
        sessionRepository.logout(id);
        redisRepository.delete(id);
    }

    public Session findFromRedis(long id) {
        return redisRepository.read(id).orElseThrow(() -> new SessionNotFoundException(
                String.format("Сессия с id: %d не найдена.", id)));
    }

    @Scheduled(cron = "${task.cron.value}")
    public void deleteOldSessions() {
        log.info(String.format("Удаление сессий, созданных больше %d дней назад.", SESSIONS_LIFE_DAYS));
    }

}
