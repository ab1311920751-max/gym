package com.example.gym.common.exception;

/**
 * 业务错误码枚举，统一管理系统中所有错误码和对应的提示信息。
 * <p>
 * 按模块分组：
 * <ul>
 *   <li>200 — 成功</li>
 *   <li>400/401/403/404 — 通用 HTTP 错误</li>
 *   <li>4001–4006 — 用户模块（不存在、密码错、用户名重复、原密码错、账号禁用、不能禁用自己）</li>
 *   <li>4101 — 余额/支付</li>
 *   <li>4201–4205 — 课程/预约模块（不存在、售罄、过期、重复预约、时间冲突）</li>
 *   <li>4301–4302 — 订单模块（不存在、状态非法）</li>
 *   <li>4901 — 分布式锁超时</li>
 *   <li>500 — 系统错误</li>
 * </ul>
 * 新增错误码时按分组规则添加，code 使用字符串类型以兼容前端判断。
 */
public enum ErrorCode {

    SUCCESS("200", "操作成功"),

    BIZ_ERROR("400", "业务异常"),
    PARAM_INVALID("400", "参数不合法"),

    UNAUTHORIZED("401", "未登录或登录已过期"),
    FORBIDDEN("403", "无权访问"),
    NOT_FOUND("404", "资源不存在"),

    // ---- 用户模块 ----
    BIZ_USER_NOT_FOUND("4001", "用户不存在"),
    BIZ_USERNAME_OR_PASSWORD_WRONG("4002", "账号或密码错误"),
    BIZ_USERNAME_EXISTS("4003", "用户名已存在"),
    BIZ_PASSWORD_WRONG("4004", "原密码错误"),
    BIZ_USER_DISABLED("4005", "账号已被禁用，请联系管理员"),
    BIZ_CANNOT_DISABLE_SELF("4006", "不能禁用自己的账号"),

    // ---- 余额/支付 ----
    BIZ_BALANCE_NOT_ENOUGH("4101", "余额不足，请先充值"),

    // ---- 课程/预约模块 ----
    BIZ_COURSE_NOT_FOUND("4201", "课程不存在"),
    BIZ_COURSE_SOLD_OUT("4202", "手慢了，该课程已售罄"),
    BIZ_COURSE_EXPIRED("4203", "该课程已过期，无法预约"),
    BIZ_BOOKING_DUPLICATE("4204", "您已预约过该课程，请勿重复下单"),
    BIZ_BOOKING_TIME_CONFLICT("4205", "时间冲突，您在该时间已有其他课程"),

    // ---- 订单模块 ----
    BIZ_BOOKING_NOT_FOUND("4301", "订单不存在"),
    BIZ_BOOKING_STATUS_ILLEGAL("4302", "订单状态不允许此操作"),

    // ---- 分布式锁 ----
    BIZ_LOCK_TIMEOUT("4901", "当前抢购人数过多，请稍后重试"),

    SYSTEM_ERROR("500", "系统繁忙，请稍后重试");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
