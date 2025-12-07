package ru.hackathon.mos.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Конфигурация приложения — пути и URL для работы с файлами.
 */
@Configuration
@ConfigurationProperties(prefix = "app.upload")
@Data
public class AppConfig {

    /**
     * Папка на диске, куда сохраняются загруженные файлы.
     * По умолчанию: ./uploads
     */
    private String dir = "./uploads";

    /**
     * Базовый URL, по которому фронтенд будет обращаться к файлам.
     * Пример: http://localhost:8080/files
     * В продакшене может быть CDN: https://cdn.mosstroy.ru
     */
    private String baseUrl;

    /**
     * Полный путь (внутренний) — создаём автоматически.
     */
    private Path uploadPath;

    @PostConstruct
    public void init() {
        this.uploadPath = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку для загрузки файлов: " + dir, e);
        }
    }
}
