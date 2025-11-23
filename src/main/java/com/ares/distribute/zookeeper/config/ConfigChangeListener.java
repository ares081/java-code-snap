package com.ares.distribute.zookeeper.config;

public interface ConfigChangeListener {

  void onChange(String newValue);
}
