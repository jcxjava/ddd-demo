package com.jiangcx.demo02.common.enum;

/**
 * 响应码枚举
 * 定义了系统中所有的响应码和对应的提示信息
 */
public enum ResponseCode {

    // 成功状态码 2xx
    SUCCESS("200", "操作成功"),

    // 客户端错误状态码 4xx
    BAD_REQUEST("400", "请求参数错误"),
    UNAUTHORIZED("401", "未授权，请登录"),
    FORBIDDEN("403", "拒绝访问"),
    NOT_FOUND("404", "资源不存在"),
    METHOD_NOT_ALLOWED("405", "请求方法不允许"),

    // 业务错误状态码 1000-1999
    BUSINESS_ERROR("1000", "业务异常"),
    PARAM_ERROR("1001", "参数验证失败"),
    DATA_NOT_FOUND("1002", "数据不存在"),
    DUPLICATE_DATA("1003", "数据已存在"),
    OPERATION_FAILED("1004", "操作失败"),

    // 系统错误状态码 2000-2999
    SYSTEM_ERROR("2000", "系统异常"),
    DATABASE_ERROR("2001", "数据库异常"),
    NETWORK_ERROR("2002", "网络异常"),
    FILE_ERROR("2003", "文件处理异常"),

    // 认证授权错误 3000-3999
    AUTH_TOKEN_EXPIRED("3000", "认证令牌已过期"),
    AUTH_TOKEN_INVALID("3001", "认证令牌无效"),
    PERMISSION_DENIED("3002", "权限不足");

    /**
     * 响应码
     */
    private final String code;

    /**
     * 响应消息
     */
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取响应码
     *
     * @return 响应码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取响应消息
     *
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }
}