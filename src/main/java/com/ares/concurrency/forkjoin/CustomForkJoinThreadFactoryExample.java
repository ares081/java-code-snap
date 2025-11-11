package com.ares.concurrency.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.RecursiveTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class CustomForkJoinThreadFactoryExample {

  private static final Logger logger = LoggerFactory.getLogger(
      CustomForkJoinThreadFactoryExample.class);

  public static void main(String[] args) {
    int parallelism = Runtime.getRuntime().availableProcessors();
    for (int i = 0; i < parallelism; i++) {
      MDC.put("traceId", System.currentTimeMillis() + "");
      ForkJoinWorkerThreadFactory threadFactory = new CustomWorkerThreadFactory("xxzp" + (i + 1));
      ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism, threadFactory, null, true);
      logger.info("mani thread:{}", Thread.currentThread().getName());
      forkJoinPool.invoke(new FibonacciTask(5));
    }
    MDC.remove("traceId");
  }

  public static class FibonacciTask extends RecursiveTask<Integer> {

    private final int n;

    public FibonacciTask(int n) {
      this.n = n;
    }

    @Override
    protected Integer compute() {
      if (n <= 1) {
        return n;
      }
      logger.info("ThreadName={},n = {}", Thread.currentThread().getName(), n);
      FibonacciTask left = new FibonacciTask(n - 1);
      FibonacciTask right = new FibonacciTask(n - 2);
      invokeAll(left, right);
      Integer rightResult = right.join();
      Integer leftResult = left.join();
      return leftResult + rightResult;
    }
  }


}
