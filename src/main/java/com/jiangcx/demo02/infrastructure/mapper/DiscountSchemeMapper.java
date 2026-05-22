package com.jiangcx.demo02.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiangcx.demo02.infrastructure.persistence.po.DiscountSchemePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DiscountSchemeMapper extends BaseMapper<DiscountSchemePO> {
}
