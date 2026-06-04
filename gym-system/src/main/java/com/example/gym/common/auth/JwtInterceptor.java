package com.example.gym.common.auth;

import cn.hutool.jwt.JWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器，在每个请求到达 Controller 前执行。
 *
 * 鉴权链路：
 *   请求 → JwtInterceptor.preHandle()
 *         → JwtSupport.parse() 验签，失败抛 UnauthorizedException (401)
 *         → uid/role 写入 RequestAttribute
 *         → Controller 方法参数通过 @CurrentUserId 注解由 CurrentUserIdResolver 注入
 *
 * Spring Security 依赖仅用于 BCrypt，未配置 SecurityFilterChain，鉴权完全由此拦截器负责。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // OPTIONS 是浏览器跨域预检请求，直接放行，不需要校验 token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        JWT jwt = JwtSupport.parse(request.getHeader("Authorization"));
        // uid 和 role 写入 RequestAttribute，供 CurrentUserIdResolver 取用
        request.setAttribute(CurrentUserIdResolver.ATTR_UID, JwtSupport.uid(jwt));
        request.setAttribute(CurrentUserIdResolver.ATTR_ROLE, JwtSupport.role(jwt));
        return true;
    }
}
