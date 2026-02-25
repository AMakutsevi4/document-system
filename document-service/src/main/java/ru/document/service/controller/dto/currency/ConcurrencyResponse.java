package ru.document.service.controller.dto.currency;

import ru.document.service.enums.DocumentStatus;

public record ConcurrencyResponse(
        Long documentId,
        long successCount,
        long conflictCount,
        long errorCount,
        DocumentStatus finalStatus
) {
}
