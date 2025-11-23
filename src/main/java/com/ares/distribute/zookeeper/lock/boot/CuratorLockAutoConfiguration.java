package com.ares.distribute.zookeeper.lock.boot;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CuratorLockProperties.class)
@ConditionalOnClass({CuratorFramework.class})
@ConditionalOnProperty(prefix = "akk.curator.lock", value = "enable", havingValue = "true")
public class CuratorLockAutoConfiguration {

  @Bean
  public InterProcessMutex lock(CuratorFramework curatorFramework, CuratorLockProperties props) {
    String path = getLockPath(props);
    return new InterProcessMutex(curatorFramework, path);
  }

  private String getLockPath(CuratorLockProperties props) {
    if (!props.getPath().startsWith("/")) {
      props.setPath("/" + props.getPath());
    }

    if (props.getName() == null || props.getName().trim().isEmpty()) {
      return props.getPath();
    }

    if (props.getName().endsWith("/")) {
      return props.getPath() + props.getName();
    } else {
      return props.getPath() + "/" + props.getName();
    }
  }
}
