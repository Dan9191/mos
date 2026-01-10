package ru.hackathon.mos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hackathon.mos.entity.FileEntity;
import ru.hackathon.mos.exception.FileNotFoundException;
import ru.hackathon.mos.repository.FileEntityRepository;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileEntityServiceTest {

    @Mock
    private FileEntityRepository fileEntityRepository;

    @InjectMocks
    private FileEntityService fileEntityService;

    private Long documentId;
    private Long fileEntityId;
    private FileEntity testFileEntity;

    @BeforeEach
    void setUp() {
        documentId = 1L;
        fileEntityId = 10L;

        testFileEntity = new FileEntity();
        testFileEntity.setId(fileEntityId);
        testFileEntity.setOwnerType("document");
        testFileEntity.setOwnerId(documentId);
        testFileEntity.setFilename("document.pdf");
        testFileEntity.setMimeType("application/pdf");
        testFileEntity.setSizeBytes(1024L);
        testFileEntity.setFileData("PDF content".getBytes());
        testFileEntity.setFileRole("CONTRACT");
        testFileEntity.setSortOrder(1);
    }

    @Test
    void getFileEntityById_ShouldReturnFile_WhenFileExists() {
        // Arrange
        when(fileEntityRepository.findById(fileEntityId)).thenReturn(Optional.of(testFileEntity));

        // Act
        FileEntity result = fileEntityService.getFileEntityById(fileEntityId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(fileEntityId));
        assertThat(result.getFilename(), is("document.pdf"));
        assertThat(result.getMimeType(), is("application/pdf"));
        assertThat(result.getFileRole(), is("CONTRACT"));
        verify(fileEntityRepository, times(1)).findById(fileEntityId);
    }

    @Test
    void getFileEntityById_ShouldThrowException_WhenFileNotFound() {
        // Arrange
        when(fileEntityRepository.findById(fileEntityId)).thenReturn(Optional.empty());

        // Act & Assert
        FileNotFoundException exception = assertThrows(
                FileNotFoundException.class,
                () -> fileEntityService.getFileEntityById(fileEntityId)
        );

        assertThat(exception.getMessage(), containsString("Файл с ID " + fileEntityId + " не найден"));
        verify(fileEntityRepository, times(1)).findById(fileEntityId);
    }

    @Test
    void deleteFile_ShouldDeleteFileSuccessfully() {
        // Arrange
        when(fileEntityRepository.findById(fileEntityId)).thenReturn(Optional.of(testFileEntity));
        doNothing().when(fileEntityRepository).delete(testFileEntity);

        // Act
        fileEntityService.deleteFile(fileEntityId);

        // Assert
        verify(fileEntityRepository, times(1)).findById(fileEntityId);
        verify(fileEntityRepository, times(1)).delete(testFileEntity);
    }

    @Test
    void deleteFile_ShouldThrowException_WhenFileNotFound() {
        // Arrange
        when(fileEntityRepository.findById(fileEntityId)).thenReturn(Optional.empty());

        // Act & Assert
        FileNotFoundException exception = assertThrows(
                FileNotFoundException.class,
                () -> fileEntityService.deleteFile(fileEntityId)
        );

        assertThat(exception.getMessage(), containsString("Файл с ID " + fileEntityId + " не найден"));
        verify(fileEntityRepository, times(1)).findById(fileEntityId);
        verify(fileEntityRepository, never()).delete(any());
    }
}