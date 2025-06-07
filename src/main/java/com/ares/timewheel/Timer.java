package com.ares.timewheel;

public interface Timer extends AutoCloseable {

  int size();

  void add(TimingTask timerTask);

  void advanceClock(long timeoutMs) throws InterruptedException;
}
