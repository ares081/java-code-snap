package com.ares.concurrency.custom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CustomThreadPool {

  /**
   * 任务队列，用于存储待执行的任务
   */
  private final BlockingQueue<Runnable> taskQueue;

  /**
   * 工作线程的集合
   */
  private final Set<WorkerThread> workers = new HashSet<>();

  /**
   * 核心线程数（即固定线程数）
   */
  private final int corePoolSize;

  /**
   * 拒绝策略
   */
  private final RejectedExecutionHandler rejectionHandler;

  /**
   * 线程池状态 volatile 保证多线程间的可见性 0: RUNNING - 接受新任务，处理队列任务 1: SHUTDOWN - 不接受新任务，但处理队列任务 2: STOP -
   * 不接受新任务，不处理队列任务，中断正在执行的任务
   */
  private volatile int state;
  private static final int RUNNING = 0;
  private static final int SHUTDOWN = 1;
  private static final int STOP = 2;

  /**
   * 构造函数
   *
   * @param corePoolSize     核心线程数
   * @param queueCapacity    任务队列容量
   * @param rejectionHandler 拒绝策略
   */
  public CustomThreadPool(int corePoolSize, int queueCapacity,
      RejectedExecutionHandler rejectionHandler) {
    if (corePoolSize <= 0 || queueCapacity <= 0) {
      throw new IllegalArgumentException("Invalid pool or queue size");
    }
    this.corePoolSize = corePoolSize;
    this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);
    this.rejectionHandler = rejectionHandler;
    this.state = RUNNING;

    // 预先创建并启动核心线程
    for (int i = 0; i < corePoolSize; i++) {
      WorkerThread worker = new WorkerThread();
      worker.start();
      workers.add(worker);
    }
  }

  /**
   * 提交任务到线程池
   *
   * @param task 要执行的任务
   */
  public void execute(Runnable task) {
    if (task == null) {
      throw new NullPointerException("Task cannot be null");
    }

    if (state != RUNNING) {
      // 如果线程池已关闭，则直接拒绝
      rejectionHandler.rejectedExecution(task, this);
      return;
    }

    // 尝试将任务添加到队列
    boolean offered = taskQueue.offer(task);
    if (!offered) {
      // 队列已满，执行拒绝策略
      rejectionHandler.rejectedExecution(task, this);
    }
  }

  /**
   * 优雅关闭线程池 1. 设置状态为 SHUTDOWN 2. 不再接受新任务 3. 等待队列中所有任务执行完毕
   */
  public void shutdown() {
    if (state < SHUTDOWN) {
      state = SHUTDOWN;
    }
    // 注意：这里没有中断线程。
    // 工作线程在 runLoop() 中会检查 state 和 队列状态，并自行决定何时终止。
  }

  /**
   * 立即关闭线程池 1. 设置状态为 STOP 2. 尝试中断所有正在执行的线程 3. 清空任务队列并返回未执行的任务
   *
   * @return 未执行的任务列表
   */
  public List<Runnable> shutdownNow() {
    if (state < STOP) {
      state = STOP;
    }

    // 中断所有工作线程
    for (WorkerThread worker : workers) {
      worker.interrupt();
    }

    // 清空队列
    List<Runnable> remainingTasks = new ArrayList<>();
    taskQueue.drainTo(remainingTasks);
    return remainingTasks;
  }

  /**
   * 等待线程池终止
   *
   * @param timeout 超时时间
   * @param unit    时间单位
   * @return 如果在超时时间内终止则返回 true，否则返回 false
   * @throws InterruptedException 如果等待被中断
   */
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    for (WorkerThread worker : workers) {
      if (worker.isAlive()) {
        long start = System.nanoTime();
        worker.join(TimeUnit.NANOSECONDS.toMillis(nanos), (int) (nanos % 1000000));
        long elapsed = System.nanoTime() - start;
        nanos -= elapsed;
        if (nanos <= 0) {
          return false; // 超时
        }
      }
    }
    return true;
  }


  /**
   * 抛出异常策略
   */
  public static class AbortPolicy implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, CustomThreadPool pool) {
      throw new RuntimeException("Task " + r.toString() + " rejected from " + pool.toString());
    }
  }


  /**
   * 丢弃任务策略
   */
  public static class DiscardPolicy implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, CustomThreadPool pool) {
      // 什么也不做，直接丢弃
    }
  }


  /**
   * 工作线程内部类
   */
  private final class WorkerThread extends Thread {

    @Override
    public void run() {
      try {
        // 循环获取任务并执行
        while (true) {
          Runnable task = getTask();
          if (task != null) {
            try {
              task.run();
            } catch (Exception e) {
              // 任务执行异常，不能影响工作线程的继续运行
              System.err.println("Task execution failed: " + e.getMessage());
            }
          } else {
            // getTask() 返回 null，表示线程池已关闭且队列为空，线程可以退出了
            break;
          }
        }
      } finally {
        // 线程退出
        workers.remove(this);
        // 可以在这里添加逻辑：如果线程是非正常退出（例如任务异常导致），
        // 且线程池仍在RUNNING，则可以创建一个新线程替换它。
        // 为简化起见，此处省略。
      }
    }

    /**
     * 从队列中获取任务 这是一个关键方法，它需要处理线程池的各种状态
     */
    private Runnable getTask() {
      try {
        // 循环检查状态，以处理 shutdown() 和 shutdownNow()
        while (true) {
          int currentState = state;

          // 状态 >= SHUTDOWN (即 SHUTDOWN 或 STOP)
          if (currentState >= SHUTDOWN) {
            // 检查队列是否为空
            Runnable task = taskQueue.poll();
            if (task != null) {
              // SHUTDOWN 状态，继续处理队列中的剩余任务
              return task;
            } else {
              // 队列为空，并且状态是 SHUTDOWN 或 STOP
              // 线程可以退出了
              return null;
            }
          }

          // 状态 = RUNNING
          // 使用 poll(timeout) 代替 take()
          // 为什么？
          // 1. 如果用 take()，在 shutdown() 时，线程会永远阻塞在 take()，无法退出。
          // 2. poll(timeout) 允许线程在一段时间后醒来，重新检查 state 状态。
          return taskQueue.poll(1, TimeUnit.SECONDS);

        }
      } catch (InterruptedException e) {
        // 发生中断，通常是因为调用了 shutdownNow()
        // 此时应该退出循环，导致线程终止
        // 返回 null 会让外层 run() 循环的 while(true) 退出
        return null;
      }
    }
  }
}
