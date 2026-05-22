package com.jiangcx.demo02.domain.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配送信息值对象，不可变地描述收货人、电话和地址
 */
@Data
@NoArgsConstructor
public class DeliveryInfo {
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    public DeliveryInfo(String receiverName, String receiverPhone, String receiverAddress) {
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverAddress = receiverAddress;
    }
}