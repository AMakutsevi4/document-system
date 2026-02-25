package ru.document.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "generator")
public class GeneratorProperties {

    private int size = 10;

    private String apiUrl;
}
