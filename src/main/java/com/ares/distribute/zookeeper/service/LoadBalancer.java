package com.ares.distribute.zookeeper.service;

import java.util.List;
import org.apache.curator.x.discovery.UriSpec;

public interface LoadBalancer<T> {

  T select(List<T> instances, int hashCode);
}
