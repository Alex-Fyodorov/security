package com.globus.modul26.security;

import com.globus.modul26.model.BannedCountry;
import com.globus.modul26.model.SecurityLog;
import com.globus.modul26.repository.BannedCountryRepository;
import com.globus.modul26.repository.SecurityLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class SuspiciousActivityDetectorTest {

    private SuspiciousActivityDetector detector;
    private SecurityLogRepository logRepository;
    private BannedCountryRepository bannedCountryRepository;

    @BeforeEach
    void setUp() {
        // Моки через Mockito
        logRepository = mock(SecurityLogRepository.class);
        bannedCountryRepository = mock(BannedCountryRepository.class);

        detector = new SuspiciousActivityDetector();
        // Внедрение зависимостей (т.к. поле private)
        var logRepoField = getField(detector, "logRepository");
        setField(logRepoField, detector, logRepository);

        var bcRepoField = getField(detector, "bannedCountryRepository");
        setField(bcRepoField, detector, bannedCountryRepository);
    }

    @Test
    void analyze() {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("country", "USA");
        metadata.put("city", "New York");

        SecurityLog log = new SecurityLog();
        log.setUserId(userId);
        log.setIpAddress("22.33.44.55");
        log.setEventType("LOGIN");
        log.setDeviceInfo("Chrome_115");
        log.setCreatedAt(now);
        log.setMetadata(metadata);
        log.setBiometryUsed(false);

        // В базе нет такого IP, Geo или устройства
        when(logRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Нет попыток неудачных логинов
        when(logRepository.findByUserIdAndIsSuspiciousAndCreatedAtAfter(anyLong(), anyBoolean(), any()))
                .thenReturn(Collections.emptyList());

        // Нет смен пароля
        when(logRepository.findByUserIdAndEventTypeAndCreatedAtAfter(anyLong(), anyString(), any()))
                .thenReturn(Collections.emptyList());

        // Нет прошлых биометрий
        when(logRepository.findByUserIdAndBiometryUsed(userId, true))
                .thenReturn(Collections.emptyList());

        // В черном списке стран есть только "RUS"
        when(bannedCountryRepository.findAll())
                .thenReturn(List.of(new BannedCountry("RUS")));

        // analyze НЕ делает никаких изменений/исключений
        assertDoesNotThrow(() -> detector.analyze(log));
    }

    // Хелперы для работы с приватными полями через Reflection
    private static java.lang.reflect.Field getField(Object target, String name) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(java.lang.reflect.Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}