package ru.hackathon.mos.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о пользователе, изменившем статус/этап")
public class ChangedByUserDTO {

    @Schema(description = "ID пользователя", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "ФИО", example = "Иванов Иван Иванович")
    private String fullName;

    @Schema(description = "Роль", example = "Клиент")
    private String role;
}