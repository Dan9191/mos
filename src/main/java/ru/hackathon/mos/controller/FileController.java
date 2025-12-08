package ru.hackathon.mos.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import ru.hackathon.mos.entity.FileEntity;
import ru.hackathon.mos.repository.FileEntityRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
class FileController {

    private final FileEntityRepository fileRepo;

    @GetMapping("/api/files/{id}")
    public ResponseEntity<Resource> serveFile(@PathVariable Long id) {
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
}
