package ru.hackathon.mos.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ со списком элементов и пагинацией")
public class ListResponse<T> {

    @Schema(description = "Список элементов")
    private List<T> items;

    @Schema(description = "Общее количество элементов", example = "150")
    private Long total;

    @Schema(description = "Номер текущей страницы", example = "1")
    private Integer page;

    @Schema(description = "Размер страницы", example = "10")
    private Integer pageSize;
}