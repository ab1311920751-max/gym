package com.example.gym.common.auth;

import cn.hutool.jwt.JWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        JWT jwt = JwtSupport.parse(request.getHeader("Authorization"));
        request.setAttribute(CurrentUserIdResolver.ATTR_UID, JwtSupport.uid(jwt));
        request.setAttribute(CurrentUserIdResolver.ATTR_ROLE, JwtSupport.role(jwt));
        return true;
    }
}
