package com.ares.distribute.zookeeper;

import java.util.ArrayList;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

/**
 * 创建并配置 CuratorFramework 客户端（含可选 Digest auth 与 ACLProvider）。
 */
public class CuratorClientFactory {

  public static CuratorFramework create(CuratorProperties properties) {
    CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
        .connectString(properties.getAddress())
        .sessionTimeoutMs(properties.getSessionTimeout())
        .connectionTimeoutMs(properties.getConnectionTimeout())
        .retryPolicy(
            new ExponentialBackoffRetry(properties.getSleepTime(), properties.getMaxRetries()));

    if (properties.getAuthDigest() == null || properties.getAuthDigest().isBlank()) {
      CuratorFramework client = builder.build();
      client.start();
      return client;
    }

    // authDigest 格式: "user:password"
    builder = builder.authorization("digest", properties.getAuthDigest().getBytes());
    // 提供 ACLProvider，让新创建的节点有 digest ACL（管理服务可以写）
    builder = builder.aclProvider(new ACLProvider() {
      @Override
      public List<ACL> getDefaultAcl() {
        List<ACL> acls = new ArrayList<>();
        // grant all to the digest auth we provided
        acls.add(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.CREATOR_ALL_ACL.get(0).getId()));
        // grant read to anyone (可改为更严格)
        acls.addAll(ZooDefs.Ids.READ_ACL_UNSAFE);
        return acls;
      }

      @Override
      public List<ACL> getAclForPath(String path) {
        return getDefaultAcl();
      }
    });

    CuratorFramework client = builder.build();
    client.start();
    return client;
  }
}
