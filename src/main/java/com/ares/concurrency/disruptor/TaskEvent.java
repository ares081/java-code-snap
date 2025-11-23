package com.ares.concurrency.disruptor;

import java.util.concurrent.atomic.AtomicBoolean;

public class TaskEvent<T> {

  private long taskId;
  private T payload;
  private final AtomicBoolean processed = new AtomicBoolean(false);

  public TaskEvent() {
  }

  public TaskEvent(long taskId, T payload) {
    this.taskId = taskId;
    this.payload = payload;
  }

  public boolean markAsProcessed() {
    return processed.compareAndSet(false, true);
  }

  public boolean isProcessed() {
    return processed.get();
  }

  public long getTaskId() {
    return taskId;
  }

  public void setTaskId(long taskId) {
    this.taskId = taskId;
  }

  public T getPayload() {
    return payload;
  }

  public void setPayload(T payload) {
    this.payload = payload;
  }
}
