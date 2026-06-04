package com.example.gym.common;

import com.example.gym.common.exception.BusinessException;
import com.example.gym.common.exception.ErrorCode;
import com.example.gym.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，使用 @RestControllerAdvice 统一拦截 Controller 层抛出的异常，
 * 将其转换为 Result 格式的 JSON 响应返回给前端。
 * <p>
 * 异常按类型分为五个层级（由具体到兜底）：
 * <ol>
 *   <li>BusinessException — 业务异常，warn 日志，取异常自带 code 和 message</li>
 *   <li>UnauthorizedException — 未授权，warn 日志，返回 401</li>
 *   <li>IllegalArgumentException — 参数校验，warn 日志，使用 PARAM_INVALID 错误码</li>
 *   <li>RuntimeException — 未预期运行时异常，error 日志（含堆栈）</li>
 *   <li>Exception — 兜底，error 日志，不暴露内部错误详情给前端</li>
 * </ol>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 处理业务异常，warn 级别，取异常的 code 和 message 构造 Result */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 处理未授权异常，返回 401 状态码 */
    @ExceptionHandler(UnauthorizedException.class)
    public Result<Void> handleUnauthorized(UnauthorizedException e) {
        log.warn("未授权: msg={}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 处理参数校验异常，使用 PARAM_INVALID 错误码 */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArg(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.error(ErrorCode.PARAM_INVALID.getCode(), e.getMessage());
    }

    /** 处理未预期的运行时异常，error 级别（含堆栈），返回通用系统错误 */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {
        log.error("未捕获运行时异常: ", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), e.getMessage());
    }

    /** 兜底异常处理，捕获所有未处理的异常，使用 SYSTEM_ERROR 错误码，不暴露内部错误详情 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
    }
}
