package com.jiangcx.demo02.domain.service.discount;

import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.model.valueobject.DiscountScheme;

import java.util.List;

/**
 * 优惠方案仓储接口，定义查询商户有效优惠方案的契约
 */
public interface DiscountSchemeRepository {
    List<DiscountScheme> findActiveByMerchantId(String merchantId);
}
