package ru.hackathon.mos.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные пользователя")
public class UserViewDto {

    @Schema(description = "ID пользователя")
    private UUID id;

    @Schema(description = "Тип пользователя")
    private UserTypeViewDto type;

    @Schema(description = "Логин")
    private String username;

    @Schema(description = "Имя")
    private String firstName;

    @Schema(description = "Фамилия")
    private String lastName;

    @Schema(description = "Отчество")
    private String surname;

    @Schema(description = "Почта пользователя")
    private String email;

    @Schema(description = "Время создания пользователя")
    private LocalDateTime createdAt;
}
