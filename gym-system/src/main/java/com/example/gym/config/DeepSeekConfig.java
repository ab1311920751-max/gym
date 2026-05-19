package com.example.gym.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {

    private boolean enabled;
    private String apiKey;
    private String baseUrl;
    private String model;
    private int timeoutMs;
    private double temperature;
}
