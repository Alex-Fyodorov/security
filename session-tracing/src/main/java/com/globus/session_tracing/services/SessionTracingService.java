package com.globus.session_tracing.services;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.exceptions.SessionNotFoundException;
import com.globus.session_tracing.exceptions.TooManySessionsException;
import com.globus.session_tracing.repositiries.RedisRepository;
import com.globus.session_tracing.repositiries.SessionRepository;
import com.globus.session_tracing.repositiries.specifications.SessionSpecification;
import com.globus.session_tracing.utils.Base64Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionTracingService {
    private final SessionRepository sessionRepository;
    private final RedisRepository redisRepository;
    private final Logger log = LoggerFactory.getLogger(SessionTracingService.class);

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
        if (method != null && !method.isBlank()) {
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
        // Если есть сессии, открытые этим же пользователем с этого же устройства, сначала закрываем их.
        try {
            logout(session.getUserId(), session.getDeviceInfo());
        } catch (SessionNotFoundException e) {
            log.info(String.format("Активныхсессий с userID: %d и deviceInfo: %s не найдено",
                    session.getUserId(), session.getDeviceInfo()));
        }

        // Проверяем количество открытых сессий у этого пользователя.
        List<Session> sessions = redisRepository.findAllByUserId(session.getUserId());
        if (sessions.size() >= 3) {
            throw new TooManySessionsException("Открыто слишком много сессий.");
        }

        // Сохраняем сессию
        session.setId(null);
        session.setIsActive(true);
        session.setDeviceInfo(Base64Service.encode(session.getDeviceInfo()));
        session.setIpAddress(Base64Service.encode(maskIp(session.getIpAddress())));
        session = sessionRepository.save(session);
        if (session.getId() != null) {
            redisRepository.add(session);
        }
        return session;
    }

    /**
     * Закрытие сессии по идентификатору пользователя и информации об устройстве
     * @param userId идентификатор пользователя
     * @param deviceInfo информация об устройстве
     */
    @Transactional
    public void logout(Integer userId, String deviceInfo) throws SessionNotFoundException {
        List<Session> sessions = findAllSessionsByUserIdAndDeviceInfo(userId, deviceInfo);
        if (sessions.isEmpty()) {
            throw new SessionNotFoundException(String.format(
                    "Активныхсессий с userID: %d и deviceInfo: %s не найдено", userId, deviceInfo));
        }
        for (Session session : sessions) {
            sessionRepository.logout(session.getId());
            redisRepository.delete(session.getId());
        }
    }

    /**
     * Поиск всех активных сессий по идентификатору пользователя и информации об устройстве.
     * В идеале таких сессий должно быть не больше одной. Однако если по каким-то причинам
     * одна из сессий не закрылась и возникло две сессии с похожими идентификатором
     * пользователя и информациуй об устройстве, может возникнуть ошибка. В данном случае
     * выгоднее, чтобы программа обработала обе сессии, чем выдала ошибку, поэтому
     * возвращается не единичная сессия, а список.
     * @param userId идентификатор пользователя
     * @param deviceInfo информация об устройстве
     * @return список сессий
     */
    private List<Session> findAllSessionsByUserIdAndDeviceInfo(Integer userId, String deviceInfo) {
        String codeDeviceInfo = Base64Service.encode(deviceInfo);
        return redisRepository.findAllByUserId(userId)
                .stream()
                .filter(s -> s.getDeviceInfo().equals(codeDeviceInfo))
                .toList();
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

    /*
    Примечание:
    Метод prolongSession() должен вызываться при каждом действии пользователя,
    подтверждая таким образом, что данная сессия активна. При этом логично было бы
    продлять срок жизни сессии по её идентификатору, то есть по Redis-ключу.
    Однако для этого необходимо этот ключ знать, и это значит, что id сессии должен
    постоянно предоставляться пользователем либо в отдельном хедере, либо как одно
    из полей jwt-токена. В следствие отсутствия архитектуры я не мог гарантировать,
    что у пользователя будут сведелия об id сессии, поэтому сделал доступ к продлению
    сессии (и некоторым другим методам) по id пользователя и информации об устройстве,
    что сильно увеличивает нагрузку на данный микросервис в общем и Redis в частности.
    Есть и другие способы решить эту проблему, но к сожалению, они приводят к новым
    проблемам.
     */

    /**
     * В Redis хранятся только активные сессии. При отсутствии пользовательсткой
     * активности сессия удаляется из Redis через 30 мин. При наличии же активности
     * со стороны пользователя данный метод восстанавливает срок жизни сессии
     * до изначальных 30 мин.
     * @param userId идентификатор пользователя
     * @param deviceInfo информация об устройстве
     */
    public void prolongSession(Integer userId, String deviceInfo) {
        List<Session> sessions = findAllSessionsByUserIdAndDeviceInfo(userId, deviceInfo);
        if (sessions.isEmpty()) {
            throw new SessionNotFoundException(String.format(
                    "Активныхсессий с userID: %d и deviceInfo: %s не найдено", userId, deviceInfo));
        }
        for (Session session : sessions) {
            redisRepository.prolongSession(session.getId());
        }
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
        if (session.getDeviceInfo() != null) {
            session.setDeviceInfo(Base64Service.decode(session.getDeviceInfo()));
        }
        if (session.getIpAddress() != null) {
            session.setIpAddress(Base64Service.decode(session.getIpAddress()));
        }
        return session;
    }
}
