package com.ares.distribute.zookeeper.service;

import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceProvider;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

public class ConsistentHashStrategy<T> implements ProviderStrategy<T> {
  @Override
  public ServiceInstance<T> getInstance(InstanceProvider<T> instance) throws Exception {
    return null;
  }
}
