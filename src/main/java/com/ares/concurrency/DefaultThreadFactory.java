package com.ares.concurrency;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {
  private static final AtomicInteger poolId = new AtomicInteger(0);
  private final AtomicInteger threadId = new AtomicInteger(0);

  private final String prefix;
  private final boolean daemon;
  private final int priority;
  protected final ThreadGroup group;

  public DefaultThreadFactory(String name) {
    this(name, false);
  }

  public DefaultThreadFactory(String name, boolean daemon) {
    this(name, daemon, Thread.NORM_PRIORITY, null);
  }

  public DefaultThreadFactory(String name, boolean daemon, ThreadGroup group) {
    this(name, daemon, Thread.NORM_PRIORITY, group);
  }

  public DefaultThreadFactory(String name, boolean daemon, int priority, ThreadGroup group) {
    this.prefix = name + "-" + poolId.incrementAndGet() + "-thread-";
    this.daemon = daemon;
    this.priority = priority;
    this.group = group;
  }

  @Override
  public Thread newThread(Runnable runnable) {
    Thread thread = new Thread(group, runnable, prefix + threadId.incrementAndGet(), 0);
    if (thread.isDaemon() != daemon) {
      thread.setDaemon(daemon);
    }

    if (thread.getPriority() != priority) {
      thread.setPriority(priority);
    }
    return thread;
  }
}
