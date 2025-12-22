package ru.hackathon.mos.service;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import ru.hackathon.mos.exception.UserAlreadyExistsException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final Keycloak keycloakAdminClient;

    private static final String REALM = "hackathon";
    private static final String DEFAULT_ROLE = "hackathon.user";
    private final UserService userService;


    /**
     * Регистрация пользователя в keycloak + сохранение в БД.
     *
     * @param username  Логин.
     * @param email     Почта.
     * @param password  Пароль.
     * @param firstName Имя.
     * @param lastName  Фамилия.
     * @return ID пользователя.
     */
    @Transactional
    public String registerUser(String username, String email, String password, String firstName, String lastName) {
        // Проверка использования логина и почты
        RealmResource realmResource = keycloakAdminClient.realm(REALM);

        if (!realmResource.users().searchByUsername(username, true).isEmpty()) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (!realmResource.users().searchByEmail(email, true).isEmpty()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);      // верифицированная почта

        // Постоянный пароль
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        // Создаём пользователя
        try (Response response = realmResource.users().create(user)) {
            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to create user: " + response.getStatusInfo());
            }

            // Получаем ID созданного пользователя из Location header
            String location = response.getLocation().toString();
            String userId = location.substring(location.lastIndexOf("/") + 1);

            // Назначаем realm role
            RoleRepresentation role = realmResource.roles().get(DEFAULT_ROLE).toRepresentation();
            realmResource.users().get(userId).roles().realmLevel().add(List.of(role));

            userService.createUser(UUID.fromString(userId), username, email, firstName, lastName);
            return userId;
        }

    }
}
