package com.ares.distribute.zookeeper.lock.boot;

import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZkReentrantLock  {

  private final static Logger logger = LoggerFactory.getLogger(ZkReentrantLock.class);
  private final InterProcessMutex lock;

  public ZkReentrantLock(InterProcessMutex lock) {
    this.lock = lock;
  }

  //@Override
  public boolean tryLock(long timeout, TimeUnit unit) throws Exception {
    boolean acquired = false;
    try {
      acquired = lock.acquire(timeout, unit);
    } finally {
      if (acquired) {
        try {
          lock.release();
        } catch (Exception e) {
          // 记录日志或上报：释放锁失败通常是会话超时/中断导致
          // 这里示例简单打印栈，生产系统请使用日志框架记录
          logger.error("zk distribute lock release failed", e);
        }
      }
    }
    return acquired;
  }
}
