package com.ares.delay.redis;

import com.ares.delay.DelayQueueExecutor;
import com.ares.delay.DelayTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

public abstract class AbstractRedisDelayQueueExecutor<T> implements DelayQueueExecutor {

  private static final Logger logger = LoggerFactory.getLogger(
      AbstractRedisDelayQueueExecutor.class);

  private static final String KEY_PREFIX = "order:delay";
  private final StringRedisTemplate redisTemplate;
  private final JsonMapper jsonMapper;

  protected AbstractRedisDelayQueueExecutor(StringRedisTemplate redisTemplate,
      JsonMapper jsonMapper) {
    this.redisTemplate = redisTemplate;
    this.jsonMapper = jsonMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute() {
    while (true) {
      long now = System.currentTimeMillis();
      Set<TypedTuple<String>> delays = redisTemplate.opsForZSet()
          .rangeByScoreWithScores(KEY_PREFIX, 0, now, 0, 10);
      if (delays == null || delays.isEmpty()) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          logger.error("Error processing delay queue for key {}: {}", KEY_PREFIX, e.getMessage(),
              e);
          Thread.currentThread().interrupt();
        }
        continue;
      }

      for (TypedTuple<String> delay : delays) {
        String strTaskValue = delay.getValue();
        if (strTaskValue == null || strTaskValue.isEmpty()) {
          continue;
        }

        try {
          DelayTask<? extends T> biz = jsonMapper.readValue(strTaskValue, DelayTask.class);
          bizHandle(biz.payload());
          redisTemplate.opsForZSet().remove(KEY_PREFIX, strTaskValue);
        } catch (JsonProcessingException e) {
          logger.error("biz data to json failed: ", e);
        }
      }
    }
  }

  protected abstract void bizHandle(T obj);
}
