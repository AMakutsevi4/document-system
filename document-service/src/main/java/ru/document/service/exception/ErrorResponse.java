package ru.document.service.exception;

public record ErrorResponse(
        String status,
        String message
) {
}
