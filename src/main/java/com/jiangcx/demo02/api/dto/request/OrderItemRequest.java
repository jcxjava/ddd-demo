package com.jiangcx.demo02.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {
    @NotBlank(message = "餐品ID不能为空")
    private String productId;

    @NotBlank(message = "餐品名称不能为空")
    private String productName;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    @Min(value = 0, message = "单价不能小于0")
    private BigDecimal unitPrice;
}
