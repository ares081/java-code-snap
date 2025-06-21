package com.ares.concurrency.threadpool;

import jakarta.annotation.Nonnull;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;

public class WorkerQueue extends LinkedTransferQueue<Runnable> {

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

  // 仿tomcat TaskQueue
  @Override
  public boolean offer(@Nonnull Runnable runnable) {
    int poolSize = executor.getPoolSize();

    // 当线程数达到最大线程数时，新提交任务入队
    if (poolSize == executor.getMaximumPoolSize()) {
      return super.offer(runnable);
    }

    // 当提交的任务数小于线程池中已有的线程数时，即有空闲线程，任务入队即可
    if (executor.getSubmittedTasksCount().get() <= poolSize) {
      return super.offer(runnable);
    }

    // 如果当前线程数量未达到最大线程数，直接返回false，让线程池创建新线程
    if (poolSize < executor.getMaximumPoolSize()) {
      return false;
    }
    // 最后的兜底，放入队列
    return super.offer(runnable);
  }
}
