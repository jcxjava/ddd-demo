package com.jiangcx.demo02.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeliveryInfoRequest {
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "手机号不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;
}
