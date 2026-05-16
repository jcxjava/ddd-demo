package com.jiangcx.demo02.domain.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

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