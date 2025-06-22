package com.globus.session_tracing.repositiries;

import com.globus.session_tracing.entities.Session;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>,
        JpaSpecificationExecutor<Session> {

    /**
     * Закрытие сессии по идентификатору.
     * @param sessionId идентификатор сессии
     */
    @Transactional
    @Modifying
    @Query("update Session s set s.isActive = false, s.logoutTime = current_timestamp where s.id = :sessionId and s.isActive = true")
    void logout (Long sessionId);

    /**
     * Удаление сессий, открытых раньше введёной даты.
     * @param date дата
     */
    @Transactional
    @Modifying
    @Query("delete from Session s where s.loginTime <= :date")
    void deleteOldSessions(LocalDateTime date);

    /**
     * Закрытие активных сессий, идентификаторов которых нет во введённом списке.
     * @param keys список идентификаторов сессий, которые должны остаться активными
     */
    @Transactional
    @Modifying
    @Query("update Session s set s.isActive = false, s.logoutTime = current_timestamp where s.isActive = true and s.id not in :keys")
    void closeNotActiveSessions(List<Long> keys);

    /**
     * Поиск идентификатора активной сессии по идентификатору пользователя и информации об устройстве
     * @param userId идентификатор пользователя
     * @param deviceInfo информация об устройстве
     * @return идентификатор сессии
     */
    @Query("select s.id from Session s where s.isActive = true and s.userId = :userId and s.deviceInfo = :deviceInfo")
    Optional<Long> findSessionIdByUserIdAndDeviceInfo(Integer userId, String deviceInfo);
}
