package com.jiangcx.demo02.domain.service.pricing;

import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.service.discount.DiscountEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 价格计算领域服务，计算订单小计、包装费、配送费，并应用最优优惠得出最终应付金额
 */
@Service
@RequiredArgsConstructor
public class PriceCalculationService {

    private static final BigDecimal PACKING_FEE = BigDecimal.valueOf(1.00);
    private static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(3.00);

    private final DiscountEngine discountEngine;

    /** 计算订单完整价格：小计→基础费用→择优优惠→最终金额 */
    public void calculateOrderPrice(Order order) {
        BigDecimal subtotal = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(subtotal);
        order.setPackingFee(PACKING_FEE);
        order.setDeliveryFee(DELIVERY_FEE);

        Optional<DiscountDetail> bestDiscount = discountEngine.selectBestDiscount(order);
        BigDecimal baseTotal = subtotal.add(PACKING_FEE).add(DELIVERY_FEE);

        if (bestDiscount.isPresent()) {
            DiscountDetail detail = bestDiscount.get();
            order.setTotalAmount(detail.getDiscountedTotal());
            order.setDiscountDetail(detail);
        } else {
            order.setTotalAmount(baseTotal);
        }
    }
}
