package com.jiangcx.demo02.application.command;

import lombok.Data;

import java.util.List;

/**
 * 创建订单命令对象，封装接口层传入的原始数据，与领域实体解耦
 */
@Data
public class CreateOrderCommand {
    private String userId;
    private String merchantId;
    private List<OrderItemCommand> items;
    private DeliveryInfoCommand deliveryInfo;
    private String note;

    @Data
    public static class OrderItemCommand {
        private String productId;
        private String productName;
        private int quantity;
        private java.math.BigDecimal unitPrice;
    }

    @Data
    public static class DeliveryInfoCommand {
        private String receiverName;
        private String receiverPhone;
        private String receiverAddress;
    }
}
