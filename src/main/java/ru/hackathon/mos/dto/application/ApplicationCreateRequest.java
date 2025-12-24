package ru.hackathon.mos.dto.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Данные для создания заявки. ID пользователя берется из jwt токена")
public class ApplicationCreateRequest {

    @NotNull
    @Schema(description = "ID шаблона проекта", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long templateId;

    @NotNull
    @Schema(description = "Контакт для связи с клиентом", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contact;
}
