package com.ares.concurrency.connectionpool;

import lombok.Getter;

@Getter
public class ConnectionWrapper<T> {
  private final T connection;
  private final long createdTime;
  private volatile long lastAccessTime;

  public ConnectionWrapper(T connection) {
    this.connection = connection;
    this.createdTime = System.currentTimeMillis();
    this.lastAccessTime = this.createdTime;
  }
}
