package com.jiangcx.demo02.domain.service.pricing;

import com.jiangcx.demo02.domain.model.entity.DeliveryInfo;
import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.entity.OrderItem;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.service.discount.DiscountEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("价格计算领域服务")
class PriceCalculationServiceTest {

    @Mock
    private DiscountEngine discountEngine;

    @InjectMocks
    private PriceCalculationService priceCalculationService;

    private Order order;

    @BeforeEach
    void setUp() {
        DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市朝阳区");
        order = new Order("U001", "M001", delivery, null);
        order.addItem(new OrderItem("P001", "宫保鸡丁", 2, new BigDecimal("25.00")));
        order.addItem(new OrderItem("P002", "米饭", 3, new BigDecimal("2.00")));
        // subtotal = 50 + 6 = 56
    }

    @Nested
    @DisplayName("基本价格计算")
    class BasicPriceCalculation {

        @Test
        @DisplayName("应正确计算餐品小计 = sum(单价 × 数量)")
        void shouldCalculateSubtotal() {
            when(discountEngine.selectBestDiscount(any())).thenReturn(Optional.empty());

            priceCalculationService.calculateOrderPrice(order);

            assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("56.00"));
        }

        @Test
        @DisplayName("应设置打包费 1.00 元")
        void shouldSetPackingFee() {
            when(discountEngine.selectBestDiscount(any())).thenReturn(Optional.empty());

            priceCalculationService.calculateOrderPrice(order);

            assertThat(order.getPackingFee()).isEqualByComparingTo(new BigDecimal("1.00"));
        }

        @Test
        @DisplayName("应设置配送费 3.00 元")
        void shouldSetDeliveryFee() {
            when(discountEngine.selectBestDiscount(any())).thenReturn(Optional.empty());

            priceCalculationService.calculateOrderPrice(order);

            assertThat(order.getDeliveryFee()).isEqualByComparingTo(new BigDecimal("3.00"));
        }

        @Test
        @DisplayName("无折扣时 totalAmount = subtotal + packingFee + deliveryFee")
        void shouldCalculateTotalWithoutDiscount() {
            when(discountEngine.selectBestDiscount(any())).thenReturn(Optional.empty());

            priceCalculationService.calculateOrderPrice(order);

            // 56 + 1 + 3 = 60
            assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("60.00"));
            assertThat(order.getDiscountDetail()).isNull();
        }
    }

    @Nested
    @DisplayName("折扣场景")
    class WithDiscount {

        @Test
        @DisplayName("有折扣命中时应使用折扣后的 totalAmount")
        void shouldApplyDiscount() {
            DiscountDetail discount = createDiscountDetail(new BigDecimal("56.00"), new BigDecimal("50.00"));
            when(discountEngine.selectBestDiscount(any())).thenReturn(Optional.of(discount));

            priceCalculationService.calculateOrderPrice(order);

            assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(order.getDiscountDetail()).isNotNull();
            assertThat(order.getDiscountDetail().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("6.00"));
        }
    }

    private DiscountDetail createDiscountDetail(BigDecimal originalTotal, BigDecimal discountedTotal) {
        DiscountDetail detail = new DiscountDetail();
        detail.setSchemeId("SCHEME-001");
        detail.setSchemeName("满50减6");
        detail.setDiscountAmount(originalTotal.subtract(discountedTotal));
        detail.setOriginalTotal(originalTotal);
        detail.setDiscountedTotal(discountedTotal);
        return detail;
    }
}
