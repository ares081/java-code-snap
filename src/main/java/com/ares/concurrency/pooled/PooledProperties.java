package com.ares.concurrency.pooled;

import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PooledProperties {

  int maximumPoolSize = 10;
  int minimumIdle = 5;
  long idleInterval = 60L;
  long idleTimeout = 3000L;
  long maxLifetime = 60 * 1000L;
  long acquireTimeout = 5000L;
  TimeUnit acquireTimeunit = TimeUnit.MILLISECONDS;

  public PooledProperties() {

  }

  public PooledProperties(int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
  }

  public PooledProperties(int maximumPoolSize, int minimumIdle, long connTimeout, TimeUnit unit) {
    this.maximumPoolSize = maximumPoolSize;
    this.minimumIdle = minimumIdle;
    this.acquireTimeout = connTimeout;
    this.acquireTimeunit = unit;
  }

}
