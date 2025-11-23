package com.ares.distribute.zookeeper.service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.curator.x.discovery.ServiceInstance;

public class ConsistentHashLoadBalancer implements LoadBalancer<ServiceInstance<ServiceMeta>> {

  private final static String VIRTUAL_NODE_SPLIT = "#";
  private final static int VIRTUAL_NODE_SIZE = 10;

  @Override
  public ServiceInstance<ServiceMeta> select(List<ServiceInstance<ServiceMeta>> instances,
      int hashCode) {
    if (instances == null || instances.isEmpty()) {
      return null;
    }
    TreeMap<Integer, ServiceInstance<ServiceMeta>> circle = buildConsistentHashRing(instances);
    return allocateNode(circle, hashCode);
  }

  private ServiceInstance<ServiceMeta> allocateNode(
      TreeMap<Integer, ServiceInstance<ServiceMeta>> circle, int hashCode) {
    Map.Entry<Integer, ServiceInstance<ServiceMeta>> entry = circle.ceilingEntry(hashCode);
    if (entry == null) {
      entry = circle.firstEntry();
    }
    return entry.getValue();
  }

  private TreeMap<Integer, ServiceInstance<ServiceMeta>> buildConsistentHashRing(
      List<ServiceInstance<ServiceMeta>> servers) {
    TreeMap<Integer, ServiceInstance<ServiceMeta>> ring = new TreeMap<>();
    for (ServiceInstance<ServiceMeta> instance : servers) {
      for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
        ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
      }
    }
    return ring;
  }

  private String buildServiceInstanceKey(ServiceInstance<ServiceMeta> instance) {
    ServiceMeta payload = instance.getPayload();
    return String.join(":", payload.getHost(), String.valueOf(payload.getPort()));
  }
}
