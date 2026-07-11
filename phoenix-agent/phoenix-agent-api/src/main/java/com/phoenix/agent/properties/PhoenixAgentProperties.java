package com.phoenix.agent.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("spring.ai.phoenix")
public class PhoenixAgentProperties {
    private String modelPath;
    private String skillPath;
    private Embedding embedding;
    @Data
    public static class Embedding {
        private String baseUrl;
        private String apiKey;
        private String model;
        private Integer dimensions = 512;
    }
}


