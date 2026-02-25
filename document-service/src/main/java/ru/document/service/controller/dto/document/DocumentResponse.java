package ru.document.service.controller.dto.document;

import ru.document.service.enums.DocumentStatus;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String number,
        String author,
        DocumentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
