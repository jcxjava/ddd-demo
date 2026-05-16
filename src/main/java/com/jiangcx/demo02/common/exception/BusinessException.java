package com.jiangcx.demo02.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于封装业务逻辑中的异常信息
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误消息
     */
    private final String errorMessage;

    /**
     * 构造函数 - 使用错误码和错误消息
     *
     * @param errorCode    错误码
     * @param errorMessage 错误消息
     */
    public BusinessException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 构造函数 - 使用错误码、错误消息和异常原因
     *
     * @param errorCode    错误码
     * @param errorMessage 错误消息
     * @param cause        异常原因
     */
    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 构造函数 - 使用错误码和默认错误消息
     *
     * @param errorCode 错误码
     */
    public BusinessException(String errorCode) {
        super();
        this.errorCode = errorCode;
        this.errorMessage = "Business error occurred";
    }
}