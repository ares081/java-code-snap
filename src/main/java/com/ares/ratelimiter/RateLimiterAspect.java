package com.ares.ratelimiter;

import com.ares.ratelimiter.annotation.RateLimiter;
import com.ares.ratelimiter.properties.RateLimiterProperties;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Aspect
public class RateLimiterAspect {


  private final RateLimiterProperties properties;


  private final RateLimiterFactory rateLimiterFactory;

  public RateLimiterAspect(RateLimiterProperties properties,
      RateLimiterFactory rateLimiterFactory) {
    this.properties = properties;
    this.rateLimiterFactory = rateLimiterFactory;
  }

  @Around("@annotation(com.ares.ratelimiter.annotation.RateLimiter)")
  public Object rateLimit(ProceedingJoinPoint point) throws Throwable {

    if (!properties.isEnabled()) {
      return point.proceed();
    }

    MethodSignature signature = (MethodSignature) point.getSignature();
    Method method = signature.getMethod();

    RateLimiter rateLimit = method.getAnnotation(RateLimiter.class);

    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
    CustomRateLimiter limiter = rateLimiterFactory.createRateLimiter(rateLimit, methodName);

    try {
      if (limiter.tryAcquire(methodName)) {
        return point.proceed();
      } else {
        // 获取自定义消息或使用默认消息
        String message = rateLimit.message();
        if (message.isEmpty()) {
          message = properties.getDefaultMessage();
        }
        if (properties.getApis().containsKey(methodName)
            && properties.getApis().get(methodName).getMessage() != null) {
          message = properties.getApis().get(methodName).getMessage();
        }
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, message);
      }
    } finally {
      limiter.release(methodName); // 如果有需要释放的资源
    }
  }
}
