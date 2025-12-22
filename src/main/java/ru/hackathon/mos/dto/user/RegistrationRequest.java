package ru.hackathon.mos.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на регистрацию нового пользователя")
public record RegistrationRequest(

        @Schema(
                description = "Уникальный логин пользователя (обычно используется как username в Keycloak)",
                example = "ivan_ivanov",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String username,

        @Schema(
                description = "Email пользователя (должен быть уникальным и валидным)",
                example = "ivan@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email,

        @Schema(
                description = "Пароль пользователя (должен соответствовать политике паролей Keycloak)",
                example = "StrongPass123!",
                requiredMode = Schema.RequiredMode.REQUIRED,
                format = "password"  // в Swagger UI поле будет типа "password" (со звёздочками)
        )
        String password,

        @Schema(
                description = "Имя пользователя",
                example = "Иван",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String firstName,

        @Schema(
                description = "Фамилия пользователя",
                example = "Иванов",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED  // если хочешь сделать необязательной — убери или оставь REQUIRED
        )
        String lastName

) {}