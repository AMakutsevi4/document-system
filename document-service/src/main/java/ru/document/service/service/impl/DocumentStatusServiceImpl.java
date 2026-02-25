package ru.document.service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.document.service.entity.ApprovalRegistry;
import ru.document.service.entity.Document;
import ru.document.service.enums.ActionType;
import ru.document.service.enums.DocumentStatus;
import ru.document.service.exception.RegistryException;
import ru.document.service.reposiory.ApprovalRegistryRepository;
import ru.document.service.reposiory.DocumentRepository;

@Service
@RequiredArgsConstructor
public class DocumentStatusServiceImpl {

    private final DocumentRepository documentRepository;
    private final ApprovalRegistryRepository approvalRegistryRepository;
    private final AuditServiceImpl auditService;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void submit(Long id, String initiator, String comment) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Документ не найден: " + id));

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new IllegalStateException("Неккоректная смена состояния");
        }
        document.setStatus(DocumentStatus.SUBMITTED);
        documentRepository.save(document);
        auditService.logAction(document, initiator, ActionType.SUBMIT, comment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approve(Long id, String initiator, String comment) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Документ не найден: " + id));

        if (document.getStatus() != DocumentStatus.SUBMITTED) {
            throw new IllegalStateException("Документ не находится в статусе SUBMITTED");
        }
        document.setStatus(DocumentStatus.APPROVED);
        documentRepository.save(document);
        auditService.logAction(document, initiator, ActionType.APPROVE, comment);
        try {
            ApprovalRegistry registry = createNew(document);
            approvalRegistryRepository.save(registry);
        } catch (Exception e) {
            throw new RegistryException("Не удалось создать запись в реестре.", e);
        }
    }

    public static ApprovalRegistry createNew(Document document) {
        ApprovalRegistry registry = new ApprovalRegistry();
        registry.setDocument(document);
        return registry;
    }
}
