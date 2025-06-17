package com.globus.modul26.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CefUtil {
    public static String toCef(String signatureId, String name, int severity, Map<String, String> extension) {
        StringBuilder cef = new StringBuilder();
        cef.append("CEF:0|YourCompany|modul26|1.0|")
                .append(signatureId).append("|")
                .append(name).append("|")
                .append(severity).append("|");

        // Добавляем timestamp в формате ISO 8601
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        cef.append("rt=").append(timestamp).append("|");

        // Добавляем extension
        if (extension != null) {
            extension.forEach((k, v) -> cef.append(k).append("=").append(v).append(" "));
        }

        return cef.toString().trim();
    }
}