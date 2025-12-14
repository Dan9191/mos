package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.hackathon.mos.dto.chatmessage.ChatMessageDto;
import ru.hackathon.mos.service.ChatMessageService;
import ru.hackathon.mos.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/chatMessages")
@RequiredArgsConstructor
@Tag(name = "Чат", description = "Управление чатом проекта")
public class ChatMessageController {
    private final OrderService orderService;
    private final ChatMessageService chatMessageService;

    @GetMapping
    @Operation(summary = "Получить историю сообщений чата проекта")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        List<ChatMessageDto> messages = chatMessageService.getChatMessagesByOrderId(orderId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    @Operation(summary = "Отправить сообщение в чат")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            @Valid @RequestBody String message) {
        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        ChatMessageDto response = chatMessageService.sendMessage(userId, orderId, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}