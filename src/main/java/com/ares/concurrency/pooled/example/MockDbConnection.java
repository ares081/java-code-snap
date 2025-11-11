package com.ares.concurrency.pooled.example;

import java.util.concurrent.atomic.AtomicInteger;

public class MockDbConnection {
  private static final AtomicInteger idCounter = new AtomicInteger(0);
  private final int id;
  private boolean closed = false;
  private long lastUsedTime;

  public MockDbConnection() {
    this.id = idCounter.incrementAndGet();
    this.lastUsedTime = System.currentTimeMillis();
    System.out.println("创建连接 #" + id);
  }

  public void executeQuery(String query) {
    if (closed) {
      throw new RuntimeException("连接 #" + id + " 已关闭。");
    }
    System.out.println("线程 " + Thread.currentThread().getName() + " 使用连接 #" + id + " 执行查询: " + query);
    this.lastUsedTime = System.currentTimeMillis();
    // 模拟耗时
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
  public boolean isClosed() {
    return closed;
  }

  public void close() {
    this.closed = true;
    System.out.println("销毁连接 #" + id);
  }

  @Override
  public String toString() {
    return "MockDbConnection[id=" + id + "]";
  }


}
