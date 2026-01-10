package ru.hackathon.mos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.hackathon.mos.dto.chatmessage.ChatMessageDto;
import ru.hackathon.mos.dto.chatmessage.SendMessageRequest;
import ru.hackathon.mos.service.ChatMessageService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatMessageController chatMessageController;

    private ObjectMapper objectMapper;
    private UUID userId;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        HandlerMethodArgumentResolver authenticationPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) ||
                        Jwt.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return jwt;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(chatMessageController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();
    }

    @Test
    void getChatMessages_ShouldReturnMessages() throws Exception {

        Long orderId = 1L;

        ChatMessageDto message1 = ChatMessageDto.builder()
                .id(1L)
                .userId(userId)
                .message("Привет, как дела?")
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessageDto message2 = ChatMessageDto.builder()
                .id(2L)
                .userId(UUID.fromString("223e4567-e89b-12d3-a456-426614174001"))
                .message("Все хорошо, работаем!")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        List<ChatMessageDto> messages = List.of(message1, message2);

        when(chatMessageService.getChatMessagesByOrderId(orderId))
                .thenReturn(messages);

        mockMvc.perform(get("/api/orders/{orderId}/chatMessages", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].message").value("Привет, как дела?"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].message").value("Все хорошо, работаем!"));
    }

    @Test
    void getChatMessages_ShouldReturnEmptyList_WhenNoMessages() throws Exception {

        Long orderId = 1L;
        when(chatMessageService.getChatMessagesByOrderId(orderId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/orders/{orderId}/chatMessages", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void sendMessage_ShouldCreateAndReturnMessage() throws Exception {

        Long orderId = 1L;
        SendMessageRequest request = new SendMessageRequest();
        request.setMessage("Новое сообщение");

        ChatMessageDto response = ChatMessageDto.builder()
                .id(3L)
                .userId(userId)
                .message("Новое сообщение")
                .createdAt(LocalDateTime.now())
                .build();

        when(chatMessageService.sendMessage(eq(userId), eq(orderId), eq("Новое сообщение")))
                .thenReturn(response);

        mockMvc.perform(post("/api/orders/{orderId}/chatMessages", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.message").value("Новое сообщение"))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void sendMessage_ShouldReturnBadRequest_WhenMessageIsEmpty() throws Exception {

        Long orderId = 1L;
        SendMessageRequest request = new SendMessageRequest();
        request.setMessage(""); /// Пустое сообщение

        mockMvc.perform(post("/api/orders/{orderId}/chatMessages", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendMessage_ShouldReturnBadRequest_WhenMessageIsNull() throws Exception {

        Long orderId = 1L;
        SendMessageRequest request = new SendMessageRequest();

        mockMvc.perform(post("/api/orders/{orderId}/chatMessages", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}