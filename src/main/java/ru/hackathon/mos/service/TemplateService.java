package ru.hackathon.mos.service;

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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateService {

    private final ProjectTemplateRepository templateRepo;
    private final FileEntityRepository fileRepo;
    private final AppConfig appConfig;


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

    /**
     * Создание шаблона.
     *
     * @param request     Данные запроса на создание шаблона.
     * @param files       Приложенные файлы.
     * @param ownerUserId ID пользователя, который сохраняет файл.
     * @return            Результат сохранения.
     * @throws IOException ошибка чтения файла.
     */
    public ProjectTemplate createTemplate(TemplateCreateRequest request,
                                          MultipartFile[] files,
                                          String ownerUserId) throws IOException {
        ProjectTemplate template = new ProjectTemplate();
        template.setTitle(request.title());
        template.setDescription(request.description());
        template.setStyle(request.style());
        template.setAreaM2(request.areaM2());
        template.setRooms(request.rooms());
        template.setBasePrice(request.basePrice());
        template.setIsActive(true);

        ProjectTemplate saved = templateRepo.save(template);
        saveFiles(saved.getId(), files, ownerUserId);
        return saved;
    }

    /**
     * Обновление шаблона.
     *
     * @param id          ID шаблона.
     * @param request     Данные запроса на обновление шаблона.
     * @param newFiles    Новые файлы.
     * @param ownerUserId ID пользователя, который сохраняет файл.
     * @return Результат сохранения.
     * @throws IOException ошибка чтения файла.
     */
    public ProjectTemplate updateTemplate(Long id,
                                          TemplateCreateRequest request,
                                          MultipartFile[] newFiles,
                                          String ownerUserId) throws IOException {
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
            saveFiles(id, newFiles, ownerUserId);
        }

        return templateRepo.save(template);
    }


    /**
     * Сохранение файлов при создании шаблона.
     *
     * @param templateId  ID шаблона.
     * @param files       Новые файлы.
     * @param ownerUserId ID пользователя, который сохраняет файл.
     * @throws IOException ошибка чтения файла.
     */
    @Transactional
    public void saveFiles(Long templateId, MultipartFile[] files, String ownerUserId) throws IOException {
        if (files == null || files.length == 0) return;

        // Проверяем, есть ли уже превью
        boolean hasPreview = fileRepo.existsByOwnerTypeAndOwnerIdAndFileRole(
                "project_template", templateId, "preview"
        );

        int currentSortOrder = 1;

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String ext = StringUtils.getFilenameExtension(originalName);
            if (ext == null) continue;
            ext = ext.toLowerCase();

            // Создаем сущность файла
            FileEntity fileEntity = new FileEntity();
            fileEntity.setOwnerType("project_template");
            fileEntity.setOwnerId(templateId);
            fileEntity.setFilename(originalName);
            fileEntity.setMimeType(file.getContentType());
            fileEntity.setSizeBytes(file.getSize());

            // Читаем файл в байтовый массив
            byte[] fileData = file.getBytes();
            fileEntity.setFileData(fileData);

            // Определяем роль файла
            String fileRole;
            if ("pdf".equals(ext)) {
                fileRole = "document";
                fileEntity.setFileRole(fileRole);
                fileEntity.setSortOrder(currentSortOrder++);
            } else if (isImageExtension(ext)) {
                if (!hasPreview) {
                    fileRole = "preview";
                    hasPreview = true;
                    fileEntity.setFileRole(fileRole);
                    fileEntity.setSortOrder(0);
                } else {
                    fileRole = "gallery";
                    fileEntity.setFileRole(fileRole);
                    fileEntity.setSortOrder(currentSortOrder++);
                }
            } else {
                fileRole = "gallery";
                fileEntity.setFileRole(fileRole);
                fileEntity.setSortOrder(currentSortOrder++);
            }

            fileEntity.setUploadedBy(ownerUserId);

            fileRepo.save(fileEntity);
        }
    }

    /**
     * Проверка, является ли расширение изображением
     */
    private boolean isImageExtension(String ext) {
        return List.of("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg")
                .contains(ext.toLowerCase());
    }
}
