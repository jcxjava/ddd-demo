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
 * 特价策略：指定商品以特价出售，优惠金额=(原价-特价)×数量；若特价不低于原价则不可用
 */
@Component
public class SpecialPriceStrategy implements DiscountStrategy {

    @Override
    public DiscountType supportedType() {
        return DiscountType.SPECIAL_PRICE;
    }

    @Override
    public Optional<DiscountDetail> calculate(Order order, DiscountScheme scheme) {
        String productId = scheme.getConfig().get("productId").toString();
        BigDecimal specialPrice = new BigDecimal(scheme.getConfig().get("specialPrice").toString());

        Optional<OrderItem> matchedItem = order.getItems().stream()
                .filter(item -> productId.equals(item.getProductId()))
                .findFirst();

        if (matchedItem.isEmpty()) {
            return Optional.empty();
        }

        OrderItem item = matchedItem.get();
        BigDecimal originalPrice = item.getUnitPrice();
        BigDecimal discountPerItem = originalPrice.subtract(specialPrice);
        if (discountPerItem.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        BigDecimal discountAmount = discountPerItem.multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal originalTotal = order.getSubtotal().add(order.getPackingFee()).add(order.getDeliveryFee());
        BigDecimal discountedTotal = originalTotal.subtract(discountAmount);

        DiscountDetail detail = new DiscountDetail();
        detail.setSchemeId(scheme.getId());
        detail.setSchemeName(scheme.getName());
        detail.setType(DiscountType.SPECIAL_PRICE);
        detail.setDiscountAmount(discountAmount);
        detail.setOriginalTotal(originalTotal);
        detail.setDiscountedTotal(discountedTotal);
        return Optional.of(detail);
    }
}
