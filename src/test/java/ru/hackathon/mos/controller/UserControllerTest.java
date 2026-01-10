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
import ru.hackathon.mos.dto.user.UpdateUserRequest;
import ru.hackathon.mos.dto.user.UserTypeViewDto;
import ru.hackathon.mos.dto.user.UserViewDto;
import ru.hackathon.mos.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private UUID userId;
    private Jwt jwt;
    private UserViewDto testUser;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        testDateTime = LocalDateTime.of(2024, 12, 10, 10, 30, 0);

        jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        UserTypeViewDto userType = UserTypeViewDto.builder()
                .id(1)
                .name("USER")
                .description("Обычный пользователь")
                .build();

        testUser = UserViewDto.builder()
                .id(userId)
                .type(userType)
                .username("ivanov")
                .firstName("Иван")
                .lastName("Иванов")
                .surname("Иванович")
                .email("ivan@example.com")
                .createdAt(testDateTime)
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

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();
    }

    @Test
    void me_ShouldReturnCurrentUser() throws Exception {

        when(userService.findUserById(userId)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.toString())))
                .andExpect(jsonPath("$.username", is("ivanov")))
                .andExpect(jsonPath("$.firstName", is("Иван")))
                .andExpect(jsonPath("$.lastName", is("Иванов")))
                .andExpect(jsonPath("$.email", is("ivan@example.com")));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {

        UUID targetUserId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");

        UserViewDto targetUser = UserViewDto.builder()
                .id(targetUserId)
                .username("petrov")
                .firstName("Петр")
                .lastName("Петров")
                .email("petr@example.com")
                .createdAt(testDateTime)
                .build();

        when(userService.findUserById(targetUserId)).thenReturn(targetUser);

        mockMvc.perform(get("/api/users/{id}", targetUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(targetUserId.toString())))
                .andExpect(jsonPath("$.username", is("petrov")))
                .andExpect(jsonPath("$.firstName", is("Петр")))
                .andExpect(jsonPath("$.email", is("petr@example.com")));
    }

    //
    @Test
    void update_ShouldUpdateCurrentUser() throws Exception {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("НовоеИмя");
        request.setLastName("НоваяФамилия");
        request.setSurname("НовоеОтчество");
        request.setEmail("newemail@example.com");

        UserViewDto updatedUser = UserViewDto.builder()
                .id(userId)
                .username("ivanov")
                .firstName("НовоеИмя")
                .lastName("НоваяФамилия")
                .surname("НовоеОтчество")
                .email("newemail@example.com")
                .createdAt(testDateTime)
                .build();

        when(userService.update(eq(userId), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("НовоеИмя")))
                .andExpect(jsonPath("$.lastName", is("НоваяФамилия")))
                .andExpect(jsonPath("$.email", is("newemail@example.com")));
    }

}