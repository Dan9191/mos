package ru.hackathon.mos.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Ответ при ошибке регистрации"
)
public record RegistrationErrorResponse(

        @Schema(
                description = "Код ошибки",
                example = "user_exists"
        )
        String error,

        @Schema(
                description = "Подробное сообщение об ошибке",
                example = "Email already registered"
        )
        String message

) {}
