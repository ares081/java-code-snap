package com.ares.distribute.zookeeper;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CuratorProperties {

  private String address;
  private String basePath;
  private Integer sessionTimeout;
  private Integer connectionTimeout;
  private Integer sleepTime;
  private Integer maxRetries;
  private String authDigest;
}
