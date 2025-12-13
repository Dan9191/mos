package ru.hackathon.mos.dto.chatMessage;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageDto(

        @JsonProperty("id")
        Long id,

        @JsonProperty("userId")
        Long userId,

        @JsonProperty("userName")
        String userName,

        @JsonProperty("userRole")
        String userRole,

        @NotBlank(message = "Сообщение не может быть пустым")
        @JsonProperty("message")
        String message,

        @JsonProperty("createdAt")
        LocalDateTime createdAt
) {}