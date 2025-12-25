package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hackathon.mos.dto.user.UpdateUserRequest;
import ru.hackathon.mos.dto.user.UserViewDto;
import ru.hackathon.mos.service.UserService;

import java.util.UUID;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователь.", description = "Получение данных и редактирование пользователя.")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получить список пользователей")
    public Page<UserViewDto> list(
            @Parameter(description = "Настройки пагинации")
            @PageableDefault(size = 12, sort = "created_at", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return userService.findAllUsers(pageable);
    }

    @GetMapping("/me")
    @Operation(summary = "Получение данных текущего пользователя.")
    public UserViewDto me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.findUserById(userId);
    }

    @PutMapping("/me")
    @Operation(summary = "Обновление данных текущего пользователя.")
    public UserViewDto update(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateUserRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.update(userId, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение данных пользователя по идентификатору",
            description = "Доступно для администратора и менеджеров")
    public UserViewDto getUserById(@Parameter(description = "ID заказа") @PathVariable String id) {
        UUID userId = UUID.fromString(id);
        return userService.findUserById(userId);
    }
}
