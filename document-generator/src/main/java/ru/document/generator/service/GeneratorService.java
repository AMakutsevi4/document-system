package ru.document.generator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeneratorService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Retryable(retryFor = RestClientException.class, maxAttempts = 3)
    public void sendDocument(String url, Map<String, String> request, int docIndex) {
        log.debug("Попытка отправки документа #{}", docIndex);
        restTemplate.postForEntity(url, request, String.class);
    }

    @Recover
    public void recover(RestClientException e, String url, Map<String, String> request, int docIndex) {
        log.error("Не удалось отправить документ #{}", docIndex);
        throw new RuntimeException("Сервис недоступен " + url);
    }
}
