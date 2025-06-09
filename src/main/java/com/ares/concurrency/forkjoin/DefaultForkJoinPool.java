package com.ares.concurrency.forkjoin;

import com.ares.concurrency.mdc.ThreadWrapper;
import jakarta.annotation.Nonnull;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultForkJoinPool extends ForkJoinPool {

  private static final Logger logger = LoggerFactory.getLogger(DefaultForkJoinPool.class);
  private static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();

  public DefaultForkJoinPool() {
    this(DEFAULT_PARALLELISM);
  }

  public DefaultForkJoinPool(int nThreads) {
    this(nThreads, new DefaultWorkerThreadFactory("default-forkjoin-pool"));
  }

  public DefaultForkJoinPool(int nThreads, ForkJoinWorkerThreadFactory factory) {
    this(nThreads, factory, null, true);
  }

  public DefaultForkJoinPool(int nThreads, ForkJoinWorkerThreadFactory factory,
      UncaughtExceptionHandler handler, boolean asyncMode) {
    super(nThreads, factory, handler, asyncMode);
  }

  @Override
  public void execute(ForkJoinTask<?> task) {
    super.execute(task);
  }

  @Override
  public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
    return super.submit(task);
  }

  @Override
  public void execute(Runnable task) {
    super.execute(task);
  }

  @Nonnull
  @Override
  public <T> ForkJoinTask<T> submit(@Nonnull Callable<T> task) {
    return super.submit(ThreadWrapper.callable(task));
  }

  @Nonnull
  @Override
  public <T> ForkJoinTask<T> submit(Runnable task, T result) {
    return super.submit(ThreadWrapper.runnable(task), result);
  }
  @Nonnull
  @Override
  public ForkJoinTask<?> submit(Runnable task) {
    return super.submit(ThreadWrapper.runnable(task));
  }
}
