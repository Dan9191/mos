package ru.hackathon.mos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.hackathon.mos.dto.application.ApplicationCreateRequest;
import ru.hackathon.mos.dto.application.ApplicationDetailsDto;
import ru.hackathon.mos.service.ApplicationService;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationController applicationController;

    private ObjectMapper objectMapper;
    private UUID userId;
    private Jwt jwt;
    private ApplicationDetailsDto testApplication;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        testApplication = ApplicationDetailsDto.builder()
                .id(1L)
                .creatorId(userId)
                .projectId(1L)
                .contact("+79001234567")
                .statusName("created")
                .statusDescription("Заявка создана")
                .createdAt(Instant.now())
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

        mockMvc = MockMvcBuilders.standaloneSetup(applicationController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();
    }

    @Test
    void create_ShouldCreateApplication() throws Exception {

        ApplicationCreateRequest request = new ApplicationCreateRequest();
        request.setTemplateId(1L);
        request.setContact("+79001234567");

        when(applicationService.createApplication(any(), eq(userId.toString())))
                .thenReturn(testApplication);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.creatorId", is(userId.toString())))
                .andExpect(jsonPath("$.contact", is("+79001234567")))
                .andExpect(jsonPath("$.statusName", is("created")));
    }

    @Test
    void take_ShouldTakeApplication() throws Exception {

        Long applicationId = 1L;
        ApplicationDetailsDto takenApplication = ApplicationDetailsDto.builder()
                .id(applicationId)
                .creatorId(userId)
                .projectId(1L)
                .statusName("consideration")
                .statusDescription("Заявка в рассмотрении")
                .managerId(userId)
                .managerFullName("Иванов Иван Иванович")
                .managerContact("ivanov@example.com")
                .contact("+79001234567")
                .createdAt(Instant.now())
                .build();

        when(applicationService.takeApplication(eq(applicationId), eq(userId.toString())))
                .thenReturn(takenApplication);

        mockMvc.perform(patch("/api/applications/{id}/take", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.statusName", is("consideration")))
                .andExpect(jsonPath("$.managerId", is(userId.toString())))
                .andExpect(jsonPath("$.managerFullName", is("Иванов Иван Иванович")));
    }

    @Test
    void reject_ShouldRejectApplication() throws Exception {

        Long applicationId = 1L;
        ApplicationDetailsDto rejectedApplication = ApplicationDetailsDto.builder()
                .id(applicationId)
                .creatorId(userId)
                .projectId(1L)
                .statusName("rejected")
                .statusDescription("Заявка отклонена")
                .managerId(userId)
                .contact("+79001234567")
                .createdAt(Instant.now())
                .build();

        when(applicationService.rejectApplication(eq(applicationId), eq(userId.toString())))
                .thenReturn(rejectedApplication);

        mockMvc.perform(patch("/api/applications/{id}/reject", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.statusName", is("rejected")));
    }

    @Test
    void accept_ShouldAcceptApplication() throws Exception {

        Long applicationId = 1L;
        ApplicationDetailsDto acceptedApplication = ApplicationDetailsDto.builder()
                .id(applicationId)
                .creatorId(userId)
                .projectId(1L)
                .statusName("accepted")
                .statusDescription("Заявка принята")
                .managerId(userId)
                .contact("+79001234567")
                .createdAt(Instant.now())
                .build();

        when(applicationService.acceptApplication(eq(applicationId), eq(userId.toString())))
                .thenReturn(acceptedApplication);

        mockMvc.perform(patch("/api/applications/{id}/accept", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.statusName", is("accepted")));
    }
}