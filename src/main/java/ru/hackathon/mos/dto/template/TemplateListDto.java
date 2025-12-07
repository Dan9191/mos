package ru.hackathon.mos.dto.template;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TemplateListDto(
        Long id,
        String title,
        String style,
        Double areaM2,
        Integer rooms,
        BigDecimal basePrice,
        String previewUrl,     // ссылка на превью
        LocalDateTime createdAt
) {}