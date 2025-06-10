package com.ares.ratelimiter;

import com.ares.ratelimiter.annotation.RateLimiter;
import com.ares.ratelimiter.impl.GuavaRateLimiter;
import com.ares.ratelimiter.impl.RedisRateLimiter;
import com.ares.ratelimiter.impl.Resilience4jRateLimiter;
import com.ares.ratelimiter.impl.SlidingWindowRateLimiter;
import com.ares.ratelimiter.properties.RateLimiterProperties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RateLimiterFactory {

  private final Map<String, CustomRateLimiter> rateLimiterCache = new ConcurrentHashMap<>();

  private final RateLimiterProperties properties;
  private final StringRedisTemplate stringRedisTemplate;

  public RateLimiterFactory(RateLimiterProperties properties,
      StringRedisTemplate stringRedisTemplate) {
    this.properties = properties;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  /**
   * 根据限流注解创建相应的限流器
   */
  public CustomRateLimiter createRateLimiter(RateLimiter rateLimit, String fallbackKey) {
    String key = rateLimit.key().isEmpty() ? fallbackKey : rateLimit.key();

    return rateLimiterCache.computeIfAbsent(key, v -> {
      RateLimiterType type = rateLimit.type();

      // 如果配置了该key的特定配置，则应用配置
      if (properties.getApis().containsKey(key)) {

        RateLimiterProperties.ApiRateLimit apiConfig = properties.getApis().get(key);

        if (apiConfig.getType() != null) {
          type = RateLimiterType.valueOf(apiConfig.getType());
        }

        int permits = apiConfig.getPermits() > 0 ? apiConfig.getPermits() : rateLimit.permits();
        int timeWindow = apiConfig.getTimeWindowSeconds() > 0 ? apiConfig.getTimeWindowSeconds()
            : (int) rateLimit.timeUnit().toSeconds(rateLimit.timeWindow());
        return switch (type) {
          case REDIS -> createRedisRateLimiter(permits, timeWindow);
          case RESILIENCE4J -> createResilience4jRateLimiter(permits, timeWindow);
          case SLIDING_WINDOW -> createSlidingWindowRateLimiter(permits, timeWindow);
          default -> createGuavaRateLimiter(permits, timeWindow);
        };
      }

      // 否则使用注解上的配置
      return switch (type) {
        case REDIS -> createRedisRateLimiter(rateLimit);
        case RESILIENCE4J -> createResilience4jRateLimiter(rateLimit);
        case SLIDING_WINDOW -> createSlidingWindowRateLimiter(rateLimit);
        default -> createGuavaRateLimiter(rateLimit);
      };
    });
  }


  /**
   * 从配置中创建限流器
   */

  public CustomRateLimiter createRateLimiterFromProperties(String key) {

    RateLimiterType type = RateLimiterType.valueOf(properties.getDefaultType());
    int permits = properties.getDefaultPermits();
    int timeWindow = properties.getDefaultTimeWindowSeconds();

    if (properties.getApis().containsKey(key)) {
      RateLimiterProperties.ApiRateLimit apiConfig = properties.getApis().get(key);
      if (apiConfig.getType() != null) {
        type = RateLimiterType.valueOf(apiConfig.getType());
      }
      if (apiConfig.getPermits() > 0) {
        permits = apiConfig.getPermits();
      }
      if (apiConfig.getTimeWindowSeconds() > 0) {
        timeWindow = apiConfig.getTimeWindowSeconds();
      }
    }
    return switch (type) {
      case REDIS -> createRedisRateLimiter(permits, timeWindow);
      case RESILIENCE4J -> createResilience4jRateLimiter(permits, timeWindow);
      case SLIDING_WINDOW -> createSlidingWindowRateLimiter(permits, timeWindow);
      default -> createGuavaRateLimiter(permits, timeWindow);
    };
  }

  private CustomRateLimiter createGuavaRateLimiter(RateLimiter rateLimiter) {
    double permitsPerSecond = calculatePermitsPerSecond(rateLimiter);
    return new GuavaRateLimiter(permitsPerSecond);
  }

  private CustomRateLimiter createGuavaRateLimiter(int permits, int timeWindowSeconds) {
    double permitsPerSecond = (double) permits / timeWindowSeconds;
    return new GuavaRateLimiter(permitsPerSecond);
  }

  private CustomRateLimiter createRedisRateLimiter(RateLimiter rateLimit) {
    if (stringRedisTemplate == null) {
      throw new IllegalStateException("Redis template is not available for Redis rate limiter");
    }
    int windowSeconds = (int) rateLimit.timeUnit().toSeconds(rateLimit.timeWindow());
    return new RedisRateLimiter(stringRedisTemplate, rateLimit.permits(), windowSeconds);
  }

  private CustomRateLimiter createRedisRateLimiter(int permits, int timeWindowSeconds) {
    if (stringRedisTemplate == null) {
      throw new IllegalStateException("Redis template is not available for Redis rate limiter");
    }
    return new RedisRateLimiter(stringRedisTemplate, permits, timeWindowSeconds);
  }

  private CustomRateLimiter createResilience4jRateLimiter(RateLimiter rateLimit) {
    return new Resilience4jRateLimiter(
        rateLimit.permits(),
        rateLimit.timeWindow(),
        rateLimit.timeUnit());
  }

  private CustomRateLimiter createResilience4jRateLimiter(int permits, int timeWindowSeconds) {
    return new Resilience4jRateLimiter(
        permits,
        timeWindowSeconds,
        TimeUnit.SECONDS);
  }

  private CustomRateLimiter createSlidingWindowRateLimiter(RateLimiter rateLimit) {
    return new SlidingWindowRateLimiter(
        rateLimit.permits(),
        rateLimit.timeWindow(),
        rateLimit.timeUnit());
  }

  private CustomRateLimiter createSlidingWindowRateLimiter(int permits, int timeWindowSeconds) {
    return new SlidingWindowRateLimiter(
        permits,
        timeWindowSeconds,
        TimeUnit.SECONDS);
  }

  private double calculatePermitsPerSecond(RateLimiter rateLimit) {
    // 将配置的时间窗口和许可数转换为每秒许可数
    double timeWindowInSeconds = rateLimit.timeUnit().toSeconds(rateLimit.timeWindow());
    if (timeWindowInSeconds == 0) {
      timeWindowInSeconds = 1; // 防止除零
    }
    return (double) rateLimit.permits() / timeWindowInSeconds;
  }
}
