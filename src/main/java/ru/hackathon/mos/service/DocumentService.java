package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.entity.Document;
import ru.hackathon.mos.entity.DocumentType;
import ru.hackathon.mos.entity.Order;
import ru.hackathon.mos.exception.DocumentNotFoundException;
import ru.hackathon.mos.exception.DocumentTypeNotFoundException;
import ru.hackathon.mos.exception.OrderNotFoundException;
import ru.hackathon.mos.repository.DocumentRepository;
import ru.hackathon.mos.repository.DocumentTypeRepository;
import ru.hackathon.mos.repository.OrderRepository;
import ru.hackathon.mos.dto.document.DocumentResponse;
import ru.hackathon.mos.dto.document.DocumentSignRequest;
import ru.hackathon.mos.dto.document.DocumentCreateRequest;
import ru.hackathon.mos.dto.document.DocumentUpdateRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final OrderRepository orderRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final FileEntityService fileEntityService;

    /**
     * Получить все документы по ID заказа
     */
    public List<DocumentResponse> getDocumentsByOrderId(Long orderId) {
        log.info("Получение документов для заказа с ID: {}", orderId);

        // Проверяем существование заказа
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException("Заказ с ID " + orderId + " не найден");
        }

        List<Document> documents = documentRepository.findByOrderId(orderId);

        return documents.stream()
                .map(this::convertToDocumentResponse)
                .toList();
    }

    /**
     * Получить конкретный документ по ID документа и ID заказа
     */
    public DocumentResponse getDocumentById(Long orderId, Long documentId) {
        log.info("Получение документа с ID: {} для заказа с ID: {}", documentId, orderId);

        Document document = documentRepository.findByIdAndOrderId(documentId, orderId)
                .orElseThrow(() -> new DocumentNotFoundException(
                        String.format("Документ с ID %s не найден для заказа с ID %s", documentId, orderId)
                ));

        return convertToDocumentResponse(document);
    }

    /**
     * Создать новый документ
     */
    @Transactional
    public DocumentResponse createDocument(Long orderId, DocumentCreateRequest request) {
        log.info("Создание документа для заказа с ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Заказ с ID " + orderId + " не найден"));

        DocumentType documentType = documentTypeRepository.findByName(DocumentType.TypeName.valueOf(request.type().toUpperCase()))
                .orElseThrow(() -> new DocumentTypeNotFoundException("Тип документа не найден: " + request.type()));

        Document document = Document.builder()
                .order(order)
                .type(documentType)
                .title(request.title())
                .description(request.description())
                .status("draft") // Статус по умолчанию
                .version(1)
                .build();

        // Если есть файл, сохраняем его
        if (request.fileContent() != null && !request.fileContent().isEmpty()) {
            Long fileEntityId = fileEntityService.saveFile(document);
            document.setFileEntityId(fileEntityId);
        }

        Document savedDocument = documentRepository.save(document);

        log.info("Документ создан с ID: {}", savedDocument.getId());
        return convertToDocumentResponse(savedDocument);
    }

    /**
     * Подписать документ электронной подписью
     */
    @Transactional
    public void signDocument(Long orderId, Long documentId, DocumentSignRequest request) {
        log.info("Подписание документа с ID: {} для заказа с ID: {}", documentId, orderId);

        Document document = documentRepository.findByIdAndOrderId(documentId, orderId)
                .orElseThrow(() -> new DocumentNotFoundException(
                        String.format("Документ с ID %s не найден для заказа с ID %s", documentId, orderId)
                ));

        // Проверяем, можно ли подписать документ
        validateDocumentForSigning(document);

        // Валидация подписи
        validateSignature(request.signature());

        // Создаем новую версию документа
        Document newVersion = createNewVersion(document);
        newVersion.setStatus("signed");

        Document updatedDocument = documentRepository.save(newVersion);

        log.info("Документ с ID {} успешно подписан. Новая версия: {}",
                documentId, updatedDocument.getVersion());
    }

    /**
     * Обновить статус документа
     */
    @Transactional
    public DocumentResponse updateDocumentStatus(Long orderId, Long documentId, String status) {
        log.info("Обновление статуса документа с ID: {} на статус: {}", documentId, status);

        Document document = documentRepository.findByIdAndOrderId(documentId, orderId)
                .orElseThrow(() -> new DocumentNotFoundException(
                        String.format("Документ с ID %s не найден для заказа с ID %s", documentId, orderId)
                ));

        validateStatusTransition(document.getStatus(), status);

        // Создаем новую версию при изменении статуса
        Document newVersion = createNewVersion(document);
        newVersion.setStatus(status);

        Document updatedDocument = documentRepository.save(newVersion);

        log.info("Статус документа с ID {} изменен на: {}", documentId, status);

        return convertToDocumentResponse(updatedDocument);
    }

    /**
     * Удалить документ (логическое удаление)
     */
    @Transactional
    public void deleteDocument(Long orderId, Long documentId) {
        log.info("Удаление документа с ID: {} для заказа с ID: {}", documentId, orderId);

        Document document = documentRepository.findByIdAndOrderId(documentId, orderId)
                .orElseThrow(() -> new DocumentNotFoundException(
                        String.format("Документ с ID %s не найден для заказа с ID %s", documentId, orderId)
                ));

        // Логическое удаление - меняем статус
        document.setStatus("deleted");
        documentRepository.save(document);

        // Если нужно физическое удаление:
        // documentRepository.delete(document);

        log.info("Документ с ID {} помечен как удаленный", documentId);
    }

    /**
     * Получить документы по статусу
     */
    public List<DocumentResponse> getDocumentsByStatus(Long orderId, String status) {
        log.info("Получение документов со статусом: {} для заказа с ID: {}", status, orderId);

        List<Document> documents = documentRepository.findByOrderIdAndStatus(orderId, status);

        return documents.stream()
                .map(this::convertToDocumentResponse)
                .toList();
    }

    /**
     * Получить историю версий документа
     */
    public List<DocumentResponse> getDocumentHistory(Long orderId, Long documentId) {
        log.info("Получение истории документа с ID: {} для заказа с ID: {}", documentId, orderId);

        Document currentDocument = documentRepository.findByIdAndOrderId(documentId, orderId)
                .orElseThrow(() -> new DocumentNotFoundException(
                        String.format("Документ с ID %s не найден для заказа с ID %s", documentId, orderId)
                ));

        // Получаем все документы с тем же order_id и title (идентификатор документа)
        List<Document> history = documentRepository.findHistoryByOrderIdAndTitle(
                orderId, currentDocument.getTitle());

        return history.stream()
                .map(this::convertToDocumentResponse)
                .toList();
    }

    /**
     * Поиск документов по типу
     */
    public List<DocumentResponse> getDocumentsByType(Long orderId, String typeName) {
        log.info("Поиск документов типа: {} для заказа с ID: {}", typeName, orderId);

        DocumentType.TypeName type;
        try {
            type = DocumentType.TypeName.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DocumentTypeNotFoundException("Неизвестный тип документа: " + typeName);
        }

        List<Document> documents = documentRepository.findByOrderIdAndTypeName(orderId, type);

        return documents.stream()
                .map(this::convertToDocumentResponse)
                .toList();
    }

    /**
     * Обновить документ
     */
    @Transactional
    public DocumentResponse updateDocument(Long orderId, Long documentId, DocumentUpdateRequest request) {
        log.info("Обновление документа с ID: {} для заказа с ID: {}", documentId, orderId);

        Document document = documentRepository.findByIdAndOrderId(documentId, orderId)
                .orElseThrow(() -> new DocumentNotFoundException(
                        String.format("Документ с ID %s не найден для заказа с ID %s", documentId, orderId)
                ));

        // Создаем новую версию
        Document newVersion = createNewVersion(document);

        if (request.title() != null) {
            newVersion.setTitle(request.title());
        }

        if (request.description() != null) {
            newVersion.setDescription(request.description());
        }

        if (request.status() != null) {
            newVersion.setStatus(request.status());
        }

        Document updatedDocument = documentRepository.save(newVersion);

        log.info("Документ с ID {} обновлен. Новая версия: {}",
                documentId, updatedDocument.getVersion());

        return convertToDocumentResponse(updatedDocument);
    }

    /**
     * Создать новую версию документа
     */
    private Document createNewVersion(Document original) {
        return Document.builder()
                .order(original.getOrder())
                .type(original.getType())
                .title(original.getTitle())
                .description(original.getDescription())
                .createdAt(LocalDateTime.now())
                .fileEntityId(original.getFileEntityId())
                .status(original.getStatus())
                .version(original.getVersion() + 1)
                .build();
    }

    /**
     * Конвертация Document в DocumentResponse (детальная)
     */
    private DocumentResponse convertToDocumentResponse(Document document) {
        var fileEntityId = document.getFileEntityId();
        var fileEntity = fileEntityService.getFileEntityById(fileEntityId);
        var fileName = fileEntity.getFilename();
//        var fileNameParts = fileName.split("\\.");
//        var fileExtension = fileNameParts[fileNameParts.length - 1].toLowerCase();
        var fileContent = Arrays.toString(fileEntity.getFileData());

        return new DocumentResponse(
                document.getId(),
                document.getType().getName().getValue(),
                document.getTitle(),
                document.getStatus(),
                document.getCreatedAt(),
                fileName, //тут лучше возвращать полное название документа, а не только его расширение
                fileContent
        );
    }

    /**
     * Валидация документа для подписания
     */
    private void validateDocumentForSigning(Document document) {
        if (!"sent".equals(document.getStatus()) && !"pending".equals(document.getStatus())) {
            throw new IllegalStateException(
                    String.format("Документ должен быть в статусе 'sent' или 'pending'. Текущий статус: %s",
                            document.getStatus())
            );
        }

        if (document.getFileEntityId() == null) {
            throw new IllegalStateException("Документ не содержит файл для подписания");
        }
    }

    /**
     * Валидация перехода статусов
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Допустимые переходы статусов
        Map<String, List<String>> allowedTransitions = Map.of(
                "draft", List.of("sent", "pending", "deleted"),
                "sent", List.of("pending", "signed", "rejected"),
                "pending", List.of("signed", "rejected"),
                "signed", List.of("approved", "rejected"),
                "approved", List.of(),
                "rejected", List.of("draft", "sent"),
                "deleted", List.of()
        );

        List<String> allowed = allowedTransitions.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("Недопустимый переход статуса: с '%s' на '%s'",
                            currentStatus, newStatus)
            );
        }
    }

    /**
     * Валидация электронной подписи
     */
    private void validateSignature(String signature) {
        if (signature == null || signature.trim().isEmpty()) {
            throw new IllegalArgumentException("Подпись не может быть пустой");
        }

        // Проверяем base64 формат
        try {
            Base64.getDecoder().decode(signature);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректный формат подписи (ожидается base64)");
        }
    }
}