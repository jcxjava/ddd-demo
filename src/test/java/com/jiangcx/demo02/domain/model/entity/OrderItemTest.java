package com.jiangcx.demo02.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderItem 实体")
class OrderItemTest {

    @Test
    @DisplayName("构造时应自动计算 totalPrice = unitPrice × quantity")
    void shouldAutoComputeTotalPrice() {
        OrderItem item = new OrderItem("P001", "宫保鸡丁", 3, new BigDecimal("25.00"));

        assertThat(item.getProductId()).isEqualTo("P001");
        assertThat(item.getProductName()).isEqualTo("宫保鸡丁");
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(item.getTotalPrice()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    @DisplayName("单价为小数时 totalPrice 应保持正确精度")
    void shouldHandleDecimalUnitPrice() {
        OrderItem item = new OrderItem("P002", "特价菜", 2, new BigDecimal("9.99"));

        assertThat(item.getTotalPrice()).isEqualByComparingTo(new BigDecimal("19.98"));
    }

    @Test
    @DisplayName("数量为 1 时 totalPrice 应等于 unitPrice")
    void shouldHandleSingleQuantity() {
        OrderItem item = new OrderItem("P003", "单品", 1, new BigDecimal("15.50"));

        assertThat(item.getTotalPrice()).isEqualByComparingTo(new BigDecimal("15.50"));
    }
}
