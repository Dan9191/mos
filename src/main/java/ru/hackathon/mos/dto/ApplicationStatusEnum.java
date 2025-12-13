package ru.hackathon.mos.dto;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ApplicationStatusEnum {
    CREATED(1, "created", "Заявка создана"),
    CONSIDERATION(2, "consideration", "Заявка на рассмотрении"),
    ACCEPTED(3, "accepted", "Заявка принята"),
    REJECTED(4, "rejected", "Заявка отклонена");

    private final int id;
    private final String name;
    private final String description;

    ApplicationStatusEnum(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static ApplicationStatusEnum fromId(int id) {
        return Arrays.stream(values())
                .filter(type -> type.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный ID: " + id));
    }
}
