package com.ares.concurrency;

import java.util.concurrent.ScheduledFuture;

public interface Scheduler {

  void startup();

  void shutdown() throws InterruptedException;

  default ScheduledFuture<?> scheduleOnce(String name, Runnable task) {
    return scheduleOnce(name, task, 0L);
  }

  default ScheduledFuture<?> scheduleOnce(String name, Runnable task, long delayMs) {
    return schedule(name, task, delayMs, -1);
  }

  ScheduledFuture<?> schedule(String name, Runnable task, long delayMs, long periodMs);

  void resizeThreadPool(int newSize);

}
