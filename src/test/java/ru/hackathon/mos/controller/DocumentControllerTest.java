package ru.hackathon.mos.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.hackathon.mos.dto.document.DocumentInformation;
import ru.hackathon.mos.dto.document.DocumentResponse;
import ru.hackathon.mos.service.DocumentService;
import ru.hackathon.mos.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    private final LocalDateTime testDateTime = LocalDateTime.of(2024, 12, 9, 10, 30, 0);

    @Test
    void getDocuments_ShouldReturnDocumentsList() throws Exception {

        Long orderId = 1L;

        DocumentInformation doc1 = new DocumentInformation(
                1L,
                "ДОГОВОР",
                "Договор поставки №123",
                "DRAFT",
                testDateTime,
                "dogovor.pdf"
        );

        DocumentInformation doc2 = new DocumentInformation(
                2L,
                "СЧЕТ",
                "Счет на оплату",
                "SENT",
                testDateTime.minusDays(1),
                "schet.pdf"
        );

        List<DocumentInformation> documents = List.of(doc1, doc2);

        when(documentService.getDocumentsByOrderId(orderId)).thenReturn(documents);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();

        mockMvc.perform(get("/api/orders/{orderId}/documents", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].type", is("ДОГОВОР")))
                .andExpect(jsonPath("$[0].title", is("Договор поставки №123")))
                .andExpect(jsonPath("$[0].status", is("DRAFT")))
                .andExpect(jsonPath("$[0].fileName", is("dogovor.pdf")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].type", is("СЧЕТ")))
                .andExpect(jsonPath("$[1].status", is("SENT")));
    }

    @Test
    void getDocuments_ShouldReturnEmptyList_WhenNoDocuments() throws Exception {

        Long orderId = 1L;
        when(documentService.getDocumentsByOrderId(orderId)).thenReturn(List.of());

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();

        mockMvc.perform(get("/api/orders/{orderId}/documents", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getDocument_ShouldReturnDocument() throws Exception {

        Long orderId = 1L;
        Long documentId = 1L;

        DocumentResponse documentResponse = new DocumentResponse(
                documentId,
                "CONTRACT",
                "Договор поставки",
                "DRAFT",
                testDateTime,
                "document.pdf",
                "Содержимое документа",
                "http://example.com/document.pdf"
        );

        when(documentService.getDocumentById(orderId, documentId)).thenReturn(documentResponse);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();

        mockMvc.perform(get("/api/orders/{orderId}/documents/{documentId}", orderId, documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(documentId.intValue())))
                .andExpect(jsonPath("$.type", is("CONTRACT")))
                .andExpect(jsonPath("$.title", is("Договор поставки")))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.fileName", is("document.pdf")))
                .andExpect(jsonPath("$.content", is("Содержимое документа")))
                .andExpect(jsonPath("$.url", is("http://example.com/document.pdf")));
    }

}