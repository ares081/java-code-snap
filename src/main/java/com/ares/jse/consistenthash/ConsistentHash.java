package com.ares.jse.consistenthash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConsistentHash<T> {

  private final HashFunction hashFunction;
  private final int virtualNodes;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final ConcurrentSkipListMap<Integer, T> circle = new ConcurrentSkipListMap<>();
  private final Map<T, List<Integer>> nodeToVirtualNodes = new ConcurrentHashMap<>();

  private final Map<String, T> keyCache = new ConcurrentHashMap<>();
  private static final int MAX_CACHE_SIZE = 1000;
  private final boolean cacheEnabled;


  public ConsistentHash(HashFunction hashFunction, int virtualNodes) {
    this(hashFunction, virtualNodes, true);
  }

  public ConsistentHash(HashFunction hashFunction, int virtualNodes, boolean cacheEnabled) {
    this.hashFunction = hashFunction;
    this.virtualNodes = virtualNodes;
    this.cacheEnabled = cacheEnabled;
  }

  public ConsistentHash(HashFunction hashFunction, int numberOfVirtualNodes, Collection<T> nodes) {
    this(hashFunction, numberOfVirtualNodes, true);

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

      List<Integer> virtualNodeHashes = new ArrayList<>(virtualNodes);

      for (int i = 0; i < virtualNodes; i++) {
        int hash = hashFunction.hash(node.toString() + "-" + i);
        circle.put(hash, node);
        virtualNodeHashes.add(hash);
      }
      nodeToVirtualNodes.put(node, virtualNodeHashes);
      clearCache();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void addNodes(Collection<T> nodes) {
    if (nodes == null || nodes.isEmpty()) {
      return;
    }
    try {
      lock.writeLock().lock();
      for (T node : nodes) {
        if (node == null || nodeToVirtualNodes.containsKey(node)) {
          continue;
        }
        List<Integer> virtualNodeHashes = new ArrayList<>(virtualNodes);

        for (int i = 0; i < virtualNodes; i++) {
          int hash = hashFunction.hash(node + "-" + i);
          circle.put(hash, node);
          virtualNodeHashes.add(hash);
        }

        nodeToVirtualNodes.put(node, virtualNodeHashes);
      }
      clearCache();
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Remove a node and its virtual nodes from the hash ring
   */
  public void removeNode(T node) {
    if (node == null) {
      return;
    }

    try {
      lock.writeLock().lock();
      List<Integer> virtualNodeHashes = nodeToVirtualNodes.remove(node);
      if (virtualNodeHashes != null) {
        for (Integer hash : virtualNodeHashes) {
          circle.remove(hash);
        }
      }
      clearCache();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public T getNode(String key) {
    if (key == null) {
      throw new NullPointerException("Key cannot be null");
    }

    if (cacheEnabled) {
      T cachedNode = keyCache.get(key);
      if (cachedNode != null) {
        return cachedNode;
      }
    }

    try {
      lock.readLock().lock();
      if (circle.isEmpty()) {
        return null;
      }

      int hash = hashFunction.hash(key);

      Map.Entry<Integer, T> entry = circle.ceilingEntry(hash);
      if (entry == null) {
        entry = circle.firstEntry();
      }

      T node = entry.getValue();

      if (cacheEnabled && keyCache.size() < MAX_CACHE_SIZE) {
        keyCache.put(key, node);
      }

      return node;
    } finally {
      lock.readLock().unlock();
    }
  }

  private void clearCache() {
    if (cacheEnabled) {
      keyCache.clear();
    }
  }

  public int getNodeCount() {
    return nodeToVirtualNodes.size();
  }

  public int getVirtualNodeCount() {
    return circle.size();
  }

  public boolean containsNode(T node) {
    return nodeToVirtualNodes.containsKey(node);
  }

  public Collection<T> getNodes() {
    return new ArrayList<>(nodeToVirtualNodes.keySet());
  }
}
