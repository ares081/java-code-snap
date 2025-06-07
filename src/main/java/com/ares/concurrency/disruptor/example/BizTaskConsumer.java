package com.ares.concurrency.disruptor.example;

import com.ares.concurrency.disruptor.AbstractTaskConsumer;
import com.ares.concurrency.disruptor.TaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BizTaskConsumer extends AbstractTaskConsumer<User> {

  private static final Logger logger = LoggerFactory.getLogger(BizTaskConsumer.class);

  @Override
  protected void bizHandler(TaskEvent<User> event) {
    logger.info("biz consumer, taskId: {}, payload: {}", event.getTaskId(), event.getPayload());
  }
}
