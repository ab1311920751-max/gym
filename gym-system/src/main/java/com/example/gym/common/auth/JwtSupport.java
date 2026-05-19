package com.example.gym.common.auth;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.example.gym.common.exception.UnauthorizedException;

/**
 * 集中 JWT 解析逻辑。签发逻辑仍留在 {@code UserServiceImpl.login}，
 * 阶段 2 配置外化后再合并到这里。
 */
public final class JwtSupport {

    // TODO 阶段 2 改成从 application.yml 注入
    private static final byte[] SECRET = "my-secret-key".getBytes();

    private JwtSupport() {}

    /**
     * 解析 Authorization header，返回 JWT。token 缺失/格式错/签名不通过时抛 UnauthorizedException。
     */
    public static JWT parse(String authorization) {
        if (StrUtil.isBlank(authorization)) {
            throw new UnauthorizedException("缺少 Authorization");
        }
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
