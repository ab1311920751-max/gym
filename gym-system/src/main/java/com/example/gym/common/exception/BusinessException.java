package com.example.gym.common.exception;

import lombok.Getter;

/**
 * 业务异常，继承 RuntimeException，由 GlobalExceptionHandler.handleBusiness() 统一捕获
 * 并转换为 Result 响应。
 * <p>
 * 携带业务错误码（code）和提示消息（message），提供三个构造器：
 * <ul>
 *   <li>直接传入 code 和 message — 自定义场景</li>
 *   <li>传入 ErrorCode 枚举 — 取枚举的 code 和 message</li>
 *   <li>传入 ErrorCode 并覆盖 message — 需要补充具体信息时使用（如"时间冲突！您在 xxx 已有其他课程"）</li>
 * </ul>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码，对应 ErrorCode 枚举中的 code 值，如 "4001"、"4204" 等 */
    private final String code;

    /** 直接传入 code 和 message */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** 使用 ErrorCode 枚举的 code 和 message */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /** 使用 ErrorCode 的错误码但覆盖默认消息，用于需要动态拼接错误信息的场景 */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
