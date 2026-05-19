package com.example.gym.common.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 方法参数注解：从当前请求的 JWT 中取 uid。
 * 拦截器已经解析过 JWT 并放到 request attribute，这里只是取出来。
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}
