package com.jiangcx.demo02.domain.model.valueobject;

import com.jiangcx.demo02.domain.model.enums.DiscountType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 优惠计算结果值对象，记录命中的优惠方案及其对订单金额的影响
 */
@Data
public class DiscountDetail {
    private String schemeId;
    private String schemeName;
    private DiscountType type;
    private BigDecimal discountAmount;
    private BigDecimal originalTotal;
    private BigDecimal discountedTotal;
}
