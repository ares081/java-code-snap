package com.ares.timewheel;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class TaskList implements Delayed {

  private final AtomicLong expiration;
  private final AtomicInteger taskCounter;
  private final TaskSlots root;

  public TaskList(AtomicInteger taskCounter) {
    this.taskCounter = taskCounter;
    this.expiration = new AtomicLong(-1);
    this.root = new TaskSlots(null, -1);
    this.root.next = root;
    this.root.prev = root;
  }

  public void add(TaskSlots slots) {
    boolean done = Boolean.FALSE;
    while (!done) {
      slots.remove();
      synchronized (this) {
        if (slots.list == null) {
          TaskSlots tail = root.prev;
          slots.next = root;
          slots.prev = tail;
          slots.list = this;
          tail.next = slots;
          root.prev = slots;
          taskCounter.incrementAndGet();
          done = true;
        }
      }
    }
  }

  public void remove(TaskSlots slots) {
    synchronized (this) {
      if (slots.list.equals(this)) {
        slots.next.prev = slots.prev;
        slots.prev.next = slots.next;
        slots.next = null;
        slots.prev = null;
        slots.list = null;
        taskCounter.decrementAndGet();
      }
    }
  }

  public synchronized void flush(Consumer<TaskSlots> f) {
    TaskSlots head = root.next;
    while (head != root) {
      remove(head);
      f.accept(head);
      head = root.next;
    }
    expiration.set(-1L);
  }

  public boolean setExpiration(long expirationMs) {
    return expiration.getAndSet(expirationMs) != expirationMs;
  }

  public long getExpiration() {
    return expiration.get();
  }
  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(Math.max(getExpiration() - System.currentTimeMillis(), 0),
        TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(Delayed other) {
    if (other instanceof TaskList o) {
      return Long.compare(getExpiration(), o.getExpiration());
    }
    return 0;
  }

}
