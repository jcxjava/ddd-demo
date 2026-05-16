package com.jiangcx.demo02.domain.model.entity;

import com.jiangcx.demo02.domain.model.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Order {
    private String orderId;
    private String orderNo;
    private String userId;
    private String merchantId;
    private List<OrderItem> items;
    private DeliveryInfo deliveryInfo;
    private BigDecimal subtotal;
    private BigDecimal packingFee;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String note;
    private OrderStatus status;
    private LocalDateTime createTime;

    public Order() {
        this.items = new ArrayList<>();
    }

    public Order(String userId, String merchantId, DeliveryInfo deliveryInfo, String note) {
        this();
        this.orderId = UUID.randomUUID().toString().replace("-", "");
        this.userId = userId;
        this.merchantId = merchantId;
        this.deliveryInfo = deliveryInfo;
        this.note = note;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.createTime = LocalDateTime.now();
        this.packingFee = BigDecimal.valueOf(1.00);
        this.deliveryFee = BigDecimal.valueOf(3.00);
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotal();
    }

    public void removeItem(String productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.subtotal = items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.add(this.packingFee).add(this.deliveryFee);
    }

    public boolean canCancel() {
        return OrderStatus.PENDING_PAYMENT.equals(this.status);
    }
}