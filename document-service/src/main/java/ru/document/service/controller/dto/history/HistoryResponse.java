package ru.document.service.controller.dto.history;

import ru.document.service.enums.ActionType;

import java.time.LocalDateTime;

public record HistoryResponse(
        String initiator,
        ActionType actionType,
        String comment,
        LocalDateTime createdAt
) {
}