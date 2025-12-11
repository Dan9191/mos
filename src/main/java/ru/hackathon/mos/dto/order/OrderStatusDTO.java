package ru.hackathon.mos.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO статуса заказа")
public class OrderStatusDTO {

    @Schema(description = "ID статуса", example = "1")
    private Long id;

    @Schema(description = "ID заказа", example = "1")
    @NotNull(message = "ID заказа обязателен")
    private Long orderId;

    @Schema(description = "Тип статуса", example = "new", required = true)
    @NotBlank(message = "Тип статуса обязателен")
    private String statusType;

    @Schema(description = "Комментарий к статусу", example = "Заказ создан")
    private String comment;

    @Schema(description = "Дата создания", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Информация о пользователе, изменившем статус")
    private ChangedByUserDTO changedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Запрос на создание статуса")
    public static class CreateStatusRequest {

        @Schema(description = "Тип статуса", example = "documentation", required = true)
        @NotBlank(message = "Тип статуса обязателен")
        private String statusType;

        @Schema(description = "Комментарий к статусу", example = "Начата подготовка документов")
        private String comment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Ответ со списком статусов")
    public static class StatusListResponse {

        @Schema(description = "Список статусов")
        private List<OrderStatusDTO> statuses;

        @Schema(description = "Общее количество", example = "15")
        private Long total;

        @Schema(description = "Текущий статус")
        private String currentStatus;
    }
}