package com.ares.distribute.zookeeper.lock.base;

import com.ares.distribute.zookeeper.CuratorProperties;
import java.util.concurrent.TimeUnit;

public class ZkLockExample {


  public static void main(String[] args) throws Exception {
    CuratorProperties properties = new CuratorProperties();
    properties.setBasePath("/locks");
    properties.setAddress("127.0.0.1:2181");
    properties.setSessionTimeout(10000);
    properties.setConnectionTimeout(1000);
    properties.setMaxRetries(3);
    properties.setSleepTime(3000);

    try (ZkDistributeLock lock = new ZkDistributeLock(properties)) {
      lock.tryLock(3000, TimeUnit.MILLISECONDS);
    }

    ZkDistributeLock lock = new ZkDistributeLock(properties);
    try {
      lock.tryLock(3000, TimeUnit.MILLISECONDS);
    } finally {
      lock.releaseLock();
    }

  }
}
