package ru.hackathon.mos.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Тип пользователя.")
public class UserTypeViewDto {

    @Schema(description = "ID типа пользователя.")
    private Integer id;

    @Schema(description = "Название типа пользователя.")
    private String name;

    @Schema(description = "Описание типа пользователя.")
    private String description;
}
