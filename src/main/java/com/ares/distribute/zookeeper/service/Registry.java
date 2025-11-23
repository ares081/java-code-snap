package com.ares.distribute.zookeeper.service;

public interface Registry {

  void register(ServiceMeta serviceMeta) throws Exception;


  void unregister(ServiceMeta serviceMeta) throws Exception;

  ServiceMeta lookup(String group, String serviceName, String version, int hash) throws Exception;
}
