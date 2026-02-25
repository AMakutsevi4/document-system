package ru.document.service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.document.service.controller.dto.currency.ConcurrencyResponse;
import ru.document.service.controller.dto.document.DocumentRequest;
import ru.document.service.controller.dto.document.DocumentResponse;
import ru.document.service.controller.dto.history.DocumentWithHistoryResponse;
import ru.document.service.controller.dto.submitAndApprove.BatchResultItem;
import ru.document.service.entity.Document;
import ru.document.service.enums.ActionType;
import ru.document.service.enums.BatchStatus;
import ru.document.service.enums.DocumentStatus;
import ru.document.service.exception.RegistryException;
import ru.document.service.mapper.DocumentMapper;
import ru.document.service.reposiory.DocumentRepository;
import ru.document.service.specification.DocumentSpecification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl {

    private final DocumentRepository documentRepository;
    private final DocumentStatusServiceImpl statusService;
    private final AuditServiceImpl auditService;
    private final DocumentMapper mapper;

    @Transactional
    public DocumentResponse create(DocumentRequest request) {
        log.info("Создание нового документа: автор={}, заголовок={}", request.author(), request.title());

        Document document = createNew(generateUniqueNumber(), request.author(), request.title());
        Document savedDoc = documentRepository.save(document);

        auditService.logAction(savedDoc,
                request.initiator(),
                ActionType.CREATE,
                "Initial creation");

        return mapper.toResponse(savedDoc);
    }

    @Transactional(readOnly = true)
    public DocumentWithHistoryResponse getById(Long id) {
        return documentRepository.findWithHistoryById(id)
                .map(mapper::toResponseWithHistory)
                .orElseThrow(() -> new EntityNotFoundException("Документ не найден с id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> getByIds(List<Long> ids, Pageable pageable) {
        log.info("Загрузка документов пакетом: количество={}", ids.size());
        return documentRepository.findAllByIdIn(ids, pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<Long> getIdsByStatus(DocumentStatus status, int limit) {
        return documentRepository.findIdsByStatus(status, Limit.of(limit));
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> search(DocumentStatus status, String author,
                                         LocalDateTime from, LocalDateTime to, Pageable pageable) {

        Specification<Document> spec = Specification.allOf(
                DocumentSpecification.hasStatus(status),
                DocumentSpecification.hasAuthor(author),
                DocumentSpecification.createdAtBetween(from, to)
        );

        return documentRepository.findAll(spec, pageable)
                .map(mapper::toResponse);
    }

    public List<BatchResultItem> submitBatch(List<Long> ids, String initiator, String comment) {
        log.info("Массовая отправка документов: количество={}", ids.size());
        List<BatchResultItem> results = new ArrayList<>();
        for (Long id : ids) {
            try {
                statusService.submit(id, initiator, comment);
                results.add(new BatchResultItem(id, BatchStatus.SUCCESS));
            } catch (EntityNotFoundException e) {
                log.warn("Документ ID={} не найден", id);
                results.add(new BatchResultItem(id, BatchStatus.NOT_FOUND));
            } catch (IllegalStateException e) {
                log.warn("Конфликт для документа ID={}: {}", id, e.getMessage());
                results.add(new BatchResultItem(id, BatchStatus.CONFLICT));
            } catch (Exception e) {
                log.error("Непредвиденная ошибка при отправке документа ID={}", id, e);
                results.add(new BatchResultItem(id, BatchStatus.CONFLICT));
            }
        }
        return results;
    }

    public List<BatchResultItem> approveBatch(List<Long> ids, String initiator, String comment) {
        log.info("Массовое утверждение документов: количество={}, инициатор={}", ids.size(), initiator);
        List<BatchResultItem> results = new ArrayList<>();
        for (Long id : ids) {
            try {
                statusService.approve(id, initiator, comment);
                results.add(new BatchResultItem(id, BatchStatus.SUCCESS));
            } catch (EntityNotFoundException e) {
                log.warn("Документ ID={} не найден", id);
                results.add(new BatchResultItem(id, BatchStatus.NOT_FOUND));
            } catch (IllegalStateException e) {
                log.warn("Конфликт для документа ID={}: {}", id, e.getMessage());
                results.add(new BatchResultItem(id, BatchStatus.CONFLICT));
            } catch (RegistryException e) {
                log.error("Ошибка реестра для документа ID={}: {}", id, e.getMessage());
                results.add(new BatchResultItem(id, BatchStatus.REGISTRY_ERROR));
            } catch (Exception e) {
                log.error("Непредвиденная ошибка при утверждении документа ID={}: ", id, e);
                results.add(new BatchResultItem(id, BatchStatus.CONFLICT));
            }
        }
        return results;
    }

    public ConcurrencyResponse runConcurrencyTest(Long id, int threads, int attempts) {
        log.info("Запуск теста конкурентности: ID={}, попыток={}, потоков={}", id, attempts, threads);

        AtomicLong success = new AtomicLong();
        AtomicLong conflict = new AtomicLong();
        AtomicLong errors = new AtomicLong();

        CountDownLatch startThread = new CountDownLatch(1);
        CountDownLatch finishThread = new CountDownLatch(attempts);

        try (var executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < attempts; i++) {
                executor.submit(() -> {
                    try {
                        startThread.await();
                        statusService.approve(id, "Concurrent-User", "Test");
                        success.incrementAndGet();
                    } catch (ObjectOptimisticLockingFailureException | IllegalStateException e) {
                        conflict.incrementAndGet();
                    } catch (Exception e) {
                        log.error("Техническая ошибка: {}", e.getMessage());
                        errors.incrementAndGet();
                    } finally {
                        finishThread.countDown();
                    }
                });
            }

            startThread.countDown();
            finishThread.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        DocumentStatus finalStatus = documentRepository.findById(id)
                .map(Document::getStatus)
                .orElse(null);

        return new ConcurrencyResponse(id, success.get(), conflict.get(), errors.get(), finalStatus);
    }

    public static Document createNew(String number, String author, String title) {
        Document doc = new Document();
        doc.setNumber(number);
        doc.setAuthor(author);
        doc.setTitle(title);
        doc.setStatus(DocumentStatus.DRAFT);
        return doc;
    }

    private String generateUniqueNumber() {
        return "D-"
                + System.currentTimeMillis()
                + "-"
                + UUID.randomUUID().toString().substring(0, 4)
                .toUpperCase();
    }
}