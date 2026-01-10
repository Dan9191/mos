package ru.hackathon.mos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.hackathon.mos.dto.user.RegistrationRequest;
import ru.hackathon.mos.exception.UserAlreadyExistsException;
import ru.hackathon.mos.service.KeycloakUserService;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private KeycloakUserService keycloakUserService;

    @InjectMocks
    private RegistrationController registrationController;

    private ObjectMapper objectMapper;
    private RegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        validRequest = new RegistrationRequest(
                "ivan_ivanov",
                "ivan@example.com",
                "StrongPass123!",
                "Иван",
                "Иванов"
        );

        mockMvc = MockMvcBuilders.standaloneSetup(registrationController).build();
    }

    @Test
    void register_ShouldReturnCreated_WhenRegistrationSuccessful() throws Exception {

        String userId = "123e4567-e89b-12d3-a456-426614174000";
        when(keycloakUserService.registerUser(
                eq("ivan_ivanov"),
                eq("ivan@example.com"),
                eq("StrongPass123!"),
                eq("Иван"),
                eq("Иванов")
        )).thenReturn(userId);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(userId)));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenUserAlreadyExists() throws Exception {

        when(keycloakUserService.registerUser(
                anyString(), anyString(), anyString(), anyString(), anyString()
        )).thenThrow(new UserAlreadyExistsException("Email already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("user_exists")))
                .andExpect(jsonPath("$.message", is("Email already registered")));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenValidationFails() throws Exception {

        RegistrationRequest invalidRequest = new RegistrationRequest(
                "", // пустой username
                "invalid-email", // невалидный email
                "weak", // слабый пароль
                "", // пустое имя
                "" /// пустая фамилия
        );

        when(keycloakUserService.registerUser(
                anyString(), anyString(), anyString(), anyString(), anyString()
        )).thenThrow(new IllegalArgumentException("Password does not meet requirements"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("registration_failed")))
                .andExpect(jsonPath("$.message", containsString("requirements")));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenGeneralException() throws Exception {

        when(keycloakUserService.registerUser(
                anyString(), anyString(), anyString(), anyString(), anyString()
        )).thenThrow(new RuntimeException("Keycloak server unavailable"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("registration_failed")))
                .andExpect(jsonPath("$.message", is("Keycloak server unavailable")));
    }
}