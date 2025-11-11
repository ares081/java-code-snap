package com.ares.concurrency.pooled;

public interface PooledFactory<T> {

  T create() throws Exception;

  void destroy(T obj);

  boolean validate(T obj);
}
