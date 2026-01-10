package ru.hackathon.mos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hackathon.mos.dto.chatmessage.ChatMessageDto;
import ru.hackathon.mos.entity.ChatMessage;
import ru.hackathon.mos.entity.Order;
import ru.hackathon.mos.entity.User;
import ru.hackathon.mos.entity.UserType;
import ru.hackathon.mos.exception.OrderNotFoundException;
import ru.hackathon.mos.exception.UserNotFoundException;
import ru.hackathon.mos.repository.ChatMessageRepository;
import ru.hackathon.mos.repository.OrderRepository;
import ru.hackathon.mos.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private UUID userId;
    private Long orderId;
    private User testUser;
    private Order testOrder;
    private ChatMessage testMessage;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        orderId = 1L;
        testDateTime = LocalDateTime.of(2024, 12, 10, 10, 30, 0);

        UserType userType = UserType.builder()
                .id(1)
                .name("USER")
                .description("Обычный пользователь")
                .build();

        testUser = User.builder()
                .id(userId)
                .type(userType)
                .username("ivanov")
                .firstName("Иван")
                .lastName("Иванов")
                .middleName("Иванович")
                .email("ivan@example.com")
                .build();

        testOrder = Order.builder()
                .id(orderId)
                .build();

        testMessage = ChatMessage.builder()
                .id(1L)
                .order(testOrder)
                .user(testUser)
                .message("Привет, как дела?")
                .createdAt(testDateTime)
                .build();
    }

    @Test
    void getChatMessagesByOrderId_ShouldReturnMessages() {
        // Arrange
        ChatMessage message2 = ChatMessage.builder()
                .id(2L)
                .order(testOrder)
                .user(testUser)
                .message("Второе сообщение")
                .createdAt(testDateTime.plusMinutes(5))
                .build();

        List<ChatMessage> messages = List.of(testMessage, message2);

        when(chatMessageRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(messages);

        // Act
        List<ChatMessageDto> result = chatMessageService.getChatMessagesByOrderId(orderId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result, hasSize(2));

        ChatMessageDto firstMessage = result.get(0);
        assertThat(firstMessage.id(), is(1L));
        assertThat(firstMessage.userId(), is(userId));
        assertThat(firstMessage.message(), is("Привет, как дела?"));
        assertThat(firstMessage.userName(), containsString("Иванов Иван Иванович"));
        assertThat(firstMessage.userRole(), is("USER"));

        ChatMessageDto secondMessage = result.get(1);
        assertThat(secondMessage.id(), is(2L));
        assertThat(secondMessage.message(), is("Второе сообщение"));

        verify(chatMessageRepository, times(1)).findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    @Test
    void getChatMessagesByOrderId_ShouldReturnEmptyList_WhenNoMessages() {
        // Arrange
        when(chatMessageRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());

        // Act
        List<ChatMessageDto> result = chatMessageService.getChatMessagesByOrderId(orderId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result, hasSize(0));
        verify(chatMessageRepository, times(1)).findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    @Test
    void sendMessage_ShouldSendMessageSuccessfully() {
        // Arrange
        String messageText = "Новое сообщение";

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);

        // Act
        ChatMessageDto result = chatMessageService.sendMessage(userId, orderId, messageText);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.id(), is(1L));
        assertThat(result.userId(), is(userId));
        assertThat(result.message(), is("Привет, как дела?"));
        assertThat(result.userName(), containsString("Иванов Иван Иванович"));
        assertThat(result.userRole(), is("USER"));

        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, times(1)).findById(userId);
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void sendMessage_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        String messageText = "Новое сообщение";

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> chatMessageService.sendMessage(userId, orderId, messageText)
        );

        assertThat(exception.getMessage(), containsString(String.valueOf(orderId)));
        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, never()).findById(any());
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    void sendMessage_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String messageText = "Новое сообщение";

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> chatMessageService.sendMessage(userId, orderId, messageText)
        );

        assertThat(exception.getMessage(), containsString("Пользователь с id = " + userId + " не найден"));
        verify(orderRepository, times(1)).findById(orderId);
        verify(userRepository, times(1)).findById(userId);
        verify(chatMessageRepository, never()).save(any());
    }

}
