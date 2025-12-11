package ru.hackathon.mos.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "DTO этапа строительства")
public class OrderStageDTO {

    @Schema(description = "ID этапа", example = "1")
    private Long id;

    @Schema(description = "ID заказа", example = "1")
    @NotNull(message = "ID заказа обязателен")
    private Long orderId;

    @Schema(description = "Тип этапа", example = "foundation", required = true)
    @NotBlank(message = "Тип этапа обязателен")
    private String stageType;

    @Schema(description = "Название этапа", example = "Заливка фундамента")
    private String stageName;

    @Schema(description = "Описание работ", example = "Подготовка основания, установка опалубки, заливка бетона")
    private String description;

    @Schema(description = "Дата начала", example = "2024-01-20T09:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Плановая дата завершения", example = "2024-02-10T18:00:00")
    private LocalDateTime plannedEndDate;

    @Schema(description = "Фактическая дата завершения", example = "2024-02-08T16:30:00")
    private LocalDateTime actualEndDate;

    @Schema(description = "Статус этапа", example = "in_progress")
    private String status;

    @Schema(description = "Прогресс выполнения (0-100)", example = "75")
    @Min(value = 0, message = "Прогресс не может быть меньше 0")
    @Max(value = 100, message = "Прогресс не может быть больше 100")
    private Integer progress;

    @Schema(description = "Дата создания", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Информация о пользователе, создавшем этап")
    private ChangedByUserDTO createdBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Запрос на создание этапа")
    public static class CreateStageRequest {

        @Schema(description = "Тип этапа", example = "walls", required = true)
        @NotBlank(message = "Тип этапа обязателен")
        private String stageType;

        @Schema(description = "Название этапа", example = "Возведение стен")
        private String stageName;

        @Schema(description = "Описание работ", example = "Кладка несущих стен и перегородок")
        private String description;

        @Schema(description = "Плановая дата завершения", example = "2024-03-15T18:00:00")
        private LocalDateTime plannedEndDate;

        @Schema(description = "Процент прогресса", example = "0")
        @NotNull(message = "Прогресс обязателен")
        @Min(value = 0, message = "Прогресс не может быть меньше 0")
        @Max(value = 100, message = "Прогресс не может быть больше 100")
        private Integer progress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Ответ со списком этапов")
    public static class StageListResponse {

        @Schema(description = "Список этапов")
        private List<OrderStageDTO> stages;

        @Schema(description = "Общее количество", example = "8")
        private Long total;

        @Schema(description = "Количество активных этапов", example = "2")
        private Long activeCount;

        @Schema(description = "Количество завершенных этапов", example = "3")
        private Long completedCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Обновление этапа")
    public static class UpdateStageRequest {

        @Schema(description = "Статус этапа", example = "completed")
        private String status;

        @Schema(description = "Прогресс выполнения (0-100)", example = "100")
        @Min(value = 0, message = "Прогресс не может быть меньше 0")
        @Max(value = 100, message = "Прогресс не может быть больше 100")
        private Integer progress;

        @Schema(description = "Комментарий к обновлению", example = "Все работы завершены")
        private String comment;

        @Schema(description = "Фактическая дата завершения", example = "2024-02-08T16:30:00")
        private LocalDateTime actualEndDate;
    }
}