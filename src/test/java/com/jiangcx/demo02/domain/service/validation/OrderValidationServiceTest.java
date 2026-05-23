package com.jiangcx.demo02.domain.service.validation;

import com.jiangcx.demo02.common.exception.BusinessException;
import com.jiangcx.demo02.domain.model.entity.DeliveryInfo;
import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("订单校验领域服务")
class OrderValidationServiceTest {

    private OrderValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new OrderValidationService();
    }

    @Nested
    @DisplayName("必填字段校验")
    class RequiredFields {

        @Test
        @DisplayName("merchantId 为 null 时应抛出 BusinessException")
        void shouldRejectNullMerchantId() {
            Order order = createValidOrder();
            order.setMerchantId(null);

            assertThatThrownBy(() -> validationService.validate(order))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("商户ID不能为空");
        }

        @Test
        @DisplayName("merchantId 为空字符串时应抛出 BusinessException")
        void shouldRejectBlankMerchantId() {
            Order order = createValidOrder();
            order.setMerchantId("   ");

            assertThatThrownBy(() -> validationService.validate(order))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("商户ID不能为空");
        }

        @Test
        @DisplayName("餐品列表为 null 时应抛出 BusinessException")
        void shouldRejectNullItems() {
            Order order = createValidOrder();
            order.setItems(null);

            assertThatThrownBy(() -> validationService.validate(order))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("餐品列表不能为空");
        }

        @Test
        @DisplayName("餐品列表为空时应抛出 BusinessException")
        void shouldRejectEmptyItems() {
            Order order = createValidOrder();
            order.getItems().clear();

            assertThatThrownBy(() -> validationService.validate(order))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("餐品列表不能为空");
        }

        @Test
        @DisplayName("配送信息为 null 时应抛出 BusinessException")
        void shouldRejectNullDeliveryInfo() {
            Order order = createValidOrder();
            order.setDeliveryInfo(null);

            assertThatThrownBy(() -> validationService.validate(order))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("配送信息不能为空");
        }

        @Test
        @DisplayName("收货人姓名为空时应抛出 BusinessException")
        void shouldRejectBlankReceiverName() {
            Order order = createValidOrder();
            order.setDeliveryInfo(new DeliveryInfo("", "13800138000", "北京市"));

            assertThatThrownBy(() -> validationService.validate(order))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("收货人姓名不能为空");
        }

        @Test
        @DisplayName("手机号为空时应抛出 BusinessException")
        void shouldRejectBlankPhone() {
            Order order = createValidOrder();
            order.setDeliveryInfo(new DeliveryInfo("张三", null, "北京市"));

            assertThatThrownBy(() -> validationService.validate(order))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("手机号不能为空");
        }

        @Test
        @DisplayName("收货地址为空时应抛出 BusinessException")
        void shouldRejectBlankAddress() {
            Order order = createValidOrder();
            order.setDeliveryInfo(new DeliveryInfo("张三", "13800138000", null));

            assertThatThrownBy(() -> validationService.validate(order))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("收货地址不能为空");
        }
    }

    @Nested
    @DisplayName("备注长度校验")
    class NoteLength {

        @Test
        @DisplayName("备注超过 200 字符时应抛出 BusinessException")
        void shouldRejectLongNote() {
            Order order = createValidOrder();
            order.setNote("A".repeat(201));

            assertThatThrownBy(() -> validationService.validate(order))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("备注信息最多200字符");
        }

        @Test
        @DisplayName("备注正好 200 字符时应通过校验")
        void shouldAcceptMaxLengthNote() {
            Order order = createValidOrder();
            order.setNote("A".repeat(200));

            assertThatCode(() -> validationService.validate(order))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("备注为 null 时应通过校验")
        void shouldAcceptNullNote() {
            Order order = createValidOrder();
            order.setNote(null);

            assertThatCode(() -> validationService.validate(order))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("完整合法订单")
    class ValidOrder {

        @Test
        @DisplayName("所有字段合法时 validate() 应不抛异常")
        void shouldPassValidation() {
            Order order = createValidOrder();

            assertThatCode(() -> validationService.validate(order))
                    .doesNotThrowAnyException();
        }
    }

    private Order createValidOrder() {
        DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市朝阳区");
        Order order = new Order("U001", "M001", delivery, "少放辣");
        order.addItem(new OrderItem("P001", "宫保鸡丁", 1, new BigDecimal("25.00")));
        return order;
    }
}
