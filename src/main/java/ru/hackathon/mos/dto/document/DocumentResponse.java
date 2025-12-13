package ru.hackathon.mos.dto.document;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String type,
        String title,
        String status,
        LocalDateTime createdAt,
        String fileName,
        String content
) {}
