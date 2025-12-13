package ru.hackathon.mos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hackathon.mos.dto.document.DocumentResponse;
import ru.hackathon.mos.dto.document.DocumentSignRequest;
import ru.hackathon.mos.service.DocumentService;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/documents")
@RequiredArgsConstructor
@Tag(name = "Документы", description = "Управление документооборотом")
public class DocumentController {
    private final DocumentService documentService;

    @GetMapping
    @Operation(summary = "Получить список документов по заказу", description = "Вернет список документов для указанного заказа")
    public ResponseEntity<List<DocumentResponse>> getDocuments(@PathVariable Long orderId) {
        var documents = documentService.getDocumentsByOrderId(orderId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Получить документ по ID", description = "Возвращает детальную информацию о документе")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long orderId, @PathVariable Long documentId) {
        var document = documentService.getDocumentById(orderId, documentId);
        return ResponseEntity.ok(document);
    }

    @PostMapping("/{documentId}/sign")
    @Operation(summary = "Подписать документ", description = "Подписать документ электронной подписью")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> signDocument(
            @PathVariable Long orderId,
            @PathVariable Long documentId,
            @RequestBody DocumentSignRequest documentSignRequest) {

        documentService.signDocument(orderId, documentId, documentSignRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}