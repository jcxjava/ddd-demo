package com.jiangcx.demo02.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiangcx.demo02.api.dto.request.CreateOrderRequest;
import com.jiangcx.demo02.api.dto.request.DeliveryInfoRequest;
import com.jiangcx.demo02.api.dto.request.OrderItemRequest;
import com.jiangcx.demo02.application.service.OrderApplicationService;
import com.jiangcx.demo02.common.exception.BusinessException;
import com.jiangcx.demo02.domain.model.entity.DeliveryInfo;
import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("订单接口层")
class OrderControllerTest {

    private MockMvc mockMvc;
    private OrderApplicationService orderApplicationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderApplicationService = mock(OrderApplicationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderApplicationService))
                .setControllerAdvice(new com.jiangcx.demo02.common.handler.GlobalExceptionHandler())
                .build();

        DeliveryInfo delivery = new DeliveryInfo("张三", "13800138000", "北京市朝阳区");
        testOrder = new Order("U0001", "M001", delivery, "少放辣");
        testOrder.setOrderId("a1b2c3d4e5f6789012345678abcdef01");
        testOrder.setOrderNo("20260522143000123456");
        testOrder.addItem(new OrderItem("P001", "宫保鸡丁", 2, new BigDecimal("25.00")));
        testOrder.recalculateTotal();
        testOrder.setCreateTime(LocalDateTime.of(2026, 5, 22, 14, 30));
    }

    @Nested
    @DisplayName("创建订单 POST /api/orders")
    class CreateOrderEndpoint {

        @Test
        @DisplayName("合法请求应返回 200 + OrderResponse")
        void shouldReturn200WithOrderResponse() throws Exception {
            when(orderApplicationService.createOrder(any())).thenReturn(testOrder);

            CreateOrderRequest request = createValidRequest();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.orderId").isNotEmpty())
                    .andExpect(jsonPath("$.data.orderNo").value("20260522143000123456"))
                    .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"))
                    .andExpect(jsonPath("$.data.subtotal").value(50.00))
                    .andExpect(jsonPath("$.data.packingFee").value(1.00))
                    .andExpect(jsonPath("$.data.deliveryFee").value(3.00))
                    .andExpect(jsonPath("$.data.totalAmount").value(54.00));
        }

        @Test
        @DisplayName("缺少 merchantId 应返回 400")
        void shouldReturn400WhenMerchantIdMissing() throws Exception {
            CreateOrderRequest request = createValidRequest();
            request.setMerchantId(null);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("1001"));
        }

        @Test
        @DisplayName("餐品列表为空应返回 400")
        void shouldReturn400WhenItemsEmpty() throws Exception {
            CreateOrderRequest request = createValidRequest();
            request.setItems(List.of());

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("备注超过 200 字符应返回 400")
        void shouldReturn400WhenNoteTooLong() throws Exception {
            CreateOrderRequest request = createValidRequest();
            request.setNote("A".repeat(201));

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("查询订单 GET /api/orders/{orderId}")
    class GetOrderEndpoint {

        @Test
        @DisplayName("订单存在应返回 200")
        void shouldReturn200WhenExists() throws Exception {
            when(orderApplicationService.getOrder("a1b2c3d4e5f6789012345678abcdef01")).thenReturn(testOrder);

            mockMvc.perform(get("/api/orders/a1b2c3d4e5f6789012345678abcdef01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderId").value("a1b2c3d4e5f6789012345678abcdef01"));
        }

        @Test
        @DisplayName("订单不存在应返回 400 错误")
        void shouldReturnErrorWhenNotFound() throws Exception {
            when(orderApplicationService.getOrder("NOT-EXIST"))
                    .thenThrow(new BusinessException("1002", "订单不存在"));

            mockMvc.perform(get("/api/orders/NOT-EXIST"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("1002"));
        }
    }

    private CreateOrderRequest createValidRequest() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId("P001");
        item.setProductName("宫保鸡丁");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("25.00"));

        DeliveryInfoRequest delivery = new DeliveryInfoRequest();
        delivery.setReceiverName("张三");
        delivery.setReceiverPhone("13800138000");
        delivery.setReceiverAddress("北京市朝阳区");

        CreateOrderRequest request = new CreateOrderRequest();
        request.setMerchantId("M001");
        request.setItems(List.of(item));
        request.setDeliveryInfo(delivery);
        request.setNote("少放辣");
        return request;
    }
}
