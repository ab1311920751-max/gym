package com.example.gym.config;

import com.alipay.api.AlipayConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AliPayConfig {

    private String appId;
    private String appPrivateKey; // 应用私钥
    private String alipayPublicKey; // 支付宝公钥
    private String notifyUrl;
    private String returnUrl;

    @Bean
    public AlipayConfig alipayConfig() {
        AlipayConfig config = new AlipayConfig();
        config.setAppId(appId);
        config.setPrivateKey(appPrivateKey);
        config.setAlipayPublicKey(alipayPublicKey);
        config.setServerUrl("https://openapi-sandbox.dl.alipaydev.com/gateway.do"); // 沙箱网关
        config.setFormat("json");
        config.setCharset("UTF-8");
        config.setSignType("RSA2");
        return config;
    }
}