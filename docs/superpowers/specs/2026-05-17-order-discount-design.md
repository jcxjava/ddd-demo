# 订单折扣方案设计文档

## 需求概述

下单时自动查询商家当前生效的折扣方案，自动选择优惠金额最大的方案应用到订单，并保存折扣详情、返回给前端。

## 折扣方案类型（5种）

| 类型 | 说明 | 示例 |
|------|------|------|
| THRESHOLD | 满减：满足金额门槛减免固定金额 | 满50元减5元 |
| RATE | 商家折扣：全场按折扣率计算 | 全场8折（discountRate=0.8） |
| SPECIAL_PRICE | 商家特价：指定菜品按特价销售 | 某菜品原价30元，特价25元 |
| BUY_N_GET_M | 买N送M：同款商品买N件送M件 | 买2送1 |
| DELIVERY_FEE | 配送费减免：减免部分或全部配送费 | 配送费减3元 |

## 业务规则

- 同一时间一个商家的多个生效方案互斥，系统自动选优惠金额最大的
- 折扣信息持久化到订单（快照），后续商家改折扣不影响已有订单
- 折扣方案在 `discount_schemes` 表中维护，手工管理

## 新增数据表

```sql
CREATE TABLE discount_schemes (
    id VARCHAR(32) PRIMARY KEY COMMENT '方案ID',
    merchant_id VARCHAR(32) NOT NULL COMMENT '商家ID',
    type VARCHAR(30) NOT NULL COMMENT '方案类型: THRESHOLD/RATE/SPECIAL_PRICE/BUY_N_GET_M/DELIVERY_FEE',
    name VARCHAR(100) NOT NULL COMMENT '方案名称',
    config JSON NOT NULL COMMENT '配置参数(JSON)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',
    start_time DATETIME COMMENT '生效开始时间',
    end_time DATETIME COMMENT '生效结束时间',
    priority INT DEFAULT 0 COMMENT '优先级(预留)',
    create_time DATETIME NOT NULL COMMENT '创建时间'
);
```

### 各类型 config JSON 结构

```json
// THRESHOLD: {"threshold": 50.00, "discountAmount": 5.00}
// RATE: {"discountRate": 0.80}
// SPECIAL_PRICE: {"productId": "P001", "specialPrice": 25.00}
// BUY_N_GET_M: {"productId": "P001", "buyQuantity": 2, "freeQuantity": 1}
// DELIVERY_FEE: {"discountAmount": 3.00}
```

## 订单表增加字段

```sql
ALTER TABLE orders ADD COLUMN discount_detail JSON COMMENT '折扣详情(快照)';
```

discountDetail JSON 结构：
```json
{
  "schemeId": "DS001",
  "schemeName": "满50减5",
  "type": "THRESHOLD",
  "discountAmount": 5.00,
  "originalTotal": 55.00,
  "discountedTotal": 50.00
}
```

## 新增文件

```
domain/
├── model/
│   ├── enums/DiscountType.java
│   └── valueobject/
│       ├── DiscountDetail.java       # 折扣详情值对象
│       └── DiscountScheme.java       # 折扣方案值对象
├── service/discount/
│   ├── DiscountEngine.java           # 折扣引擎
│   ├── DiscountStrategy.java         # 策略接口
│   └── strategy/
│       ├── ThresholdDiscountStrategy.java
│       ├── RateDiscountStrategy.java
│       ├── SpecialPriceStrategy.java
│       ├── BuyNGetMStrategy.java
│       └── DeliveryFeeDiscountStrategy.java
infrastructure/
├── persistence/po/DiscountSchemePO.java
├── mapper/DiscountSchemeMapper.java
└── repository/DiscountSchemeRepository.java
```

## 修改文件

| 文件 | 改动 |
|------|------|
| Order.java | 增加 discountDetail 字段 |
| OrderDO.java | 增加 discountDetail 字段（JSON） |
| PriceCalculationService.java | 集成 DiscountEngine |
| OrderApplicationService.java | 注入 DiscountEngine |
| OrderResponse.java | 增加 discountDetail 返回 |
| OrderRepositoryImpl.java | DO↔实体转换增加 discountDetail |

## 核心计算流程

```
PriceCalculationService.calculatePrice(order)
  1. 计算餐品总价 subtotal
  2. 调用 DiscountEngine.selectBestDiscount(merchantId, order)
     a. 查询该商家所有ACTIVE且在有效期内的方案
     b. 逐个策略计算折扣金额
     c. 返回优惠金额最大的方案
  3. 应用折扣: totalAmount = subtotal + packingFee + deliveryFee - discountAmount
  4. 将折扣详情快照写入 order.discountDetail
```

## 策略计算细节

| 策略 | 计算逻辑 | 适用条件 |
|------|---------|---------|
| ThresholdDiscountStrategy | 若 subtotal >= threshold，优惠 = discountAmount | subtotal >= threshold |
| RateDiscountStrategy | 优惠 = subtotal * (1 - discountRate) | 无门槛 |
| SpecialPriceStrategy | 优惠 = (原价 - 特价) * 购买数量 | 订单含该商品 |
| BuyNGetMStrategy | 优惠 = 单价 * freeQuantity * (buyQuantity / buyN) 取整 | 该商品购买量 >= N |
| DeliveryFeeDiscountStrategy | 优惠 = min(discountAmount, deliveryFee) | 无门槛 |
