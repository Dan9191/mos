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
import ru.hackathon.mos.service.DocumentService;
import ru.hackathon.mos.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/documents")
@RequiredArgsConstructor
@Tag(name = "Документы", description = "Управление документооборотом")
public class DocumentController {
    private final OrderService orderService;
    private final DocumentService documentService;

    @GetMapping
    @Operation(summary = "Получить список документов по заказу", description = "Вернет список документов для указанного заказа")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {
        UUID userId = UUID.fromString(jwt.getSubject());
//        orderService.checkOrderAccess(orderId, userId);

        var documents = documentService.getDocumentsByOrderId(orderId);
        return ResponseEntity.ok(documents);
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
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            @PathVariable Long documentId) {
        UUID userId = UUID.fromString(jwt.getSubject());
//        orderService.checkOrderAccess(orderId, userId);

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
}