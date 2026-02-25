package ru.document.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.document.service.entity.Document;
import ru.document.service.entity.History;
import ru.document.service.enums.ActionType;
import ru.document.service.reposiory.DocumentHistoryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl {

    private final DocumentHistoryRepository historyRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void logAction(Document document, String initiator, ActionType action, String comment) {
        log.debug("История изменена: документ Id={}, действие ={}, инициатор={}",
                document.getId(), action, initiator);
        History history = createNew(document, initiator, action, comment);
        historyRepository.save(history);
    }

    public static History createNew(Document document, String initiator, ActionType actionType, String comment) {
        History history = new History();
        history.setDocument(document);
        history.setInitiator(initiator);
        history.setActionType(actionType);
        history.setComment(comment);
        return history;
    }
}
