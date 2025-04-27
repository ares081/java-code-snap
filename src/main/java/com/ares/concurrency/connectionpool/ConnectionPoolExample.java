package com.ares.concurrency.connectionpool;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionPoolExample {

  private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolExample.class);

  public static void main(String[] args) {
    // 配置连接池
    PoolConfigProperties properties = new PoolConfigProperties(5, 10, 1500, 10, 1600);
    // 创建连接验证器
    ConnectionValidator<String> validator = new ConnectionPoolExampleValidator();
    // 创建连接池
    try (TransferQueueConnectionPool<String> pool = new TransferQueueConnectionPool<>(
        () -> "Connection-" + System.currentTimeMillis(), validator, properties)) {
      // 使用连接池
      String conn = null;
      try {
        for (int i = 0; i < 50; i++) {
          conn = pool.acquire(10, TimeUnit.MILLISECONDS);
          Thread.sleep(20);
          // 使用连接...
          logger.info("Using connection: " + conn);
        }
      } catch (Exception e) {
        logger.info("acquire failed: {}", e);
      } finally {
        pool.release(conn);
      }

      // 打印池状态
      logger.info("Total connections: {}", pool.totalConns.get());
      logger.info("Idle connections: {}", pool.idleConns.get());
      logger.info("Active connections:{}", pool.totalConns.get() - pool.idleConns.get());
    } catch (Exception e) {
      // Handle exception from close()
      logger.error("processe error:{}", e);
    } // 自动关闭连接池

  }

  public static class ConnectionPoolExampleValidator implements ConnectionValidator<String> {
    @Override
    public boolean validate(String connection) {
      // 实现连接验证逻辑
      return connection != null && !connection.isEmpty();
    }

    @Override
    public void closeConnection(String connection) {
      // 实现连接关闭逻辑
      logger.info("Closing connection: " + connection);
    }
  }
}
