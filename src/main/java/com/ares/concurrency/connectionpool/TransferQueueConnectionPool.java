package com.ares.concurrency.connectionpool;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferQueueConnectionPool<T> implements AutoCloseable {

  private final Logger logger = LoggerFactory.getLogger(TransferQueueConnectionPool.class);

  private final LinkedTransferQueue<ConnectionWrapper<T>> pool;
  private final Supplier<T> connFactory;
  private final ConnectionValidator<T> validator;
  private final PoolConfigProperties properties;
  private final ScheduledExecutorService scheduledExecutor;

  final AtomicBoolean isShutdown = new AtomicBoolean(false);
  public final AtomicInteger totalConns = new AtomicInteger(0);
  public final AtomicInteger idleConns = new AtomicInteger(0);

  public TransferQueueConnectionPool(Supplier<T> connFactory,
      ConnectionValidator<T> validator, PoolConfigProperties properties) {
    this.connFactory = connFactory;
    this.validator = validator;
    this.properties = properties;
    this.pool = new LinkedTransferQueue<>();
    this.scheduledExecutor = Executors.newScheduledThreadPool(1);

    initializePool();
    startScheduleTasks();
  }

  private void initializePool() {
    for (int i = 0; i < properties.minSize(); i++) {
      createAndAddConnection();
    }
  }

  private void createAndAddConnection() {
    logger.info("create connection");
    T conn = connFactory.get();
    ConnectionWrapper<T> wrapper = new ConnectionWrapper<>(conn);
    pool.offer(wrapper);
    totalConns.incrementAndGet();
    idleConns.incrementAndGet();
  }

  public T acquire(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
    if (isShutdown.get()) {
      throw new IllegalStateException("pool is shutdown");
    }
    ConnectionWrapper<T> wrapper = pool.poll();
    if (wrapper != null) {
      idleConns.decrementAndGet();
      return wrapper.getConnection();
    }

    if (totalConns.get() < properties.maxSize()) {
      synchronized (this) {
        if (totalConns.get() < properties.maxSize()) {
          createAndAddConnection();
          wrapper = pool.poll();
          if (wrapper != null) {
            idleConns.decrementAndGet();
            return wrapper.getConnection();
          }
        }
      }
    }
    wrapper = pool.poll(timeout, unit);
    if (wrapper != null) {
      idleConns.decrementAndGet();
      return wrapper.getConnection();
    }
    throw new TimeoutException("Connection acquisition timed out");
  }

  public void release(T connection) {
    logger.info("release connection id: {}", connection);
    if (connection == null) {
      return;
    }
    if (!isShutdown.get()) {
      ConnectionWrapper<T> wrapper = new ConnectionWrapper<>(connection);
      pool.offer(wrapper);
      idleConns.incrementAndGet();
    } else {
      validator.closeConnection(connection);
      idleConns.decrementAndGet();
      totalConns.decrementAndGet();
    }
  }

  @Override
  public void close() throws Exception {
    if (isShutdown.compareAndSet(false, true)) {
      scheduledExecutor.shutdown();
      try {
        if (scheduledExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
          scheduledExecutor.shutdown();
        }
      } catch (InterruptedException e) {
        scheduledExecutor.shutdownNow();
        Thread.currentThread().interrupt();
      }
      ConnectionWrapper<T> wrapper;
      while ((wrapper = pool.poll()) != null) {
        validator.closeConnection(wrapper.getConnection());
        totalConns.decrementAndGet();
        idleConns.decrementAndGet();
      }
    }
  }

  private void startScheduleTasks() {
    scheduledExecutor.scheduleAtFixedRate(
        this::checkIdsTask,
        properties.validationInterval(),
        properties.validationInterval(),
        TimeUnit.MILLISECONDS);
    // 定期记录池状态
    scheduledExecutor.scheduleAtFixedRate(this::logPoolStats, 1, 1, TimeUnit.SECONDS);
  }

  private void checkIdsTask() {
    try {
      long now = System.currentTimeMillis();
      pool.removeIf(wrapper -> {
        boolean remove = false;

        // 检查连接是否过期
        if (now - wrapper.getCreatedTime() > properties.maxLifetime()) {
          remove = true;
        }
        // 检查空闲连接
        else if (now - wrapper.getLastAccessTime() > properties.maxIdleTime()) {
          remove = true;
        }
        // 验证连接是否有效
        else if (!validator.validate(wrapper.getConnection())) {
          remove = true;
        }
        if (remove) {
          validator.closeConnection(wrapper.getConnection());
          totalConns.decrementAndGet();
          idleConns.decrementAndGet();
        }
        return remove;
      });
      // 确保维持最小连接数
      while (totalConns.get() < properties.minSize()) {
        createAndAddConnection();
      }
    } catch (Exception e) {
      logger.warn("Maintenance task error: " + e.getMessage());
    }
  }

  private void logPoolStats() {
    int totalConnections = totalConns.get();
    int idleConnections = idleConns.get();
    int activeConnections = totalConnections - idleConnections;
    logger.info("Pool Stats - Total: {} Active: {}, Idle: {}", totalConnections, activeConnections,
        idleConnections);
  }
}