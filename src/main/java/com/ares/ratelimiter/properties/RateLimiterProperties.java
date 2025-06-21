package com.ares.ratelimiter.properties;

import com.ares.ratelimiter.RateLimiterType;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

  /**
   * 是否启用限流功能
   */
  private boolean enabled = true;
  /**
   * 默认限流类型: GUAVA, REDIS, RESILIENCE4J, SLIDING_WINDOW
   */
  private String defaultType = RateLimiterType.GUAVA.name();
  /**
   * 默认每个时间窗口允许的请求数
   */
  private int defaultPermits = 10;
  /**
   * 默认时间窗口大小（秒）
   */
  private int defaultTimeWindowSeconds = 1;

  /**
   * 限流触发时的默认响应消息
   */
  private String defaultMessage = "请求过于频繁，请稍后再试";
  /**
   * 自定义接口限流配置 Key 是 API 路径或方法名，Value 是限流配置
   */
  private Map<String, ApiRateLimit> apis = new HashMap<>();

  @Setter
  @Getter
  public static class ApiRateLimit {

    private String type;
    private int permits;
    private int timeWindowSeconds;
    private String message;
  }
}
