package com.globus.modul26.repository;

import com.globus.modul26.model.SecurityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface SecurityLogRepository extends JpaRepository<SecurityLog, Integer> {

    List<SecurityLog> findByUserIdAndIsSuspiciousAndCreatedAtAfter(Long userId, Boolean isSuspicious, LocalDateTime createdAt);

    List<SecurityLog> findByUserIdAndEventTypeAndCreatedAtAfter(Long userId, String eventType, LocalDateTime after);

    List<SecurityLog> findByUserIdAndBiometryUsed(Long userId, boolean biometryUsed);

    List<SecurityLog> findByUserId(Long userId);

    //  Для подозрительных логов по userId
    List<SecurityLog> findByUserIdAndIsSuspiciousTrue(Long userId);

    // Для получения последних 3 попыток входа пользователя (LOGIN_ATTEMPT), сортировка по времени
    List<SecurityLog> findTop3ByUserIdAndEventTypeOrderByCreatedAtDesc(Long userId, String eventType);
}