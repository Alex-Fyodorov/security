package com.globus.modul26.util;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

@Component
public class LogRotator {
    // Логгер для вывода сообщений (с автоматической датой/временем)
    private static final Logger logger = LoggerFactory.getLogger(LogRotator.class);

    // Константы для путей и форматов дат
    private static final String LOG_FILE = "logs/cef.log";
    private static final String BACKUP_DIR = "log/cef/backup";
    private static final String ARCHIVE_DIR = "log/cef/archive";

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @PostConstruct
    public void onStartup() {
        try {
            // Создание директорий, если они не существуют
            Files.createDirectories(Paths.get(BACKUP_DIR));
            Files.createDirectories(Paths.get(ARCHIVE_DIR));

            LocalDate today = LocalDate.now();
            String todayName = "cef-" + DAY_FMT.format(today) + ".log";
            String monthName = "cef-" + MONTH_FMT.format(today) + ".tar.gz";

            // Проверка и создание ежедневного бэкапа
            if (needDailyBackup(todayName)) {
                Path source = Paths.get(LOG_FILE);
                Path target = Paths.get(BACKUP_DIR, todayName);
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Daily log backup created: {}", todayName);
            }

            // Проверка и создание ежемесячного архива
            if (needMonthlyArchive(monthName)) {
                createMonthlyArchive();
            }

        } catch (Exception e) {
            logger.error("Error during log rotation", e);
        }
    }

    /**
     * Проверяет, нужен ли ежедневный бэкап.
     */
    private boolean needDailyBackup(String todayName) throws IOException {
        try (Stream<Path> files = Files.list(Paths.get(BACKUP_DIR))) {
            return files.noneMatch(path -> path.getFileName().toString().equals(todayName));
        }
    }

    /**
     * Проверяет, нужен ли ежемесячный архив.
     */
    private boolean needMonthlyArchive(String monthName) throws IOException {
        try (Stream<Path> files = Files.list(Paths.get(ARCHIVE_DIR))) {
            return files.noneMatch(path -> path.getFileName().toString().equals(monthName));
        }
    }

    /**
     * Создает архив за предыдущий месяц.
     */
    private void createMonthlyArchive() throws IOException {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate prevMonth = firstDayOfMonth.minusDays(1);
        String prevMonthPrefix = "cef-" + MONTH_FMT.format(prevMonth);

        // Сбор файлов для архивации
        List<Path> filesToArchive;
        try (Stream<Path> files = Files.list(Paths.get(BACKUP_DIR))) {
            filesToArchive = files
                    .filter(f -> f.getFileName().toString().startsWith(prevMonthPrefix))
                    .toList();
        }

        if (!filesToArchive.isEmpty()) {
            Path archivePath = Paths.get(ARCHIVE_DIR, "cef-" + MONTH_FMT.format(prevMonth) + ".tar.gz");
            createTarGzArchive(filesToArchive, archivePath);
            logger.info("Monthly archive created: {}", archivePath.getFileName());
            // Удаление заархивированных файлов (опционально)
            filesToArchive.forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                    logger.info("Deleted old backup: {}", f.getFileName());
                } catch (IOException e) {
                    logger.error("Failed to delete backup file {}", f.getFileName(), e);
                }
            });
        }
    }

    /**
     * Создает .tar.gz архив из списка файлов.
     */
    private void createTarGzArchive(List<Path> files, Path archivePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(archivePath.toFile());
             GZIPOutputStream gos = new GZIPOutputStream(fos)) {
            for (Path file : files) {
                Files.copy(file, gos);
            }
        }
    }
}