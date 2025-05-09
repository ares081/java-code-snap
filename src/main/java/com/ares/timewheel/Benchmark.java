package com.ares.timewheel;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Benchmark {

  private static final Logger log = LoggerFactory.getLogger(Benchmark.class);

  public static void main(String[] args) throws Exception {
    int taskCount = 100000;
    CountDownLatch latch = new CountDownLatch(taskCount);
    AtomicInteger executedTasks = new AtomicInteger(0);

    TaskExecutor execute = new TaskExecutor("benchmark", 2048, 1L);

    long startTime = System.currentTimeMillis();
    System.out.println("Adding " + taskCount + " tasks...");
    for (int i = 0; i < taskCount; i++) {
      long delay = 1 + (long) (Math.random() * 60000);
      execute.add(new TimingTask(delay) {
        @Override
        public void run() {
          int taskId = executedTasks.incrementAndGet();
          log.info("task running, delayMs:{}, id: {}", delay + Instant.now().toEpochMilli(),
              taskId);
          latch.countDown();
        }
      });
    }
    log.info("Added all tasks in {}ms", System.currentTimeMillis() - startTime);
    log.info("Waiting for all tasks to complete...");

    do {
      execute.advanceClock(20L);
      log.info("unexecute tasks: {}", latch.getCount());
    } while (latch.getCount() != 0);

    log.info("All tasks completed in {}ms", System.currentTimeMillis() - startTime);
    log.info("Executed tasks: {}", executedTasks.get());

    execute.close();
  }
}
