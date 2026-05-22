package com.jiangcx.demo02.domain.model.valueobject;

import com.jiangcx.demo02.domain.model.enums.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 优惠方案配置值对象，从商户配置加载；config为动态参数（如门槛金额、折扣率等），由各策略自行解析
 */
@Data
public class DiscountScheme {
    private String id;
    private String merchantId;
    private DiscountType type;
    private String name;
    private Map<String, Object> config;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer priority;
}
