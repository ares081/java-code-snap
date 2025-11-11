package com.ares.concurrency.pooled;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PooledWrapper<T> {

  private final T resource;
  private final long creationTime;
  private final long lastAccessTime;

  PooledWrapper(T resource) {
    this.resource = resource;
    this.creationTime = System.currentTimeMillis();
    this.lastAccessTime = this.creationTime;
  }
}
