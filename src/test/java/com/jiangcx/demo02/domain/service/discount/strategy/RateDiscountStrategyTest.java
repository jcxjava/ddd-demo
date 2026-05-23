package com.jiangcx.demo02.domain.service.discount.strategy;

import com.jiangcx.demo02.domain.model.entity.DeliveryInfo;
import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.entity.OrderItem;
import com.jiangcx.demo02.domain.model.enums.DiscountType;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.model.valueobject.DiscountScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("折扣率策略")
class RateDiscountStrategyTest {

    private RateDiscountStrategy strategy;
    private Order order;
    private DiscountScheme scheme;

    @BeforeEach
    void setUp() {
        strategy = new RateDiscountStrategy();
        DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市");
        order = new Order("U001", "M001", delivery, null);
        order.addItem(new OrderItem("P001", "测试餐品", 2, new BigDecimal("50.00")));
        // subtotal = 100, totalAmount = 104
        order.recalculateTotal();

        scheme = new DiscountScheme();
        scheme.setId("S001");
        scheme.setName("8折优惠");
        scheme.setType(DiscountType.RATE);
        scheme.setConfig(Map.of("discountRate", "0.80"));
    }

    @Test
    @DisplayName("8 折时应仅对小计打折，不减包装费和配送费")
    void shouldDiscountSubtotalOnly() {
        Optional<DiscountDetail> result = strategy.calculate(order, scheme);

        assertThat(result).isPresent();
        // discountAmount = 100 × (1 - 0.8) = 20
        assertThat(result.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
        // discountedTotal = 104 - 20 = 84
        assertThat(result.get().getDiscountedTotal()).isEqualByComparingTo(new BigDecimal("84.00"));
    }

    @Test
    @DisplayName("折扣率 1.0 时 discountAmount 为 0")
    void shouldHandleFullPrice() {
        scheme.setConfig(Map.of("discountRate", "1.00"));

        Optional<DiscountDetail> result = strategy.calculate(order, scheme);

        assertThat(result).isPresent();
        assertThat(result.get().getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("支持的优惠类型应为 RATE")
    void shouldSupportRateType() {
        assertThat(strategy.supportedType()).isEqualTo(DiscountType.RATE);
    }
}
