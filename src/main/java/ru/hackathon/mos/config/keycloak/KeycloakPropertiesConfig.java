package ru.hackathon.mos.config.keycloak;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация keycloak.
 */
@Configuration
@ConfigurationProperties(prefix = "app.keycloak")

@Data
public class KeycloakPropertiesConfig {

    private String serverUrl;

    private String realm;

    private String adminClientId;

    private String adminClientSecret;

}