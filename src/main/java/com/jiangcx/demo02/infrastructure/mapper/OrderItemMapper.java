package com.jiangcx.demo02.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiangcx.demo02.infrastructure.persistence.po.OrderItemDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemDO> {
}
