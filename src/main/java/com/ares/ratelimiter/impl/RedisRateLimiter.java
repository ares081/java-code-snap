package com.ares.ratelimiter.impl;

import com.ares.ratelimiter.CustomRateLimiter;
import java.util.Collections;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class RedisRateLimiter implements CustomRateLimiter {

  private final StringRedisTemplate stringRedisTemplate;
  private final int maxPermits;
  private final int windowSeconds;

  private static final String REDIS_SCRIPT =
      "local key = KEYS[1] " +
          "local currentCount = redis.call('incr', key) " +
          "if tonumber(currentCount) == 1 then " +
          "    redis.call('expire', key, ARGV[1]) " +
          "end " +
          "return currentCount <= tonumber(ARGV[2])";


  public RedisRateLimiter(StringRedisTemplate stringRedisTemplate, int maxPermits,
      int windowSeconds) {
    this.maxPermits = maxPermits;
    this.windowSeconds = windowSeconds;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean tryAcquire(String key) {
    DefaultRedisScript<Boolean> script = new DefaultRedisScript<>(REDIS_SCRIPT, Boolean.class);

    Boolean result =
        stringRedisTemplate.execute(script, Collections.singletonList("rate:limit:" + key),
            String.valueOf(windowSeconds), String.valueOf(maxPermits));
    return result != null && result;
  }
}
