package ru.hackathon.mos.dto.webcamera;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record WebCameraRequest(

        @NotBlank(message = "Название камеры не может быть пустым")
        @JsonProperty("name")
        String name,

        @NotBlank(message = "IP-адрес не может быть пустым")
        @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
                message = "Некорректный IP-адрес")
        @JsonProperty("ip")
        String ipAddress,

        @Pattern(regexp = "^[0-9]{1,5}$", message = "Некорректный порт (1-65535)")
        @JsonProperty("port")
        String port
) {}