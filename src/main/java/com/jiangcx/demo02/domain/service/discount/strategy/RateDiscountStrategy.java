package com.jiangcx.demo02.domain.service.discount.strategy;

import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.enums.DiscountType;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.model.valueobject.DiscountScheme;
import com.jiangcx.demo02.domain.service.discount.DiscountStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * 折扣率策略：按配置比例对餐品小计打折，仅折扣餐品部分，不折扣包装费和配送费
 */
@Component
public class RateDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType supportedType() {
        return DiscountType.RATE;
    }

    @Override
    public Optional<DiscountDetail> calculate(Order order, DiscountScheme scheme) {
        BigDecimal discountRate = new BigDecimal(scheme.getConfig().get("discountRate").toString());
        BigDecimal originalTotal = order.getSubtotal().add(order.getPackingFee()).add(order.getDeliveryFee());
        BigDecimal discountAmount = order.getSubtotal()
                .multiply(BigDecimal.ONE.subtract(discountRate))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedTotal = originalTotal.subtract(discountAmount);

        DiscountDetail detail = new DiscountDetail();
        detail.setSchemeId(scheme.getId());
        detail.setSchemeName(scheme.getName());
        detail.setType(DiscountType.RATE);
        detail.setDiscountAmount(discountAmount);
        detail.setOriginalTotal(originalTotal);
        detail.setDiscountedTotal(discountedTotal);
        return Optional.of(detail);
    }
}
