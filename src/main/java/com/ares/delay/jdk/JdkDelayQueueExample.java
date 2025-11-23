package com.ares.delay.jdk;

import com.ares.delay.DelayTask;
import com.ares.delay.OrderInfo;
import java.util.concurrent.DelayQueue;

public class JdkDelayQueueExample {

  public static void main(String[] args) {
    OrderInfo o1 = new OrderInfo(10000L, 10000L, 100000L, "o1");
    OrderInfo o2 = new OrderInfo(10000L, 10000L, 100000L, "o2");
    OrderInfo o3 = new OrderInfo(10000L, 10000L, 100000L, "o3");

    DelayTask<OrderInfo> d1 = new DelayTask<>("order-service", o1, 5 * 1000L,
        System.currentTimeMillis() + 5 * 1000L);
    DelayTask<OrderInfo> d2 = new DelayTask<>("order-service", o2, 5 * 1000L,
        System.currentTimeMillis() + 5 * 1000L);
    DelayTask<OrderInfo> d3 = new DelayTask<>("order-service", o3, 5 * 1000L,
        System.currentTimeMillis() + 5 * 1000L);

    DelayQueue<JdkDelayElement<OrderInfo>> queue = new DelayQueue<>();
    queue.put(new JdkDelayElement<>(d1));
    queue.put(new JdkDelayElement<>(d2));
    queue.put(new JdkDelayElement<>(d3));

    JdkDelayQueueExecutor executor = new JdkDelayQueueExecutor(queue);
    executor.execute();
  }

}
