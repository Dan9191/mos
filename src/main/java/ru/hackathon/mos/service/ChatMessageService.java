package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.dto.chatmessage.ChatMessageDto;
import ru.hackathon.mos.entity.ChatMessage;
import ru.hackathon.mos.entity.Order;
import ru.hackathon.mos.entity.User;
import ru.hackathon.mos.exception.OrderNotFoundException;
import ru.hackathon.mos.exception.UserNotFoundException;
import ru.hackathon.mos.repository.ChatMessageRepository;
import ru.hackathon.mos.repository.OrderRepository;
import ru.hackathon.mos.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public List<ChatMessageDto> getChatMessagesByOrderId(Long orderId) {
        List<ChatMessage> messages = chatMessageRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        return messages.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional
    public ChatMessageDto sendMessage(UUID userId, Long orderId, String message) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + userId + " не найден."));

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
        var user = chatMessage.getUser();
        var userFullName = user.getLastName() + " " + user.getFirstName() + " " + user.getMiddleName();

        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .userId(user.getId())
                .userName(userFullName)
                .userRole(user.getType().getName())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}