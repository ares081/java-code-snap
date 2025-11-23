package com.ares.distribute.zookeeper.config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

public class LeaderService extends LeaderSelectorListenerAdapter implements AutoCloseable {

  private final LeaderSelector selector;
  private final AtomicBoolean isLeader = new AtomicBoolean(false);

  public LeaderService(CuratorFramework client, String leaderPath, String id) {
    this.selector = new LeaderSelector(client, leaderPath, this);
    this.selector.setId(id);
    // 参加选举后，当释放领导权，会自动重新加入
    this.selector.autoRequeue();
  }

  @Override
  public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
    // 线程获得领导权时会调用此方法，直到方法返回领导权才会交出
    isLeader.set(true);
    try {
      // Block until we are interrupted (release leadership when interrupted)
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
      // lost leadership or closing
    } finally {
      isLeader.set(false);
    }
  }

  /**
   * Run a write operation only if current instance is leader. Caller should handle exception.
   */
  public <T> T runIfLeader(Supplier<T> writeAction) {
    if (!isLeader()) {
      throw new IllegalStateException("Not leader. Writes must be performed by leader.");
    }
    return writeAction.get();
  }

  public void start() {
    selector.start();
  }

  public boolean isLeader() {
    return isLeader.get();
  }

  @Override
  public void close() throws Exception {
    selector.close();
  }
}
