package ru.document.service.controller.dto.submitAndApprove;

import ru.document.service.enums.BatchStatus;

public record BatchResultItem(
        Long id,
        BatchStatus status
) {
}
