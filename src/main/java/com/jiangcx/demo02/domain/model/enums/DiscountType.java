package com.jiangcx.demo02.domain.model.enums;

import lombok.Getter;

/**
 * 优惠类型枚举，每种类型对应不同的优惠计算策略
 */
@Getter
public enum DiscountType {
    THRESHOLD("THRESHOLD", "满减"),
    RATE("RATE", "商家折扣"),
    SPECIAL_PRICE("SPECIAL_PRICE", "商家特价"),
    BUY_N_GET_M("BUY_N_GET_M", "买N送M"),
    DELIVERY_FEE("DELIVERY_FEE", "配送费减免");

    private final String code;
    private final String description;

    DiscountType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DiscountType fromCode(String code) {
        for (DiscountType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown discount type: " + code);
    }
}
