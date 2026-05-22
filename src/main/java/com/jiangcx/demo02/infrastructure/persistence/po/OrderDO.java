package com.jiangcx.demo02.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class OrderDO {
    @TableId(value = "order_id", type = IdType.ASSIGN_ID)
    private String orderId;
    private String orderNo;
    private String userId;
    private String merchantId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private BigDecimal subtotal;
    private BigDecimal packingFee;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String note;
    private String discountDetail;
    private String status;
    private LocalDateTime createTime;
}
