package ru.hackathon.mos.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import ru.hackathon.mos.entity.FileEntity;
import ru.hackathon.mos.repository.FileEntityRepository;
import ru.hackathon.mos.service.FileEntityService;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileEntityRepository fileRepo;

    @Mock
    private FileEntityService fileEntityService;

    @InjectMocks
    private FileController fileController;

    private FileEntity testFile;

    @BeforeEach
    void setUp() {
        testFile = new FileEntity();
        testFile.setId(1L);
        testFile.setFilename("document.pdf");
        testFile.setMimeType("application/pdf");
        testFile.setSizeBytes(1024L);
        testFile.setFileData("PDF content".getBytes());
    }

    @Test
    void serveFile_ShouldReturnFile_WhenFileExists() {

        when(fileRepo.findById(1L)).thenReturn(Optional.of(testFile));

        ResponseEntity<org.springframework.core.io.Resource> response = fileController.serveFile(1L);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getHeaders().getContentType().toString(), is("application/pdf"));
        assertThat(response.getHeaders().getContentDisposition().toString(),
                containsString("inline; filename=\"document.pdf\""));
        assertThat(response.getBody(), is(instanceOf(ByteArrayResource.class)));
    }

    @Test
    void serveFile_ShouldThrowNotFound_WhenFileDoesNotExist() {

        when(fileRepo.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> fileController.serveFile(999L)
        );

        assertThat(exception.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void serveFile_ShouldThrowNotFound_WhenFileDataIsEmpty() {

        testFile.setFileData(new byte[0]);
        when(fileRepo.findById(1L)).thenReturn(Optional.of(testFile));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> fileController.serveFile(1L)
        );

        assertThat(exception.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteFile_ShouldCallServiceDelete() {

        doNothing().when(fileEntityService).deleteFile(1L);

        ResponseEntity<Void> response = fileController.deleteFile(1L);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
        verify(fileEntityService, times(1)).deleteFile(1L);
    }
}