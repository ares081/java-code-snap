package com.ares.concurrency.threadpool;

import com.ares.concurrency.DefaultThreadFactory;
import jakarta.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

public class DefaultThreadPoolExecutor extends ThreadPoolExecutor {

  private static final int MAXIMUM_POOL_SIZE = 200;
  private static final int KEEP_ALIVE_TIME = 60 * 1000;
  private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;

  // 正在处理的任务数
  @Getter
  protected final AtomicInteger submittedTasksCount = new AtomicInteger(0);

  // 最大允许同时处理的任务数
  @Getter
  protected final int maxTask;

  public DefaultThreadPoolExecutor() {
    this(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE);
  }

  public DefaultThreadPoolExecutor(String name) {
    this(MAXIMUM_POOL_SIZE, MAXIMUM_POOL_SIZE);
  }

  public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
    this(corePoolSize, maximumPoolSize, maximumPoolSize);
  }

  public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
      BlockingQueue<Runnable> workQueue) {
    this(corePoolSize, maximumPoolSize, maximumPoolSize, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
        workQueue, new DefaultThreadFactory("default-execute-pool"));
  }

  public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
      ThreadFactory threadFactory) {
    this(corePoolSize, maximumPoolSize, maximumPoolSize, new WorkerQueue(), threadFactory);
  }

  public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int queueCapacity) {
    this(corePoolSize, maximumPoolSize, queueCapacity, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
        new WorkerQueue());
  }

  public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int queueCapacity,
      BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    this(corePoolSize, maximumPoolSize, queueCapacity, KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
        workQueue, threadFactory);
  }

  public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int queueCapacity,
      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    this(corePoolSize, maximumPoolSize, queueCapacity, keepAliveTime, unit, workQueue,
        new DefaultThreadFactory("default-execute-pool"));
  }

  public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int queueCapacity,
      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory) {
    this(corePoolSize, maximumPoolSize, queueCapacity, keepAliveTime, unit, workQueue,
        threadFactory, new AbortPolicy());
  }

  public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int queueCapacity,
      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
      RejectedExecutionHandler handler) {
    this(corePoolSize, maximumPoolSize, queueCapacity, keepAliveTime, unit, workQueue,
        new DefaultThreadFactory("default-execute-pool"), handler);
  }

  public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int queueCapacity,
      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    ((WorkerQueue) getQueue()).setExecutor(this);
    this.maxTask = maximumPoolSize + queueCapacity;
  }

  @Override
  public void execute(@Nonnull Runnable command) {
    int count = submittedTasksCount.incrementAndGet();
    // 超过最大的并发任务限制，进行 reject
    // 依赖的LinkedTransferQueue没有长度限制，因此这里进行控制
    if (count > maxTask) {
      submittedTasksCount.decrementAndGet();
      getRejectedExecutionHandler().rejectedExecution(command, this);
    }

    try {
      super.execute(command);
    } catch (RejectedExecutionException rx) {
      if (!((WorkerQueue) getQueue()).force(command)) {
        submittedTasksCount.decrementAndGet();
        getRejectedExecutionHandler().rejectedExecution(command, this);
      }
    }
  }

  protected void afterExecute(Runnable r, Throwable t) {
    submittedTasksCount.decrementAndGet();
  }
}
