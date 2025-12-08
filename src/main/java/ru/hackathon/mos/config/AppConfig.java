package ru.hackathon.mos.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация приложения — пути и URL для работы с файлами.
 */
@Configuration
@ConfigurationProperties(prefix = "app.upload")
@Data
public class AppConfig {

    /**
     * Базовый URL, по которому фронтенд будет обращаться к файлам.
     * Пример: http://localhost:8080/files
     * В продакшене может быть CDN: https://cdn.mosstroy.ru
     */
    private String baseUrl;

}