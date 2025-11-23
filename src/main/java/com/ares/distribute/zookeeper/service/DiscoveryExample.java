package com.ares.distribute.zookeeper.service;

import com.ares.distribute.zookeeper.CuratorProperties;

public class DiscoveryExample {

  public static void main(String[] args) throws Exception {
    CuratorProperties properties = new CuratorProperties();
    properties.setBasePath("test");
    properties.setMaxRetries(5);
    properties.setSleepTime(1000);
    properties.setAddress("127.0.0.1:2181");

    ZkRegistry registry = new ZkRegistry(properties);

    for (int i = 1; i < 11; i++) {
      ServiceMeta serviceMeta = new ServiceMeta();
      serviceMeta.setApplication("order-service");
      serviceMeta.setGroup("A");
      serviceMeta.setVersion("1.0");
      serviceMeta.setHost("192.168.0." + i);
      serviceMeta.setPort(8080);
      registry.register(serviceMeta);
    }

    ServiceMeta meta = registry.lookup("A", "order-service", "1.0", 2126277029);
    System.out.println(meta.getApplication() + "#" + meta.getHost() + ":" + meta.getPort());
    ServiceMeta meta1 = registry.lookup("A", "order-service", "1.0", 113466687);
    System.out.println(meta1.getApplication() + "#" + meta1.getHost() + ":" + meta1.getPort());
  }

}
