package com.jiangcx.demo02.domain.model.entity;

import com.jiangcx.demo02.domain.model.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("Order 聚合根")
class OrderTest {

    @Nested
    @DisplayName("创建订单")
    class CreateOrder {

        @Test
        @DisplayName("应自动生成 32 位唯一 orderId（UUID 去横线）")
        void shouldGenerateUniqueOrderId() {
            DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市朝阳区");

            Order order = new Order("U001", "M001", delivery, "少放辣");

            assertThat(order.getOrderId()).isNotNull().hasSize(32);
        }

        @Test
        @DisplayName("应自动生成格式为 yyyyMMddHHmmss + 6 位随机数的订单号")
        void shouldGenerateFormattedOrderNo() {
            DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市朝阳区");

            Order order = new Order("U001", "M001", delivery, null);

            assertThat(order.getOrderNo()).isNotNull().hasSize(20);
            // 前 14 位为日期时间数字
            assertThat(order.getOrderNo().substring(0, 14)).containsPattern("^\\d{14}$");
            // 后 6 位为随机数
            assertThat(order.getOrderNo().substring(14)).containsPattern("^\\d{6}$");
        }

        @Test
        @DisplayName("应自动设置订单状态为 PENDING_PAYMENT")
        void shouldSetStatusToPendingPayment() {
            DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市朝阳区");

            Order order = new Order("U001", "M001", delivery, null);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        }

        @Test
        @DisplayName("应自动设置打包费 1.00 元、配送费 3.00 元")
        void shouldSetDefaultFees() {
            DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市朝阳区");

            Order order = new Order("U001", "M001", delivery, null);

            assertThat(order.getPackingFee()).isEqualByComparingTo(new BigDecimal("1.00"));
            assertThat(order.getDeliveryFee()).isEqualByComparingTo(new BigDecimal("3.00"));
        }

        @Test
        @DisplayName("应记录用户ID、商户ID、配送信息和备注")
        void shouldRecordBasicFields() {
            DeliveryInfo delivery = new DeliveryInfo("李四", "13900139000", "上海市浦东新区");

            Order order = new Order("U999", "M888", delivery, "不要香菜");

            assertThat(order.getUserId()).isEqualTo("U999");
            assertThat(order.getMerchantId()).isEqualTo("M888");
            assertThat(order.getDeliveryInfo()).isEqualTo(delivery);
            assertThat(order.getNote()).isEqualTo("不要香菜");
        }

        @Test
        @DisplayName("应自动设置 createTime")
        void shouldSetCreateTime() {
            DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市朝阳区");

            Order order = new Order("U001", "M001", delivery, null);

            assertThat(order.getCreateTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("添加/移除餐品")
    class AddRemoveItems {

        @Test
        @DisplayName("添加餐品后应自动重算 subtotal 和 totalAmount")
        void shouldRecalculateAfterAdd() {
            Order order = createEmptyOrder();
            OrderItem item1 = new OrderItem("P001", "宫保鸡丁", 2, new BigDecimal("25.00"));
            OrderItem item2 = new OrderItem("P002", "米饭", 3, new BigDecimal("2.00"));

            order.addItem(item1);
            order.addItem(item2);

            // subtotal = 25×2 + 2×3 = 56
            assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("56.00"));
            // totalAmount = 56 + 1 + 3 = 60
            assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("60.00"));
        }

        @Test
        @DisplayName("移除餐品后应自动重算金额")
        void shouldRecalculateAfterRemove() {
            Order order = createEmptyOrder();
            OrderItem item1 = new OrderItem("P001", "宫保鸡丁", 2, new BigDecimal("25.00"));
            OrderItem item2 = new OrderItem("P002", "米饭", 3, new BigDecimal("2.00"));
            order.addItem(item1);
            order.addItem(item2);

            order.removeItem("P001");

            // 移除后只剩米饭: 2×3 = 6
            assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("6.00"));
            assertThat(order.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("空餐品列表时 subtotal 为 0，totalAmount 仅为费用 4.00 元")
        void shouldHandleEmptyItems() {
            Order order = createEmptyOrder();

            order.recalculateTotal();

            assertThat(order.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("4.00"));
        }
    }

    @Nested
    @DisplayName("取消订单")
    class CancelOrder {

        @Test
        @DisplayName("PENDING_PAYMENT 状态应可取消")
        void shouldAllowCancelWhenPending() {
            Order order = createEmptyOrder();

            assertThat(order.canCancel()).isTrue();
        }

        @Test
        @DisplayName("PAID 状态不可取消")
        void shouldNotAllowCancelWhenPaid() {
            Order order = createEmptyOrder();
            order.setStatus(OrderStatus.PAID);

            assertThat(order.canCancel()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED 状态不可取消")
        void shouldNotAllowCancelWhenCancelled() {
            Order order = createEmptyOrder();
            order.setStatus(OrderStatus.CANCELLED);

            assertThat(order.canCancel()).isFalse();
        }
    }

    @Nested
    @DisplayName("BigDecimal 精度")
    class BigDecimalPrecision {

        @Test
        @DisplayName("单价含小数时 subtotal 应精确到 2 位小数的精度")
        void shouldMaintainPrecision() {
            Order order = createEmptyOrder();
            // 9.99 × 3 = 29.97
            OrderItem item = new OrderItem("P001", "测试餐品", 3, new BigDecimal("9.99"));
            order.addItem(item);

            assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("29.97"));
            assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("33.97"));
        }
    }

    private Order createEmptyOrder() {
        DeliveryInfo delivery = new DeliveryInfo("测试", "13800000000", "测试地址");
        return new Order("U001", "M001", delivery, null);
    }
}
