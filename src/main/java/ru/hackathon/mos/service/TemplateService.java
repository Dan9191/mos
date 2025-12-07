package ru.hackathon.mos.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hackathon.mos.config.*;
import ru.hackathon.mos.dto.FileDto;
import ru.hackathon.mos.dto.template.TemplateCreateRequest;
import ru.hackathon.mos.dto.template.TemplateDetailDto;
import ru.hackathon.mos.dto.template.TemplateListDto;
import ru.hackathon.mos.entity.FileEntity;
import ru.hackathon.mos.entity.ProjectTemplate;
import ru.hackathon.mos.repository.FileEntityRepository;
import ru.hackathon.mos.repository.ProjectTemplateRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateService {

    private final ProjectTemplateRepository templateRepo;
    private final FileEntityRepository fileRepo;
    private final Path uploadPath = Paths.get("./uploads");
    private final AppConfig appConfig;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку для файлов", e);
        }
    }

    // 1. Список шаблонов
    public Page<TemplateListDto> getActiveTemplates(Pageable pageable) {
        Page<ProjectTemplate> page = templateRepo.findAllByIsActiveTrue(pageable);

        List<TemplateListDto> dtos = page.getContent().stream()
                .map(template -> {
                    String previewUrl = fileRepo.findAllByOwnerTypeAndOwnerIdAndFileRole(
                                    "project_template", template.getId(), "preview")
                            .stream()
                            .findFirst()
                            .map(f -> appConfig.getBaseUrl() + "/" + f.getId())
                            .orElse(null);

                    return new TemplateListDto(
                            template.getId(),
                            template.getTitle(),
                            template.getStyle(),
                            template.getAreaM2(),
                            template.getRooms(),
                            template.getBasePrice(),
                            previewUrl,
                            template.getCreatedAt()
                    );
                })
                .toList();

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    // 2. Детальная информация
    public TemplateDetailDto getTemplate(Long id) {
        ProjectTemplate template = templateRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Шаблон не найден"));

        List<FileDto> files = fileRepo.findAllByOwnerTypeAndOwnerIdOrderBySortOrderAsc("project_template", id)
                .stream()
                .map(f -> new FileDto(
                        f.getId(),
                        f.getFilename(),
                        appConfig.getBaseUrl() + "/" + f.getId(),
                        f.getFileRole(),
                        f.getSortOrder()
                ))
                .toList();

        return new TemplateDetailDto(
                template.getId(),
                template.getTitle(),
                template.getDescription(),
                template.getStyle(),
                template.getAreaM2(),
                template.getRooms(),
                template.getBasePrice(),
                template.getIsActive(),
                template.getCreatedAt(),
                files
        );
    }

    // 3. Создание
    public ProjectTemplate createTemplate(TemplateCreateRequest request, MultipartFile[] files) throws IOException {
        ProjectTemplate template = new ProjectTemplate();
        template.setTitle(request.title());
        template.setDescription(request.description());
        template.setStyle(request.style());
        template.setAreaM2(request.areaM2());
        template.setRooms(request.rooms());
        template.setBasePrice(request.basePrice());
        template.setIsActive(true);

        ProjectTemplate saved = templateRepo.save(template);
        saveFiles(saved.getId(), files);
        return saved;
    }

    // 4. Обновление
    public ProjectTemplate updateTemplate(Long id, TemplateCreateRequest request, MultipartFile[] newFiles) throws IOException {
        ProjectTemplate template = templateRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        template.setTitle(request.title());
        template.setDescription(request.description());
        template.setStyle(request.style());
        template.setAreaM2(request.areaM2());
        template.setRooms(request.rooms());
        template.setBasePrice(request.basePrice());
        template.setIsActive(request.isActive());

        if (newFiles != null && newFiles.length > 0) {
            saveFiles(id, newFiles);
        }

        return templateRepo.save(template);
    }


    private void saveFiles(Long templateId, MultipartFile[] files) throws IOException {
        if (files == null) return;

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String uuid = UUID.randomUUID().toString();
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = uuid + "." + ext;
            Path path = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), path);

            FileEntity fe = new FileEntity();
            fe.setOwnerType("project_template");
            fe.setOwnerId(templateId);
            fe.setFilename(file.getOriginalFilename());
            fe.setMimeType(file.getContentType());
            fe.setSizeBytes(file.getSize());
            fe.setStoragePath("/uploads/" + filename);

            // Определяем роль: если это первое превью — preview, иначе gallery
            boolean hasPreview = fileRepo.existsByOwnerTypeAndOwnerIdAndFileRole("project_template", templateId, "preview");
            fe.setFileRole(hasPreview ? "gallery" : "preview");
            fe.setSortOrder(0); // можно улучшить

            fileRepo.save(fe);
        }
    }
}
