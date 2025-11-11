package com.ares.concurrency.threadpool;

import com.ares.concurrency.DefaultThreadFactory;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomThreadExecutorExample {

  private static final Logger logger = LoggerFactory.getLogger(CustomThreadExecutorExample.class);

  public static void main(String[] args) {

    DefaultThreadPoolExecutor executor1 = new DefaultThreadPoolExecutor(10, 100);
    DefaultThreadPoolExecutor executor2 = new DefaultThreadPoolExecutor(10, 20);

    DefaultThreadPoolExecutor executor3 = new DefaultThreadPoolExecutor(10, 30, 60, 60,
        TimeUnit.SECONDS, new WorkerQueue(), new DefaultThreadFactory("test"), new DiscardPolicy());

    for (int i = 0; i < 100; i++) {
      executor1.execute(() -> {
        logger.info("task1 is running: {}", Thread.currentThread().getName());
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });

      executor2.execute(() -> {
        logger.info("task2 is running: {}", Thread.currentThread().getName());
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });

      executor3.execute(() -> {
        logger.info("task3 is running: {}", Thread.currentThread().getName());

      });
    }
    executor1.shutdown();
    executor2.shutdown();
  }
}
