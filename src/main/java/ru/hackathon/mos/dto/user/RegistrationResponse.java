package ru.hackathon.mos.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Ответ при удачной регистрации"
)
public record RegistrationResponse(
        @Schema(description = "ID пользователя")
        String userId
) {}
