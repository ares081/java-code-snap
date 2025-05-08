package com.ares.timewheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeWheelExample {

  private static final Logger log = LoggerFactory.getLogger(TimeWheelExample.class);

  public static void main(String[] args) throws Exception {
    TaskExecutor execute = new TaskExecutor("example");
    try {
      for (int i = 0; i < 1000; i++) {
        int finalI = i;
        execute.add(new TimingTask(1000L) {
          final int taskId = finalI;
          @Override
          public void run() {
            log.info("task is running, id: {}", taskId);
          }
        });
      }

      for (; ; ) {
        execute.advanceClock(20L);
      }
    } finally {
      execute.close();
    }
  }

}
