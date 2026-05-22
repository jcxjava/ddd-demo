package com.jiangcx.demo02.domain.model.entity;

import com.jiangcx.demo02.domain.model.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单聚合根，封装订单全生命周期的核心业务规则
 */
@Data
public class Order {
    private String orderId;
    /** 对外展示的订单编号，由时间戳+随机数生成，保证唯一性 */
    private String orderNo;
    private String userId;
    private String merchantId;
    private List<OrderItem> items;
    private DeliveryInfo deliveryInfo;
    /** 餐品小计，不含包装费和配送费 */
    private BigDecimal subtotal;
    private BigDecimal packingFee;
    private BigDecimal deliveryFee;
    /** 最终应付金额，小计+包装费+配送费-优惠金额 */
    private BigDecimal totalAmount;
    private String note;
    private OrderStatus status;
    /** 命中的最优优惠明细，无优惠时为null */
    private com.jiangcx.demo02.domain.model.valueobject.DiscountDetail discountDetail;
    private LocalDateTime createTime;

    public Order() {
        this.items = new ArrayList<>();
    }

    /**
     * 创建新订单，自动生成唯一ID和订单编号，初始状态为待支付
     */
    public Order(String userId, String merchantId, DeliveryInfo deliveryInfo, String note) {
        this();
        this.orderId = UUID.randomUUID().toString().replace("-", "");
        this.orderNo = generateOrderNo();
        this.userId = userId;
        this.merchantId = merchantId;
        this.deliveryInfo = deliveryInfo;
        this.note = note;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.createTime = LocalDateTime.now();
        this.packingFee = BigDecimal.valueOf(1.00);
        this.deliveryFee = BigDecimal.valueOf(3.00);
    }

    /** 添加餐品后自动重算金额，保证数据一致性 */
    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotal();
    }

    /** 移除餐品后自动重算金额，保证数据一致性 */
    public void removeItem(String productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotal();
    }

    /** 小计=∑(单价×数量)，总价=小计+包装费+配送费（不含优惠） */
    public void recalculateTotal() {
        this.subtotal = items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.add(this.packingFee).add(this.deliveryFee);
    }

    /** 仅待支付状态的订单可取消，已支付/已取消的订单不可取消 */
    public boolean canCancel() {
        return OrderStatus.PENDING_PAYMENT.equals(this.status);
    }

    private String generateOrderNo() {
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        return datetime + random;
    }
}