package com.ares.concurrency.example;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class FibForkJoinExample {

  public static class FibTask extends ForkJoinTask<Integer> {

    private final int num;
    private Integer result;

    public FibTask(int num) {
      this.num = num;
    }

    @Override
    public Integer getRawResult() {
      return result;
    }

    @Override
    protected void setRawResult(Integer value) {
      this.result = value;
    }

    @Override
    protected boolean exec() {
      if (num <= 1) {
        result = num;
      } else {
        FibTask f1 = new FibTask(num - 1);
        FibTask f2 = new FibTask(num - 2);
        f1.fork();
        f2.invoke();
        f1.join();
        result = f1.result + f2.result;
      }
      return true;
    }
  }

  public static class FibonacciTask extends RecursiveTask<Integer> {

    private final int num;

    public FibonacciTask(int num) {
      this.num = num;
    }


    @Override
    protected Integer compute() {
      if (num <= 1) {
        return num;
      }
      FibonacciTask f1 = new FibonacciTask(num - 1);
      FibonacciTask f2 = new FibonacciTask(num - 2);
      f1.fork();
      f2.invoke();
      return f2.compute() + f1.join();
    }
  }

  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    int num = 20;
    FibTask fun = new FibTask(num);
    ForkJoinPool pool = new ForkJoinPool();
    pool.invoke(fun);
    System.out.println("Fib(" + num + ") = " + fun.getRawResult());
    long end = System.currentTimeMillis();
    System.out.println("cost=" + (end - start) + "ms");
  }
}
