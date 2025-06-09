package com.ares.concurrency.forkjoin;

import java.util.Map;
import java.util.concurrent.RecursiveTask;
import org.slf4j.MDC;

public abstract class AbstractForkJoinTask<T> extends RecursiveTask<T> {

  private final ThreadLocal<Map<String, String>> mdcCache = new ThreadLocal<>();

  @Override
  protected final T compute() {
    Map<String, String> oldMdc = MDC.getCopyOfContextMap();
    try {
      if (oldMdc != null) {
        oldMdc = MDC.getCopyOfContextMap();
        mdcCache.set(oldMdc);
      }
      return computeWithMdc();
    } finally {
      if (oldMdc != null) {
        MDC.setContextMap(oldMdc);
      } else {
        MDC.clear();
      }
      mdcCache.remove();
    }
  }

  protected abstract T computeWithMdc();
}
