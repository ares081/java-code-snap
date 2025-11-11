package com.ares.concurrency.example;

import com.ares.concurrency.DefaultScheduler;
import com.ares.concurrency.DefaultThreadFactory;
import com.ares.concurrency.Scheduler;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class SchedulerExample {

  private static final Logger log = LoggerFactory.getLogger(SchedulerExample.class);

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    // 原生schedule
    ScheduledExecutorService scheduleService = new ScheduledThreadPoolExecutor(1,
        new DefaultThreadFactory("test"));

    MDC.put("traceId", String.valueOf(Instant.now().toEpochMilli()));
    log.info("test schedule task, thread name:{}.....................................",
        Thread.currentThread().getName());

    scheduleService.scheduleAtFixedRate(
        () -> log.info("sub task scheduleService, Thread Nmae: {}..........................",
            Thread.currentThread().getName()), 0,
        3, TimeUnit.SECONDS);

    // 封装scheduler
    Scheduler scheduler = new DefaultScheduler(1);
    scheduler.startup();
    scheduler.schedule(
        () -> log.info("sub task scheduleService, Thread Nmae: {}..........................",
            Thread.currentThread().getName()), 1, 2, TimeUnit.SECONDS);
    MDC.remove("traceId");

  }

}
