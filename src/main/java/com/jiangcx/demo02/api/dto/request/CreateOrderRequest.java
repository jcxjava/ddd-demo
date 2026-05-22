package com.jiangcx.demo02.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "商户ID不能为空")
    private String merchantId;

    @NotEmpty(message = "餐品列表不能为空")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "配送信息不能为空")
    @Valid
    private DeliveryInfoRequest deliveryInfo;

    @Size(max = 200, message = "备注最多200字符")
    private String note;
}
