package ru.hackathon.mos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;


    @GetMapping
    public Page<TemplateListDto> list(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return templateService.getActiveTemplates(pageable);
    }

    @GetMapping("/{id}")
    public TemplateDetailDto get(@PathVariable Long id) {
        return templateService.getTemplate(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectTemplate> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files) throws IOException {
        String userId = jwt.getSubject();
        ObjectMapper mapper = new ObjectMapper();
        TemplateCreateRequest request = mapper.readValue(dataJson, TemplateCreateRequest.class);

        ProjectTemplate created = templateService.createTemplate(request, files, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectTemplate update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestPart("data") TemplateCreateRequest request,
            @RequestPart(value = "files", required = false) MultipartFile[] files) throws IOException {
        String userId = jwt.getSubject();
        return templateService.updateTemplate(id, request, files, userId);
    }
}
