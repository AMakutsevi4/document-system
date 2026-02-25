package ru.document.generator.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.document.generator.config.GeneratorProperties;
import ru.document.generator.service.GeneratorService;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentGeneratorRunner implements CommandLineRunner {

    private final GeneratorProperties properties;
    private final GeneratorService generatorService;

    @Override
    public void run(String... args) {
        log.info("Запуск генерации {} документов...", properties.getSize());
        long startTime = System.currentTimeMillis();
        for (int i = 1; i <= properties.getSize(); i++) {
            try {
                Map<String, String> request = Map.of(
                        "author", "Author demo-generator",
                        "title", "Document " + i,
                        "initiator", "Generator-Tool"
                );
                generatorService.sendDocument(properties.getApiUrl(), request, i);
                if (i % 100 == 0) {
                    log.info("Прогресс: {}/{}", i, properties.getSize());
                }
            } catch (RuntimeException e) {
                log.error("Генерация прервана: {}", e.getMessage());
                break;
            }
        }
        log.info("Завершено за {} мс", System.currentTimeMillis() - startTime);
    }
}
