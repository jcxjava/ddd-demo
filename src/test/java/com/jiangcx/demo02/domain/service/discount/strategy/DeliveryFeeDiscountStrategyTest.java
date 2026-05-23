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

@DisplayName("配送费减免策略")
class DeliveryFeeDiscountStrategyTest {

    private DeliveryFeeDiscountStrategy strategy;
    private Order order;

    @BeforeEach
    void setUp() {
        strategy = new DeliveryFeeDiscountStrategy();
        DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市");
        order = new Order("U001", "M001", delivery, null);
        order.addItem(new OrderItem("P001", "测试餐品", 1, new BigDecimal("50.00")));
        order.recalculateTotal();
        // totalAmount = 50 + 1 + 3 = 54, deliveryFee = 3
    }

    @Test
    @DisplayName("配置减免 5 元时实际只减免配送费上限 3 元")
    void shouldCapAtDeliveryFee() {
        DiscountScheme scheme = createScheme("5.00");

        Optional<DiscountDetail> result = strategy.calculate(order, scheme);

        assertThat(result).isPresent();
        assertThat(result.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(result.get().getDiscountedTotal()).isEqualByComparingTo(new BigDecimal("51.00"));
    }

    @Test
    @DisplayName("配置减免 2 元时实际减免 2 元（未超上限）")
    void shouldApplyExactDiscount() {
        DiscountScheme scheme = createScheme("2.00");

        Optional<DiscountDetail> result = strategy.calculate(order, scheme);

        assertThat(result).isPresent();
        assertThat(result.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(result.get().getDiscountedTotal()).isEqualByComparingTo(new BigDecimal("52.00"));
    }

    @Test
    @DisplayName("支持的优惠类型应为 DELIVERY_FEE")
    void shouldSupportDeliveryFeeType() {
        assertThat(strategy.supportedType()).isEqualTo(DiscountType.DELIVERY_FEE);
    }

    private DiscountScheme createScheme(String discountAmount) {
        DiscountScheme s = new DiscountScheme();
        s.setId("S001");
        s.setName("配送费减免" + discountAmount + "元");
        s.setType(DiscountType.DELIVERY_FEE);
        s.setConfig(Map.of("discountAmount", discountAmount));
        return s;
    }
}
