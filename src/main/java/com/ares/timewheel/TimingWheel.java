package com.ares.timewheel;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimingWheel {

  private final Logger log = LoggerFactory.getLogger(TimingWheel.class);

  /**
   * 时间轮由多个时间格组成，每个时间格就是 tickMs，它代表当前时间轮的基本时间跨度
   */
  private final Long tickMs;

  /**
   * 代表每一层时间轮的格数
   */
  private final Integer wheelSize;

  /**
   * 当前时间轮的总体时间跨度，interval=tickMs × wheelSize
   */
  private final Long interval;

  /**
   * 构造当层时间轮时候的当前时间
   */
  private Long startMs;

  /**
   * 表示时间轮当前所处的时间，currentTime 是 tickMs 的整数倍
   */
  private Long currentTime;

  private final TaskList[] buckets;

  private final AtomicInteger counter;

  private final DelayQueue<TaskList> queue;

  /**
   * overflowWheel可能由两个并发线程通过add（）进行更新和读取。 因此，由于JVM的双重检查锁定模式的问题，它需要是易变的
   */
  private volatile TimingWheel overflowWheel;

  public TimingWheel(Long tickMs, Integer wheelSize, Long startMs, AtomicInteger taskCounter,
      DelayQueue<TaskList> queue) {
    this.tickMs = tickMs;
    this.wheelSize = wheelSize;
    this.counter = taskCounter;
    this.queue = queue;
    this.buckets = new TaskList[wheelSize];
    this.interval = tickMs * wheelSize;
    this.startMs = startMs;
    this.currentTime = startMs - (startMs % tickMs);
    for (int i = 0; i < buckets.length; i++) {
      buckets[i] = new TaskList(taskCounter);
    }
  }

  public boolean add(TaskSlots slots) {
    long expiration = slots.expireMs;

    if (slots.cancelled()) {
      // Cancelled
      return Boolean.FALSE;
    } else if (expiration < currentTime + tickMs) {
      // Already expired
      return Boolean.FALSE;
    } else if (expiration < currentTime + interval) {
      // 设置到当前 bucket
      long virtualId = expiration / tickMs;
      // 找到任务对应本时间轮的bucket
      int bucketId = (int) (virtualId % (long) wheelSize);
      TaskList bucket = buckets[bucketId];
      bucket.add(slots);

      if (bucket.setExpiration(virtualId * tickMs)) {
        // bucket是一个TimerTaskList，它实现了java.util.concurrent.Delayed接口，里面是一个多任务组成的链表
        // bucket 需要排队，因为它是一个过期的 bucket
        // 我们只需要在 bucket 的过期时间发生变化时排队，即轮子已经前进，以前的 bucket 得到重用；
        // 在同一轮循环内设置过期的进一步调用将传入相同的值，因此返回 false，因此具有相同过期的 bucket 将不会多次排队。
        queue.offer(bucket);
      }
      return Boolean.TRUE;
    } else {
      //任务的过期时间不在本时间轮周期内说明需要升级时间轮，如果不存在则构造上一层时间轮，继续用上一层时间轮添加任务
      if (overflowWheel == null) {
        addOverflowWheel();
      }
      return overflowWheel.add(slots);
    }
  }

  private synchronized void addOverflowWheel() {
    if (overflowWheel == null) {
      overflowWheel = new TimingWheel(interval, wheelSize, currentTime, counter, queue);
    }
  }

  public void advanceClock(long timeMs) {

    if (timeMs >= currentTime + tickMs) {
      // 把当前时间打平为时间轮tickMs的整数倍
      currentTime = timeMs - (timeMs % tickMs);
      if (overflowWheel != null) {
        // 如果有溢流轮，尝试提前溢流轮的时钟
        //驱动上层时间轮，这里的传给上层的currentTime时间是本层时间轮打平过的，但是在上层时间轮还是会继续打平
        overflowWheel.advanceClock(currentTime);
      }
    }
  }
}
