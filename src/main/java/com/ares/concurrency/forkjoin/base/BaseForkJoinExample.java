package com.ares.concurrency.forkjoin.base;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class BaseForkJoinExample {

  private static final int n = 10;

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    ForkJoinPool pool = new ForkJoinPool(4);
    Integer result1 = pool.submit(new FibonacciTask(n)).join();
    Integer result2 = pool.invoke(new FibonacciTask(n));
    System.out.println("result = " + result1);
    System.out.println("result = " + result2);
    pool.shutdown();
  }

  public static class FibonacciTask extends RecursiveTask<Integer> {

    private final int n;

    public FibonacciTask(int n) {
      this.n = n;
    }

   /* @Override
    protected Integer compute() {
      if (n <= 1) {
        return n;
      }
      System.out.println("thread name = " + Thread.currentThread().getName() + ", n=" + n);
      FibonacciTask left = new FibonacciTask(n - 1);
       FibonacciTask right = new FibonacciTask(n - 2);
      left.fork();
      right.fork();
      Integer rightResult = right.join();
      Integer leftResult = left.join();
      return leftResult + rightResult;
    }*/

    @Override
    protected Integer compute() {
      if (n <= 1) {
        return n;
      }
      System.out.println("thread name = " + Thread.currentThread().getName() + ", n=" + n);
      FibonacciTask left = new FibonacciTask(n - 1);
      FibonacciTask right = new FibonacciTask(n - 2);
      left.fork();
      Integer rightResult = right.compute();
      Integer leftResult = left.join();
      return leftResult + rightResult;
    }

   /* @Override
    protected Integer compute() {
      if (n <= 1) {
        return n;
      }
      System.out.println("thread name = " + Thread.currentThread().getName() + ", n=" + n);
      FibonacciTask left = new FibonacciTask(n - 1);
      FibonacciTask right = new FibonacciTask(n - 2);
      invokeAll(left, right);
      Integer rightResult = right.join();
      Integer leftResult = left.join();
      return leftResult + rightResult;
    }*/
  }
}
