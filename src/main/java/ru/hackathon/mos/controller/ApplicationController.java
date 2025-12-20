package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hackathon.mos.dto.application.ApplicationCreateRequest;
import ru.hackathon.mos.dto.application.ApplicationDetailsDto;
import ru.hackathon.mos.service.ApplicationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Заявки", description = "Управление заявками на строительство")
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    @Operation(summary = "Получить список заявок")
    public Page<ApplicationDetailsDto> list(
            @Parameter(description = "Настройки пагинации")
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return applicationService.findAllSortByStatus(pageable);
    }

    @GetMapping("/user")
    @Operation(summary = "Получить список заявок для пользователя",
            description = "Доступно обычному пользователю")
    public Page<ApplicationDetailsDto> listByUser(
            @Parameter(description = "Настройки пагинации")
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return applicationService.findAllByUserSortByStatus(pageable, userId);
    }

    @GetMapping("/manager")
    @Operation(summary = "Получить список курируемых заявок для менеджера",
            description = "Доступно менеджеру и админу")
    public Page<ApplicationDetailsDto> listByManager(
            @Parameter(description = "Настройки пагинации")
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return applicationService.findAllByManagerSortByStatus(pageable, userId);
    }

    @Operation(summary = "Создать заявку")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDetailsDto create(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        String userId = jwt.getSubject();
        return applicationService.createApplication(request.getTemplateId(), userId);
    }

    @Operation(summary = "Взять заявку на исполнение")
    @PatchMapping("/{id}/take")
    public ApplicationDetailsDto take(@AuthenticationPrincipal Jwt jwt,
                            @PathVariable Long id) {
        String managerId = jwt.getSubject();
        return applicationService.takeApplication(id, managerId);
    }

    @Operation(summary = "Отменить заявку")
    @PatchMapping("/{id}/reject")
    public ApplicationDetailsDto reject(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        String managerId = jwt.getSubject();
        return applicationService.rejectApplication(id, managerId);
    }

    @Operation(summary = "Принять заявку")
    @PatchMapping("/{id}/accept")
    public ApplicationDetailsDto accept(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        String managerId = jwt.getSubject();
        return applicationService.acceptApplication(id, managerId);
    }
}
