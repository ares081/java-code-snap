package com.ares.distribute.zookeeper.lock.boot;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnClass(CuratorFramework.class)
@ConditionalOnProperty(prefix = "akk.curator", name = "enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CuratorProperties.class)
public class CuratorAutoConfiguration {

  @Bean(initMethod = "start", destroyMethod = "close")
  @ConditionalOnMissingBean
  public CuratorFramework curatorFramework(CuratorProperties props) {
    ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(props.getBaseSleepTimeMs(),
        props.getMaxRetries());

    CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory
        .builder()
        .connectString(props.getAddress())
        .retryPolicy(retryPolicy)
        .connectionTimeoutMs(props.getConnectionTimeoutMs())
        .sessionTimeoutMs(props.getSessionTimeoutMs());

    if (props.getNamespace() != null && !props.getNamespace().trim().isEmpty()) {
      builder.namespace(props.getNamespace());
    }
    return builder.build();
  }

}
