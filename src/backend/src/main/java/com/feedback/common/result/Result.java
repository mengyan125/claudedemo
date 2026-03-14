package com.feedback.common.result;

import lombok.Data;

/**
 * 统一响应封装类
 *
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {

    /** 响应状态码 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功响应（带数据） */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功响应（无数据） */
    public static <T> Result<T> ok() {
        return new Result<>(200, "success", null);
    }

    /** 失败响应（默认状态码500） */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /** 失败响应（自定义状态码） */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
