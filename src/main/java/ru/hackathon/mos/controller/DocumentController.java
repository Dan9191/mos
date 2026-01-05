package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hackathon.mos.dto.document.DocumentCreateRequest;
import ru.hackathon.mos.dto.document.DocumentResponse;
import ru.hackathon.mos.dto.document.DocumentSignRequest;
import ru.hackathon.mos.entity.Document;
import ru.hackathon.mos.entity.FileEntity;
import ru.hackathon.mos.exception.NotFoundException;
import ru.hackathon.mos.repository.DocumentRepository;
import ru.hackathon.mos.repository.FileEntityRepository;
import ru.hackathon.mos.service.DocumentService;
import ru.hackathon.mos.service.OrderService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/documents")
@RequiredArgsConstructor
@Tag(name = "Документы", description = "Управление документооборотом")
public class DocumentController {
    private final OrderService orderService;
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final FileEntityRepository fileRepo;

    @GetMapping
    @Operation(summary = "Получить список документов по заказу", description = "Вернет список документов для указанного заказа")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @PathVariable Long orderId) {

        var documents = documentService.getDocumentsByOrderId(orderId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}/download")
    @Operation(summary = "Скачать файл документа")
    public ResponseEntity<Resource> downloadDocumentFile(
            @PathVariable Long orderId,
            @PathVariable Long documentId) {

        Document document = documentRepository.findByIdAndOrderId(documentId, orderId)
                .orElseThrow(() -> new NotFoundException("Документ не найден"));

        if (document.getFileEntityId() == null) {
            throw new NotFoundException("Файл не найден");
        }

        FileEntity file = fileRepo.findById(document.getFileEntityId())
                .orElseThrow(() -> new NotFoundException("Файл не найден"));

        ByteArrayResource resource = new ByteArrayResource(file.getFileData());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodeFilename(file.getFilename()) + "\"")
                .body(resource);
    }

    @PostMapping
    @Operation(summary = "Загрузка документа", description = "Загрузить документ")
    public ResponseEntity<DocumentResponse> addDocuments(
            @PathVariable Long orderId,
            @RequestBody DocumentCreateRequest request
    ) {
        documentService.createDocument(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Получить документ по ID", description = "Возвращает детальную информацию о документе")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long orderId,
            @PathVariable Long documentId) {

        var document = documentService.getDocumentById(orderId, documentId);
        return ResponseEntity.ok(document);
    }

    @PostMapping("/{documentId}/sign")
    @Operation(summary = "Подписать документ", description = "Подписать документ электронной подписью")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> signDocument(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            @PathVariable Long documentId,
            @RequestBody DocumentSignRequest documentSignRequest) {
        UUID userId = UUID.fromString(jwt.getSubject());
        orderService.checkOrderAccess(orderId, userId);

        documentService.signDocument(orderId, documentId, documentSignRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
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