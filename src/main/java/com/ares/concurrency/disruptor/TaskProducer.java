package com.ares.concurrency.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class TaskProducer<T> {
  private final RingBuffer<TaskEvent<T>> ringBuffer;

  public TaskProducer(DisruptorQueue<T> queue) {
    Disruptor<TaskEvent<T>> disruptor = queue.getDisruptor();
    this.ringBuffer = disruptor.getRingBuffer();
  }

  public void push(TaskEvent<T> event) {
    long sequence = ringBuffer.next();
    try {
      TaskEvent<T> taskEvent = ringBuffer.get(sequence);
      taskEvent.setTaskId(event.getTaskId());
      taskEvent.setPayload(event.getPayload());
    } finally {
      ringBuffer.publish(sequence);
    }
  }
}
