package com.jiangcx.demo02.domain.service.discount.strategy;

import com.jiangcx.demo02.domain.model.entity.DeliveryInfo;
import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.entity.OrderItem;
import com.jiangcx.demo02.domain.model.enums.DiscountType;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.model.valueobject.DiscountScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("买N送M策略")
class BuyNGetMStrategyTest {

    private BuyNGetMStrategy strategy;
    private Order order;
    private DiscountScheme scheme;

    @BeforeEach
    void setUp() {
        strategy = new BuyNGetMStrategy();
        DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市");
        order = new Order("U001", "M001", delivery, null);
    }

    @Nested
    @DisplayName("触发赠送")
    class Triggered {

        @Test
        @DisplayName("买3送1，购买6件 → 触发2次，折扣 = 单价 × 1 × 2")
        void shouldTriggerMultipleTimes() {
            order.addItem(new OrderItem("P001", "鸡腿", 6, new BigDecimal("10.00")));
            order.recalculateTotal();

            scheme = createScheme("P001", 3, 1);

            Optional<DiscountDetail> result = strategy.calculate(order, scheme);

            assertThat(result).isPresent();
            // discount = 10 × 1 × (6/3=2) = 20
            assertThat(result.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
        }

        @Test
        @DisplayName("买3送1，购买3件 → 触发1次")
        void shouldTriggerOnce() {
            order.addItem(new OrderItem("P001", "鸡腿", 3, new BigDecimal("10.00")));
            order.recalculateTotal();

            scheme = createScheme("P001", 3, 1);

            Optional<DiscountDetail> result = strategy.calculate(order, scheme);

            assertThat(result).isPresent();
            assertThat(result.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        }
    }

    @Nested
    @DisplayName("未触发")
    class NotTriggered {

        @Test
        @DisplayName("商品不在订单中 → empty")
        void shouldReturnEmptyWhenProductNotInOrder() {
            order.addItem(new OrderItem("P001", "鸡腿", 6, new BigDecimal("10.00")));
            order.recalculateTotal();
            scheme = createScheme("P999", 3, 1);

            Optional<DiscountDetail> result = strategy.calculate(order, scheme);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("购买数量不足 → empty")
        void shouldReturnEmptyWhenInsufficientQuantity() {
            order.addItem(new OrderItem("P001", "鸡腿", 2, new BigDecimal("10.00")));
            order.recalculateTotal();
            scheme = createScheme("P001", 3, 1);

            Optional<DiscountDetail> result = strategy.calculate(order, scheme);

            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("支持的优惠类型应为 BUY_N_GET_M")
    void shouldSupportBuyNGetMType() {
        assertThat(strategy.supportedType()).isEqualTo(DiscountType.BUY_N_GET_M);
    }

    private DiscountScheme createScheme(String productId, int buyQuantity, int freeQuantity) {
        DiscountScheme s = new DiscountScheme();
        s.setId("S001");
        s.setName("买" + buyQuantity + "送" + freeQuantity);
        s.setType(DiscountType.BUY_N_GET_M);
        s.setConfig(Map.of(
                "productId", productId,
                "buyQuantity", String.valueOf(buyQuantity),
                "freeQuantity", String.valueOf(freeQuantity)
        ));
        return s;
    }
}
