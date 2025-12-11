package ru.hackathon.mos.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание заказа")
public class CreateOrderRequest {

    @Schema(description = "ID проекта", example = "1", required = true)
    @NotNull(message = "ID проекта обязателен")
    private Long projectId;

    @Schema(description = "Адрес строительства",
            example = "г. Москва, ул. Ленина, д. 10",
            required = true)
    @NotBlank(message = "Адрес обязателен")
    private String address;

    @Schema(description = "Комментарий к заказу", example = "Участок с уклоном")
    private String comment;
}