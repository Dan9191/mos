package ru.hackathon.mos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hackathon.mos.config.AppPropertiesConfig;
import ru.hackathon.mos.dto.document.DocumentInformation;
import ru.hackathon.mos.dto.document.DocumentResponse;
import ru.hackathon.mos.dto.document.DocumentSignRequest;
import ru.hackathon.mos.entity.*;
import ru.hackathon.mos.exception.DocumentNotFoundException;
import ru.hackathon.mos.exception.OrderNotFoundException;
import ru.hackathon.mos.repository.DocumentRepository;
import ru.hackathon.mos.repository.DocumentTypeRepository;
import ru.hackathon.mos.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private FileEntityService fileEntityService;

    @Mock
    private AppPropertiesConfig appPropertiesConfig;

    @InjectMocks
    private DocumentService documentService;

    private Long orderId;
    private Long documentId;
    private Order testOrder;
    private Document testDocument;
    private DocumentType testDocumentType;
    private FileEntity testFileEntity;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        orderId = 1L;
        documentId = 10L;
        testDateTime = LocalDateTime.of(2024, 12, 10, 10, 30, 0);

        testOrder = Order.builder()
                .id(orderId)
                .build();

        testDocumentType = DocumentType.builder()
                .id(1L)
                .name(DocumentType.TypeName.CONTRACT)
                .build();

        testDocument = Document.builder()
                .id(documentId)
                .order(testOrder)
                .type(testDocumentType)
                .title("Договор поставки")
                .description("Описание договора")
                .createdAt(testDateTime)
                .fileEntityId(100L)
                .status("pending")
                .version(1)
                .build();

        testFileEntity = new FileEntity();
        testFileEntity.setId(100L);
        testFileEntity.setFilename("contract.pdf");
        testFileEntity.setFileData("PDF content".getBytes());
    }

    @Test
    void getDocumentsByOrderId_ShouldReturnDocumentsList() {
        // Arrange
        Document document2 = Document.builder()
                .id(11L)
                .order(testOrder)
                .type(testDocumentType)
                .title("Счет на оплату")
                .status("sent")
                .createdAt(testDateTime)
                .build();

        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(documentRepository.findByOrderId(orderId)).thenReturn(List.of(testDocument, document2));
        when(fileEntityService.getFileEntityById(100L)).thenReturn(testFileEntity);

        // Act
        List<DocumentInformation> result = documentService.getDocumentsByOrderId(orderId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result, hasSize(2));

        DocumentInformation firstDoc = result.get(0);
        assertThat(firstDoc.id(), is(documentId));
        assertThat(firstDoc.type(), is("CONTRACT"));
        assertThat(firstDoc.title(), is("Договор поставки"));
        assertThat(firstDoc.status(), is("pending"));

        DocumentInformation secondDoc = result.get(1);
        assertThat(secondDoc.id(), is(11L));
        assertThat(secondDoc.title(), is("Счет на оплату"));

        verify(orderRepository, times(1)).existsById(orderId);
        verify(documentRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    void getDocumentsByOrderId_ShouldThrowException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.existsById(orderId)).thenReturn(false);

        // Act & Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> documentService.getDocumentsByOrderId(orderId)
        );

        assertThat(exception.getMessage(), containsString("Заказ с ID " + orderId + " не найден"));
        verify(orderRepository, times(1)).existsById(orderId);
        verify(documentRepository, never()).findByOrderId(any());
    }

    @Test
    void getDocumentById_ShouldReturnDocument_WhenDocumentExists() {
        // Arrange
        when(documentRepository.findByIdAndOrderId(documentId, orderId))
                .thenReturn(Optional.of(testDocument));
        when(fileEntityService.getFileEntityById(100L)).thenReturn(testFileEntity);
        when(appPropertiesConfig.getBaseUrl()).thenReturn("http://localhost:8080");

        // Act
        DocumentResponse result = documentService.getDocumentById(orderId, documentId);

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.id(), is(documentId));
        assertThat(result.type(), is("CONTRACT"));
        assertThat(result.title(), is("Договор поставки"));
        assertThat(result.status(), is("pending"));
        assertThat(result.fileName(), is("contract.pdf"));
        assertThat(result.url(), is("http://localhost:8080/100"));
        verify(documentRepository, times(1)).findByIdAndOrderId(documentId, orderId);
    }

    @Test
    void getDocumentById_ShouldThrowException_WhenDocumentNotFound() {
        // Arrange
        when(documentRepository.findByIdAndOrderId(documentId, orderId))
                .thenReturn(Optional.empty());

        // Act & Assert
        DocumentNotFoundException exception = assertThrows(
                DocumentNotFoundException.class,
                () -> documentService.getDocumentById(orderId, documentId)
        );

        assertThat(exception.getMessage(), containsString("Документ с ID " + documentId + " не найден"));
        verify(documentRepository, times(1)).findByIdAndOrderId(documentId, orderId);
        verify(fileEntityService, never()).getFileEntityById(any());
    }

    @Test
    void signDocument_ShouldSignDocumentSuccessfully() {
        // Arrange
        DocumentSignRequest signRequest = new DocumentSignRequest("base64signature");

        when(documentRepository.findByIdAndOrderId(documentId, orderId))
                .thenReturn(Optional.of(testDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        // Act
        documentService.signDocument(orderId, documentId, signRequest);

        // Assert
        verify(documentRepository, times(1)).findByIdAndOrderId(documentId, orderId);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void signDocument_ShouldThrowException_WhenDocumentNotFound() {
        // Arrange
        DocumentSignRequest signRequest = new DocumentSignRequest("base64signature");

        when(documentRepository.findByIdAndOrderId(documentId, orderId))
                .thenReturn(Optional.empty());

        // Act & Assert
        DocumentNotFoundException exception = assertThrows(
                DocumentNotFoundException.class,
                () -> documentService.signDocument(orderId, documentId, signRequest)
        );

        assertThat(exception.getMessage(), containsString("Документ с ID " + documentId + " не найден"));
        verify(documentRepository, times(1)).findByIdAndOrderId(documentId, orderId);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void deleteDocument_ShouldDeleteDocumentSuccessfully() {
        // Arrange
        when(documentRepository.findByIdAndOrderId(documentId, orderId))
                .thenReturn(Optional.of(testDocument));
        doNothing().when(documentRepository).delete(testDocument);

        // Act
        documentService.deleteDocument(orderId, documentId);

        // Assert
        verify(documentRepository, times(1)).findByIdAndOrderId(documentId, orderId);
        verify(documentRepository, times(1)).delete(testDocument);
    }

    @Test
    void deleteDocument_ShouldThrowException_WhenDocumentNotFound() {
        // Arrange
        when(documentRepository.findByIdAndOrderId(documentId, orderId))
                .thenReturn(Optional.empty());

        // Act & Assert
        DocumentNotFoundException exception = assertThrows(
                DocumentNotFoundException.class,
                () -> documentService.deleteDocument(orderId, documentId)
        );

        assertThat(exception.getMessage(), containsString("Документ с ID " + documentId + " не найден"));
        verify(documentRepository, times(1)).findByIdAndOrderId(documentId, orderId);
        verify(documentRepository, never()).delete(any());
    }

    @Test
    void getDocumentsByStatus_ShouldReturnFilteredDocuments() {
        // Arrange
        when(documentRepository.findByOrderIdAndStatus(orderId, "pending"))
                .thenReturn(List.of(testDocument));
        when(fileEntityService.getFileEntityById(100L)).thenReturn(testFileEntity);
        when(appPropertiesConfig.getBaseUrl()).thenReturn("http://localhost:8080");

        // Act
        List<DocumentResponse> result = documentService.getDocumentsByStatus(orderId, "pending");

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result, hasSize(1));

        DocumentResponse doc = result.get(0);
        assertThat(doc.id(), is(documentId));
        assertThat(doc.status(), is("pending"));
        verify(documentRepository, times(1)).findByOrderIdAndStatus(orderId, "pending");
    }
}