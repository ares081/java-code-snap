package com.ares.ratelimiter.impl;

import com.ares.ratelimiter.CustomRateLimiter;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class SlidingWindowRateLimiter implements CustomRateLimiter {
  private final Map<String, Queue<Long>> windowMap = new ConcurrentHashMap<>();
  private final int maxPermits;
  private final long windowMillis;


  public SlidingWindowRateLimiter(int maxPermits, int timeWindow, TimeUnit timeUnit) {
    this.maxPermits = maxPermits;
    this.windowMillis = timeUnit.toMillis(timeWindow);
  }

  @Override
  public synchronized boolean tryAcquire(String key) {
    long currentTime = Instant.now().toEpochMilli();
    Queue<Long> window = windowMap.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>());
    // 清理过期的时间戳
    while (!window.isEmpty() && currentTime - window.peek() > windowMillis) {
      window.poll();
    } // 判断是否超过限制
    if (window.size() < maxPermits) {
      window.offer(currentTime);
      return true;
    }
    return false;
  }
}
