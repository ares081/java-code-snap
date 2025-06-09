package com.ares.concurrency.mdc;

import jakarta.annotation.Nonnull;
import org.springframework.core.task.TaskDecorator;

// 这里封装的是Spring中的 ThreadPoolTaskExecutor
public class ThreadPoolTaskWrapper implements TaskDecorator {

  @Nonnull
  @Override
  public Runnable decorate(@Nonnull Runnable runnable) {
    return ThreadWrapper.runnable(runnable);
  }
}
