package com.ares.jse.consistenthash.optimized;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import lombok.Getter;
import lombok.Setter;

// 解决热点节点问题
public class WeightedNode implements LoadAwareNode {

  private final String id;
  @Getter
  private final String name;
  @Getter
  private final String ip;

  private final AtomicLong requestCount = new AtomicLong(0);
  private final DoubleAdder loadMetric = new DoubleAdder();

  @Setter
  private volatile double capacity;

  private final int hashCode;

  public WeightedNode(String id, String name, String ip, double capacity) {
    this.id = id;
    this.name = name;
    this.ip = ip;

    this.capacity = capacity;
    this.hashCode = Objects.hash(id);
  }

  public void decayLoad(double factor) {
    if (factor < 0 || factor > 1) {
      throw new IllegalArgumentException("Decay factor must be between 0 and 1");
    }

    double currentLoad = loadMetric.sum();
    loadMetric.reset();
    loadMetric.add(currentLoad * factor);
  }

  public long getRequestCount() {
    return requestCount.get();
  }


  @Override
  public void recordAccess(int keyHash, double weight) {
    requestCount.incrementAndGet();
    loadMetric.add(weight);
  }


  @Override
  public String getId() {
    return id;
  }

  @Override
  public double getLoad() {
    return loadMetric.sum();
  }

  @Override
  public double getCapacity() {
    return capacity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WeightedNode that = (WeightedNode) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public String toString() {
    return "Node{id='" + id + "', load=" + getLoad() + ", capacity=" + capacity + "}";
  }

}
