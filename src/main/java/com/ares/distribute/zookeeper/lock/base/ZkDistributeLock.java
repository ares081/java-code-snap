package com.ares.distribute.zookeeper.lock.base;

import com.ares.distribute.zookeeper.CuratorProperties;
import com.ares.distribute.zookeeper.lock.DistributeLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;

public class ZkDistributeLock implements DistributeLock {

  private final AtomicBoolean locked = new AtomicBoolean(false);
  private final InterProcessMutex lock;

  public ZkDistributeLock(CuratorProperties properties) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory.builder()
        .connectString(properties.getAddress())
        .connectionTimeoutMs(properties.getConnectionTimeout())
        .sessionTimeoutMs(properties.getSessionTimeout())
        .retryPolicy(
            new ExponentialBackoffRetry(properties.getSleepTime(), properties.getMaxRetries()))
        .build();
    client.start();
    client.create().withMode(CreateMode.PERSISTENT).withACL(Ids.OPEN_ACL_UNSAFE);

    this.lock = new InterProcessMutex(client, properties.getBasePath() + "/inter-mutex");
  }

  @Override
  public boolean tryLock(long timeout, TimeUnit unit) throws Exception {
    if (timeout > 0) {
      boolean acquired = lock.acquire(timeout, unit);
      if (acquired) {
        locked.compareAndSet(false, true);
      }
    } else {
      lock.acquire();
      locked.compareAndSet(false, true);
    }
    return locked.get();
  }

  @Override
  public void releaseLock() throws Exception {
    if (locked.compareAndSet(true, false)) {
      lock.release();
    }
  }

  @Override
  public void close() throws Exception {
    releaseLock();
  }

  public boolean isLocked() {
    return locked.get();
  }
}
