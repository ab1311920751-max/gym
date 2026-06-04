package com.example.gym.common;

import lombok.Data;

/**
 * 统一响应结果封装，所有 Controller 方法通过此类包装返回给前端。
 * <p>
 * 包含三个字段：code（状态码，字符串）、msg（提示信息）、data（泛型响应数据）。
 * 提供 success() 和 error() 两组静态工厂方法，简化 Controller 层代码。
 * 注意：前端 response 拦截器用字符串比较 code（res.code === '200'），
 * 所有状态码必须为字符串类型。
 */
@Data
public class Result<T> {

    /** 状态码，字符串类型。"200" 成功，"401" 未授权，"500" 系统错误，业务错误码如 "4001" 等由 ErrorCode 枚举定义 */
    private String code;

    /** 给用户或前端展示的提示信息，如"操作成功"、"余额不足"等 */
    private String msg;

    /** 泛型响应数据，success(T) 时传入，error() 时该字段为 null */
    private T data;

    /** 无数据的成功响应，code="200"，msg="操作成功" */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode("200");
        result.setMsg("操作成功");
        return result;
    }

    /** 带数据的成功响应，code="200"，msg="操作成功" */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode("200");
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /** 默认 500 错误响应，code="500" */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode("500");
        result.setMsg(msg);
        return result;
    }

    /** 自定义错误码和消息的响应，用于业务异常场景 */
    public static <T> Result<T> error(String code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}