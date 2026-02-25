package ru.document.service.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.document.service.controller.dto.document.DocumentResponse;
import ru.document.service.controller.dto.history.DocumentWithHistoryResponse;
import ru.document.service.controller.dto.history.HistoryResponse;
import ru.document.service.entity.Document;
import ru.document.service.entity.History;
import ru.document.service.enums.ActionType;
import ru.document.service.enums.DocumentStatus;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-25T18:30:23+1000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class DocumentMapperImpl implements DocumentMapper {

    @Override
    public DocumentResponse toResponse(Document entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String number = null;
        String author = null;
        DocumentStatus status = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = entity.getId();
        number = entity.getNumber();
        author = entity.getAuthor();
        status = entity.getStatus();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        DocumentResponse documentResponse = new DocumentResponse( id, number, author, status, createdAt, updatedAt );

        return documentResponse;
    }

    @Override
    public DocumentWithHistoryResponse toResponseWithHistory(Document entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String number = null;
        String author = null;
        String title = null;
        DocumentStatus status = null;
        LocalDateTime createdAt = null;
        List<HistoryResponse> history = null;

        id = entity.getId();
        number = entity.getNumber();
        author = entity.getAuthor();
        title = entity.getTitle();
        status = entity.getStatus();
        createdAt = entity.getCreatedAt();
        history = historyListToHistoryResponseList( entity.getHistory() );

        DocumentWithHistoryResponse documentWithHistoryResponse = new DocumentWithHistoryResponse( id, number, author, title, status, createdAt, history );

        return documentWithHistoryResponse;
    }

    protected HistoryResponse historyToHistoryResponse(History history) {
        if ( history == null ) {
            return null;
        }

        String initiator = null;
        ActionType actionType = null;
        String comment = null;
        LocalDateTime createdAt = null;

        initiator = history.getInitiator();
        actionType = history.getActionType();
        comment = history.getComment();
        createdAt = history.getCreatedAt();

        HistoryResponse historyResponse = new HistoryResponse( initiator, actionType, comment, createdAt );

        return historyResponse;
    }

    protected List<HistoryResponse> historyListToHistoryResponseList(List<History> list) {
        if ( list == null ) {
            return null;
        }

        List<HistoryResponse> list1 = new ArrayList<HistoryResponse>( list.size() );
        for ( History history : list ) {
            list1.add( historyToHistoryResponse( history ) );
        }

        return list1;
    }
}
