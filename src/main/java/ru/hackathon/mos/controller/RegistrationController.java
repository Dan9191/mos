package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.hackathon.mos.dto.user.RegistrationRequest;
import ru.hackathon.mos.dto.user.RegistrationErrorResponse;
import ru.hackathon.mos.dto.user.RegistrationResponse;
import ru.hackathon.mos.exception.UserAlreadyExistsException;
import ru.hackathon.mos.service.KeycloakUserService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Контроллер регистрации пользователя")
public class RegistrationController {

    private final KeycloakUserService keycloakUserService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт пользователя в Keycloak, назначает роль hackathon.user, устанавливает постоянный пароль и верифицирует email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegistrationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации или пользователь уже существует",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegistrationErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера (например, проблема с Keycloak)"
            )
    })
    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        try {
            String userId = keycloakUserService.registerUser(
                    request.username(),
                    request.email(),
                    request.password(),
                    request.firstName(),
                    request.lastName()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(new RegistrationResponse(userId));

        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest()
                    .body(new RegistrationErrorResponse("user_exists", e.getMessage()));
        } catch (Exception e) {
            // На случай других ошибок Keycloak (например, слабый пароль)
            return ResponseEntity.badRequest()
                    .body(new RegistrationErrorResponse("registration_failed", e.getMessage()));
        }
    }
}
