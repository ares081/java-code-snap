package com.ares.distribute.zookeeper.lock.boot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Setter
@Getter
@ConfigurationProperties(prefix = "akk.curator")
public class CuratorProperties {
  private String address = "127.0.0.1:2181";
  /**
   * 可选：namespace，会加在所有路径前
   */
  private String namespace;
  private int baseSleepTimeMs = 1000;
  private int maxRetries = 3;
  private int sessionTimeoutMs = 60_000;
  private int connectionTimeoutMs = 15_000;

  /**
   * 是否启用自动配置
   */
  private boolean enabled = true;
}
