package com.jiangcx.demo02.domain.service.discount;

import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.model.valueobject.DiscountScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 优惠引擎，遍历商户所有有效优惠方案，通过策略链计算出优惠金额最大的方案
 */
@Service
@RequiredArgsConstructor
public class DiscountEngine {

    private final DiscountSchemeRepository discountSchemeRepository;
    private final List<DiscountStrategy> strategies;

    /** 对所有可用策略逐一计算，返回优惠金额最大的方案；无可用的优惠时返回empty */
    public Optional<DiscountDetail> selectBestDiscount(Order order) {
        List<DiscountScheme> schemes = discountSchemeRepository.findActiveByMerchantId(order.getMerchantId());

        return schemes.stream()
                .flatMap(scheme -> strategies.stream()
                        .filter(s -> s.supportedType() == scheme.getType())
                        .flatMap(strategy -> strategy.calculate(order, scheme).stream()))
                .max(Comparator.comparing(DiscountDetail::getDiscountAmount));
    }
}
