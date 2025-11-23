package com.ares.delay.jdk;

import com.ares.delay.DelayTask;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;


public record JdkDelayElement<T>(DelayTask<T> task) implements Delayed {

  @Override
  public long getDelay(@NotNull TimeUnit unit) {
    return unit.convert(task.expireTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(@NotNull Delayed o) {
    if (this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS)) {
      return 1;
    } else if (this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS)) {
      return -1;
    }
    return 0;
  }

}
