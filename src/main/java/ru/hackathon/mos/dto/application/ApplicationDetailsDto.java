package ru.hackathon.mos.dto.application;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hackathon.mos.dto.ApplicationStatusEnum;
import ru.hackathon.mos.entity.Application;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Детальная информация о заявке")
public class ApplicationDetailsDto {

    @Schema(description = "Идентификатор заявки", example = "12345")
    private Long id;

    /** UUID пользователя, который создал заявку */
    @Schema(description = "UUID пользователя, создавшего заявку",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID creatorId;

    /** ID проекта/шаблона */
    @Schema(description = "Идентификатор проекта/шаблона", example = "100")
    private Long projectId;

    /** Кодовое имя статуса заявки (например, created, accepted) */
    @Schema(description = "Кодовое имя статуса заявки",
            example = "created",
            allowableValues = {"created", "consideration", "accepted", "rejected"})
    private String statusName;

    /** Описание статуса заявки */
    @Schema(description = "Описание статуса заявки",
            example = "Заявка создана.")
    private String statusDescription;

    /** UUID менеджера, который рассматривает заявку (nullable) */
    @Schema(description = "UUID менеджера, рассматривающего заявку",
            example = "b2c3d4e5-f6a7-8901-bcde-f23456789012",
            nullable = true)
    private UUID managerId;

    /** Дата и время создания заявки */
    @Schema(description = "Дата и время создания заявки в формате ISO 8601",
            example = "2024-12-15T10:30:00Z")
    private Instant createdAt;

    public ApplicationDetailsDto(Application application) {
        ApplicationStatusEnum applicationStatusEnum = ApplicationStatusEnum.fromId(application.getStatus().getId());
        this.id = application.getId();
        this.creatorId = application.getCreatorId();
        this.projectId = application.getProjectId();
        this.statusName = applicationStatusEnum.getName();
        this.statusDescription = applicationStatusEnum.getDescription();
        if (application.getManagerId() != null) { this.managerId = application.getManagerId(); }
        this.createdAt = application.getCreatedAt();
    }
}
