package com.jiangcx.demo02.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangcx.demo02.domain.model.enums.DiscountType;
import com.jiangcx.demo02.domain.model.valueobject.DiscountScheme;
import com.jiangcx.demo02.domain.service.discount.DiscountSchemeRepository;
import com.jiangcx.demo02.infrastructure.mapper.DiscountSchemeMapper;
import com.jiangcx.demo02.infrastructure.persistence.po.DiscountSchemePO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DiscountSchemeRepositoryImpl implements DiscountSchemeRepository {

    private final DiscountSchemeMapper discountSchemeMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<DiscountScheme> findActiveByMerchantId(String merchantId) {
        LocalDateTime now = LocalDateTime.now();
        List<DiscountSchemePO> poList = discountSchemeMapper.selectList(
                new LambdaQueryWrapper<DiscountSchemePO>()
                        .eq(DiscountSchemePO::getMerchantId, merchantId)
                        .eq(DiscountSchemePO::getStatus, "ACTIVE")
                        .le(DiscountSchemePO::getStartTime, now)
                        .ge(DiscountSchemePO::getEndTime, now)
        );
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    private DiscountScheme toDomain(DiscountSchemePO po) {
        DiscountScheme scheme = new DiscountScheme();
        scheme.setId(po.getId());
        scheme.setMerchantId(po.getMerchantId());
        scheme.setType(DiscountType.fromCode(po.getType()));
        scheme.setName(po.getName());
        scheme.setConfig(parseConfig(po.getConfig()));
        scheme.setStatus(po.getStatus());
        scheme.setStartTime(po.getStartTime());
        scheme.setEndTime(po.getEndTime());
        scheme.setPriority(po.getPriority());
        return scheme;
    }

    private Map<String, Object> parseConfig(String configJson) {
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse discount config: " + configJson, e);
        }
    }
}
