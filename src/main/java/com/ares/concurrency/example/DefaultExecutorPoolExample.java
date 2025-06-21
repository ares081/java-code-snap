package com.ares.concurrency.example;

import com.ares.concurrency.DefaultThreadFactory;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class DefaultExecutorPoolExample {

  private static final Logger logger = LoggerFactory.getLogger(DefaultExecutorPoolExample.class);

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    MDC.put("traceId", String.valueOf(Instant.now().toEpochMilli()));
    ExecutorService executorService = new ThreadPoolExecutor(10, 10, 3, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(65535), new DefaultThreadFactory("test"));

    for (int i = 0; i < 10; i++) {
      Future<String> future = executorService.submit(() -> {
        logger.info("sub task, thread name: {}", Thread.currentThread().getName());
        return Thread.currentThread().getName();
      });
      logger.info("sub task result: {}", future.get());
    }
    executorService.shutdown();
    MDC.remove("traceId");
  }

}
