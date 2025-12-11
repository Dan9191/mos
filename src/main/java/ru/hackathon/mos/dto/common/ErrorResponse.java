package ru.hackathon.mos.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ об ошибке")
public class ErrorResponse {

    @Schema(description = "Код ошибки", example = "ORDER_NOT_FOUND")
    private String errorCode;

    @Schema(description = "Сообщение об ошибке", example = "Заказ не найден")
    private String message;

    @Schema(description = "Время возникновения ошибки")
    private String timestamp;
}