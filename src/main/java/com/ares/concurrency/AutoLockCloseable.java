package com.ares.concurrency;

import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class AutoLockCloseable implements AutoCloseable {

  private final Lock lock;

  public AutoLockCloseable(Lock lock) {
    if (lock == null) {
      throw new IllegalArgumentException("Lock cannot be null");
    }
    this.lock = lock;
    this.lock.lock();
  }

  public static <T, R> R withLock(Lock lock, T arg, Function<T, R> function) {
    if (function == null) {
      throw new IllegalArgumentException("Function cannot be null");
    }
    try (AutoLockCloseable ignored = new AutoLockCloseable(lock)) {
      return function.apply(arg);
    }
  }

  public static <R> R withLock(Lock lock, Supplier<R> supplier) {
    if (supplier == null) {
      throw new IllegalArgumentException("Supplier cannot be null");
    }
    try (AutoLockCloseable ignored = new AutoLockCloseable(lock)) {
      return supplier.get();
    }
  }

  public static void withLock(Lock lock, Runnable runnable) {
    if (runnable == null) {
      throw new IllegalArgumentException("Runnable cannot be null");
    }
    try (AutoLockCloseable ignored = new AutoLockCloseable(lock)) {
      runnable.run();
    }
  }

  @Override
  public void close() {
    lock.unlock();
  }
}