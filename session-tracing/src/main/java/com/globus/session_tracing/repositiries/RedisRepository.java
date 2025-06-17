package com.globus.session_tracing.repositiries;

import com.globus.session_tracing.entities.Session;
import com.globus.session_tracing.exceptions.SessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.data.redis.lifetime.minutes}")
    private int sessionLifeTimeMinutes;

    /**
     * Добавить сессию к Redis-репозиторию
     * @param session
     */
    public void add(Session session) {
        String key = String.valueOf(session.getId());
        redisTemplate.opsForValue().set(key, session);
        redisTemplate.expire(key, sessionLifeTimeMinutes, TimeUnit.MINUTES);
    }

    /**
     * В Redis хранятся только активные сессии. При отсутствии пользовательсткой
     * активности сессия удаляется из Redis через 30 мин. При наличии же активности
     * со стороны пользователя данный метод восстанавливает срок жизни сессии
     * до изначальных 30 мин.
     * @param id идентификатор сессии
     */
    public void prolongSession(Long id) {
        String key = String.valueOf(id);
        if (!redisTemplate.hasKey(key)) {
            throw new SessionNotFoundException("Сессия не найдена или уже завершена");
        }
        redisTemplate.expire(key, sessionLifeTimeMinutes, TimeUnit.MINUTES);
    }

    /**
     * Поиск сессии по идентификатору.
     * @param id идентификатор сессии
     * @return Optional<Session> возвращает Optional, чтобы можно было определить,
     * что сессия не найдена.
     */
    public Optional<Session> findBySessionId(long id) {
        return Optional.ofNullable((Session) redisTemplate.opsForValue().get(String.valueOf(id)));
    }

    /**
     * Поиск всех сессий в Redis-репозитории.
     * @return List<Session> список всех найденных сессий
     */
    public List<Session> findAll() {
        Set<String> redisKeys = redisTemplate.keys("*");
        List<Session> sessions = new ArrayList<>();
        for (String redisKey : redisKeys) {
            Session session = (Session) redisTemplate.opsForValue().get(redisKey);
            sessions.add(session);
        }
        return sessions;
    }

    /**
     * Поиск всех идентификаторов сессий, находящихся в Redis-репозитории.
     * @return Set<String> Множество id в строковом формате.
     */
    public Set<String> findAllKeys() {
        return redisTemplate.keys("*");
    }

    /**
     * Удаление сессии из Redis-репозитория по идентификатору.
     * @param id идентификатор сессии
     * @return boolean подтверждение операции
     */
    public boolean delete(long id) {
        return redisTemplate.delete(String.valueOf(id));
    }
}
