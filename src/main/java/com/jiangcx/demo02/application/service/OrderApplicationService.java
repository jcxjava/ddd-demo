package com.jiangcx.demo02.application.service;

import com.jiangcx.demo02.application.command.CreateOrderCommand;
import com.jiangcx.demo02.application.event.OrderCreatedEvent;
import com.jiangcx.demo02.common.exception.BusinessException;
import com.jiangcx.demo02.common.model.api.PageResult;
import com.jiangcx.demo02.domain.model.entity.DeliveryInfo;
import com.jiangcx.demo02.domain.model.entity.Order;
import com.jiangcx.demo02.domain.model.entity.OrderItem;
import com.jiangcx.demo02.domain.repository.IOrderRepository;
import com.jiangcx.demo02.domain.service.pricing.PriceCalculationService;
import com.jiangcx.demo02.domain.service.validation.OrderValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单应用服务，编排领域服务完成订单创建、查询、取消等完整用例流程
 */
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final IOrderRepository orderRepository;
    private final PriceCalculationService priceCalculationService;
    private final OrderValidationService orderValidationService;
    private final ApplicationEventPublisher eventPublisher;

    /** 创建订单全流程：组装实体→校验→计价→持久化→发布事件，整个流程在同一事务中保证原子性 */
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        DeliveryInfo deliveryInfo = new DeliveryInfo(
                command.getDeliveryInfo().getReceiverName(),
                command.getDeliveryInfo().getReceiverPhone(),
                command.getDeliveryInfo().getReceiverAddress()
        );

        Order order = new Order(command.getUserId(), command.getMerchantId(), deliveryInfo, command.getNote());

        for (CreateOrderCommand.OrderItemCommand itemCmd : command.getItems()) {
            OrderItem item = new OrderItem(
                    itemCmd.getProductId(),
                    itemCmd.getProductName(),
                    itemCmd.getQuantity(),
                    itemCmd.getUnitPrice()
            );
            order.addItem(item);
        }

        orderValidationService.validate(order);
        priceCalculationService.calculateOrderPrice(order);
        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(order.getOrderId(), order.getOrderNo(), order.getUserId()));

        return order;
    }

    /** 按ID查询订单，不存在时抛出业务异常 */
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("1002", "订单不存在"));
    }

    /** 分页查询用户订单列表 */
    public PageResult<Order> listOrders(String userId, int page, int size) {
        List<Order> orders = orderRepository.findByUserIdWithPage(userId, page, size);
        long total = orderRepository.countByUserId(userId);

        PageResult<Order> result = new PageResult<>();
        result.setRecords(orders);
        result.setTotal(total);
        result.setSize(size);
        result.setCurrent(page);
        return result;
    }

    /** 取消订单：仅待支付状态允许取消，变更状态后持久化 */
    @Transactional
    public Order cancelOrder(String orderId) {
        Order order = getOrder(orderId);
        if (!order.canCancel()) {
            throw new BusinessException("1004", "当前订单状态不允许取消");
        }
        order.setStatus(com.jiangcx.demo02.domain.model.enums.OrderStatus.CANCELLED);
        orderRepository.updateStatus(order.getOrderId(), order.getStatus());
        return order;
    }
}
