package ru.hackathon.mos.dto.template;

import java.math.BigDecimal;

public record TemplateCreateRequest(
        String title,
        String description,
        String style,
        Double areaM2,
        Integer rooms,
        BigDecimal basePrice,
        Boolean isActive
) {}
