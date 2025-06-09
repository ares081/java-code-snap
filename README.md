
- 自定义ThreadFactory：DefaultThreadFactory
  - 增加MDC透传
- 基于LinkedTransferQueue实现自定义线程池：DefaultThreadPoolExecutor
- 基于LinkedTransferQueue实现通用池：TransferQueueConnectionPool
- 仿kafka时间轮实现
- Reactor模型实现
- 一致性Hash实现
  - 虚拟节点
  - 热点问题解决
- FieldReference: 优化JpaQueryHelper中使用字段名
- JpaQueryHelper: 简化JPA动态条件查询构建
  ```java
  Specification<SaleOrderEntity> spec = JpaQueryHelper.and(
        JpaQueryHelper.equals(FieldReference.of(SaleOrderEntity::getOrderId).name(),
            request.getOrderId()),
        JpaQueryHelper.equals(FieldReference.of(SaleOrderEntity::getUserId).name(),
            request.getUserId()),
        JpaQueryHelper.equals(FieldReference.of(SaleOrderEntity::getState).name(),
            request.getState()));

  List<SaleOrderEntity> entities = saleOrderRepository.findAll(spec);
  ```
  在Repository中增加：JpaSpecificationExecutor 接口
  ```java
  @Repository
  public interface SaleOrderRepository extends JpaRepository<SaleOrderEntity, Long>,
  JpaSpecificationExecutor<SaleOrderEntity> {}
  ```
- ElasticQueryBuilder：简化Elastic动态条件查询构建
  ```java
  ElasticQueryBuilder<SaleOrderAdminEntity> query =
        ElasticQueryBuilder.<SaleOrderAdminEntity>of()
            .withTerm(FieldReference.of(SaleOrderAdminEntity::getOrderId), request.getOrderId())
            .withTerm(FieldReference.of(SaleOrderAdminEntity::getUserId), request.getUserId())
            .withTerm(FieldReference.of(SaleOrderAdminEntity::getOrderStatus),
                request.getState());
  
  NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
  queryBuilder.withQuery(query.build()._toQuery());
  ```