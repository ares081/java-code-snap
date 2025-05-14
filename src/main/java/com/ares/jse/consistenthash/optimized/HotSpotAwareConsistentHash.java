package com.ares.jse.consistenthash.optimized;

import com.ares.concurrency.DefaultScheduler;
import com.ares.jse.consistenthash.HashFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HotSpotAwareConsistentHash<T extends LoadAwareNode> {

  // Hash function
  private final HashFunction hashFunction;

  // Base number of virtual nodes per physical node
  private final int baseVirtualNodes;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  // The hash ring - virtual node hash to physical node
  private final ConcurrentSkipListMap<Integer, T> circle = new ConcurrentSkipListMap<>();

  // Maps physical nodes to their virtual node hashes for efficient operations
  private final Map<T, Set<Integer>> nodeToVirtualNodes = new ConcurrentHashMap<>();

  // Maps physical nodes to their virtual node count
  private final Map<T, Integer> nodeToVirtualNodeCount = new ConcurrentHashMap<>();

  // Hot key tracking - maps key hashes to access counts
  private final Map<Integer, Long> hotKeys = new ConcurrentHashMap<>();

  // Hot key cache - directly maps hot keys to nodes to bypass hash lookup
  private final Map<String, T> hotKeyCache = new ConcurrentHashMap<>();

  private final DefaultScheduler scheduler = new DefaultScheduler(1,
      "HotSpotAwareConsistentHash-Scheduler");

  private final int maxVirtualNodesPerNode;
  private final int hotKeyThreshold;
  private final int maxHotKeys;
  private final double loadImbalanceThreshold;


  private final AtomicBoolean rebalancing = new AtomicBoolean(false);

  public HotSpotAwareConsistentHash(
      HashFunction hashFunction,
      int baseVirtualNodes,
      int maxVirtualNodesPerNode,
      int hotKeyThreshold,
      int maxHotKeys,
      boolean enableAutoBalance,
      double loadImbalanceThreshold) {
    this.hashFunction = hashFunction;
    this.baseVirtualNodes = baseVirtualNodes;
    this.maxVirtualNodesPerNode = maxVirtualNodesPerNode;
    this.hotKeyThreshold = hotKeyThreshold;
    this.maxHotKeys = maxHotKeys;
    this.loadImbalanceThreshold = loadImbalanceThreshold;

    if (enableAutoBalance) {
      scheduler.schedule("10s-schedule", this::processHotKeys, 10, 10, TimeUnit.SECONDS);
      scheduler.schedule("30s-schedule", this::rebalanceNodes, 30, 30, TimeUnit.SECONDS);
      scheduler.schedule("60s-schedule", this::decayNodeLoads, 60, 60, TimeUnit.SECONDS);
    }
  }


  public HotSpotAwareConsistentHash(HashFunction hashFunction, int baseVirtualNodes,
      Collection<T> nodes) {
    this(hashFunction, baseVirtualNodes, baseVirtualNodes * 3, 1000, 100, true, 0.2);

    for (T node : nodes) {
      addNode(node);
    }
  }

  public void addNode(T node) {
    if (node == null) {
      throw new NullPointerException("Node cannot be null");
    }

    try {
      lock.writeLock().lock();

      if (nodeToVirtualNodes.containsKey(node)) {
        return;
      }
      double weight = node.getWeight();
      int virtualNodeCount = calculateVirtualNodeCount(weight);

      Set<Integer> virtualNodeHashes = new HashSet<>(virtualNodeCount);

      for (int i = 0; i < virtualNodeCount; i++) {
        int hash = hashFunction.hash(node.getId() + "-" + i);
        circle.put(hash, node);
        virtualNodeHashes.add(hash);
      }

      nodeToVirtualNodes.put(node, virtualNodeHashes);
      nodeToVirtualNodeCount.put(node, virtualNodeCount);

      hotKeyCache.clear();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void removeNode(T node) {
    if (node == null) {
      return;
    }

    try {
      lock.writeLock().lock();

      Set<Integer> virtualNodeHashes = nodeToVirtualNodes.remove(node);
      if (virtualNodeHashes != null) {
        for (Integer hash : virtualNodeHashes) {
          circle.remove(hash);
        }
        nodeToVirtualNodeCount.remove(node);
      }

      hotKeyCache.clear();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public T getNode(String key) {
    return getNode(key, 1.0);
  }


  public T getNode(String key, double accessWeight) {
    if (key == null) {
      throw new NullPointerException("Key cannot be null");
    }
    T cachedNode = hotKeyCache.get(key);
    if (cachedNode != null) {
      cachedNode.recordAccess(hashFunction.hash(key), accessWeight);
      return cachedNode;
    }

    try {
      lock.readLock().lock();

      if (circle.isEmpty()) {
        return null;
      }

      int hash = hashFunction.hash(key);

      hotKeys.compute(hash, (k, v) -> v == null ? 1L : v + 1L);

      Map.Entry<Integer, T> entry = circle.ceilingEntry(hash);
      if (entry == null) {
        entry = circle.firstEntry();
      }

      T node = entry.getValue();
      node.recordAccess(hash, accessWeight);

      return node;
    } finally {
      lock.readLock().unlock();
    }
  }

  private int calculateVirtualNodeCount(double weight) {
    int virtualNodes = (int) Math.max(1, Math.ceil(baseVirtualNodes * weight));
    return Math.min(virtualNodes, maxVirtualNodesPerNode);
  }

  private void processHotKeys() {
    if (hotKeys.isEmpty() || circle.isEmpty()) {
      return;
    }

    try {
      lock.readLock().lock();

      List<Map.Entry<Integer, Long>> topHotKeys = hotKeys.entrySet().stream()
          .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
          .limit(maxHotKeys)
          .filter(e -> e.getValue() >= hotKeyThreshold)
          .toList();

      hotKeys.clear();

      for (Map.Entry<Integer, Long> entry : topHotKeys) {
        int keyHash = entry.getKey();

        Map.Entry<Integer, T> nodeEntry = circle.ceilingEntry(keyHash);
        if (nodeEntry == null) {
          nodeEntry = circle.firstEntry();
        }

        hotKeyCache.put("hash-" + keyHash, nodeEntry.getValue());
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  private void rebalanceNodes() {
    if (rebalancing.getAndSet(true) || nodeToVirtualNodes.isEmpty()) {
      return;
    }

    try {
      lock.writeLock().lock();

      List<T> nodes = new ArrayList<>(nodeToVirtualNodes.keySet());

      double totalLoad = 0;
      double maxLoad = 0;
      double minLoad = Double.MAX_VALUE;

      for (T node : nodes) {
        double load = node.getLoad();
        totalLoad += load;
        maxLoad = Math.max(maxLoad, load);
        minLoad = Math.min(minLoad, load);
      }

      double avgLoad = totalLoad / nodes.size();

      double loadRatio = avgLoad > 0 ? maxLoad / avgLoad : 1.0;

      if (loadRatio <= 1.0 + loadImbalanceThreshold) {
        return;
      }

      for (T node : nodes) {
        double load = node.getLoad();
        double loadFactor = avgLoad > 0 ? load / avgLoad : 1.0;

        double weight = 1.0 / loadFactor;
        weight = Math.max(0.1, Math.min(weight, 3.0));

        int currentVirtualNodes = nodeToVirtualNodeCount.get(node);
        int newVirtualNodes = calculateVirtualNodeCount(node.getWeight() * weight);
        if (Math.abs(currentVirtualNodes - newVirtualNodes) > baseVirtualNodes * 0.1) {
          Set<Integer> hashes = nodeToVirtualNodes.get(node);
          for (Integer hash : hashes) {
            circle.remove(hash);
          }
          Set<Integer> newHashes = new HashSet<>(newVirtualNodes);
          for (int i = 0; i < newVirtualNodes; i++) {
            int hash = hashFunction.hash(node.getId() + "-" + i);
            circle.put(hash, node);
            newHashes.add(hash);
          }
          nodeToVirtualNodes.put(node, newHashes);
          nodeToVirtualNodeCount.put(node, newVirtualNodes);
        }
      }
      hotKeyCache.clear();
    } finally {
      lock.writeLock().unlock();
      rebalancing.set(false);
    }
  }


  private void decayNodeLoads() {
    try {
      lock.readLock().lock();

      for (T node : nodeToVirtualNodes.keySet()) {
        if (node instanceof WeightedNode) {
          ((WeightedNode) node).decayLoad(0.5);
        }
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  public Collection<T> getNodes() {
    return new ArrayList<>(nodeToVirtualNodes.keySet());
  }

  public void shutdown() {
    scheduler.shutdown();
  }

}
