package com.example.gym.common.exception;

/**
 * 未授权异常，继承 RuntimeException，表示用户未登录或 token 无效/过期。
 * 由 GlobalExceptionHandler.handleUnauthorized() 统一捕获并返回 401 状态码给前端。
 * <p>
 * code 固定为 ErrorCode.UNAUTHORIZED（"401"）。JwtInterceptor 和 JwtSupport 解析 token
 * 失败时抛出此异常，BookingController.assertOwner() 校验订单归属失败时也复用此异常。
 */
public class UnauthorizedException extends RuntimeException {

    /** 固定为 "401"，对应 ErrorCode.UNAUTHORIZED */
    private final String code;

    /** 默认构造器，使用 ErrorCode.UNAUTHORIZED 的默认消息 */
    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED.getMessage());
        this.code = ErrorCode.UNAUTHORIZED.getCode();
    }

    /** 自定义消息构造器，可用于补充具体原因（如"无权操作他人订单"） */
    public UnauthorizedException(String message) {
        super(message);
        this.code = ErrorCode.UNAUTHORIZED.getCode();
    }

    public String getCode() {
        return code;
    }
}
