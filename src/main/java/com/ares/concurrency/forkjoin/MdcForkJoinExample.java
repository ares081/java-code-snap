package com.ares.concurrency.forkjoin;

import java.time.Instant;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class MdcForkJoinExample {

  private static final Logger logger = LoggerFactory.getLogger(MdcForkJoinExample.class);

  public static void main(String[] args) {

    MDC.put("traceId", String.valueOf(Instant.now().toEpochMilli()));
    logger.info("Task tarting with forkjoin pool........");
    ForkJoinPool forkJoinPool = new DefaultForkJoinPool();

    try {
      for (int i = 0; i < 10; i++) {
        String result = forkJoinPool.submit(new SampleMdcTask()).join();
        logger.info("Task completed with result: {}", result);
      }

      for (int i = 0; i < 10; i++) {
        String result = forkJoinPool.submit(() -> {
          logger.info("fork join: callable");
          return "";
        }).getRawResult();
      }

      for (int i = 0; i < 10; i++) {
        forkJoinPool.execute(() -> logger.info("fork join: runnable"));
      }

    } finally {
      forkJoinPool.shutdown();
      MDC.clear();
    }
  }

  public static class SampleMdcTask extends AbstractForkJoinTask<String> {

    @Override
    protected String computeWithMdc() {
      // 记录日志，会包含 MDC 信息
      logger.info("Executing task in thread");

      // 创建子任务
      SubTask subTask = new SubTask();
      subTask.fork();

      // 等待子任务完成并合并结果
      String subResult = subTask.join();
      return "Main Result: " + subResult;
    }
  }

  public static class SubTask extends AbstractForkJoinTask<String> {

    @Override
    protected String computeWithMdc() {
      // 子任务中的日志也会包含 MDC 信息
      logger.info("Executing subtask in thread");
      return "Sub Result";
    }
  }
}
