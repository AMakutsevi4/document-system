package ru.document.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.document.service.controller.dto.document.DocumentResponse;
import ru.document.service.controller.dto.history.DocumentWithHistoryResponse;
import ru.document.service.entity.Document;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DocumentMapper {

    DocumentResponse toResponse(Document entity);

    DocumentWithHistoryResponse toResponseWithHistory(Document entity);
}
