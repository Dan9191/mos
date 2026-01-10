package ru.hackathon.mos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.hackathon.mos.config.AppPropertiesConfig;
import ru.hackathon.mos.dto.FileDto;
import ru.hackathon.mos.dto.template.TemplateCreateRequest;
import ru.hackathon.mos.dto.template.TemplateDetailDto;
import ru.hackathon.mos.entity.FileEntity;
import ru.hackathon.mos.entity.ProjectTemplate;
import ru.hackathon.mos.repository.FileEntityRepository;
import ru.hackathon.mos.repository.ProjectTemplateRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private ProjectTemplateRepository templateRepo;

    @Mock
    private FileEntityRepository fileRepo;

    @Mock
    private AppPropertiesConfig appPropertiesConfig;

    @InjectMocks
    private TemplateService templateService;

    private ProjectTemplate testTemplate;
    private TemplateCreateRequest testRequest;
    private Long templateId;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        templateId = 1L;
        testDateTime = LocalDateTime.of(2024, 12, 10, 10, 30, 0);

        // Создаем объект через конструктор или сеттеры
        testTemplate = new ProjectTemplate();
        testTemplate.setId(templateId);
        testTemplate.setTitle("Современный дом 120м²");
        testTemplate.setDescription("Современный двухэтажный дом с гаражом");
        testTemplate.setStyle("modern");
        testTemplate.setAreaM2(120.5);
        testTemplate.setRooms(5);
        testTemplate.setBasePrice(new BigDecimal("5000000"));
        testTemplate.setIsActive(true);
        testTemplate.setCreatedAt(testDateTime);

        testRequest = new TemplateCreateRequest(
                "Современный дом 120м²",
                "Современный двухэтажный дом с гаражом",
                "modern",
                120.5,
                5,
                new BigDecimal("5000000"),
                true
        );
    }

    @Test
    void getTemplate_ShouldReturnTemplateDetail_WhenTemplateExists() {
        // Arrange
        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(1L);
        fileEntity.setFilename("preview.jpg");
        fileEntity.setFileRole("preview");

        when(templateRepo.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(fileRepo.findAllByOwnerTypeAndOwnerIdOrderBySortOrderAsc("project_template", templateId))
                .thenReturn(List.of(fileEntity));
        when(appPropertiesConfig.getBaseUrl()).thenReturn("http://localhost:8080");

        // Act
        TemplateDetailDto result = templateService.getTemplate(templateId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.id(), is(templateId));
        assertThat(result.title(), is("Современный дом 120м²"));
        assertThat(result.style(), is("modern"));
        assertThat(result.areaM2(), is(120.5));
        assertThat(result.isActive(), is(true));
        assertThat(result.files(), hasSize(1));

        FileDto fileDto = result.files().get(0);
        assertThat(fileDto.id(), is(1L));
        assertThat(fileDto.filename(), is("preview.jpg"));
        verify(templateRepo, times(1)).findById(templateId);
        verify(fileRepo, times(1))
                .findAllByOwnerTypeAndOwnerIdOrderBySortOrderAsc("project_template", templateId);
    }

    @Test
    void getTemplate_ShouldThrowException_WhenTemplateNotFound() {
        // Arrange
        when(templateRepo.findById(templateId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> templateService.getTemplate(templateId)
        );

        assertThat(exception.getStatusCode().value(), is(404));
        assertThat(exception.getReason(), containsString("Шаблон не найден"));
        verify(templateRepo, times(1)).findById(templateId);
        verify(fileRepo, never()).findAllByOwnerTypeAndOwnerIdOrderBySortOrderAsc(any(), any());
    }

    @Test
    void updateTemplate_ShouldUpdateTemplateSuccessfully() throws IOException {
        // Arrange
        TemplateCreateRequest updateRequest = new TemplateCreateRequest(
                "Обновленный дом",
                "Обновленное описание",
                "classic",
                150.0,
                6,
                new BigDecimal("6000000"),
                false
        );

        when(templateRepo.findById(templateId)).thenReturn(Optional.of(testTemplate));
        when(templateRepo.save(any(ProjectTemplate.class))).thenReturn(testTemplate);

        // Act
        ProjectTemplate result = templateService.updateTemplate(templateId, updateRequest, null, "user123");

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(templateId));
        verify(templateRepo, times(1)).findById(templateId);
        verify(templateRepo, times(1)).save(testTemplate);
    }

    @Test
    void deleteTemplate_ShouldDeleteTemplateSuccessfully() {
        // Arrange
        when(templateRepo.findById(templateId)).thenReturn(Optional.of(testTemplate));
        doNothing().when(templateRepo).delete(testTemplate);

        // Act
        templateService.deleteTemplate(templateId);

        // Assert
        verify(templateRepo, times(1)).findById(templateId);
        verify(templateRepo, times(1)).delete(testTemplate);
    }

    @Test
    void deleteTemplate_ShouldThrowException_WhenTemplateNotFound() {
        // Arrange
        when(templateRepo.findById(templateId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> templateService.deleteTemplate(templateId)
        );

        assertThat(exception.getStatusCode().value(), is(404));
        verify(templateRepo, times(1)).findById(templateId);
        verify(templateRepo, never()).delete(any());
    }
}