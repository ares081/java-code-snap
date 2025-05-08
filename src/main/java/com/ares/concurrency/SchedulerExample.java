package com.ares.concurrency;

import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerExample {

  private static final Logger log = LoggerFactory.getLogger(SchedulerExample.class);

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    DefaultScheduler scheduler = new DefaultScheduler(1, "test");
    scheduler.startup();
    scheduler.schedule("test", () -> log.info("test scheduler"), 5, 10);
  }

}
