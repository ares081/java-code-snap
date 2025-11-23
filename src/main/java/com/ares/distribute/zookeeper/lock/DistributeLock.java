package com.ares.distribute.zookeeper.lock;

import java.util.concurrent.TimeUnit;

public interface DistributeLock extends AutoCloseable {

  boolean tryLock(long timeout, TimeUnit unit) throws Exception;

  void releaseLock() throws Exception;
}
