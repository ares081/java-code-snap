package com.ares.ratelimiter.configuration;

import com.ares.ratelimiter.CustomRateLimiter;
import com.ares.ratelimiter.RateLimiterAspect;
import com.ares.ratelimiter.RateLimiterFactory;
import com.ares.ratelimiter.properties.RateLimiterProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@AutoConfiguration
@ConditionalOnClass(CustomRateLimiter.class)
@ConditionalOnProperty(prefix = "rate-limiter", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(RateLimiterProperties.class)
@Import({RateLimiterRedisConfiguration.class, RateLimiterResilience4jConfiguration.class})
@ComponentScan("com.ares.ratelimiter")
public class RateLimiterAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public RateLimiterFactory rateLimiterFactory(RateLimiterProperties properties,
      StringRedisTemplate redisTemplate) {
    return new RateLimiterFactory(properties, redisTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  public RateLimiterAspect rateLimitAspect(RateLimiterProperties properties,
      RateLimiterFactory rateLimiterFactory) {
    return new RateLimiterAspect(properties, rateLimiterFactory);
  }
}
