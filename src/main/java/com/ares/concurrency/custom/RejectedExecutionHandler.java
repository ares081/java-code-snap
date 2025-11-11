package com.ares.concurrency.custom;

public interface RejectedExecutionHandler {

  /**
   * 当任务被拒绝时调用的方法
   *
   * @param r    被拒绝的任务
   * @param pool 提交任务的线程池
   */
  void rejectedExecution(Runnable r, CustomThreadPool pool);
}
