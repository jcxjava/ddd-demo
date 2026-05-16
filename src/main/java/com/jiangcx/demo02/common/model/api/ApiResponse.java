package com.jiangcx.demo02.common.model.api;

import com.jiangcx.demo02.common.enum.ResponseCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应对象
 * 用于统一接口返回格式
 *
 * @param <T> 数据类型
 */
@Data
public class ApiResponse<T> implements Serializable {

    /**
     * 响应码
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 成功响应
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return of(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（无数据）
     *
     * @return 响应对象
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 错误响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 响应对象
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return of(code, message, null);
    }

    /**
     * 使用ResponseCode错误码错误响应
     *
     * @param responseCode 响应码枚举
     * @return 响应对象
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        return error(responseCode.getCode(), responseCode.getMessage());
    }

    /**
     * 使用ResponseCode错误码和自定义消息错误响应
     *
     * @param responseCode 响应码枚举
     * @param message      自定义消息
     * @return 响应对象
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode, String message) {
        return error(responseCode.getCode(), message);
    }

    /**
     * 响应对象构造方法
     *
     * @param code    响应码
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> of(String code, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return ResponseCode.SUCCESS.getCode().equals(this.code);
    }
}