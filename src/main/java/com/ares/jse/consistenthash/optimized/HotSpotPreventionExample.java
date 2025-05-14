package com.ares.jse.consistenthash.optimized;

import com.ares.jse.consistenthash.HashFunction;
import com.ares.jse.consistenthash.MurmurHashFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HotSpotPreventionExample {

  private static final int BASE_VIRTUAL_NODES = 200;
  private static final int NUM_NODES = 5;
  private static final int NUM_KEYS = 100_000;
  private static final int HOT_KEY_THRESHOLD = 100;

  public static void main(String[] args) throws Exception {

    HashFunction hashFunction = new MurmurHashFunction();

    List<WeightedNode> nodes = new ArrayList<>();
    for (int i = 1; i <= NUM_NODES; i++) {
      double capacity = 100 * (1 + 0.2 * (i - 1));
      nodes.add(new WeightedNode("node-" + i, "Node " + i, "192.168.0." + i, capacity));
    }

    HotSpotAwareConsistentHash<WeightedNode> hashRing = new HotSpotAwareConsistentHash<>(
        hashFunction, BASE_VIRTUAL_NODES,
        BASE_VIRTUAL_NODES * 3,
        HOT_KEY_THRESHOLD,
        100,
        true,
        0.2
    );

    for (WeightedNode node : nodes) {
      hashRing.addNode(node);
    }

    List<String> keys = new ArrayList<>(NUM_KEYS);
    for (int i = 0; i < NUM_KEYS; i++) {
      keys.add("key-" + i);
    }

    System.out.println("Simulating uniform access pattern...");
    simulateUniformAccess(hashRing, keys);
    printNodeStatistics(nodes);

    for (WeightedNode node : nodes) {
      node.decayLoad(0);
    }

    System.out.println("\nSimulating skewed access pattern with hot keys...");
    simulateSkewedAccess(hashRing, keys);
    printNodeStatistics(nodes);

    System.out.println("\nWaiting for auto-rebalancing...");
    Thread.sleep(10000);
    // Reset node loads
    for (WeightedNode node : nodes) {
      node.decayLoad(0);
    }

    System.out.println("\nSimulating skewed access pattern after rebalancing...");
    simulateSkewedAccess(hashRing, keys);
    printNodeStatistics(nodes);

    hashRing.shutdown();
  }

  private static void simulateUniformAccess(
      HotSpotAwareConsistentHash<WeightedNode> hashRing,
      List<String> keys) throws InterruptedException {

    int numThreads = 4;
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    CountDownLatch latch = new CountDownLatch(numThreads);
    Random random = new Random();

    for (int t = 0; t < numThreads; t++) {
      executor.submit(() -> {
        try {
          for (int i = 0; i < 100_000; i++) {
            String key = keys.get(random.nextInt(keys.size()));
            hashRing.getNode(key);
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
  }

  private static void simulateSkewedAccess(
      HotSpotAwareConsistentHash<WeightedNode> hashRing,
      List<String> keys) throws InterruptedException {

    int numThreads = 4;
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    CountDownLatch latch = new CountDownLatch(numThreads);
    Random random = new Random();

    List<String> hotKeys = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      hotKeys.add(keys.get(random.nextInt(keys.size())));
    }

    for (int t = 0; t < numThreads; t++) {
      executor.submit(() -> {
        try {
          for (int i = 0; i < 100_000; i++) {
            String key;
            if (random.nextDouble() < 0.8) {
              key = hotKeys.get(random.nextInt(hotKeys.size()));
            } else {
              key = keys.get(random.nextInt(keys.size()));
            }

            double weight = random.nextDouble() < 0.8 ? 2.0 : 1.0;
            hashRing.getNode(key, weight);
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
  }

  private static void printNodeStatistics(List<WeightedNode> nodes) {
    System.out.println("Node load statistics:");

    double totalLoad = nodes.stream()
        .mapToDouble(WeightedNode::getLoad)
        .sum();

    long totalRequests = nodes.stream()
        .mapToLong(WeightedNode::getRequestCount)
        .sum();

    for (WeightedNode node : nodes) {
      double loadPct = totalLoad > 0 ? (node.getLoad() / totalLoad) * 100 : 0;
      double requestPct = totalRequests > 0 ? (node.getRequestCount() * 100.0 / totalRequests) : 0;

      System.out.printf(
          "%s: Load=%.2f (%.2f%%), Requests=%d (%.2f%%), Capacity=%.2f, Weight=%.2f%n",
          node.getName(), node.getLoad(), loadPct,
          node.getRequestCount(), requestPct,
          node.getCapacity(), node.getWeight());
    }
  }
}
