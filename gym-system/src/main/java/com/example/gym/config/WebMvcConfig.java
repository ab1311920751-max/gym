package com.example.gym.config;

import com.example.gym.common.auth.CurrentUserIdResolver;
import com.example.gym.common.auth.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 配置类，实现 WebMvcConfigurer，注册三项配置：
 * <ol>
 *   <li>CORS 跨域：允许所有来源和方法，携带 Cookie，maxAge=3600</li>
 *   <li>JWT 鉴权拦截器：排除 /auth/、/alipay/、/banner/list、/error 等白名单路径</li>
 *   <li>@CurrentUserId 参数解析器：从 RequestAttribute 注入当前登录用户 ID</li>
 * </ol>
 * <p>
 * 鉴权链路：addInterceptors() → JwtInterceptor.preHandle() → JwtSupport.parse() 验签 → Controller 参数注入。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final CurrentUserIdResolver currentUserIdResolver;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",          // 登录/注册
                        "/alipay/pay",       // 支付宝拉起页面（GET，无 token 上下文）
                        "/alipay/return",    // 支付宝同步回调
                        "/alipay/notify",    // 支付宝异步通知
                        "/banner/list",      // 首页轮播图（未登录可见）
                        "/error"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdResolver);
    }
}
