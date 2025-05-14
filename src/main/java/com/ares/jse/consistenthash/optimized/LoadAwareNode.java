package com.ares.jse.consistenthash.optimized;

public interface LoadAwareNode {

  /**
   * Get the unique identifier for this node
   */
  String getId();

  /**
   * Get the current load of this node (higher values indicate higher load)
   */
  double getLoad();

  /**
   * Get the maximum capacity of this node (used for weight calculation)
   */
  double getCapacity();

  void recordAccess(int keyHash, double weight);
  /**
   * Calculate the node's weight based on its load and capacity
   */
  default double getWeight() {
    return Math.max(0.1, 1 - (getLoad() / getCapacity()));
  }
}
