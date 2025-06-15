package com.globus.session_tracing.services;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.exceptions.SessionNotFoundException;
import com.globus.session_tracing.exceptions.SessionsOperationsException;
import com.globus.session_tracing.exceptions.TooManySessionsException;
import com.globus.session_tracing.repositiries.RedisRepository;
import com.globus.session_tracing.repositiries.SessionRepository;
import com.globus.session_tracing.repositiries.specifications.SessionSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionTracingService {
    private final SessionRepository sessionRepository;
    private final RedisRepository redisRepository;

    @Value("${sessions.page.quantity}")
    private int pageSize;
    @Value("${sessions.life.days}")
    private int sessionLifeDays;

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
                pageSize, Sort.by(sort)));
    }

    public Session findBySessionId(long id) {
        return sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(
                String.format("Сессия с id: %d не найдена.", id)));
    }

    @Transactional
    public Session save(Session session) {
        int count = sessionRepository.sessionCount(session.getUserId());
        if (count >= 3) {
            throw new TooManySessionsException("Открыто слишком много сессий.");
        }
        session.setId(null);
        session.setIsActive(true);
        session = sessionRepository.save(session);
        if (session.getId() != null) {
            redisRepository.add(session);
        }
        return session;
    }

    @Transactional
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
        return redisRepository.findBySessionId(id).orElseThrow(() -> new SessionNotFoundException(
                String.format("Сессия с id: %d не найдена.", id)));
    }

    @Scheduled(cron = "${sessions.life.task.cron.delete-old}")
    public void deleteOldSessions() {
        LocalDateTime date = LocalDateTime.now().minusDays(sessionLifeDays);
        sessionRepository.deleteOldSessions(date);
        log.info(String.format("Удаление сессий, созданных больше %d дней назад.", sessionLifeDays));
    }

    public List<Session> findAllFromRedis() {
        return redisRepository.findAll();
    }

    public void prolongSession(Long id) {
        redisRepository.prolongSession(id);
    }

    @Scheduled(cron = "${sessions.life.task.cron.delete-not-active}")
    public void deleteNotActiveSessions() {
        List<Long> keys = redisRepository.findAllKeys().stream().map(Long::valueOf).toList();
        sessionRepository.closeNotActiveSessions(keys);
        log.info("Закрытие сессий с истёкшим сроком ожидания.");
    }
}
