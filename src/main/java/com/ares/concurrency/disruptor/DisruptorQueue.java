package com.ares.concurrency.disruptor;

import com.ares.concurrency.DefaultThreadFactory;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class DisruptorQueue<T> {

  public static final Integer DEFAULT_BUFFER_SIZE = 4096 << 1 << 1;

  private final Disruptor<TaskEvent<T>> disruptor;

  public DisruptorQueue() {
    this(DEFAULT_BUFFER_SIZE);
  }

  public DisruptorQueue(int bufferSize) {
    this(bufferSize, ProducerType.SINGLE, new BlockingWaitStrategy(),
        new DefaultThreadFactory("default-disruptor"));
  }

  public DisruptorQueue(int bufferSize, ThreadFactory threadFactory) {
    this(bufferSize, ProducerType.SINGLE, new BlockingWaitStrategy(), threadFactory);
  }

  public DisruptorQueue(int bufferSize, ProducerType producerType, WaitStrategy waitStrategy,
      ThreadFactory threadFactory) {

    this.disruptor = new Disruptor<>(
        new TaskEventFactory<>(),
        bufferSize,
        threadFactory,
        producerType,
        waitStrategy);
  }

  @SafeVarargs
  public final void start(AbstractTaskConsumer<T>... consumer) {
    disruptor.handleEventsWith(consumer);
    disruptor.start();
  }

  public Disruptor<TaskEvent<T>> getDisruptor() {
    return this.disruptor;
  }

  public void shutdown() throws TimeoutException {
    disruptor.shutdown(5, TimeUnit.SECONDS);
  }
}
