package ru.workflow.generator.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.workflow.generator.config.GeneratorProperties;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentGeneratorRunner implements CommandLineRunner {

    private final GeneratorProperties properties;

    @Override
    public void run(String... args) {
        RestTemplate restTemplate = new RestTemplate();
        log.info("Запуск генерации {} документов через API: {}", properties.getSize(), properties.getApiUrl());

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= properties.getSize(); i++) {
            try {
                Map<String, String> request = Map.of(
                        "author", "Author demo",
                        "title", "Document " + i,
                        "initiator", "Generator-Tool"
                );
                restTemplate.postForEntity(properties.getApiUrl(), request, String.class);

                if (i % 10 == 0) {
                    log.info("Прогресс: {}/{} создано", i, properties.getSize());
                }
            } catch (Exception e) {
                log.error("Ошибка на документе {}: {}", i, e.getMessage());
            }
        }
        log.info("Готово! Затрачено времени: {} мс", System.currentTimeMillis() - startTime);
    }
}
