package com.ares.concurrency.disruptor;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTaskConsumer<T> implements EventHandler<TaskEvent<T>> {

  private final Logger logger = LoggerFactory.getLogger(AbstractTaskConsumer.class);

  @Override
  public void onEvent(TaskEvent<T> event, long sequence, boolean endOfBatch) throws Exception {
    if (event.markAsProcessed()) {
      logger.info("consumer sequence: {}", sequence);
      try {
        bizHandler(event);
      } catch (Exception e) {
        throw e;
      }
    }
  }

  protected abstract void bizHandler(TaskEvent<T> event);
}
