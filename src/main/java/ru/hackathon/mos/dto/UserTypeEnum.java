package ru.hackathon.mos.dto;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum UserTypeEnum {
    USER(1, "User", "ROLE_hackathon.user"),
    MANAGER(2, "Manager", "ROLE_hackathon.manager"),
    ADMIN(3, "Admin", "ROLE_hackathon.admin");

    private final int id;
    private final String name;
    private final String role;

    UserTypeEnum(int id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public static UserTypeEnum fromId(int id) {
        return Arrays.stream(values())
                .filter(type -> type.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный ID: " + id));
    }
}
