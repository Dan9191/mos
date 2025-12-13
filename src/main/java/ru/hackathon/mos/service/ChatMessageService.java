package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.dto.chatMessage.ChatMessageDto;
import ru.hackathon.mos.entity.ChatMessage;
import ru.hackathon.mos.entity.Order;
import ru.hackathon.mos.entity.User;
import ru.hackathon.mos.repository.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final OrderService orderService;

    public List<ChatMessageDto> getChatMessagesByOrderId(Long orderId) {
        List<ChatMessage> messages = chatMessageRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        return messages.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional
    public ChatMessageDto sendMessage(Long orderId, String message) {
//        var orderDTO = orderService.getOrderById(orderId);
        Order order = new Order(); // TODO: Найти order по orderId
        User user = new User(); // TODO: Из SecurityContext

        ChatMessage chatMessage = ChatMessage.builder()
                .order(order)
                .user(user)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        var savedMessage = chatMessageRepository.save(chatMessage);

        return convertToDto(savedMessage);
    }

    private ChatMessageDto convertToDto(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .userId(1L) // TODO: Из SecurityContext
                .userName("Тестовый пользователь")  // TODO: Из SecurityContext
                .userRole("client")  // TODO: Из SecurityContext
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}