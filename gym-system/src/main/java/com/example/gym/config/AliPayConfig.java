package com.example.gym.config;

import com.alipay.api.AlipayConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝支付配置类，读取 application.yml 中 alipay.* 配置项，
 * 创建 AlipayConfig Bean 供 AlipayController 使用。
 * <p>
 * 当前使用支付宝沙箱环境（openapi-sandbox.dl.alipaydev.com），
 * 生产环境需修改 serverUrl 和应用公钥。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AliPayConfig {

    /** 支付宝应用 ID（沙箱环境） */
    private String appId;

    /** 应用私钥，用于签名请求 */
    private String appPrivateKey;

    /** 支付宝公钥，用于验签回调 */
    private String alipayPublicKey;

    /** 支付宝异步通知回调地址（本地开发环境不可达，依赖同步回调） */
    private String notifyUrl;

    /** 支付宝同步跳转地址（支付完成后浏览器跳转） */
    private String returnUrl;

    @Bean
    public AlipayConfig alipayConfig() {
        AlipayConfig config = new AlipayConfig();
        config.setAppId(appId);
        config.setPrivateKey(appPrivateKey);
        config.setAlipayPublicKey(alipayPublicKey);
        // 沙箱网关，生产环境改为 https://openapi.alipay.com/gateway.do
        config.setServerUrl("https://openapi-sandbox.dl.alipaydev.com/gateway.do");
        config.setFormat("json");
        config.setCharset("UTF-8");
        config.setSignType("RSA2");
        return config;
    }
}