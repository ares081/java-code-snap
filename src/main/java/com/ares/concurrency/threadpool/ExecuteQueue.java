package com.ares.concurrency.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

public class ExecuteQueue extends LinkedBlockingQueue<Runnable> {

  private DefaultThreadPoolExecutor executor;

  public void setExecutor(DefaultThreadPoolExecutor executor) {
    this.executor = executor;
  }

  public boolean force(Runnable o) {
    if (!executor.isShutdown()) {
      throw new RejectedExecutionException(
          "Executor not running, can't force a command into the queue");
    }
    return super.offer(o);
  }

  @Override
  public boolean offer(Runnable runnable) {
    int poolSize = executor.getPoolSize();

    if (poolSize == executor.getMaximumPoolSize()) {
      return super.offer(runnable);
    }

    if (executor.getSubmittedTasksCount().get() <= poolSize) {
      return super.offer(runnable);
    }

    if (poolSize < executor.getMaximumPoolSize()) {
      return false;
    }

    return super.offer(runnable);
  }
}
