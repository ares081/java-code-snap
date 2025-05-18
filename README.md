
- 自定义ThreadFactory：DefaultThreadFactory
- 基于LinkedTransferQueue实现自定义线程池：DefaultThreadPoolExecutor
- 基于LinkedTransferQueue实现通用池：TransferQueueConnectionPool
- 仿kafka时间轮实现
- Reactor模型实现
- 一致性Hash实现
  - 虚拟节点
  - 热点问题解决
- JpaQueryHelper: 简化JPA动态条件查询
  ```java
  Specification<SaleOrderEntity> spec = JpaQueryHelper.and(
        JpaQueryHelper.equals("orderId", request.getOrderId()),
        JpaQueryHelper.equals("userId", request.getUserId()),
        JpaQueryHelper.equals("state", request.getState())
    );
  List<SaleOrderEntity> entities = saleOrderRepository.findAll(spec);
  ```
  在Repository中增加：JpaSpecificationExecutor 接口
  ```java
  @Repository
  public interface SaleOrderRepository extends JpaRepository<SaleOrderEntity, Long>,
  JpaSpecificationExecutor<SaleOrderEntity> {}
  ```