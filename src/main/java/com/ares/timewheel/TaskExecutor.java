package com.ares.timewheel;

import com.ares.concurrency.DefaultThreadFactory;
import java.time.Instant;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskExecutor implements Timer {

  private final Logger log = LoggerFactory.getLogger(TaskExecutor.class);

  private final ExecutorService taskExecutor;
  private final DelayQueue<TaskList> delayQueue;
  private final AtomicInteger taskCounter;

  @Getter
  private final TimingWheel timingWheel;

  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
  private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();


  public TaskExecutor(String executeName) {
    this(executeName, 1, 20, Instant.now().toEpochMilli());
  }

  public TaskExecutor(String executeName, Integer wheelSize) {
    this(executeName, 1, wheelSize, Instant.now().toEpochMilli());
  }

  public TaskExecutor(String executeName, Integer wheelSize, Long tickMs) {
    this(executeName, tickMs, wheelSize, Instant.now().toEpochMilli());
  }

  public TaskExecutor(String executeName, long tickMs, int wheelSize, long startMs) {
    this.taskExecutor = Executors.newFixedThreadPool(1, new DefaultThreadFactory(executeName));
    this.delayQueue = new DelayQueue<>();
    this.taskCounter = new AtomicInteger(0);
    this.timingWheel = new TimingWheel(tickMs, wheelSize, startMs, taskCounter, delayQueue);
  }

  @Override
  public void add(TimingTask timerTask) {
    readLock.lock();
    try {
      addTaskSlots(new TaskSlots(timerTask, timerTask.delayMs + Instant.now().toEpochMilli()));
    } finally {
      readLock.unlock();
    }
  }

  private void addTaskSlots(TaskSlots slots) {
    if (!timingWheel.add(slots)) {
      if (!slots.cancelled()) {
        taskExecutor.submit(slots.task);
      }
    }
  }

  @Override
  public boolean advanceClock(long timeoutMs) throws InterruptedException {
    TaskList bucket = delayQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    if (bucket != null) {
      writeLock.lock();
      try {
        while (bucket != null) {
          log.info("task delayMs:{}, currentMs:{}", bucket.getExpiration(),
              Instant.now().toEpochMilli());
          timingWheel.advanceClock(bucket.getExpiration());
          bucket.flush(this::addTaskSlots);
          bucket = delayQueue.poll();
        }
      } finally {
        writeLock.unlock();
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int size() {
    return taskCounter.get();
  }

  @Override
  public void close() throws Exception {
    shutdownExecutorServiceQuietly(taskExecutor, 5, TimeUnit.SECONDS);
  }

  public void shutdownExecutorServiceQuietly(ExecutorService executorService,
      long timeout, TimeUnit timeUnit) {
    if (executorService == null) {
      return;
    }
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(timeout, timeUnit)) {
        executorService.shutdownNow();
        if (!executorService.awaitTermination(timeout, timeUnit)) {
          log.error("Executor {} did not terminate in time", executorService);
        }
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
