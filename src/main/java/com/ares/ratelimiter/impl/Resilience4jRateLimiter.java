package com.ares.ratelimiter.impl;


import com.ares.ratelimiter.CustomRateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Resilience4jRateLimiter implements CustomRateLimiter {

  private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
  private final int limitForPeriod;
  private final int timeWindow;
  private final TimeUnit timeUnit;

  public Resilience4jRateLimiter(int limitForPeriod, int timeWindow, TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
    this.timeWindow = timeWindow;
    this.limitForPeriod = limitForPeriod;
  }

  @Override
  public boolean tryAcquire(String key) {
    RateLimiter rateLimiter = limiters.computeIfAbsent(key, v -> {
      RateLimiterConfig config = RateLimiterConfig.custom()
          .limitForPeriod(limitForPeriod)
          .limitRefreshPeriod(Duration.ofMillis(timeUnit.toMillis(timeWindow)))
          .timeoutDuration(Duration.ofMillis(0)) // 非阻塞
          .build();
      return RateLimiterRegistry.of(config).rateLimiter(key);
    });
    return rateLimiter.acquirePermission();
  }
}
