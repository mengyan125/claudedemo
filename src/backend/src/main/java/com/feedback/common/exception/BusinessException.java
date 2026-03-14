package com.feedback.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于业务逻辑校验失败时抛出，由全局异常处理器统一捕获处理
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误状态码，默认500 */
    private final int code;

    /** 错误消息 */
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
