package com.ares.delay.redis;

import com.ares.delay.OrderInfo;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public class OrderDelayTaskExecutor extends AbstractRedisDelayQueueExecutor<OrderInfo> {

  private static final Logger logger = LoggerFactory.getLogger(OrderDelayTaskExecutor.class);

  public OrderDelayTaskExecutor(StringRedisTemplate redisTemplate, JsonMapper jsonMapper) {
    super(redisTemplate, jsonMapper);
  }

  @Override
  protected void bizHandle(OrderInfo obj) {
    logger.info("order delay handle, orderId={}, userId={}, skuId={}", obj.getOrderId(),
        obj.getUserId(), obj.getSkuId());
  }
}
