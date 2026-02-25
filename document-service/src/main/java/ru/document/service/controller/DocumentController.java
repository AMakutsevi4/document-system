package ru.document.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.document.service.controller.dto.currency.ConcurrencyResponse;
import ru.document.service.controller.dto.document.DocumentRequest;
import ru.document.service.controller.dto.document.DocumentResponse;
import ru.document.service.controller.dto.history.DocumentWithHistoryResponse;
import ru.document.service.controller.dto.submitAndApprove.BatchResultItem;
import ru.document.service.controller.dto.submitAndApprove.BatchSubmitAndResultRequest;
import ru.document.service.enums.DocumentStatus;
import ru.document.service.service.impl.DocumentServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentServiceImpl documentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse save(@RequestBody @Valid DocumentRequest documentRequest) {
        return documentService.create(documentRequest);
    }

    @GetMapping("/{id}")
    public DocumentWithHistoryResponse getOne(@PathVariable Long id) {
        return documentService.getById(id);
    }

    @GetMapping
    public Page<DocumentResponse> getBatch(
            @RequestParam List<Long> ids,
            Pageable pageable) {
        return documentService.getByIds(ids, pageable);
    }

    @GetMapping("/search")
    public Page<DocumentResponse> search(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        return documentService.search(status, author, from, to, pageable);
    }

    @PostMapping("/submit")
    public List<BatchResultItem> submitBatch(
            @RequestBody @Valid BatchSubmitAndResultRequest request) {
        return documentService.submitBatch(
                request.ids(),
                request.initiator(),
                request.comment()
        );
    }

    @PostMapping("/approve")
    public List<BatchResultItem> approveBatch(
            @RequestBody @Valid BatchSubmitAndResultRequest request) {
        return documentService.approveBatch(
                request.ids(),
                request.initiator(),
                request.comment()
        );
    }

    @PostMapping("/test-concurrency/{id}")
    public ConcurrencyResponse testConcurrency(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int threads,
            @RequestParam(defaultValue = "10") int attempts) {
        return documentService.runConcurrencyTest(id, threads, attempts);
    }
}