package com.ares.distribute.zookeeper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Setter
@Getter
@ConfigurationProperties(prefix = "akk.curator.lock")
public class CuratorLockProperties {
  private String path = "/akk/lock";
  private String name;
  private boolean enabled = false;
}
