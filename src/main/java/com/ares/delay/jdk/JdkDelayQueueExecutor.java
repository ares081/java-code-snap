package com.ares.delay.jdk;

import com.ares.delay.DelayQueueExecutor;
import com.ares.delay.DelayTask;
import com.ares.delay.OrderInfo;
import java.util.concurrent.DelayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdkDelayQueueExecutor implements DelayQueueExecutor {

  private static final Logger logger = LoggerFactory.getLogger(JdkDelayQueueExecutor.class);
  private final DelayQueue<JdkDelayElement<OrderInfo>> queue;

  public JdkDelayQueueExecutor(DelayQueue<JdkDelayElement<OrderInfo>> queue) {
    this.queue = queue;
  }

  @Override
  public void execute() {
    while (!queue.isEmpty()) {
      try {
        JdkDelayElement<OrderInfo> element = queue.take();
        DelayTask<OrderInfo> delayTask = element.task();
        OrderInfo orderInfo = delayTask.payload();
        logger.info("orderId={}, userId={}, delayTime={}, expireTime={}", orderInfo.getOrderId(),
            orderInfo.getUserId(), delayTask.delayTime(), delayTask.expireTime());
      } catch (InterruptedException e) {
        logger.error("jdk delay task execute failed: ", e);
      }
    }
  }
}
