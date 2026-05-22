package com.jiangcx.demo02.domain.service.discount.strategy;

import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.entity.OrderItem;
import com.jiangcx.demo02.domain.model.enums.DiscountType;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.model.valueobject.DiscountScheme;
import com.jiangcx.demo02.domain.service.discount.DiscountStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * 买N送M策略：购买指定商品满N件后赠送M件，按实际购买件数计算赠送次数（向下取整）
 */
@Component
public class BuyNGetMStrategy implements DiscountStrategy {

    @Override
    public DiscountType supportedType() {
        return DiscountType.BUY_N_GET_M;
    }

    @Override
    public Optional<DiscountDetail> calculate(Order order, DiscountScheme scheme) {
        String productId = scheme.getConfig().get("productId").toString();
        int buyQuantity = Integer.parseInt(scheme.getConfig().get("buyQuantity").toString());
        int freeQuantity = Integer.parseInt(scheme.getConfig().get("freeQuantity").toString());

        Optional<OrderItem> matchedItem = order.getItems().stream()
                .filter(item -> productId.equals(item.getProductId()))
                .findFirst();

        if (matchedItem.isEmpty()) {
            return Optional.empty();
        }

        OrderItem item = matchedItem.get();
        int actualQuantity = item.getQuantity();
        int triggerTimes = actualQuantity / buyQuantity;

        if (triggerTimes == 0) {
            return Optional.empty();
        }

        BigDecimal discountAmount = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(freeQuantity))
                .multiply(BigDecimal.valueOf(triggerTimes))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal originalTotal = order.getSubtotal().add(order.getPackingFee()).add(order.getDeliveryFee());
        BigDecimal discountedTotal = originalTotal.subtract(discountAmount);

        DiscountDetail detail = new DiscountDetail();
        detail.setSchemeId(scheme.getId());
        detail.setSchemeName(scheme.getName());
        detail.setType(DiscountType.BUY_N_GET_M);
        detail.setDiscountAmount(discountAmount);
        detail.setOriginalTotal(originalTotal);
        detail.setDiscountedTotal(discountedTotal);
        return Optional.of(detail);
    }
}
