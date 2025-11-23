package com.ares.distribute.zookeeper;

import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;

public class CuratorCacheExample {

  static CuratorFramework client = CuratorFrameworkFactory
      .newClient("localhost:2181",
          new ExponentialBackoffRetry(1000, 3));

  public static void main(String[] args) throws Exception {
    client.start();
    client.create()
        .withMode(CreateMode.PERSISTENT)
        .withACL(Ids.OPEN_ACL_UNSAFE)
        .forPath("/cache");

    WatcherExample.example();

    CuratorCacheListenerExample.example();
    //client.delete().idempotent().forPath("/cache");
    //client.close();
  }

  static class CuratorCacheListenerExample {

    public static void example() throws Exception {

      client.getCuratorListenable().addListener(
          (curatorFramework, curatorEvent) -> System.out.println("事件： " + curatorEvent));


      for (int i = 0; i < 10; i++) {
        String p = "cache-" + i;
        client.create()
            .withMode(CreateMode.PERSISTENT)
            .withACL(Ids.OPEN_ACL_UNSAFE)
            .forPath("/cache/" + p, p.getBytes());
      }

    }
  }

  static class WatcherExample {

    public static void example() throws Exception {
      List<String> children = client.getChildren().usingWatcher(
          (Watcher) watchedEvent -> System.out.println("监控： " + watchedEvent)).forPath("/cache");

      for (String child : children) {
        System.out.println("get child: " + child);
      }
    }

  }
}
