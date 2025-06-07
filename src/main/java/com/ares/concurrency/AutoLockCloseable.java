package com.ares.concurrency;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

public class AutoLockCloseable implements AutoCloseable {

  ConcurrentHashMap<Thread, Lock> locks = new ConcurrentHashMap<>();

  private final Lock lock;

  public AutoLockCloseable(Lock lock) {
    this.lock = lock;
    this.lock.lock();
  }

  /**
   * 用于自动加锁并执行带返回值的函数
   */
  public static <T, R> R withLock(Lock lock, T arg, Function<T, R> function) {
    try (AutoLockCloseable ignored = new AutoLockCloseable(lock)) {
      return function.apply(arg);
    }
  }

  @Override
  public void close() {
    lock.unlock();
  }
}
