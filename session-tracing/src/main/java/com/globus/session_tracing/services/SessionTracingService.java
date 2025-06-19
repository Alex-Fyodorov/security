package com.globus.session_tracing.services;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.exceptions.SessionNotFoundException;
import com.globus.session_tracing.exceptions.SessionsOperationsException;
import com.globus.session_tracing.exceptions.TooManySessionsException;
import com.globus.session_tracing.repositiries.RedisRepository;
import com.globus.session_tracing.repositiries.SessionRepository;
import com.globus.session_tracing.repositiries.specifications.SessionSpecification;
import com.globus.session_tracing.utils.Base64Service;
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

    /**
     * Поиск всех сессий из базы данных, согласно введённым фильтрам.
     * @param userId идентификатор польхователя
     * @param minLoginTime время открытия сессии
     * @param method метод входа пользователем в аккаунт
     * @param isActive активна ли сессия
     * @param page номер страницы
     * @param sort сортировка списка на странице
     * @return Page<Session> страница с сессиями
     */
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
                pageSize, Sort.by(sort))).map(this::decode);
    }

    /**
     * Поиск сессии в базе данных по идентификатору
     * @param id идентификатор сессии
     * @return Session
     */
    public Session findBySessionId(long id) {
        Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(
                String.format("Сессия с id: %d не найдена.", id)));
        return decode(session);
    }

    /**
     * Внесение новой сессии в базу данных и Redis-репозиторий
     * @param session сессия
     * @return сессия после сохранения уже с присвоенным идентификатором
     */
    @Transactional
    public Session save(Session session) {
        int count = sessionRepository.sessionCount(session.getUserId());
        if (count >= 3) {
            throw new TooManySessionsException("Открыто слишком много сессий.");
        }
        session.setId(null);
        session.setIsActive(true);
        session.setDeviceInfo(Base64Service.encode(session.getDeviceInfo()));
        session.setIpAddress(maskIp(Base64Service.encode(session.getIpAddress())));
        session = sessionRepository.save(session);
        if (session.getId() != null) {
            redisRepository.add(session);
        }
        return session;
    }

    /**
     * Закрытие сессии по идентификатору.
     * @param id идентификатор сессии
     */
    @Transactional
    public void logout(long id) {
        Optional<Session> session = redisRepository.findBySessionId(id);
        if (session.isEmpty()) {
            session = sessionRepository.findById(id);
            if (session.isEmpty()) {
                throw new SessionNotFoundException(
                        String.format("Сессия с id: %d не найдена.", id));
            }
            if (!session.get().getIsActive()) {
                throw new SessionsOperationsException(String.format(
                        "Невозможно выполнить операцию. Сессия с id: %d закрыта.", id));
            }
        }
        sessionRepository.logout(id);
        redisRepository.delete(id);
    }

    /**
     * Поиск всех сессий в Redis-репозитории
     * @return List<Session> список сессий
     */
    public List<Session> findAllFromRedis() {
        return redisRepository.findAll().stream().map(this::decode).toList();
    }

    /**
     * Поиск конкретной сессии в Redis-репозитории по идентификатору
     * @param id идентификатор сессии
     * @return Session
     */
    public Session findFromRedisById(long id) {
        Session session = redisRepository.findBySessionId(id).orElseThrow(() -> new SessionNotFoundException(
                String.format("Сессия с id: %d не найдена.", id)));
        return decode(session);
    }

    /**
     * В Redis хранятся только активные сессии. При отсутствии пользовательсткой
     * активности сессия удаляется из Redis через 30 мин. При наличии же активности
     * со стороны пользователя данный метод восстанавливает срок жизни сессии
     * до изначальных 30 мин.
     * @param id идентификатор сессии
     */
    public void prolongSession(Long id) {
        redisRepository.prolongSession(id);
    }

    /**
     * В Redis хранятся только активные сессии. При отсутствии пользовательсткой
     * активности сессия удаляется из Redis через 30 мин. При наличии же активности
     * со стороны пользователя данный метод восстанавливает срок жизни сессии
     * до изначальных 30 мин.
     * @param userId идентификатор пользователя
     * @param deviceInfo информация об устройстве
     */
    public void prolongSessionByUserId(Integer userId, String deviceInfo) {
        Long sessionId = sessionRepository.findSessionIdByUserIdAndDeviceInfo(userId, deviceInfo)
                .orElseThrow(() -> new SessionNotFoundException(String.format(
                        "Активная сессия с userId: %d и deviceInfo: %s не найдена.", userId, deviceInfo)));
        prolongSession(sessionId);
    }

    /**
     * Регулярный метод. Срабатывает, согласно времени, указанному в настройках.
     * Удаляет из базы данных сессии, созданные раньше Х дней назад.
     * Величина Х также прописана в настройках {sessions.life.days}.
     */
    @Scheduled(cron = "${sessions.life.task.cron.delete-old}")
    public void deleteOldSessions() {
        LocalDateTime date = LocalDateTime.now().minusDays(sessionLifeDays);
        sessionRepository.deleteOldSessions(date);
        log.info(String.format("Удаление сессий, созданных больше %d дней назад.", sessionLifeDays));
    }

    /**
     * Регулярный метод. Срабатывает, согласно времени, указанному в настройках.
     * Каждые несколько минут метод запрашивает в Redis-репозитории список
     * идентификаторов активных сессий и предоставляет его базе данных.
     * Сессии, котороые числятся в базе данных активными,
     * но отсутствуют в этом списке, закрываются.
     */
    @Scheduled(cron = "${sessions.life.task.cron.delete-not-active}")
    public void deleteNotActiveSessions() {
        List<Long> keys = redisRepository.findAllKeys().stream().map(Long::valueOf).toList();
        sessionRepository.closeNotActiveSessions(keys);
        log.info("Закрытие сессий с истёкшим сроком ожидания.");
    }

    /**
     * Маскировка IP-адреса
     * @param ip IP-адрес
     * @return маскированный IP-адрес
     */
    private String maskIp(String ip) {
        if (ip == null) return null;
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return ip;
        String first = parts[0];
        String second = parts[1].isEmpty() ? "*" : parts[1].substring(0, 1);
        String fourth = parts[3];
        return String.format("%s.%s**.***.%s", first, second, fourth);
    }

    /**
     * Декодирование IP-адреса и информации об устройстве
     * @param session закодированная сессия
     * @return декодированная сессия
     */
    private Session decode(Session session) {
        session.setDeviceInfo(Base64Service.decode(session.getDeviceInfo()));
        session.setIpAddress(Base64Service.decode(session.getIpAddress()));
        return session;
    }
}
