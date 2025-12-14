package ru.hackathon.mos.dto.document;

public record DocumentCreateRequest(
        String type,
        String title,
        String description,
        String fileName,
        String extension,
        String fileContent // base64 encoded
) {}
