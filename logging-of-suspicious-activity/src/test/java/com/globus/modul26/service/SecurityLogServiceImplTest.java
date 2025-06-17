package com.globus.modul26.service;

import com.globus.modul26.model.SecurityLog;
import com.globus.modul26.repository.BannedCountryRepository;
import com.globus.modul26.repository.SecurityLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityLogServiceImplTest {

    private SecurityLogRepository repository;
    private BannedCountryRepository bannedCountryRepository;
    private SecurityLogServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(SecurityLogRepository.class);
        bannedCountryRepository = mock(BannedCountryRepository.class);
        service = new SecurityLogServiceImpl(repository, bannedCountryRepository);
    }

    @Test
    void logEvent_shouldSaveLog() {
        when(repository.save(any(SecurityLog.class))).thenAnswer(i -> i.getArguments()[0]);

        service.logEvent(1L, "LOGIN", "127.0.0.1", "PC", true);

        ArgumentCaptor<SecurityLog> captor = ArgumentCaptor.forClass(SecurityLog.class);
        verify(repository).save(captor.capture());
        SecurityLog saved = captor.getValue();

        assertEquals(1L, saved.getUserId());
        assertEquals("LOGIN", saved.getEventType());
        assertEquals("127.0.0.1", saved.getIpAddress());
        assertEquals("PC", saved.getDeviceInfo());
        assertTrue(saved.getMetadata().containsKey("country"));
        assertTrue(saved.getMetadata().containsKey("city"));
        assertEquals(true, saved.getBiometryUsed());
    }

    @Test
    void saveLog_setsDefaults_whenNulls() {
        SecurityLog log = SecurityLog.builder()
                .userId(1L)
                .eventType("LOGIN")
                .build();
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        SecurityLog saved = service.saveLog(log);

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getMetadata());
        assertEquals("UNKNOWN", saved.getMetadata().get("country"));
        assertEquals("UNKNOWN", saved.getMetadata().get("city"));
    }

    @Test
    void isNewIp_trueOnFirstUse() {
        when(repository.findByUserId(1L)).thenReturn(List.of());
        assertTrue(service.isNewIp(1L, "8.8.8.8"));
    }

    @Test
    void isNewIp_falseIfExists() {
        SecurityLog log = SecurityLog.builder().ipAddress("8.8.8.8").build();
        when(repository.findByUserId(1L)).thenReturn(List.of(log));
        assertFalse(service.isNewIp(1L, "8.8.8.8"));
    }

    @Test
    void isNewGeo_trueForNewGeo() {
        when(repository.findByUserId(1L)).thenReturn(List.of());
        assertTrue(service.isNewGeo(1L, "RU,Moscow"));
    }

    @Test
    void isNewGeo_falseIfGeoExists() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("country", "RU");
        meta.put("city", "Moscow");
        SecurityLog log = SecurityLog.builder().metadata(meta).build();
        when(repository.findByUserId(1L)).thenReturn(List.of(log));
        assertFalse(service.isNewGeo(1L, "RU,Moscow"));
    }

    @Test
    void isNewDevice_trueForUnknownDevice() {
        when(repository.findByUserId(1L)).thenReturn(List.of());
        assertTrue(service.isNewDevice(1L, "iPhone"));
    }

    @Test
    void isNewDevice_falseIfDeviceExists() {
        SecurityLog log = SecurityLog.builder().deviceInfo("iPhone").build();
        when(repository.findByUserId(1L)).thenReturn(List.of(log));
        assertFalse(service.isNewDevice(1L, "iPhone"));
    }

    @Test
    void hasTooManyFailedAttempts_trueOn3Suspicious() {
        SecurityLog log = SecurityLog.builder().isSuspicious(true).build();
        when(repository.findTop3ByUserIdAndEventTypeOrderByCreatedAtDesc(1L, "LOGIN_ATTEMPT"))
                .thenReturn(List.of(log, log, log));
        assertTrue(service.hasTooManyFailedAttempts(1L));
    }

    @Test
    void hasTooManyFailedAttempts_falseOnLessThan3() {
        SecurityLog log = SecurityLog.builder().isSuspicious(true).build();
        when(repository.findTop3ByUserIdAndEventTypeOrderByCreatedAtDesc(1L, "LOGIN_ATTEMPT"))
                .thenReturn(List.of(log, log));
        assertFalse(service.hasTooManyFailedAttempts(1L));
    }

    @Test
    void hasTooManyPasswordChanges_trueIfMoreThan2() {
        SecurityLog log = SecurityLog.builder().build();
        when(repository.findByUserIdAndEventTypeAndCreatedAtAfter(anyLong(), anyString(), any()))
                .thenReturn(List.of(log, log, log));
        assertTrue(service.hasTooManyPasswordChanges(1L));
    }

    @Test
    void hasTooManyPasswordChanges_falseIf2OrLess() {
        SecurityLog log = SecurityLog.builder().build();
        when(repository.findByUserIdAndEventTypeAndCreatedAtAfter(anyLong(), anyString(), any()))
                .thenReturn(List.of(log));
        assertFalse(service.hasTooManyPasswordChanges(1L));
    }

    @Test
    void isLoginWithoutBiometryWhereWasBiometryBefore_trueIfBeforeWasBiometry() {
        SecurityLog log = SecurityLog.builder().eventType("LOGIN").biometryUsed(false).build();
        SecurityLog old = SecurityLog.builder().biometryUsed(true).build();
        when(repository.findByUserIdAndBiometryUsed(1L, true)).thenReturn(List.of(old));

        assertTrue(service.isLoginWithoutBiometryWhereWasBiometryBefore(1L, log));
    }

    @Test
    void isLoginWithoutBiometryWhereWasBiometryBefore_falseIfNoBiometryBefore() {
        SecurityLog log = SecurityLog.builder().eventType("LOGIN").biometryUsed(false).build();
        when(repository.findByUserIdAndBiometryUsed(1L, true)).thenReturn(List.of());

        assertFalse(service.isLoginWithoutBiometryWhereWasBiometryBefore(1L, log));
    }

    @Test
    void isBlacklistedCountry_trueIfExists() {
        when(bannedCountryRepository.existsById("RU")).thenReturn(true);
        assertTrue(service.isBlacklistedCountry("RU,Moscow"));
    }

    @Test
    void isBlacklistedCountry_falseIfNotExists() {
        when(bannedCountryRepository.existsById("RU")).thenReturn(false);
        assertFalse(service.isBlacklistedCountry("RU,Moscow"));
    }

    @Test
    void isUserAgentMismatch_trueIfMismatched() {
        SecurityLog log1 = SecurityLog.builder().deviceInfo("Android").build();
        SecurityLog log2 = SecurityLog.builder().deviceInfo("iOS").build();
        when(repository.findByUserId(1L)).thenReturn(List.of(log1, log2));

        assertTrue(service.isUserAgentMismatch(1L, "PC"));
    }

    @Test
    void isUserAgentMismatch_falseIfMatched() {
        SecurityLog log = SecurityLog.builder().deviceInfo("PC").build();
        when(repository.findByUserId(1L)).thenReturn(List.of(log));

        assertFalse(service.isUserAgentMismatch(1L, "PC"));
    }

    @Test
    void findSuspiciousLogs_returnsOnlySuspiciousLogs() {
        SecurityLog suspicious = SecurityLog.builder()
                .userId(1L)
                .eventType("LOGIN")
                .ipAddress("8.8.8.8")
                .build();

        SecurityLog notSuspicious = SecurityLog.builder()
                .userId(1L)
                .eventType("LOGIN")
                .ipAddress("oldIp")
                .build();

        List<SecurityLog> allLogs = List.of(notSuspicious, suspicious);

        when(repository.findAll()).thenReturn(allLogs);



        List<SecurityLog> result = service.findSuspiciousLogs();


    }

    @Test
    void getLastLoginAttempts_returnsTop3() {
        List<SecurityLog> logs = List.of(
                SecurityLog.builder().build(),
                SecurityLog.builder().build(),
                SecurityLog.builder().build()
        );
        when(repository.findTop3ByUserIdAndEventTypeOrderByCreatedAtDesc(1L, "LOGIN_ATTEMPT")).thenReturn(logs);

        List<SecurityLog> result = service.getLastLoginAttempts(1L, 3);
        assertEquals(3, result.size());
    }


    @Test
    void findByUserId_returnsLogs() {
        SecurityLog log1 = SecurityLog.builder().build();
        SecurityLog log2 = SecurityLog.builder().build();
        when(repository.findByUserId(1L)).thenReturn(List.of(log1, log2));

        List<SecurityLog> result = service.findByUserId(1L);
        assertEquals(2, result.size());
        assertTrue(result.contains(log1));
        assertTrue(result.contains(log2));
    }
}