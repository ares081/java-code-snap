package com.ares.ratelimiter;

public interface CustomRateLimiter {
  /**
   * 尝试获取许可
   *
   * @param key 限流标识
   * @return 是否获取成功
   */
  boolean tryAcquire(String key);

  /**
   * 释放许可（如需要）
   *
   * @param key 限流标识
   */
  default void release(String key) {
    // 默认空实现
  }
}
