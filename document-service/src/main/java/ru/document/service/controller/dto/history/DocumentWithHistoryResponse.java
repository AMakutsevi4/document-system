package ru.document.service.controller.dto.history;

import ru.document.service.enums.DocumentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentWithHistoryResponse(
        Long id,
        String number,
        String author,
        String title,
        DocumentStatus status,
        LocalDateTime createdAt,
        List<HistoryResponse> history
) {
}
