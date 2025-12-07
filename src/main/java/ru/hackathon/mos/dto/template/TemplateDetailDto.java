package ru.hackathon.mos.dto.template;

import ru.hackathon.mos.dto.FileDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TemplateDetailDto(
        Long id,
        String title,
        String description,
        String style,
        Double areaM2,
        Integer rooms,
        BigDecimal basePrice,
        boolean isActive,
        LocalDateTime createdAt,
        List<FileDto> files
) {}
