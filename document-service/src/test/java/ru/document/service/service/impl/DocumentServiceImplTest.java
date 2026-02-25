package ru.document.service.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.document.service.controller.dto.document.DocumentRequest;
import ru.document.service.controller.dto.document.DocumentResponse;
import ru.document.service.controller.dto.submitAndApprove.BatchResultItem;
import ru.document.service.entity.Document;
import ru.document.service.enums.BatchStatus;
import ru.document.service.enums.DocumentStatus;
import ru.document.service.reposiory.ApprovalRegistryRepository;
import ru.document.service.reposiory.DocumentHistoryRepository;
import ru.document.service.reposiory.DocumentRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@DisplayName("Тесты бизнес-логики документа")
class DocumentServiceImplTest {

    @Autowired
    private DocumentServiceImpl documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentHistoryRepository historyRepository;

    @MockBean
    private ApprovalRegistryRepository registryRepository;

    @BeforeEach
    void setUp() {
        registryRepository.deleteAllInBatch();
        historyRepository.deleteAllInBatch();
        documentRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Создание одного документа")
    void create_happyPath() {
        DocumentRequest request = new DocumentRequest("Ivanov", "Title 1", "User-1");

        DocumentResponse response = documentService.create(request);

        assertNotNull(response.id());
        assertTrue(documentRepository.existsById(response.id()));
    }

    @Test
    @DisplayName("Пакетный submit")
    void submitBatch_success() {
        Long id1 = createTestDocument("DOC-1", DocumentStatus.DRAFT);
        Long id2 = createTestDocument("DOC-2", DocumentStatus.DRAFT);

        List<BatchResultItem> results = documentService.submitBatch(List.of(id1, id2), "Robot", "Comment");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.status() == BatchStatus.SUCCESS));
        assertEquals(DocumentStatus.SUBMITTED, documentRepository.findById(id1).orElseThrow().getStatus());
        assertEquals(DocumentStatus.SUBMITTED, documentRepository.findById(id2).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Пакетный approve")
    void approveBatch_partialResults() {
        Long idSuccess = createTestDocument("DOC-OK", DocumentStatus.SUBMITTED);
        Long idConflict = createTestDocument("DOC-BAD", DocumentStatus.DRAFT);
        Long idNotFound = 444L;

        List<BatchResultItem> results = documentService.approveBatch(
                List.of(idSuccess, idConflict, idNotFound), "Director", "Final");

        assertEquals(3, results.size());
        assertTrue(results.stream().anyMatch(r -> r.id().equals(idSuccess) && r.status() == BatchStatus.SUCCESS));
        assertTrue(results.stream().anyMatch(r -> r.id().equals(idConflict) && r.status() == BatchStatus.CONFLICT));
        assertTrue(results.stream().anyMatch(r -> r.id().equals(idNotFound) && r.status() == BatchStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Откат approve")
    void approve_rollbackThenRegistryError() {
        Long id = createTestDocument("DOC-ROLLBACK", DocumentStatus.SUBMITTED);
        doThrow(new RuntimeException()).when(registryRepository).save(any());
        documentService.approveBatch(List.of(id), "User", "Test");
        Document finalDoc = documentRepository.findById(id).orElseThrow();

        assertEquals(DocumentStatus.SUBMITTED, finalDoc.getStatus());
    }

    private Long createTestDocument(String number, DocumentStatus status) {
        Document doc = new Document();
        doc.setNumber(number);
        doc.setAuthor("Test Author");
        doc.setTitle("Test Title");
        doc.setStatus(status);
        return documentRepository.save(doc).getId();
    }
}