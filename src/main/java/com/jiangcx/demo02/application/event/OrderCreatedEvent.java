package com.jiangcx.demo02.application.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 订单创建成功事件，用于解耦后续流程（如短信通知、数据统计等），不阻塞主流程
 */
@Getter
public class OrderCreatedEvent {
    private final String orderId;
    private final String orderNo;
    private final String userId;
    private final LocalDateTime occurredAt;

    public OrderCreatedEvent(String orderId, String orderNo, String userId) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.userId = userId;
        this.occurredAt = LocalDateTime.now();
    }
}
