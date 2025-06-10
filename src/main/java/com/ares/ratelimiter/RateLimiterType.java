package com.ares.ratelimiter;

public enum RateLimiterType {
  /**
   * 基于Guava的RateLimiter
   */
  GUAVA,

  /**
   * 基于Redis的分布式限流器
   */
  REDIS,

  /**
   * 基于Resilience4j的限流器
   */
  RESILIENCE4J,

  /**
   * 自定义滑动窗口限流器
   */
  SLIDING_WINDOW
}
