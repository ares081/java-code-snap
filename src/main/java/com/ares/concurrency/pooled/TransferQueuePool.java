package com.ares.concurrency.pooled;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferQueuePool<T> implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(TransferQueuePool.class);
  private final PooledProperties properties;
  private final LinkedTransferQueue<PooledWrapper<T>> pool;
  private final PooledFactory<T> factory;
  private final ScheduledExecutorService scheduler;

  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final AtomicInteger total = new AtomicInteger(0);
  private final AtomicInteger idle = new AtomicInteger(0);

  public TransferQueuePool(PooledProperties properties, PooledFactory<T> factory) throws Exception {
    this.properties = properties;
    this.factory = factory;
    this.pool = new LinkedTransferQueue<>();
    this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
      Thread t = new Thread(r);
      t.setDaemon(true);
      t.setName("transfer-pool-scheduler-" + t.getId());
      return t;
    });
    ((ScheduledThreadPoolExecutor) this.scheduler).setRemoveOnCancelPolicy(true);
    initializePool();
    startCheckTask();
  }

  private void initializePool() throws Exception {
    for (int i = 0; i < properties.getMinimumIdle(); i++) {
      createPoolWrapper();
    }
  }

  private void createPoolWrapper() throws Exception {
    logger.info("create pool wrapper......................");
    T wrapper = factory.create();
    PooledWrapper<T> pooledWrapper = new PooledWrapper<>(wrapper);
    pool.offer(pooledWrapper);
    total.incrementAndGet();
    idle.incrementAndGet();
  }

  private void startCheckTask() {
    scheduler.scheduleAtFixedRate(
        this::checkIdleTask,
        properties.getIdleInterval(),
        properties.getIdleInterval(),
        TimeUnit.SECONDS);
  }

  private void checkIdleTask() {
    long now = System.currentTimeMillis();
    logger.info("check idle task,runtime:{}", now);
    try {
      pool.removeIf(wrapper -> {
        boolean removed = false;
        // 检查是否过期
        if (now - wrapper.getCreationTime() > properties
            .getMaxLifetime()) {
          removed = true;
        } else if (!factory.validate(wrapper.getResource())) {
          removed = true;
        }

        if (removed) {
          factory.destroy(wrapper.getResource());
          total.decrementAndGet();
          idle.decrementAndGet();
        }
        return removed;
      });

      // 确保维持最小连接数
      while (total.get() < properties.getMinimumIdle()) {
        createPoolWrapper();
      }
    } catch (Exception e) {
      logger.error("check idle task error", e);
    }
  }

  public T tryAcquire() throws Exception {
    return tryAcquire(properties.getAcquireTimeout(), TimeUnit.SECONDS);
  }

  public T tryAcquire(long timeout, TimeUnit unit) throws Exception {
    if (closed.get()) {
      throw new IllegalStateException("pool is closed!!!");
    }
    long deadline = System.nanoTime() + unit.toNanos(timeout);
    while (true) {
      long remainingNanos = deadline - System.nanoTime();
      if (remainingNanos <= 0) {
        throw new TimeoutException("Timeout waiting for object from pool.");
      }

      PooledWrapper<T> wrapper = pool.poll();
      if (wrapper != null) {
        if (factory.validate(wrapper.getResource())) {
          idle.decrementAndGet();
          return wrapper.getResource();
        } else {
          factory.destroy(wrapper.getResource());
          total.decrementAndGet();
        }
      }
      // 队列为空，检查是否可以创建新对象
      if (total.get() < properties.getMaximumPoolSize()) {
        synchronized (pool) {
          if (total.get() < properties.getMaximumPoolSize()) {
            createPoolWrapper();
            wrapper = pool.poll();
            if (wrapper != null) {
              idle.decrementAndGet();
              return wrapper.getResource();
            }
          }
        }
      }

      // 队列已满，必须阻塞等待
      wrapper = pool.poll(timeout, unit);
      if (wrapper != null) {
        idle.decrementAndGet();
        return wrapper.getResource();
      }
      throw new TimeoutException("resource acquisition timed out");
    }
  }

  public void release(T resource) {
    logger.info("release resource {}", resource);
    if (resource == null) {
      return;
    }

    if (!closed.get() && factory.validate(resource)) {
      PooledWrapper<T> wrapper = new PooledWrapper<>(resource);
      if (!pool.tryTransfer(wrapper)) {
        pool.offer(wrapper);
        idle.incrementAndGet();
      }
    } else {
      factory.destroy(resource);
      total.decrementAndGet();
      idle.decrementAndGet();
    }
  }

  @Override
  public void close() throws Exception {
    logger.info("starting close pool..........................");
    if (closed.compareAndSet(false, true)) {
      scheduler.shutdown();
      try {
        // 如果等待超时则强制关闭
        if (!scheduler.awaitTermination(properties.getAcquireTimeout(), TimeUnit.MILLISECONDS)) {
          scheduler.shutdownNow();
        }
      } catch (InterruptedException e) {
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
      }
      PooledWrapper<T> wrapper;
      while ((wrapper = pool.poll()) != null) {
        factory.destroy(wrapper.getResource());
        total.decrementAndGet();
        idle.decrementAndGet();
      }
    }
  }
}
