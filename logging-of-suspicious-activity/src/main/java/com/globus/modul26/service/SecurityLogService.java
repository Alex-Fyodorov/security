package com.globus.modul26.service;

import com.globus.modul26.model.SecurityLog;

import java.util.List;

public interface SecurityLogService {
    SecurityLog saveLog(SecurityLog log);
    boolean isNewIp(Long userId, String currentIp);
    boolean isNewGeo(Long userId, String currentGeoLocation);
    boolean isNewDevice(Long userId, String currentDeviceInfo);
    boolean hasTooManyFailedAttempts(Long userId);
    boolean hasTooManyPasswordChanges(Long userId);
    boolean isLoginWithoutBiometryWhereWasBiometryBefore(Long userId, SecurityLog log);
    boolean isBlacklistedCountry(String geoLocation);
    boolean isUserAgentMismatch(Long userId, String currentDeviceInfo);
    List<SecurityLog> findSuspiciousLogs();
    List<SecurityLog> findByUserId(Long userId);
    List<SecurityLog> findSuspiciousLogsByUserId(Long userId);
    List<SecurityLog> getLastLoginAttempts(Long userId, int limit);


    void logEvent(Long userId, String eventType, String ipAddress, String deviceInfo, Boolean biometryUsed);

    // статические методы
    static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return null;
        String[] parts = email.split("@");
        String namePart = parts[0];
        String domainPart = parts[1];
        String maskedName = namePart.length() > 2
                ? namePart.substring(0, 2) + "**"
                : namePart + "**";
        String[] domainParts = domainPart.split("\\.");
        String maskedDomain = domainParts[0].length() > 2
                ? domainParts[0].substring(0, 2) + "****"
                : domainParts[0] + "****";
        return maskedName + "@" + maskedDomain + "." + domainParts[1];
    }

    static String maskIp(String ip) {
        if (ip == null) return null;
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1].substring(0, 2) + "**.***." + parts[3];
        }
        return ip;
    }
}