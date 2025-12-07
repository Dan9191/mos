package ru.hackathon.mos.dto;

public record FileDto(
        Long id,
        String filename,
        String url,           // полный URL: http://localhost:8080/files/123
        String fileRole,
        Integer sortOrder
) {}
