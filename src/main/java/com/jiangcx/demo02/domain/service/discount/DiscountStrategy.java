package com.jiangcx.demo02.domain.service.discount;

import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.enums.DiscountType;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.model.valueobject.DiscountScheme;

import java.util.Optional;

/**
 * 优惠策略接口，策略模式的核心抽象；每种优惠类型对应一个实现，由DiscountEngine统一调度
 */
public interface DiscountStrategy {
    DiscountType supportedType();
    Optional<DiscountDetail> calculate(Order order, DiscountScheme scheme);
}
