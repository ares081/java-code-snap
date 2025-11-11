package com.ares.concurrency.pooled.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ares.concurrency.pooled.PooledProperties;
import com.ares.concurrency.pooled.TransferQueuePool;

public class MockDbExample {

  public static void main(String[] args) throws Exception {
    // 1.配置
    PooledProperties properties = new PooledProperties();
    properties.setMaximumPoolSize(20);
    properties.setAcquireTimeout(2000);
    properties.setAcquireTimeunit(TimeUnit.MILLISECONDS);

    // 2.创建池
    TransferQueuePool<MockDbConnection> pool = new TransferQueuePool<>(properties,
        new DbConnectionFactory());

    // 3. 创建线程池来模拟并发请求
    int numTasks = 15;
    ExecutorService executor = Executors.newFixedThreadPool(numTasks);

    System.out.printf("--- 启动 %d 个并发任务，池大小为 %d ---\n", numTasks,
        properties.getMaximumPoolSize());

    for (int i = 0; i < numTasks; i++) {
      final int taskId = i;
      executor.submit(() -> {
        MockDbConnection connection = null;
        try {
          // 4. 借用连接
          System.out.println("任务 " + taskId + " 尝试借用连接...");
          connection = pool.tryAcquire();

          // 5. 使用连接
          connection.executeQuery("SELECT * FROM users WHERE id=" + taskId);

        } catch (Exception e) {
          System.err.println("任务 " + taskId + " 获取连接失败: " + e.getMessage());
        } finally {
          if (connection != null) {
            // 6. 归还连接
            System.out.println("任务 " + taskId + " 归还连接 " + connection);
            pool.release(connection);
          }
        }
      });
    }

    // 7. 关闭
    executor.shutdown();
    if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
      executor.shutdownNow(); // 超时则强制终止线程
    }

    pool.close();
  }

}
