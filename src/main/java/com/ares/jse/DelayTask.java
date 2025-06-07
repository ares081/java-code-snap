package com.ares.jse;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayTask implements Delayed {

  private final long executeTime; // 执行时间（毫秒）
  private final String taskName;

  public DelayTask(long delayMillis, String taskName) {
    this.executeTime = System.currentTimeMillis() + delayMillis;
    this.taskName = taskName;
  }

  @Override
  public long getDelay(TimeUnit unit) {
    long diff = executeTime - System.currentTimeMillis();
    return unit.convert(diff, TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(Delayed o) {
    if (this.executeTime < ((DelayTask) o).executeTime) {
      return -1;
    }
    if (this.executeTime > ((DelayTask) o).executeTime) {
      return 1;
    }
    return 0;
  }

  public String getTaskName() {
    return taskName;
  }
}
