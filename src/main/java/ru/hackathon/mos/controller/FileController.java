package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.hackathon.mos.entity.FileEntity;
import ru.hackathon.mos.repository.FileEntityRepository;
import ru.hackathon.mos.service.FileEntityService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(
        name = "Файлы",
        description = "Загрузка, просмотр и удаление файлов"
)
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
class FileController {

    private final FileEntityRepository fileRepo;

    private final FileEntityService fileEntityService;

    @Operation(
            summary = "Получить файл по ID",
            description = "Возвращает содержимое файла для отображения в браузере (inline)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Файл успешно найден и возвращён"),
            @ApiResponse(responseCode = "404", description = "Файл не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Resource> serveFile(
            @Parameter(description = "ID файла", required = true, example = "123")
            @PathVariable Long id) {

        FileEntity file = fileRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ByteArrayResource resource = new ByteArrayResource(file.getFileData());
        if (file.getFileData().length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + encodeFilename(file.getFilename()) + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.getSizeBytes()))
                .body(resource);
    }

    /**
     * Кодирование имени файла для безопасной передачи в HTTP-заголовках
     */
    private String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");
        } catch (Exception e) {
            return filename;
        }
    }

    @Operation(
            summary = "Удалить файл",
            description = "Удаляет файл по указанному идентификатору. Действие необратимо."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Файл успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Файл не найден", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "ID файла, который нужно удалить", required = true, example = "456")
            @PathVariable Long id) {

        fileEntityService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
