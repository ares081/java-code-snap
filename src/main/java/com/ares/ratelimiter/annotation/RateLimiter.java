package com.ares.ratelimiter.annotation;

import com.ares.ratelimiter.RateLimiterType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

  /**
   * 限流唯一标识，默认为方法全限定名
   */
  String key() default "";

  /**
   * 限流策略，支持不同类型的限流器
   */
  RateLimiterType type() default RateLimiterType.GUAVA;

  /**
   * 限流时间窗口
   */
  int timeWindow() default 1;

  /**
   * 时间单位
   */
  TimeUnit timeUnit() default TimeUnit.SECONDS;

  /**
   * 在时间窗口内允许通过的请求数
   */
  int permits() default 50;

  /**
   * 获取令牌最大等待时间，0表示非阻塞
   */
  long timeout() default 0;

  /**
   * 触发限流时的提示消息
   */
  String message() default "请求过于频繁，请稍后再试";
}
