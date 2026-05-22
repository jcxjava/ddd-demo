package com.jiangcx.demo02.domain.service.validation;

import com.jiangcx.demo02.common.exception.BusinessException;
import com.jiangcx.demo02.domain.model.entity.Order;
import org.springframework.stereotype.Service;

/**
 * 订单校验领域服务，确保订单数据满足业务约束后才进入后续流程
 */
@Service
public class OrderValidationService {

    /** 对订单执行全部校验规则，校验不通过直接抛出BusinessException */
    public void validate(Order order) {
        validateRequiredFields(order);
        validateNoteLength(order);
    }

    private void validateRequiredFields(Order order) {
        if (order.getMerchantId() == null || order.getMerchantId().isBlank()) {
            throw new BusinessException("1001", "商户ID不能为空");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BusinessException("1001", "餐品列表不能为空");
        }
        if (order.getDeliveryInfo() == null) {
            throw new BusinessException("1001", "配送信息不能为空");
        }
        if (order.getDeliveryInfo().getReceiverName() == null
                || order.getDeliveryInfo().getReceiverName().isBlank()) {
            throw new BusinessException("1001", "收货人姓名不能为空");
        }
        if (order.getDeliveryInfo().getReceiverPhone() == null
                || order.getDeliveryInfo().getReceiverPhone().isBlank()) {
            throw new BusinessException("1001", "手机号不能为空");
        }
        if (order.getDeliveryInfo().getReceiverAddress() == null
                || order.getDeliveryInfo().getReceiverAddress().isBlank()) {
            throw new BusinessException("1001", "收货地址不能为空");
        }
    }

    private void validateNoteLength(Order order) {
        if (order.getNote() != null && order.getNote().length() > 200) {
            throw new BusinessException("1001", "备注信息最多200字符");
        }
    }
}
