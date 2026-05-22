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
 * 配送费减免策略：减免部分或全部配送费，减免上限为实际配送费金额
 */
@Component
public class DeliveryFeeDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType supportedType() {
        return DiscountType.DELIVERY_FEE;
    }

    @Override
    public Optional<DiscountDetail> calculate(Order order, DiscountScheme scheme) {
        BigDecimal discountAmount = new BigDecimal(scheme.getConfig().get("discountAmount").toString());

        BigDecimal actualDiscount = discountAmount.min(order.getDeliveryFee());
        BigDecimal originalTotal = order.getSubtotal().add(order.getPackingFee()).add(order.getDeliveryFee());
        BigDecimal discountedTotal = originalTotal.subtract(actualDiscount);

        DiscountDetail detail = new DiscountDetail();
        detail.setSchemeId(scheme.getId());
        detail.setSchemeName(scheme.getName());
        detail.setType(DiscountType.DELIVERY_FEE);
        detail.setDiscountAmount(actualDiscount);
        detail.setOriginalTotal(originalTotal);
        detail.setDiscountedTotal(discountedTotal);
        return Optional.of(detail);
    }
}
