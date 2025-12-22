package ru.hackathon.mos.config.keycloak;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Настройка бинов для работы с keycloak.
 */
@Configuration
@Data
@RequiredArgsConstructor
public class KeycloakConfig {

    private final KeycloakPropertiesConfig keycloakPropertiesConfig;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakPropertiesConfig.getServerUrl())
                .realm(keycloakPropertiesConfig.getRealm())
                .clientId(keycloakPropertiesConfig.getAdminClientId())
                .clientSecret(keycloakPropertiesConfig.getAdminClientSecret())
                .grantType("client_credentials")
                .build();
    }
}