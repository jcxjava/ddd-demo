package com.jiangcx.demo02.application.service;

import com.jiangcx.demo02.application.command.CreateOrderCommand;
import com.jiangcx.demo02.common.exception.BusinessException;
import com.jiangcx.demo02.common.model.api.PageResult;
import com.jiangcx.demo02.domain.model.entity.DeliveryInfo;
import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.entity.OrderItem;
import com.jiangcx.demo02.domain.model.enums.OrderStatus;
import com.jiangcx.demo02.domain.repository.IOrderRepository;
import com.jiangcx.demo02.domain.service.pricing.PriceCalculationService;
import com.jiangcx.demo02.domain.service.validation.OrderValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单应用服务")
class OrderApplicationServiceTest {

    @Mock
    private IOrderRepository orderRepository;
    @Mock
    private PriceCalculationService priceCalculationService;
    @Mock
    private OrderValidationService orderValidationService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderApplicationService applicationService;

    private CreateOrderCommand validCommand;

    @BeforeEach
    void setUp() {
        CreateOrderCommand.DeliveryInfoCommand deliveryCmd = new CreateOrderCommand.DeliveryInfoCommand();
        deliveryCmd.setReceiverName("张三");
        deliveryCmd.setReceiverPhone("13800138000");
        deliveryCmd.setReceiverAddress("北京市朝阳区");

        CreateOrderCommand.OrderItemCommand itemCmd = new CreateOrderCommand.OrderItemCommand();
        itemCmd.setProductId("P001");
        itemCmd.setProductName("宫保鸡丁");
        itemCmd.setQuantity(2);
        itemCmd.setUnitPrice(new BigDecimal("25.00"));

        validCommand = new CreateOrderCommand();
        validCommand.setUserId("U001");
        validCommand.setMerchantId("M001");
        validCommand.setNote("少放辣");
        validCommand.setDeliveryInfo(deliveryCmd);
        validCommand.setItems(List.of(itemCmd));
    }

    @Nested
    @DisplayName("创建订单")
    class CreateOrder {

        @Test
        @DisplayName("应协调校验→计价→保存→发布事件全流程")
        void shouldOrchestrateFullFlow() {
            applicationService.createOrder(validCommand);

            verify(orderValidationService).validate(any(Order.class));
            verify(priceCalculationService).calculateOrderPrice(any(Order.class));
            verify(orderRepository).save(any(Order.class));
            verify(eventPublisher).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("应返回包含完整信息的 Order")
        void shouldReturnOrder() {
            Order order = applicationService.createOrder(validCommand);

            assertThat(order).isNotNull();
            assertThat(order.getOrderId()).isNotNull();
            assertThat(order.getUserId()).isEqualTo("U001");
            assertThat(order.getMerchantId()).isEqualTo("M001");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
            assertThat(order.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("校验失败时应传播异常")
        void shouldPropagateValidationError() {
            doThrow(new BusinessException("1001", "商户ID不能为空"))
                    .when(orderValidationService).validate(any());

            assertThatThrownBy(() -> applicationService.createOrder(validCommand))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("商户ID不能为空");

            // 后续步骤不应执行
            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("查询订单")
    class GetOrder {

        @Test
        @DisplayName("订单存在时应返回 Order")
        void shouldReturnOrderWhenExists() {
            Order order = applicationService.createOrder(validCommand);
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));

            Order result = applicationService.getOrder(order.getOrderId());

            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(order.getOrderId());
        }

        @Test
        @DisplayName("订单不存在时应抛 BusinessException")
        void shouldThrowWhenNotFound() {
            when(orderRepository.findById("NOT-EXIST")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.getOrder("NOT-EXIST"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("订单不存在");
        }
    }

    @Nested
    @DisplayName("分页查询")
    class ListOrders {

        @Test
        @DisplayName("应返回分页结果")
        void shouldReturnPageResult() {
            Order order = createTestOrder();
            when(orderRepository.findByUserIdWithPage("U001", 1, 10)).thenReturn(List.of(order));
            when(orderRepository.countByUserId("U001")).thenReturn(1L);

            PageResult<Order> result = applicationService.listOrders("U001", 1, 10);

            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getTotal()).isEqualTo(1L);
            assertThat(result.getCurrent()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("取消订单")
    class CancelOrder {

        @Test
        @DisplayName("待支付订单可取消")
        void shouldCancelPendingOrder() {
            Order order = createTestOrder();
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));

            Order result = applicationService.cancelOrder(order.getOrderId());

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(orderRepository).updateStatus(order.getOrderId(), OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("已取消订单不可再次取消")
        void shouldRejectCancelCancelledOrder() {
            Order order = createTestOrder();
            order.setStatus(OrderStatus.CANCELLED);
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> applicationService.cancelOrder(order.getOrderId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不允许取消");
        }
    }

    private Order createTestOrder() {
        DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市");
        Order order = new Order("U001", "M001", delivery, null);
        order.addItem(new OrderItem("P001", "宫保鸡丁", 1, new BigDecimal("25.00")));
        return order;
    }
}
