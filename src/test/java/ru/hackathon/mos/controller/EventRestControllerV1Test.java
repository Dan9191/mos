package ru.hackathon.mos.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventRestControllerV1Test {

    @InjectMocks
    private EventRestControllerV1 eventRestController;

    @Test
    void getMyResource_ShouldReturnEmailAndUserId() throws Exception {

        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String email = "user@example.com";

        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email", email)
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

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(eventRestController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();

        mockMvc.perform(get("/api/v1/events/my-resource"))
                .andExpect(status().isOk())
                .andExpect(content().string(email + ":" + userId));
    }

    @Test
    void getMyResource_ShouldReturnOnlyUserId_WhenEmailIsNull() throws Exception {

        UUID userId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");

        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
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

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(eventRestController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();

        //
        mockMvc.perform(get("/api/v1/events/my-resource"))
                .andExpect(status().isOk())
                .andExpect(content().string("null:" + userId)); // email будет null
    }
}