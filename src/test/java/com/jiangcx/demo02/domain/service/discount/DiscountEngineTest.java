package com.jiangcx.demo02.domain.service.discount;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("优惠引擎")
class DiscountEngineTest {

    @Mock
    private DiscountSchemeRepository discountSchemeRepository;

    private DiscountEngine discountEngine;
    private Order order;

    @BeforeEach
    void setUp() {
        // 注册所有策略
        List<DiscountStrategy> strategies = List.of(
                new com.jiangcx.demo02.domain.service.discount.strategy.ThresholdDiscountStrategy(),
                new com.jiangcx.demo02.domain.service.discount.strategy.RateDiscountStrategy()
        );
        discountEngine = new DiscountEngine(discountSchemeRepository, strategies);

        DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市");
        order = new Order("U001", "M001", delivery, null);
        order.addItem(new OrderItem("P001", "测试餐品", 3, new BigDecimal("20.00")));
        order.recalculateTotal();
        // subtotal = 60, totalAmount = 64
    }

    @Nested
    @DisplayName("择优选择")
    class SelectBest {

        @Test
        @DisplayName("两个方案都可用时应选 discountAmount 最大的")
        void shouldSelectMaxDiscount() {
            DiscountScheme scheme1 = new DiscountScheme();
            scheme1.setId("S001");
            scheme1.setName("满50减10");
            scheme1.setType(DiscountType.THRESHOLD);
            scheme1.setConfig(Map.of("threshold", "50.00", "discountAmount", "10.00"));

            DiscountScheme scheme2 = new DiscountScheme();
            scheme2.setId("S002");
            scheme2.setName("满50减15");
            scheme2.setType(DiscountType.THRESHOLD);
            scheme2.setConfig(Map.of("threshold", "50.00", "discountAmount", "15.00"));

            when(discountSchemeRepository.findActiveByMerchantId("M001"))
                    .thenReturn(List.of(scheme1, scheme2));

            Optional<DiscountDetail> result = discountEngine.selectBestDiscount(order);

            assertThat(result).isPresent();
            assertThat(result.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
            assertThat(result.get().getSchemeName()).isEqualTo("满50减15");
        }
    }

    @Nested
    @DisplayName("无可用方案")
    class NoAvailableScheme {

        @Test
        @DisplayName("商户无活动方案时应返回 empty")
        void shouldReturnEmptyWhenNoSchemes() {
            when(discountSchemeRepository.findActiveByMerchantId("M001"))
                    .thenReturn(List.of());

            Optional<DiscountDetail> result = discountEngine.selectBestDiscount(order);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("所有方案均不满足条件时应返回 empty")
        void shouldReturnEmptyWhenNoMatch() {
            DiscountScheme scheme = new DiscountScheme();
            scheme.setId("S001");
            scheme.setName("满100减10");
            scheme.setType(DiscountType.THRESHOLD);
            scheme.setConfig(Map.of("threshold", "100.00", "discountAmount", "10.00"));

            when(discountSchemeRepository.findActiveByMerchantId("M001"))
                    .thenReturn(List.of(scheme));

            Optional<DiscountDetail> result = discountEngine.selectBestDiscount(order);
            // 小计 60 < 门槛 100，不满足条件

            assertThat(result).isEmpty();
        }
    }
}
