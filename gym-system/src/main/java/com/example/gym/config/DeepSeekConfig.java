package com.example.gym.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek AI 配置类，读取 application.yml 中 deepseek.* 配置项。
 * API Key 优先读环境变量 DEEPSEEK_API_KEY，未设置则回退到 yml 中的默认值。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {

    /** AI 功能总开关，false 时所有 AI 接口返回"AI 功能未开启" */
    private boolean enabled;

    /** DeepSeek API 密钥 */
    private String apiKey;

    /** API 基础地址，默认 https://api.deepseek.com */
    private String baseUrl;

    /** 模型名称，如 "deepseek-chat" */
    private String model;

    /** HTTP 请求超时时间（毫秒） */
    private int timeoutMs;

    /** 生成温度（0.0~2.0），越高越随机，越低越确定。默认 0.7 */
    private double temperature;
}
