package com.ares.timewheel;

public interface Timer extends AutoCloseable {

  int size();

  void add(TimingTask timerTask);

  boolean advanceClock(long timeoutMs) throws InterruptedException;
}
