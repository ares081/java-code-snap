package com.ares.concurrency;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface Scheduler {

  void startup();

  void shutdown() throws InterruptedException;

  default ScheduledFuture<?> scheduleOnce(String name, Runnable task) {
    return scheduleOnce(task, 0L);
  }

  default ScheduledFuture<?> scheduleOnce(Runnable task, long delayMs) {
    return schedule( task, delayMs, -1);
  }

  ScheduledFuture<?> schedule(Runnable task, long delayMs, long periodMs);

  ScheduledFuture<?> schedule(Runnable task, long delayMs, long periodMs,
      TimeUnit unit);


  void resizeThreadPool(int newSize);

}
