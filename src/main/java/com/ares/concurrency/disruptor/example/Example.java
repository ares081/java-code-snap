package com.ares.concurrency.disruptor.example;

import com.ares.concurrency.disruptor.DisruptorQueue;
import com.ares.concurrency.disruptor.TaskEvent;
import com.ares.concurrency.disruptor.TaskProducer;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Example {

  private static final Logger logger = LoggerFactory.getLogger(Example.class);

  public static void main(String[] args) throws Exception {
    DisruptorQueue<User> queue = new DisruptorQueue<>();

    BizTaskConsumer[] consumers = new BizTaskConsumer[4];
    for (int i = 0; i < 4; i++) {
      consumers[i] = new BizTaskConsumer();
    }
    queue.start(consumers);

    TaskProducer<User> producer = new TaskProducer<>(queue);
    for (long i = 0; i < 10L; i++) {
      MDC.put("traceId", String.valueOf(i));
      logger.info("id: {}", i);
      TaskEvent<User> taskEvent = new TaskEvent<>();
      taskEvent.setTaskId(Instant.now().toEpochMilli());
      taskEvent.setPayload(new User(i, "ares"));
      producer.push(taskEvent);
    }

    queue.shutdown();
  }
}
