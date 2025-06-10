package com.ares.ratelimiter.impl;

import com.ares.ratelimiter.CustomRateLimiter;
import com.google.common.util.concurrent.RateLimiter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuavaRateLimiter implements CustomRateLimiter {

  private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
  private final double permitsPerSecond;

  public GuavaRateLimiter(double permitsPerSecond) {
    this.permitsPerSecond = permitsPerSecond;
  }

  @Override
  public boolean tryAcquire(String key) {
    RateLimiter rateLimiter =
        limiters.computeIfAbsent(key, k -> RateLimiter.create(permitsPerSecond));
    return rateLimiter.tryAcquire();
  }
}
