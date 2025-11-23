package com.ares.delay.redis;

import com.ares.delay.DelayTask;
import com.ares.delay.OrderInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisDelayQueueExample {

  private final RedisTemplate<String, String> redisTemplate;
  private final JsonMapper jsonMapper;

  public RedisDelayQueueExample(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.jsonMapper = new JsonMapper();
  }

  public void redisDelayQueue(String[] args) throws JsonProcessingException {
    OrderInfo o1 = new OrderInfo(100001L, 10000L, 100000L, "o1");
    OrderInfo o2 = new OrderInfo(100002L, 10000L, 100000L, "o2");
    OrderInfo o3 = new OrderInfo(100003L, 10000L, 100000L, "o3");

    DelayTask<OrderInfo> d1 = new DelayTask<>("order-service", o1, 5 * 1000L,
        System.currentTimeMillis() + 5 * 1000L);
    DelayTask<OrderInfo> d2 = new DelayTask<>("order-service", o2, 5 * 1000L,
        System.currentTimeMillis() + 5 * 1000L);
    DelayTask<OrderInfo> d3 = new DelayTask<>("order-service", o3, 5 * 1000L,
        System.currentTimeMillis() + 5 * 1000L);

    redisTemplate.opsForZSet()
        .add("order:delay", jsonMapper.writeValueAsString(d1), d1.expireTime());
    redisTemplate.opsForZSet()
        .add("order:delay", jsonMapper.writeValueAsString(d2), d2.expireTime());
    redisTemplate.opsForZSet()
        .add("order:delay", jsonMapper.writeValueAsString(d3), d3.expireTime());

  }

}
