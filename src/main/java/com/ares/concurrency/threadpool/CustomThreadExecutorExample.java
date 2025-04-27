package com.ares.concurrency.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomThreadExecutorExample {

  private static final Logger logger = LoggerFactory.getLogger(CustomThreadExecutorExample.class);

  public static void main(String[] args) {
    DefaultThreadPoolExecutor executor1 = new DefaultThreadPoolExecutor();
    DefaultThreadPoolExecutor executor2 = new DefaultThreadPoolExecutor();

    for (int i = 0; i < 10; i++) {
      executor1.execute(() -> {
        logger.info("task is running: {}", Thread.currentThread().getName());
      });
      executor2.execute(() -> {
        logger.info("task is running: {}", Thread.currentThread().getName());
      });
    }
    executor1.shutdown();
    executor2.shutdown();
  }
}
