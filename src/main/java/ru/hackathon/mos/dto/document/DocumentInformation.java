package ru.hackathon.mos.dto.document;

import java.time.LocalDateTime;

public record DocumentInformation(
        Long id,
        String type,
        String title,
        String status,
        LocalDateTime createdAt,
        String fileName
) {
}
