package com.example.gym.common.auth;

import com.example.gym.common.exception.UnauthorizedException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @CurrentUserId 注解的参数解析器，将 JwtInterceptor 写入 RequestAttribute 的 uid
 * 自动注入到 Controller 方法的 Long 类型参数。
 *
 * 用法：Controller 方法声明 @CurrentUserId Long userId，框架自动填值，无需从请求体取。
 * 不从请求体/URL 取 userId 的原因：客户端可以随意填写请求参数，从 token 解析才是可信来源。
 */
@Component
public class CurrentUserIdResolver implements HandlerMethodArgumentResolver {

    public static final String ATTR_UID = "auth.uid";
    public static final String ATTR_ROLE = "auth.role";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new UnauthorizedException();
        }
        Object uid = request.getAttribute(ATTR_UID);
        if (uid == null) {
            throw new UnauthorizedException();
        }
        return uid;
    }
}
