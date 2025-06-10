package com.ares.consistenthash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsistentHashExample {

  private static final int VIRTUAL_NODES = 200;

  public static void main(String[] args) throws Exception {
    // Create nodes
    List<Node> nodes = new ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      nodes.add(new Node("node-" + i, "Node " + i, "192.168.0." + i));
    }

    // Create hash function and consistent hash
    HashFunction hashFunction = new MurmurHashFunction();
    ConsistentHash<Node> consistentHash =
        new ConsistentHash<>(hashFunction, VIRTUAL_NODES, nodes);

    // Test basic functionality
    System.out.println("Initial distribution:");
    Map<Node, AtomicInteger> distribution = testDistribution(consistentHash, 10000);
    printDistribution(distribution);

    // Test concurrent access
    System.out.println("\nTesting concurrent access...");
    testConcurrentAccess(consistentHash);

    // Add a new node
    Node newNode = new Node("node-4", "Node 4", "192.168.0.4");
    System.out.println("\nAdding node: " + newNode.getName());
    consistentHash.addNode(newNode);

    // Test distribution after adding node
    System.out.println("\nDistribution after adding node:");
    distribution = testDistribution(consistentHash, 10000);
    printDistribution(distribution);

    // Remove a node
    Node nodeToRemove = nodes.get(1);
    System.out.println("\nRemoving node: " + nodeToRemove.getName());
    consistentHash.removeNode(nodeToRemove);

    // Test distribution after removing node
    System.out.println("\nDistribution after removing node:");
    distribution = testDistribution(consistentHash, 10000);
    printDistribution(distribution);
  }

  private static Map<Node, AtomicInteger> testDistribution(
      ConsistentHash<Node> consistentHash, int numKeys) {
    Map<Node, AtomicInteger> distribution = new HashMap<>();

    for (int i = 0; i < numKeys; i++) {
      String key = "key-" + i;
      Node node = consistentHash.getNode(key);
      distribution.computeIfAbsent(node, k -> new AtomicInteger(0)).incrementAndGet();
    }

    return distribution;
  }

  private static void printDistribution(Map<Node, AtomicInteger> distribution) {
    int total = distribution.values().stream().mapToInt(AtomicInteger::get).sum();

    for (Map.Entry<Node, AtomicInteger> entry : distribution.entrySet()) {
      double percentage = 100.0 * entry.getValue().get() / total;
      System.out.printf("%s: %d keys (%.2f%%)\n",
          entry.getKey().getName(), entry.getValue().get(), percentage);
    }
  }

  private static void testConcurrentAccess(ConsistentHash<Node> consistentHash) throws Exception {
    int numThreads = 10;
    int keysPerThread = 100000;
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    for (int i = 0; i < numThreads; i++) {
      final int threadId = i;
      executor.submit(() -> {
        for (int j = 0; j < keysPerThread; j++) {
          String key = "key-" + threadId + "-" + j;
          consistentHash.getNode(key);
        }
      });
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
    System.out.println("Successfully completed concurrent access test");
  }
}
