package com.ares.concurrency.mdc;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.slf4j.MDC;

public class ThreadWrapper {

  public static <T> Callable<T> callable(final Callable<T> callable) {
    final Map<String, String> context = MDC.getCopyOfContextMap();
    return () -> {
      Map<String, String> previousMdcContext = MDC.getCopyOfContextMap();
      if (context == null || context.isEmpty()) {
        MDC.clear();
      } else {
        MDC.setContextMap(context);
      }
      try {
        return callable.call();
      } finally {
        if (previousMdcContext != null) {
          // 恢复之前的MDC上下文
          MDC.setContextMap(previousMdcContext);
        } else {
          // 清除子线程的MDC，避免内存溢出
          MDC.clear();
        }
      }
    };
  }

  public static Runnable runnable(final Runnable runnable) {
    final Map<String, String> context = MDC.getCopyOfContextMap();
    return () -> {
      Map<String, String> previousMdcContext = MDC.getCopyOfContextMap();
      if (context == null || context.isEmpty()) {
        MDC.clear();
      } else {
        MDC.setContextMap(context);
      }
      try {
        runnable.run();
      } finally {
        if (previousMdcContext != null) {
          // 恢复之前的MDC上下文
          MDC.setContextMap(previousMdcContext);
        } else {
          // 清除子线程的MDC，避免内存溢出
          MDC.clear();
        }
      }
    };
  }

  public static <T> Supplier<T> supplier(Supplier<T> supplier) {
    final Map<String, String> context = MDC.getCopyOfContextMap();
    return () -> {
      Map<String, String> previousMdcContext = MDC.getCopyOfContextMap();
      if (context == null || context.isEmpty()) {
        MDC.clear();
      } else {
        MDC.setContextMap(context);
      }
      try {
        // 调用原始Supplier
        return supplier.get();
      } finally {
        // 恢复之前的MDC上下文
        if (previousMdcContext != null) {
          MDC.setContextMap(previousMdcContext);
        } else {
          // 清除子线程的MDC，避免内存溢出
          MDC.clear();
        }
      }
    };
  }
}
