package ru.hackathon.mos.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Модель для обновления заказа", example = "г. Москва")
public class OrderUpdateRequest {

    @Schema(description = "Адрес строительства в заказе", example = "г. Москва")
    private String address;
}
