package com.ares.jse;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounterRateLimiter {

  private final Logger logger = LoggerFactory.getLogger(CounterRateLimiter.class);
  // 时间窗口大小（毫秒）
  private final long windowSize;
  // 允许的最大请求数
  private final long maxRequests;
  // 当前窗口的请求计数
  private final AtomicLong count;
  // 当前窗口的开始时间
  private volatile long windowStart;

  /**
   * 构造函数
   *
   * @param windowSize  时间窗口大小（毫秒）
   * @param maxRequests 允许的最大请求数
   */
  public CounterRateLimiter(long windowSize, long maxRequests) {
    this.windowSize = windowSize;
    this.maxRequests = maxRequests;
    this.count = new AtomicLong(0);
    this.windowStart = Instant.now().toEpochMilli();
  }

  /**
   * 尝试获取请求许可
   *
   * @return true表示允许请求，false表示拒绝请求
   */
  public boolean tryAcquire() {
    long now = Instant.now().toEpochMilli();
    long currentWindow = windowStart;
    long timer = now - currentWindow;

    logger.info("time counter: " + timer);

    // 如果已经进入了新的时间窗口，重置计数器
    if (timer > windowSize) {
      synchronized (this) {
        // 双重检查，确保在锁内再次验证时间窗口
        timer = now - windowStart;
        if (timer > windowSize) {
          windowStart = now;
          count.set(0);
        }
      }
    }
    // 原子操作增加计数并检查是否超过限制
    long currentCount = count.incrementAndGet();
    if (currentCount <= maxRequests) {
      return true;
    } else {
      // 如果超过限制，回退计数
      count.decrementAndGet();
      return false;
    }
  }

  public static void main(String[] args) throws Exception {
    CounterRateLimiter limiter = new CounterRateLimiter(2000, 100);
    for (int i = 0; i < 220; i++) {
      Thread.sleep(10);
      System.out.println("counter:" + i + ",acquire result: " + limiter.tryAcquire());
    }
  }
}
