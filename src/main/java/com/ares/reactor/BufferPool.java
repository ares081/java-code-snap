package com.ares.reactor;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferPool {

  private final Queue<ByteBuffer> pool;
  private final int bufferSize;
  private final int maxPoolSize;
  private final AtomicInteger currentSize = new AtomicInteger(0);

  public BufferPool(int bufferSize, int maxPoolSize) {
    this.bufferSize = bufferSize;
    this.maxPoolSize = maxPoolSize;
    this.pool = new ConcurrentLinkedQueue<>();

    // 预分配一些缓冲区
    int initialSize = Math.min(maxPoolSize, Runtime.getRuntime().availableProcessors() * 2);
    for (int i = 0; i < initialSize; i++) {
      pool.offer(ByteBuffer.allocate(bufferSize));
      currentSize.incrementAndGet();
    }
  }

  /**
   * 从池中获取一个缓冲区 如果池为空且未达到最大大小，则创建新的缓冲区
   */
  public ByteBuffer acquire() {
    ByteBuffer buffer = pool.poll();
    if (buffer == null) {
      // 池为空，创建新的缓冲区
      if (currentSize.get() < maxPoolSize) {
        buffer = ByteBuffer.allocate(bufferSize);
        currentSize.incrementAndGet();
      } else {
        // 达到最大大小，只能等待
        while ((buffer = pool.poll()) == null) {
          // 让出CPU
          Thread.yield();
        }
      }
    }
    // 确保缓冲区是干净的
    buffer.clear();
    return buffer;
  }

  /**
   * 将缓冲区归还到池中
   */
  public void release(ByteBuffer buffer) {
    if (buffer != null && buffer.capacity() == bufferSize) {
      buffer.clear();
      pool.offer(buffer);
    }
  }

  /**
   * 获取当前池大小
   */
  public int size() {
    return currentSize.get();
  }

}
