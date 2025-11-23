package com.ares.distribute.zookeeper.service;

import com.ares.distribute.zookeeper.CuratorProperties;
import java.util.Collection;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

public class ZkRegistry implements Registry {

  private final ServiceDiscovery<ServiceMeta> discovery;

  public ZkRegistry(CuratorProperties properties) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory.newClient(properties.getAddress(),
        new ExponentialBackoffRetry(properties.getSleepTime(), properties.getMaxRetries()));
    client.start();
    JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(
        ServiceMeta.class);
    this.discovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
        .client(client)
        .serializer(serializer)
        .basePath("/" + properties.getBasePath())
        .build();
    this.discovery.start();
  }


  @Override
  public void register(ServiceMeta serviceMeta) throws Exception {
    ServiceInstance<ServiceMeta> instance = ServiceInstance.<ServiceMeta>builder()
        .name(buildNamespace(serviceMeta.getGroup(), serviceMeta.getApplication(),
            serviceMeta.getVersion()))
        .address(serviceMeta.getHost())
        .port(serviceMeta.getPort())
        .payload(serviceMeta)
        .build();
    discovery.registerService(instance);
  }

  @Override
  public void unregister(ServiceMeta serviceMeta) throws Exception {
    ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
        .<ServiceMeta>builder()
        .name(buildNamespace(serviceMeta.getGroup(), serviceMeta.getApplication(),
            serviceMeta.getVersion()))
        .address(serviceMeta.getHost())
        .port(serviceMeta.getPort())
        .payload(serviceMeta)
        .build();
    discovery.unregisterService(serviceInstance);
  }

  @Override
  public ServiceMeta lookup(String group, String serviceName,
      String version, int hashCode) throws Exception {

    Collection<ServiceInstance<ServiceMeta>> instances = discovery
        .queryForInstances(buildNamespace(group, serviceName, version));

    ServiceInstance<ServiceMeta> instance = new ConsistentHashLoadBalancer()
        .select((List<ServiceInstance<ServiceMeta>>) instances, hashCode);

    if (instance != null) {
      return instance.getPayload();
    }
    return null;
  }

  private String buildNamespace(String group, String application, String version) {
    return group + "#" + application + "#" + version;
  }
}
