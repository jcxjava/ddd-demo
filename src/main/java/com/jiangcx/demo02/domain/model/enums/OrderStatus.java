package com.jiangcx.demo02.domain.model.enums;

import lombok.Getter;

/**
 * 订单状态枚举，控制订单的生命周期流转
 */
@Getter
public enum OrderStatus {
    PENDING_PAYMENT("PENDING_PAYMENT", "待支付"),
    PAID("PAID", "已支付"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;

    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }
}