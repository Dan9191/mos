package ru.hackathon.mos.dto.document;

public record DocumentUpdateRequest(
        String title,
        String description,
        String status,
        String fileName,
        String extension,
        String fileContent
) {}
