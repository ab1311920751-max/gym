package com.example.gym.common.auth;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.example.gym.common.exception.UnauthorizedException;

/**
 * JWT 解析工具类，供 JwtInterceptor 调用。
 * 签发逻辑在 UserServiceImpl.login()，payload 包含 uid、role、exp。
 * TODO 阶段 2 将 SECRET 改为从 application.yml 注入。
 */
public final class JwtSupport {

    private static final byte[] SECRET = "my-secret-key".getBytes();

    private JwtSupport() {}

    /**
     * 解析 Authorization 请求头，返回已验签的 JWT 对象。
     * token 缺失、格式错误或签名不通过时抛 UnauthorizedException（返回 401）。
     */
    public static JWT parse(String authorization) {
        if (StrUtil.isBlank(authorization)) {
            throw new UnauthorizedException("缺少 Authorization");
        }
        // 兼容带 "Bearer " 前缀和不带前缀两种格式
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;
        if (!JWTUtil.verify(token, SECRET)) {
            throw new UnauthorizedException("Token 无效或已过期");
        }
        return JWTUtil.parseToken(token);
    }

    public static Long uid(JWT jwt) {
        Object uid = jwt.getPayload("uid");
        if (uid == null) {
            throw new UnauthorizedException("Token 缺少 uid");
        }
        return Long.valueOf(uid.toString());
    }

    public static String role(JWT jwt) {
        Object role = jwt.getPayload("role");
        return role == null ? null : role.toString();
    }
}
