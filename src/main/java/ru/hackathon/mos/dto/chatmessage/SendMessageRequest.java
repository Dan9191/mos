package ru.hackathon.mos.dto.chatmessage;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    @NotBlank(message = "Сообщение не может быть пустым")
    private String message;
    private UUID userId;
    private String userName;
    private String userRole;
}
