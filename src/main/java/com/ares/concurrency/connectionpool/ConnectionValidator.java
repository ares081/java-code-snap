package com.ares.concurrency.connectionpool;

public interface ConnectionValidator<T> {
  boolean validate(T connection);

  void closeConnection(T connection);
}
