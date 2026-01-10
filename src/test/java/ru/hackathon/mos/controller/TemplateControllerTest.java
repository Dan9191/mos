package ru.hackathon.mos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.hackathon.mos.dto.template.TemplateDetailDto;
import ru.hackathon.mos.dto.template.TemplateListDto;
import ru.hackathon.mos.service.TemplateService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TemplateControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TemplateService templateService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TemplateController templateController;

    private UUID userId;
    private Jwt jwt;
    private TemplateDetailDto testTemplateDetail;
    private TemplateListDto testTemplateList;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        testTemplateDetail = new TemplateDetailDto(
                1L,
                "Современный дом 120м²",
                "Современный двухэтажный дом с гаражом",
                "modern",
                120.5,
                5,
                new BigDecimal("5000000"),
                true,
                LocalDateTime.now(),
                java.util.List.of()
        );

        testTemplateList = new TemplateListDto(
                1L,
                "Современный дом 120м²",
                "modern",
                120.5,
                5,
                new BigDecimal("5000000"),
                true,
                "http://localhost:8080/files/123",
                LocalDateTime.now()
        );

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

        mockMvc = MockMvcBuilders.standaloneSetup(templateController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();
    }

    @Test
    void get_ShouldReturnTemplateDetail() throws Exception {

        Long templateId = 1L;
        when(templateService.getTemplate(templateId)).thenReturn(testTemplateDetail);

        mockMvc.perform(get("/api/templates/{id}", templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Современный дом 120м²")))
                .andExpect(jsonPath("$.style", is("modern")))
                .andExpect(jsonPath("$.areaM2", is(120.5)))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    void get_ShouldReturnNotFound_WhenTemplateDoesNotExist() throws Exception {

        Long templateId = 999L;
        when(templateService.getTemplate(templateId)).thenThrow(
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Шаблон не найден"
                )
        );

        mockMvc.perform(get("/api/templates/{id}", templateId))
                .andExpect(status().isNotFound());
    }
}