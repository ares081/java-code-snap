package com.ares.timewheel;

public class TaskSlots {

  protected final TimingTask task;
  protected final long expireMs;
  protected volatile TaskList list;
  TaskSlots prev;
  TaskSlots next;

  public TaskSlots(TimingTask task, long expireMs) {
    this.task = task;
    this.expireMs = expireMs;

    if (task != null) {
      task.setTaskSlots(this);
    }
  }

  public boolean cancelled() {
    return task.getTimerTaskEntry() != this;
  }

  public void remove() {
    TaskList currentList = list;
    while (currentList != null) {
      currentList.remove(this);
      currentList = list;
    }
  }
}
