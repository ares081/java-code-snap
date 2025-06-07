package com.ares.concurrency.disruptor;

import com.lmax.disruptor.EventFactory;

public class TaskEventFactory<T> implements EventFactory<TaskEvent<T>> {

  @Override
  public TaskEvent<T> newInstance() {
    return new TaskEvent<>();
  }
}
