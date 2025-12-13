package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hackathon.mos.dto.chatMessage.ChatMessageDto;
import ru.hackathon.mos.service.ChatMessageService;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/chatMessages")
@RequiredArgsConstructor
@Tag(name = "Чат", description = "Управление чатом проекта")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @GetMapping
    @Operation(summary = "Получить историю сообщений чата проекта")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable Long orderId) {
        List<ChatMessageDto> messages = chatMessageService.getChatMessagesByOrderId(orderId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    @Operation(summary = "Отправить сообщение в чат")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable Long orderId,
            @Valid @RequestBody String message) {

        ChatMessageDto response = chatMessageService.sendMessage(orderId, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}