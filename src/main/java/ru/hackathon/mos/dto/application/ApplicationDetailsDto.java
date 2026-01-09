package ru.hackathon.mos.dto.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hackathon.mos.dto.ApplicationStatusEnum;
import ru.hackathon.mos.entity.Application;
import ru.hackathon.mos.entity.User;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Детальная информация о заявке")
public class ApplicationDetailsDto {

    @NotNull
    @Schema(description = "Идентификатор заявки", example = "12345")
    private Long id;

    @NotNull
    @Schema(description = "UUID пользователя, создавшего заявку",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID creatorId;

    @NotNull
    @Schema(description = "Идентификатор проекта/шаблона", example = "100")
    private Long projectId;

    @NotNull
    @Schema(description = "Контакт для связи с клиентом", example = "+79000000000")
    private String contact;

    @NotNull
    @Schema(description = "Кодовое имя статуса заявки",
            example = "created",
            allowableValues = {"created", "consideration", "accepted", "rejected"})
    private String statusName;

    @NotNull
    @Schema(description = "Описание статуса заявки",
            example = "Заявка создана.")
    private String statusDescription;

    @Schema(description = "UUID менеджера, рассматривающего заявку",
            example = "b2c3d4e5-f6a7-8901-bcde-f23456789012",
            nullable = true)
    private UUID managerId;

    @Schema(description = "ФИО менеджера",
            example = "Иванов Иван Иванович",
            nullable = true)
    private String managerFullName;

    @Schema(description = "Контакт менеджера (email)",
            example = "manager@example.com",
            nullable = true)
    private String managerContact;

    @NotNull
    @Schema(description = "Дата и время создания заявки в формате ISO 8601",
            example = "2024-12-15T10:30:00Z")
    private Instant createdAt;

    public ApplicationDetailsDto(Application application, User manager) {
        ApplicationStatusEnum applicationStatusEnum = ApplicationStatusEnum.fromId(application.getStatus().getId());
        this.id = application.getId();
        this.creatorId = application.getCreatorId();
        this.projectId = application.getProjectId();
        this.contact = application.getContact();
        this.statusName = applicationStatusEnum.getName();
        this.statusDescription = applicationStatusEnum.getDescription();

        if (application.getManagerId() != null) {
            this.managerId = application.getManagerId();

            if (manager != null) {
                this.managerFullName = formatManagerFullName(manager);
                this.managerContact = manager.getEmail();
            }
        }
        this.createdAt = application.getCreatedAt();
    }

    private String formatManagerFullName(User manager) {
        if (manager == null) return null;

        StringBuilder fullName = new StringBuilder();
        if (manager.getLastName() != null) {
            fullName.append(manager.getLastName());
        }
        if (manager.getFirstName() != null) {
            if (!fullName.isEmpty()) fullName.append(" ");
            fullName.append(manager.getFirstName());
        }
        if (manager.getMiddleName() != null) {
            if (!fullName.isEmpty()) fullName.append(" ");
            fullName.append(manager.getMiddleName());
        }

        return fullName.isEmpty() ? null : fullName.toString();
    }
}