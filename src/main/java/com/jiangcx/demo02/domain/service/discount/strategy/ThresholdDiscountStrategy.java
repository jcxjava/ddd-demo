package com.jiangcx.demo02.domain.service.discount.strategy;

import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.enums.DiscountType;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.model.valueobject.DiscountScheme;
import com.jiangcx.demo02.domain.service.discount.DiscountStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 满减策略：订单小计达到门槛金额后，减免固定金额；未达门槛则不可用
 */
@Component
public class ThresholdDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType supportedType() {
        return DiscountType.THRESHOLD;
    }

    @Override
    public Optional<DiscountDetail> calculate(Order order, DiscountScheme scheme) {
        BigDecimal threshold = new BigDecimal(scheme.getConfig().get("threshold").toString());
        BigDecimal discountAmount = new BigDecimal(scheme.getConfig().get("discountAmount").toString());

        if (order.getSubtotal().compareTo(threshold) < 0) {
            return Optional.empty();
        }

        BigDecimal originalTotal = order.getSubtotal().add(order.getPackingFee()).add(order.getDeliveryFee());
        BigDecimal discountedTotal = originalTotal.subtract(discountAmount);

        DiscountDetail detail = new DiscountDetail();
        detail.setSchemeId(scheme.getId());
        detail.setSchemeName(scheme.getName());
        detail.setType(DiscountType.THRESHOLD);
        detail.setDiscountAmount(discountAmount);
        detail.setOriginalTotal(originalTotal);
        detail.setDiscountedTotal(discountedTotal);
        return Optional.of(detail);
    }
}
