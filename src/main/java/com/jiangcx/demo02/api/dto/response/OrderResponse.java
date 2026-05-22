package com.jiangcx.demo02.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private String orderId;
    private String orderNo;
    private String userId;
    private String merchantId;
    private List<OrderItemResponse> items;
    private DeliveryInfoResponse deliveryInfo;
    private BigDecimal subtotal;
    private BigDecimal packingFee;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String note;
    private String status;
    private DiscountDetailResponse discountDetail;
    private LocalDateTime createTime;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DiscountDetailResponse {
        private String schemeId;
        private String schemeName;
        private String type;
        private String typeDesc;
        private BigDecimal discountAmount;
        private BigDecimal originalTotal;
        private BigDecimal discountedTotal;
    }

    @Data
    public static class OrderItemResponse {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    @Data
    public static class DeliveryInfoResponse {
        private String receiverName;
        private String receiverPhone;
        private String receiverAddress;
    }
}
