package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hackathon.mos.dto.application.ApplicationCreateRequest;
import ru.hackathon.mos.dto.application.ApplicationDetailsDto;
import ru.hackathon.mos.service.ApplicationService;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Заявки", description = "Управление заявками на строительство")
public class ApplicationController {

    private final ApplicationService applicationService;

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
    @PostMapping("/{id}/take")
    public ApplicationDetailsDto take(@AuthenticationPrincipal Jwt jwt,
                            @PathVariable Long id) {
        String managerId = jwt.getSubject();
        return applicationService.takeApplication(id, managerId);
    }

    @Operation(summary = "Отменить заявку")
    @PostMapping("/{id}/reject")
    public ApplicationDetailsDto reject(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        String managerId = jwt.getSubject();
        return applicationService.rejectApplication(id, managerId);
    }

    @Operation(summary = "Принять заявку")
    @PostMapping("/{id}/accept")
    public ApplicationDetailsDto accept(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        String managerId = jwt.getSubject();
        return applicationService.acceptApplication(id, managerId);
    }
}
