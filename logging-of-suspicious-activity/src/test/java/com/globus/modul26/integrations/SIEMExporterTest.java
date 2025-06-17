package com.globus.modul26.integrations;

import com.globus.modul26.model.SecurityLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SIEMExporterTest {

    private SIEMExporter exporter;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        exporter = new SIEMExporter();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void exportInCEF() {
        // --- Создаем "заглушку" для SecurityLog ---
        SecurityLog event = new SecurityLog();
        event.setId(42);
        event.setEventType("LOGIN");
        event.setDeviceInfo("Chrome");
        event.setIpAddress("192.168.0.1");

        // --- Тестируем метод ---
        exporter.exportInCEF(event);

        String log = outContent.toString().trim();

        // Проверяем, что строка содержит нужные значения
        assertTrue(log.contains("CEF:0|Globus|modul26|1.0|LOGIN|Chrome|severity| eventId=42 src=192.168.0.1"));
        assertTrue(log.startsWith("SIEM CEF:"));

        // Чистим после теста
        System.setOut(originalOut);
    }
}