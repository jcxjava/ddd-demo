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

@DisplayName("特价策略")
class SpecialPriceStrategyTest {

    private SpecialPriceStrategy strategy;
    private Order order;
    private DiscountScheme scheme;

    @BeforeEach
    void setUp() {
        strategy = new SpecialPriceStrategy();
        DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市");
        order = new Order("U001", "M001", delivery, null);
        order.addItem(new OrderItem("P001", "宫保鸡丁", 2, new BigDecimal("30.00")));
        order.recalculateTotal();

        scheme = new DiscountScheme();
        scheme.setId("S001");
        scheme.setName("宫保鸡丁特价25元");
        scheme.setType(DiscountType.SPECIAL_PRICE);
        scheme.setConfig(Map.of("productId", "P001", "specialPrice", "25.00"));
    }

    @Nested
    @DisplayName("匹配场景")
    class Matched {

        @Test
        @DisplayName("商品在订单中时应返回折扣：优惠 = (原价 - 特价) × 数量")
        void shouldApplySpecialPrice() {
            Optional<DiscountDetail> result = strategy.calculate(order, scheme);

            assertThat(result).isPresent();
            // discount = (30 - 25) × 2 = 10
            assertThat(result.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        }
    }

    @Nested
    @DisplayName("不匹配场景")
    class NotMatched {

        @Test
        @DisplayName("商品不在订单中时应返回 empty")
        void shouldReturnEmptyWhenProductNotInOrder() {
            scheme.setConfig(Map.of("productId", "P999", "specialPrice", "25.00"));

            Optional<DiscountDetail> result = strategy.calculate(order, scheme);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("特价 >= 原价时应返回 empty")
        void shouldReturnEmptyWhenPriceNotLower() {
            scheme.setConfig(Map.of("productId", "P001", "specialPrice", "35.00"));

            Optional<DiscountDetail> result = strategy.calculate(order, scheme);

            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("支持的优惠类型应为 SPECIAL_PRICE")
    void shouldSupportSpecialPriceType() {
        assertThat(strategy.supportedType()).isEqualTo(DiscountType.SPECIAL_PRICE);
    }
}
