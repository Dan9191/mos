package ru.hackathon.mos.dto.document;

import jakarta.validation.constraints.NotBlank;

public record DocumentSignRequest(
        @NotBlank(message = "Подпись не может быть пустой")
        String signature
) {}
