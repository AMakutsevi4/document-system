package ru.document.service.sheduller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.document.service.enums.DocumentStatus;
import ru.document.service.service.impl.DocumentServiceImpl;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentBatchWorkers {

    private final DocumentServiceImpl documentService;

    @Value("${service.batch-size:500}")
    private int batchSize;

    @Scheduled(cron = "0 */2 * * * ?")
    public void runSubmitWorker() {
        List<Long> ids = documentService.getIdsByStatus(DocumentStatus.DRAFT, batchSize);
        if (!ids.isEmpty()) {
            log.info("SUBMIT-worker: Нашел {} черновиков, отправляю на согласование...", ids.size());
            documentService.submitBatch(ids, "SUBMIT_ROBOT", "Автоматическая отправка");
        }
    }

    @Scheduled(cron = "0 */3 * * * ?")
    public void runApproveWorker() {
        List<Long> ids = documentService.getIdsByStatus(DocumentStatus.SUBMITTED, batchSize);
        if (!ids.isEmpty()) {
            log.info("APPROVE-worker: Нашел {} на утверждение, обрабатываю...", ids.size());
            documentService.approveBatch(ids, "APPROVE_ROBOT", "Авто-утверждение");
        }
    }
}