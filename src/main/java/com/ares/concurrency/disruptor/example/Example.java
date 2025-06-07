package com.ares.concurrency.disruptor.example;

import com.ares.concurrency.disruptor.DisruptorQueue;
import com.ares.concurrency.disruptor.TaskEvent;
import com.ares.concurrency.disruptor.TaskProducer;
import java.time.Instant;

public class Example {

  public static void main(String[] args) throws Exception {
    DisruptorQueue<User> queue = new DisruptorQueue<>();

    BizTaskConsumer[] consumers = new BizTaskConsumer[4];
    for (int i = 0; i < 4; i++) {
      consumers[i] = new BizTaskConsumer();
    }
    queue.start(consumers);

    TaskProducer<User> producer = new TaskProducer<>(queue);
    for (long i = 0; i < 10L; i++) {
      TaskEvent<User> taskEvent = new TaskEvent<>();
      taskEvent.setTaskId(Instant.now().toEpochMilli());
      taskEvent.setPayload(new User(i, "ares"));
      producer.push(taskEvent);
    }

    queue.shutdown();
  }
}
