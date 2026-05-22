package com.jiangcx.demo02.api.controller;

import com.jiangcx.demo02.api.dto.request.CreateOrderRequest;
import com.jiangcx.demo02.api.dto.response.OrderResponse;
import com.jiangcx.demo02.application.command.CreateOrderCommand;
import com.jiangcx.demo02.application.service.OrderApplicationService;
import com.jiangcx.demo02.common.model.api.ApiResponse;
import com.jiangcx.demo02.common.model.api.PageResult;
import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.entity.OrderItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "订单管理", description = "订单创建、查询、取消相关接口")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    @Operation(summary = "创建订单")
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = toCommand(request);
        Order order = orderApplicationService.createOrder(command);
        return ApiResponse.success(toResponse(order));
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @Parameter(description = "订单ID") @PathVariable String orderId) {
        Order order = orderApplicationService.getOrder(orderId);
        return ApiResponse.success(toResponse(order));
    }

    @Operation(summary = "查询订单列表")
    @GetMapping
    public ApiResponse<PageResult<OrderResponse>> listOrders(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        PageResult<Order> pageResult = orderApplicationService.listOrders(userId, page, size);

        PageResult<OrderResponse> response = new PageResult<>();
        response.setTotal(pageResult.getTotal());
        response.setSize(pageResult.getSize());
        response.setCurrent(pageResult.getCurrent());
        response.setRecords(pageResult.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
        return ApiResponse.success(response);
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable String orderId) {
        Order order = orderApplicationService.cancelOrder(orderId);
        return ApiResponse.success(toResponse(order));
    }

    private CreateOrderCommand toCommand(CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand();
        command.setUserId(getCurrentUserId());
        command.setMerchantId(request.getMerchantId());
        command.setNote(request.getNote());

        CreateOrderCommand.DeliveryInfoCommand deliveryCmd = new CreateOrderCommand.DeliveryInfoCommand();
        deliveryCmd.setReceiverName(request.getDeliveryInfo().getReceiverName());
        deliveryCmd.setReceiverPhone(request.getDeliveryInfo().getReceiverPhone());
        deliveryCmd.setReceiverAddress(request.getDeliveryInfo().getReceiverAddress());
        command.setDeliveryInfo(deliveryCmd);

        List<CreateOrderCommand.OrderItemCommand> items = new ArrayList<>();
        for (var itemReq : request.getItems()) {
            CreateOrderCommand.OrderItemCommand itemCmd = new CreateOrderCommand.OrderItemCommand();
            itemCmd.setProductId(itemReq.getProductId());
            itemCmd.setProductName(itemReq.getProductName());
            itemCmd.setQuantity(itemReq.getQuantity());
            itemCmd.setUnitPrice(itemReq.getUnitPrice());
            items.add(itemCmd);
        }
        command.setItems(items);
        return command;
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setUserId(order.getUserId());
        response.setMerchantId(order.getMerchantId());
        response.setSubtotal(order.getSubtotal());
        response.setPackingFee(order.getPackingFee());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTotalAmount(order.getTotalAmount());
        response.setNote(order.getNote());
        response.setStatus(order.getStatus().getCode());
        response.setCreateTime(order.getCreateTime());

        if (order.getDiscountDetail() != null) {
            OrderResponse.DiscountDetailResponse discountResp = new OrderResponse.DiscountDetailResponse();
            discountResp.setSchemeId(order.getDiscountDetail().getSchemeId());
            discountResp.setSchemeName(order.getDiscountDetail().getSchemeName());
            discountResp.setType(order.getDiscountDetail().getType().getCode());
            discountResp.setTypeDesc(order.getDiscountDetail().getType().getDescription());
            discountResp.setDiscountAmount(order.getDiscountDetail().getDiscountAmount());
            discountResp.setOriginalTotal(order.getDiscountDetail().getOriginalTotal());
            discountResp.setDiscountedTotal(order.getDiscountDetail().getDiscountedTotal());
            response.setDiscountDetail(discountResp);
        }

        if (order.getDeliveryInfo() != null) {
            OrderResponse.DeliveryInfoResponse deliveryResp = new OrderResponse.DeliveryInfoResponse();
            deliveryResp.setReceiverName(order.getDeliveryInfo().getReceiverName());
            deliveryResp.setReceiverPhone(order.getDeliveryInfo().getReceiverPhone());
            deliveryResp.setReceiverAddress(order.getDeliveryInfo().getReceiverAddress());
            response.setDeliveryInfo(deliveryResp);
        }

        List<OrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderResponse.OrderItemResponse itemResp = new OrderResponse.OrderItemResponse();
            itemResp.setProductId(item.getProductId());
            itemResp.setProductName(item.getProductName());
            itemResp.setQuantity(item.getQuantity());
            itemResp.setUnitPrice(item.getUnitPrice());
            itemResp.setTotalPrice(item.getTotalPrice());
            itemResponses.add(itemResp);
        }
        response.setItems(itemResponses);
        return response;
    }

    private String getCurrentUserId() {
        return "U0001";
    }
}
