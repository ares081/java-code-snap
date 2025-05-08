package com.ares.timewheel;

public abstract class TimingTask implements Runnable {

  private volatile TaskSlots slots;

  protected final Long delayMs;

  TimingTask(Long delayMs) {
    this.delayMs = delayMs;
  }

  public void cancel() {
    synchronized (this) {
      if (slots != null) {
        slots.remove();
      }
      slots = null;
    }
  }

  final void setTaskSlots(TaskSlots entry) {
    synchronized (this) {
      if (slots != null && slots != entry) {
        slots.remove();
      }
      slots = entry;
    }
  }

  TaskSlots getTimerTaskEntry() {
    return slots;
  }
}
