package com.jiangcx.demo02.domain.repository;

import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * 订单仓储接口，定义领域层的持久化契约，由基础设施层实现
 */
public interface IOrderRepository {
    void save(Order order);

    Optional<Order> findById(String orderId);

    List<Order> findByUserId(String userId);

    List<Order> findByUserIdWithPage(String userId, int page, int size);

    long countByUserId(String userId);

    void updateStatus(String orderId, OrderStatus status);
}
