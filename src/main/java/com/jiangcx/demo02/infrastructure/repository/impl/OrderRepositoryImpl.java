package com.jiangcx.demo02.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangcx.demo02.domain.model.entity.DeliveryInfo;
import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.entity.OrderItem;
import com.jiangcx.demo02.domain.model.enums.DiscountType;
import com.jiangcx.demo02.domain.model.enums.OrderStatus;
import com.jiangcx.demo02.domain.model.valueobject.DiscountDetail;
import com.jiangcx.demo02.domain.repository.IOrderRepository;
import com.jiangcx.demo02.infrastructure.mapper.OrderItemMapper;
import com.jiangcx.demo02.infrastructure.mapper.OrderMapper;
import com.jiangcx.demo02.infrastructure.persistence.po.OrderDO;
import com.jiangcx.demo02.infrastructure.persistence.po.OrderItemDO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements IOrderRepository {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void save(Order order) {
        OrderDO orderDO = toOrderDO(order);
        orderMapper.insert(orderDO);

        for (OrderItem item : order.getItems()) {
            OrderItemDO itemDO = toOrderItemDO(item, order.getOrderId());
            orderItemMapper.insert(itemDO);
        }
    }

    @Override
    public Optional<Order> findById(String orderId) {
        OrderDO orderDO = orderMapper.selectById(orderId);
        if (orderDO == null) {
            return Optional.empty();
        }
        List<OrderItemDO> itemDOs = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemDO>()
                        .eq(OrderItemDO::getOrderId, orderId));
        return Optional.of(toOrder(orderDO, itemDOs));
    }

    @Override
    public List<Order> findByUserId(String userId) {
        List<OrderDO> orderDOs = orderMapper.selectList(
                new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getUserId, userId)
                        .orderByDesc(OrderDO::getCreateTime));
        return orderDOs.stream().map(od -> {
            List<OrderItemDO> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItemDO>()
                            .eq(OrderItemDO::getOrderId, od.getOrderId()));
            return toOrder(od, items);
        }).collect(Collectors.toList());
    }

    @Override
    public List<Order> findByUserIdWithPage(String userId, int page, int size) {
        Page<OrderDO> pageParam = new Page<>(page, size);
        Page<OrderDO> pageResult = orderMapper.selectPage(pageParam,
                new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getUserId, userId)
                        .orderByDesc(OrderDO::getCreateTime));
        return pageResult.getRecords().stream().map(od -> {
            List<OrderItemDO> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItemDO>()
                            .eq(OrderItemDO::getOrderId, od.getOrderId()));
            return toOrder(od, items);
        }).collect(Collectors.toList());
    }

    @Override
    public long countByUserId(String userId) {
        return orderMapper.selectCount(
                new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getUserId, userId));
    }

    @Override
    public void updateStatus(String orderId, OrderStatus status) {
        OrderDO updateDO = new OrderDO();
        updateDO.setStatus(status.getCode());
        orderMapper.update(updateDO,
                new LambdaQueryWrapper<OrderDO>()
                        .eq(OrderDO::getOrderId, orderId));
    }

    private OrderDO toOrderDO(Order order) {
        OrderDO orderDO = new OrderDO();
        orderDO.setOrderId(order.getOrderId());
        orderDO.setOrderNo(order.getOrderNo());
        orderDO.setUserId(order.getUserId());
        orderDO.setMerchantId(order.getMerchantId());
        if (order.getDeliveryInfo() != null) {
            orderDO.setReceiverName(order.getDeliveryInfo().getReceiverName());
            orderDO.setReceiverPhone(order.getDeliveryInfo().getReceiverPhone());
            orderDO.setReceiverAddress(order.getDeliveryInfo().getReceiverAddress());
        }
        orderDO.setSubtotal(order.getSubtotal());
        orderDO.setPackingFee(order.getPackingFee());
        orderDO.setDeliveryFee(order.getDeliveryFee());
        orderDO.setTotalAmount(order.getTotalAmount());
        orderDO.setNote(order.getNote());
        if (order.getDiscountDetail() != null) {
            try {
                orderDO.setDiscountDetail(objectMapper.writeValueAsString(order.getDiscountDetail()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize discount detail", e);
            }
        }
        orderDO.setStatus(order.getStatus().getCode());
        orderDO.setCreateTime(order.getCreateTime());
        return orderDO;
    }

    private OrderItemDO toOrderItemDO(OrderItem item, String orderId) {
        OrderItemDO itemDO = new OrderItemDO();
        itemDO.setId(item.getId());
        itemDO.setOrderId(orderId);
        itemDO.setProductId(item.getProductId());
        itemDO.setProductName(item.getProductName());
        itemDO.setQuantity(item.getQuantity());
        itemDO.setUnitPrice(item.getUnitPrice());
        itemDO.setTotalPrice(item.getTotalPrice());
        return itemDO;
    }

    private DiscountDetail parseDiscountDetail(String json) {
        try {
            Map<String, Object> map = objectMapper.readValue(json,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            DiscountDetail detail = new DiscountDetail();
            detail.setSchemeId((String) map.get("schemeId"));
            detail.setSchemeName((String) map.get("schemeName"));
            detail.setType(DiscountType.fromCode((String) map.get("type")));
            detail.setDiscountAmount(new BigDecimal(map.get("discountAmount").toString()));
            detail.setOriginalTotal(new BigDecimal(map.get("originalTotal").toString()));
            detail.setDiscountedTotal(new BigDecimal(map.get("discountedTotal").toString()));
            return detail;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse discount detail", e);
        }
    }

    private Order toOrder(OrderDO orderDO, List<OrderItemDO> itemDOs) {
        DeliveryInfo deliveryInfo = new DeliveryInfo(
                orderDO.getReceiverName(),
                orderDO.getReceiverPhone(),
                orderDO.getReceiverAddress()
        );

        Order order = new Order();
        order.setOrderId(orderDO.getOrderId());
        order.setOrderNo(orderDO.getOrderNo());
        order.setUserId(orderDO.getUserId());
        order.setMerchantId(orderDO.getMerchantId());
        order.setDeliveryInfo(deliveryInfo);
        order.setSubtotal(orderDO.getSubtotal());
        order.setPackingFee(orderDO.getPackingFee());
        order.setDeliveryFee(orderDO.getDeliveryFee());
        order.setTotalAmount(orderDO.getTotalAmount());
        order.setNote(orderDO.getNote());
        if (orderDO.getDiscountDetail() != null) {
            order.setDiscountDetail(parseDiscountDetail(orderDO.getDiscountDetail()));
        }
        order.setStatus(OrderStatus.fromCode(orderDO.getStatus()));
        order.setCreateTime(orderDO.getCreateTime());

        List<OrderItem> orderItems = itemDOs.stream().map(ido -> {
            OrderItem item = new OrderItem();
            item.setId(ido.getId());
            item.setProductId(ido.getProductId());
            item.setProductName(ido.getProductName());
            item.setQuantity(ido.getQuantity());
            item.setUnitPrice(ido.getUnitPrice());
            item.setTotalPrice(ido.getTotalPrice());
            return item;
        }).collect(Collectors.toList());
        order.setItems(orderItems);

        return order;
    }
}
