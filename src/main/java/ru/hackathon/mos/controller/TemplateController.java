package ru.hackathon.mos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.hackathon.mos.dto.template.TemplateCreateRequest;
import ru.hackathon.mos.dto.template.TemplateDetailDto;
import ru.hackathon.mos.dto.template.TemplateListDto;
import ru.hackathon.mos.entity.ProjectTemplate;
import ru.hackathon.mos.service.TemplateService;

import java.io.IOException;

/**
 * Контроллер для работы с архитектурными шаблонами.
 */
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Tag(name = "Шаблоны проектов", description = "Управление шаблонами")
public class TemplateController {

    /**
     * Сервис работы с шаблонами.
     */
    private final TemplateService templateService;

    @Operation(summary = "Список активных шаблонов-проектов", description = "Получение списка активных шаблонов с пагинацией")
    @GetMapping
    public Page<TemplateListDto> list(
            @Parameter(description = "Настройки пагинации")
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return templateService.getActiveTemplates(pageable);
    }

    @Operation(summary = "Детали шаблона-проекта", description = "Получение детальной информации о шаблоне по ID")
    @GetMapping("/{id}")
    public TemplateDetailDto get(
            @Parameter(description = "ID шаблона") @PathVariable Long id) {
        return templateService.getTemplate(id);
    }

    @Operation(summary = "Создание шаблона проекта", description = "Создание нового архитектурного шаблона с возможностью прикрепления файлов")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectTemplate> create(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "JSON с данными шаблона") @RequestPart("data") String dataJson,
            @Parameter(description = "Файлы, прикрепленные к шаблону") @RequestPart(value = "files", required = false) MultipartFile[] files
    ) throws IOException {
        String userId = jwt.getSubject();
        ObjectMapper mapper = new ObjectMapper();
        TemplateCreateRequest request = mapper.readValue(dataJson, TemplateCreateRequest.class);

        ProjectTemplate created = templateService.createTemplate(request, files, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Обновление шаблона проекта", description = "Обновление существующего шаблона по ID с возможностью прикрепления файлов")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectTemplate update(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID шаблона для обновления") @PathVariable Long id,
            @Parameter(description = "Новые данные шаблона") @RequestPart("data") TemplateCreateRequest request,
            @Parameter(description = "Файлы для обновления шаблона") @RequestPart(value = "files", required = false) MultipartFile[] files
    ) throws IOException {
        String userId = jwt.getSubject();
        return templateService.updateTemplate(id, request, files, userId);
    }
}
