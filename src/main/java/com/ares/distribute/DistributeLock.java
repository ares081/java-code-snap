package com.ares.distribute;

import java.util.concurrent.TimeUnit;

public interface DistributeLock {

  boolean tryLock(long timeout, TimeUnit unit) throws Exception;
}
