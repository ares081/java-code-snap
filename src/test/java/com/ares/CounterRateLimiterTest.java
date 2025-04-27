package com.ares;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ares.jse.CounterRateLimiter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class CounterRateLimiterTest {

  @Test
  void testBasicRateLimiting() {
    CounterRateLimiter limiter = new CounterRateLimiter(1000, 5);

    // First 5 requests should succeed
    IntStream.range(0, 5)
        .forEach(i -> assertTrue(limiter.tryAcquire(), "Request " + i + " should be accepted"));

    // 6th request should fail
    assertFalse(limiter.tryAcquire(), "6th request should be rejected");
  }

  @Test
  void testWindowReset() throws InterruptedException {
    CounterRateLimiter limiter = new CounterRateLimiter(100, 1);

    assertTrue(limiter.tryAcquire(), "First request should be accepted");
    assertFalse(limiter.tryAcquire(), "Second request should be rejected");

    // Wait for window to reset
    Thread.sleep(200);

    assertTrue(limiter.tryAcquire(), "Request after window reset should be accepted");
  }

  @Test
  void testConcurrentAccess() throws InterruptedException {
    final int THREAD_COUNT = 10;
    final int REQUESTS_PER_THREAD = 100;
    final int WINDOW_SIZE = 1000;
    final int MAX_REQUESTS = 50;

    CounterRateLimiter limiter = new CounterRateLimiter(WINDOW_SIZE, MAX_REQUESTS);
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
    AtomicInteger acceptedRequests = new AtomicInteger(0);

    // Launch concurrent requests
    for (int i = 0; i < THREAD_COUNT; i++) {
      executor.submit(() -> {
        try {
          for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
            if (limiter.tryAcquire()) {
              acceptedRequests.incrementAndGet();
            }
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();

    // Verify that we didn't exceed our limit
    assertTrue(acceptedRequests.get() <= MAX_REQUESTS,
        "Accepted requests should not exceed max requests");
  }

  @Test
  void testEdgeCases() {
    // Test zero window size
    CounterRateLimiter limiter1 = new CounterRateLimiter(0, 5);
    assertTrue(limiter1.tryAcquire(), "Should accept first request even with zero window");

    // Test zero max requests
    CounterRateLimiter limiter2 = new CounterRateLimiter(1000, 0);
    assertFalse(limiter2.tryAcquire(), "Should reject all requests when max requests is zero");
  }

  @Test
  void testBurstySituation() throws InterruptedException {
    CounterRateLimiter limiter = new CounterRateLimiter(500, 5);

    // First burst
    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.tryAcquire(), "Should accept request in first burst");
    }
    assertFalse(limiter.tryAcquire(), "Should reject after limit reached");

    // Wait for window reset
    Thread.sleep(600);

    // Second burst
    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.tryAcquire(), "Should accept request in second burst");
    }
    assertFalse(limiter.tryAcquire(), "Should reject after limit reached in new window");
  }
}
