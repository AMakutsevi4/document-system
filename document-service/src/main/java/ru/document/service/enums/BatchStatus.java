package ru.document.service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BatchStatus {
    SUCCESS("Успешно"),
    NOT_FOUND("Не найден"),
    CONFLICT("Конфликт"),
    REGISTRY_ERROR("Ошибка регистрации");

    private final String value;
}
