package com.ares.concurrency.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExample {

  private final Lock lock = new ReentrantLock();

  public void testLock() {
    lock.lock();
    try {
      System.out.println("ReentrantLock example");
    } finally {
      lock.unlock();
    }
  }

  public static void main(String[] args) {
    ReentrantLockExample example = new ReentrantLockExample();
    example.testLock();
  }

}
