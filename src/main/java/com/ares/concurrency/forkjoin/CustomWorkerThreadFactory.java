package com.ares.concurrency.forkjoin;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.MDC;

public class CustomWorkerThreadFactory implements ForkJoinWorkerThreadFactory {

  private final String namePrefix;
  private static final AtomicInteger poolNumber = new AtomicInteger(1);
  private final AtomicInteger threadNumber = new AtomicInteger(1);

  protected final Map<String, String> inheritedMdc;

  public CustomWorkerThreadFactory(String namePrefix) {
    this.namePrefix = namePrefix + "-" + poolNumber.getAndIncrement() + "-worker-";
    this.inheritedMdc = MDC.getCopyOfContextMap();
  }

  @Override
  public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
    ForkJoinWorkerThread thread = new DefaultForkJoinWorkerThread(pool);
    thread.setName(namePrefix + threadNumber.getAndIncrement());
    return thread;
  }

  public class DefaultForkJoinWorkerThread extends ForkJoinWorkerThread {

    protected DefaultForkJoinWorkerThread(ForkJoinPool forkJoinPool) {
      super(forkJoinPool);
    }

    @Override
    protected void onStart() {
      super.onStart();
      // 在线程启动时复制 MDC 上下文
      if (inheritedMdc != null) {
        MDC.setContextMap(inheritedMdc);
      }
    }

    @Override
    protected void onTermination(Throwable exception) {
      try {
        // 在线程结束时清理 MDC 上下文
        MDC.clear();
        inheritedMdc.clear();
      } finally {
        super.onTermination(exception);
      }
    }
  }
}
