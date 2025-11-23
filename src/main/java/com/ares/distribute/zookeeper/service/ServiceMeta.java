package com.ares.distribute.zookeeper.service;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceMeta {

  private String application;
  private String version = "1.0";
  private String host;
  private Integer port;
  private String group = "default";
}
