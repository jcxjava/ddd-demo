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

@DisplayName("满减策略")
class ThresholdDiscountStrategyTest {

    private ThresholdDiscountStrategy strategy;
    private Order order;
    private DiscountScheme scheme;

    @BeforeEach
    void setUp() {
        strategy = new ThresholdDiscountStrategy();
        DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市");
        order = new Order("U001", "M001", delivery, null);
        order.addItem(new OrderItem("P001", "测试餐品", 2, new BigDecimal("30.00")));
        // subtotal = 60, totalAmount = 64

        scheme = new DiscountScheme();
        scheme.setId("S001");
        scheme.setName("满50减10");
        scheme.setType(DiscountType.THRESHOLD);
        scheme.setConfig(Map.of("threshold", "50.00", "discountAmount", "10.00"));
    }

    @Nested
    @DisplayName("达到门槛")
    class ThresholdMet {

        @Test
        @DisplayName("小计 >= 门槛时应返回折扣明细")
        void shouldApplyDiscount() {
            Optional<DiscountDetail> result = strategy.calculate(order, scheme);

            assertThat(result).isPresent();
            assertThat(result.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        }

        @Test
        @DisplayName("折扣后总价 = 原总价 - 折扣金额")
        void shouldCalculateCorrectTotal() {
            Optional<DiscountDetail> result = strategy.calculate(order, scheme);

            // 原总价 = 60 + 1 + 3 = 64，折扣后 = 64 - 10 = 54
            assertThat(result.get().getOriginalTotal()).isEqualByComparingTo(new BigDecimal("64.00"));
            assertThat(result.get().getDiscountedTotal()).isEqualByComparingTo(new BigDecimal("54.00"));
        }
    }

    @Nested
    @DisplayName("未达门槛")
    class ThresholdNotMet {

        @Test
        @DisplayName("小计 < 门槛时应返回 empty")
        void shouldReturnEmpty() {
            scheme.setConfig(Map.of("threshold", "100.00", "discountAmount", "10.00"));

            Optional<DiscountDetail> result = strategy.calculate(order, scheme);

            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("支持的优惠类型应为 THRESHOLD")
    void shouldSupportThresholdType() {
        assertThat(strategy.supportedType()).isEqualTo(DiscountType.THRESHOLD);
    }
}
