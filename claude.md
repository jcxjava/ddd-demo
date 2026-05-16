# 订餐平台订单系统开发规范

## 开发流程

### 第一步：需求分析和架构设计
基于SpringBoot + MyBatis-Plus技术栈，采用轻量化DDD思想，解析业务需求并完成架构设计。
- 梳理核心业务流程、实体关系和业务边界
- 输出标准DDD四层架构：接口层、应用层、领域层、基础设施层
- 明确各层职责、数据流向、依赖关系和标准项目目录结构
- 规划全局统一响应、全局异常、分页通用组件
- 给出标准化开发顺序

### 第二步：全局编码约束
后续所有代码严格遵守以下约束，禁止私自改动：
- 技术栈固定：SpringBoot、MyBatis-Plus、Lombok、MapStruct、参数校验框架
- DDD分层规则
- 编码规范
- 对象分离原则

### 第三步：原子化分步开发
1. **定义领域实体、业务枚举、核心业务规则**
   - 创建Order、OrderItem、DeliveryInfo等实体
   - 定义OrderStatus等业务枚举
   - 实现价格计算等核心业务规则
   
2. **开发仓储接口及MP数据库实现层**
   - 创建IOrderRepository接口
   - 实现OrderRepositoryImpl
   - 编写MyBatis Mapper
   
3. **开发Controller接口，实现参数校验与结果封装**
   - 创建OrderController
   - 实现参数校验
   - 统一响应封装
   
4. **对已生成代码做全量审查、纠错与优化**
   - 代码风格检查
   - 业务逻辑验证
   - 性能优化
   
5. **优化事务逻辑，修补潜在业务漏洞**
   - 事务边界定义
   - 并发控制
   - 异常处理完善
   
6. **分阶段开发与验证**
   - 每完成一个功能模块，进行人工检查
   - 确认无误后再继续后续开发
   - 及时修复发现的问题，避免问题累积

## 需求迭代变更规范

**核心原则：**
- 严格遵守：固定整体DDD架构、目录结构、全局公共组件不变
- 仅增量修改DTO、领域业务规则、应用层流程
- 复用原有工具、配置、转换逻辑，不重构底层架构，保证项目稳定性

**迭代策略：**
1. 新功能开发时，复用现有分层结构
2. 业务规则变更时，只修改领域层相关代码
3. 接口变更时，只调整DTO和Controller
4. 数据库变更时，只修改DO和Mapper

## 技术栈约束

**固定技术栈：**
- SpringBoot 3.5.14
- MyBatis-Plus 3.5.16
- Lombok
- MapStruct
- 参数校验框架（JSR 303）
- MySQL 8.0+
- Java 17

## DDD分层规则

### 1. Controller层（接口层）
- **职责**：仅做参数接收、校验、接口响应
- **禁止事项**：
  - 包含任何业务逻辑
  - 直接调用Repository
  - 跨Service调用
- **允许操作**：
  - 接收HTTP请求
  - 参数校验（@Valid）
  - 返回统一响应结构
  - 调用应用层Service

### 2. 应用层（Application Layer）
- **职责**：负责流程编排、事务控制、能力聚合
- **禁止事项**：
  - 包含核心业务规则
  - 直接操作数据库
- **允许操作**：
  - 协调多个领域服务
  - 管理事务边界
  - 处理用例流程
  - 调用基础设施层

### 3. 领域层（Domain Layer）
- **职责**：承载核心业务规则、实体、业务枚举，纯业务逻辑
- **禁止事项**：
  - 依赖外部框架
  - 包含技术实现细节
  - 直接依赖数据库
- **允许操作**：
  - 业务规则实现
  - 实体行为定义
  - 领域服务封装
  - 值对象设计

### 4. 基础设施层（Infrastructure Layer）
- **职责**：仅做数据库操作、工具实现、外部能力适配
- **禁止事项**：
  - 包含业务逻辑
  - 直接暴露给上层
- **允许操作**：
  - 数据库CRUD操作
  - 外部系统集成
  - 工具类实现
  - 配置管理

## 编码规范

### 1. 对象分离原则
- **DO（Data Object）**：数据库表映射对象，仅用于数据持久化
- **领域实体**：包含业务逻辑和行为的对象
- **DTO/VO**：数据传输对象，用于接口层交互
- **严禁混用**：三者职责分离，禁止直接转换

### 2. 对象转换规范
- **使用MapStruct**进行对象转换
- **禁止手写getter/setter**转换
- **转换位置**：在应用层进行转换
- **示例**：
```java
@Mapper
public interface OrderMapper {
    OrderDTO toDTO(Order order);
    Order toEntity(OrderDTO dto);
}
```

### 3. RESTful接口规范
- **统一三段式返回结构**：
  ```java
  {
    "code": 200,
    "message": "success",
    "data": {}
  }
  ```
- **HTTP状态码规范**：
  - 200：成功
  - 400：参数错误
  - 401：未授权
  - 500：系统错误

### 4. 校验规范
- **所有入参必填校验**：
  ```java
  @NotNull(message = "商户ID不能为空")
  private String merchantId;
  ```
- **业务状态合法性校验**：
  ```java
  @AssertTrue(message = "订单状态不合法")
  private boolean isStatusValid();
  ```

### 5. 异常处理规范
- **自定义业务异常**：
  ```java
  public class BusinessException extends RuntimeException {
      private int code;
      private String message;
  }
  ```
- **全局统一捕获**：
  ```java
  @RestControllerAdvice
  public class GlobalExceptionHandler {
      @ExceptionHandler(BusinessException.class)
      public ApiResponse<?> handleBusinessException(BusinessException e) {
          return ApiResponse.fail(e.getCode(), e.getMessage());
      }
  }
  ```

## 项目结构规范

```
src/main/java/com/jiangcx/demo02/
├── api/                    # 接口层
│   ├── controller/        # 控制器
│   └── dto/              # 数据传输对象
├── application/          # 应用层
│   ├── service/          # 应用服务
│   ├── command/          # 命令对象
│   └── event/            # 事件对象
├── domain/               # 领域层
│   ├── model/            # 领域模型
│   ├── service/          # 领域服务
│   └── repository/       # 仓库接口
└── infrastructure/      # 基础设施层
    ├── repository/       # 仓库实现
    ├── mapper/          # MyBatis Mapper
    └── config/          # 配置类
```

## 其他约束

1. **禁止私自改动技术栈**
2. **严格遵守分层规则**
3. **代码必须通过SonarQube检查**
4. **单元测试覆盖率不低于80%**
5. **API文档必须通过Swagger生成**