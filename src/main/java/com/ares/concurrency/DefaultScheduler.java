package com.ares.concurrency;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultScheduler implements Scheduler {

  private final Logger log = LoggerFactory.getLogger(DefaultScheduler.class);
  private final int threads;
  private final DefaultThreadFactory threadFactory;
  private volatile ScheduledThreadPoolExecutor scheduledPool;

  public DefaultScheduler(int threads) {
    this(threads, "default-schedule-pool");
  }

  public DefaultScheduler(final int threads, String taskName) {
    this(threads, false, taskName);

  }

  public DefaultScheduler(final int threads, boolean daemon, String taskName) {
    this.threads = threads;
    this.threadFactory = new DefaultThreadFactory(taskName, daemon);
  }

  @Override
  public void startup() {
    synchronized (this) {
      if (scheduledPool == null) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(threads,
            threadFactory);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setRemoveOnCancelPolicy(true);
        this.scheduledPool = executor;
      }
    }
  }

  @Override
  public ScheduledFuture<?> schedule(String name, Runnable task, long delayMs, long periodMs) {
    synchronized (this) {
      if (scheduledPool != null) {
        Runnable runnable = task::run;
        if (periodMs > 0) {
          return scheduledPool.scheduleAtFixedRate(runnable, delayMs, periodMs,
              TimeUnit.MILLISECONDS);
        } else {
          return scheduledPool.schedule(runnable, delayMs, TimeUnit.MILLISECONDS);
        }
      } else {
        log.info("scheduler is not running at the task '{}' scheduled. The task is ignored.", name);
        return new NoOpScheduledFutureTask();
      }
    }
  }

  @Override
  public void shutdown() throws InterruptedException {
    ScheduledThreadPoolExecutor maybeExecutor = null;
    synchronized (this) {
      if (scheduledPool != null) {
        maybeExecutor = scheduledPool;
        maybeExecutor.shutdown();
        this.scheduledPool = null;
      }
    }
    if (maybeExecutor != null) {
      maybeExecutor.shutdownNow();
    }
  }

  @Override
  public void resizeThreadPool(int newSize) {
    synchronized (this) {
      if (scheduledPool != null) {
        scheduledPool.setCorePoolSize(newSize);
      }
    }
  }

  private static class NoOpScheduledFutureTask implements ScheduledFuture<Void> {

    @Override
    public long getDelay(TimeUnit unit) {
      return 0;
    }

    @Override
    public int compareTo(Delayed o) {
      long diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
      if (diff < 0) {
        return -1;
      } else if (diff > 0) {
        return 1;
      } else {
        return 0;
      }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return true;
    }

    @Override
    public boolean isCancelled() {
      return true;
    }

    @Override
    public boolean isDone() {
      return true;
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
      return null;
    }

    @Override
    public Void get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      return null;
    }
  }
}
