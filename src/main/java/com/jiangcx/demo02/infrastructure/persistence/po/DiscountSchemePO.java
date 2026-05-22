package com.jiangcx.demo02.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("discount_schemes")
public class DiscountSchemePO {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    private String merchantId;
    private String type;
    private String name;
    private String config;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer priority;
    private LocalDateTime createTime;
}
