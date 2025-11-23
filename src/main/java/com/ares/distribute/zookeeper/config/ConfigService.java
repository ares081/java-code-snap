package com.ares.distribute.zookeeper.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfigService 提供读取、灰度发布、确认合并、回滚操作。
 * <p>
 * 数据路径模型: /config/{app}/{env}/{key} -> 正式发布数据 (release) /config/{app}/{env}/gray/{grayId}/{key} ->
 * 灰度数据（待确认） /config/{app}/{env}/releases/{releaseId}/{key} -> release 历史快照
 */
public class ConfigService implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
  private final CuratorFramework client;
  private final CuratorCache cache;
  private final ObjectMapper mapper = new ObjectMapper();
  private final String configRootPath = "/config";

  public ConfigService(CuratorFramework client, String app) {
    this.client = client;
    String watchPath = configRootPath + "/" + app;
    this.cache = CuratorCache.build(client, watchPath);
    // 监听所有变化（可更细化）
    CuratorCacheListener listener = CuratorCacheListener.builder()
        .forAll((type, oldData, newData) -> {
          String path = newData != null ? newData.getPath()
              : (oldData != null ? oldData.getPath() : "unknown");
          logger.info("[CuratorCache] event={} path={}}", type, path);
        })
        .build();
    this.cache.listenable().addListener(listener);
    this.cache.start();
  }

  private String keyPath(String app, String env, String key) {
    return String.format("%s/%s/%s/%s", configRootPath, app, env, key);
  }

  private String grayKeyPath(String app, String env, String grayId, String key) {
    return String.format("%s/%s/%s/gray/%s/%s", configRootPath, app, env, grayId, key);
  }

  private String releaseSnapshotPath(String app, String env, String releaseId, String key) {
    return String.format("%s/%s/%s/releases/%s/%s", configRootPath, app, env, releaseId, key);
  }

  private String releasesMetaPath(String app, String env) {
    return String.format("%s/%s/%s/releases_meta", configRootPath, app, env);
  }

  // 获取正式值（读取 cache first, fallback to zk）
  public Optional<String> get(String app, String env, String key) throws Exception {
    String path = keyPath(app, env, key);
    byte[] data = client.getData().forPath(path);
    return Optional.ofNullable(data).map(b -> new String(b, StandardCharsets.UTF_8));
  }

  // 创建或更新正式值（应由 leader 执行） - 这是一个原子更新示例：先保存快照，再写新值，并在 releases_meta 记录 releaseId
  public String putRelease(String app, String env, Map<String, String> kvs, String author)
      throws Exception {
    String releaseId = String.valueOf(Instant.now().toEpochMilli());
    List<CuratorOp> ops = new ArrayList<>();
    // 为每个 key 做两个操作：
    // 1) 备份当前值到 releases/{releaseId}/{key}（如果存在）
    // 2) setData 到正式路径（create if not exist）
    for (Map.Entry<String, String> e : kvs.entrySet()) {
      String key = e.getKey();
      String newValue = e.getValue();
      String targetPath = keyPath(app, env, key);
      String snapshotPath = releaseSnapshotPath(app, env, releaseId, key);
      byte[] newBytes = newValue.getBytes(StandardCharsets.UTF_8);

      // 创建 snapshot node: 获取原值（若不存在则写空）
      byte[] old = null;
      try {
        old = client.getData().forPath(targetPath);
      } catch (Exception ex) {
        // 节点可能不存在
      }
      byte[] snapshotBytes = old == null ? new byte[0] : old;

      CuratorOp opCreateSnapshot = client.transactionOp().create()
          .forPath(snapshotPath, snapshotBytes);
      ops.add(opCreateSnapshot);

      ensurePathExists(String.format("%s/%s/%s", configRootPath, app, env));
      try {
        client.checkExists().forPath(targetPath);
        CuratorOp setOp = client.transactionOp().setData().forPath(targetPath, newBytes);
        ops.add(setOp);
      } catch (Exception ex) {
        // node missing -> create op
        CuratorOp createOp = client.transactionOp().create().withMode(CreateMode.PERSISTENT)
            .forPath(targetPath, newBytes);
        ops.add(createOp);
      }
    }

    // Add release meta record
    String metaPath = releasesMetaPath(app, env) + "/" + releaseId;
    Map<String, Object> meta = new HashMap<>();
    meta.put("id", releaseId);
    meta.put("author", author);
    meta.put("time", Instant.now().toString());
    byte[] metaBytes = mapper.writeValueAsBytes(meta);
    ensurePathExists(releasesMetaPath(app, env));
    CuratorOp metaOp = client.transactionOp().create().forPath(metaPath, metaBytes);
    ops.add(metaOp);

    // 执行事务 (atomic): create snapshots + setData/create targets + create meta
    client.transaction().forOperations(ops.toArray(new CuratorOp[0]));
    return releaseId;
  }

  // 创建灰度发布（灰度数据写到 gray/{grayId} 下），灰度发布不改变正式数据
  public void createGrayRelease(String app, String env, String grayId, Map<String, String> kvs,
      String author) throws Exception {
    // ensure parent
    ensurePathExists(String.format("%s/%s/%s/gray/%s", configRootPath, app, env, grayId));
    for (Map.Entry<String, String> e : kvs.entrySet()) {
      String path = grayKeyPath(app, env, grayId, e.getKey());
      byte[] b = e.getValue().getBytes(StandardCharsets.UTF_8);
      // create or set
      if (client.checkExists().forPath(path) == null) {
        client.create().creatingParentsIfNeeded().forPath(path, b);
      } else {
        client.setData().forPath(path, b);
      }
    }
    // optional: record gray meta
    String meta = String.format("{\"id\":\"%s\",\"author\":\"%s\",\"time\":\"%s\"}", grayId, author,
        Instant.now().toString());
    String metaPath = String.format("%s/%s/%s/gray/%s/_meta", configRootPath, app, env, grayId);
    if (client.checkExists().forPath(metaPath) == null) {
      client.create().forPath(metaPath, meta.getBytes(StandardCharsets.UTF_8));
    } else {
      client.setData().forPath(metaPath, meta.getBytes(StandardCharsets.UTF_8));
    }
  }

  // 确认灰度：将 gray/{grayId} 内容原子合并到正式路径，并写 release 历史（snapshot）
  public String confirmGrayRelease(String app, String env, String grayId, String author)
      throws Exception {
    // 收集 gray 节点下的 kv
    String grayBase = String.format("%s/%s/%s/gray/%s", configRootPath, app, env, grayId);
    List<String> children = client.getChildren().forPath(grayBase);
    if (children == null || children.isEmpty()) {
      throw new IllegalArgumentException("gray release empty: " + grayId);
    }
    String releaseId = String.valueOf(Instant.now().toEpochMilli());
    List<CuratorOp> ops = new ArrayList<>();

    // ensure parent release path exists
    ensurePathExists(String.format("%s/%s/%s/releases/%s", configRootPath, app, env, releaseId));

    // 对 gray 下每个 key（忽略 _meta），先备份原值到 releases/{releaseId}/{key}，再将 gray 值写入正式 path
    for (String child : children) {
      if (child.equals("_meta")) {
        continue;
      }
      String grayNode = grayBase + "/" + child;
      byte[] grayBytes = client.getData().forPath(grayNode);

      String targetPath = keyPath(app, env, child);
      String snapshotPath = releaseSnapshotPath(app, env, releaseId, child);

      byte[] old = null;
      try {
        old = client.getData().forPath(targetPath);
      } catch (Exception ex) {
        logger.error("get old error: ", ex);
      }
      byte[] snapshotBytes = old == null ? new byte[0] : old;

      CuratorOp snapOp = client.transactionOp().create().forPath(snapshotPath, snapshotBytes);
      ops.add(snapOp);

      // 保证父节点存在
      ensurePathExists(String.format("%s/%s/%s", configRootPath, app, env));
      if (client.checkExists().forPath(targetPath) != null) {
        CuratorOp setOp = client.transactionOp().setData().forPath(targetPath, grayBytes);
        ops.add(setOp);
      } else {
        CuratorOp createOp = client.transactionOp().create().forPath(targetPath, grayBytes);
        ops.add(createOp);
      }
    }

    // 添加 release meta
    String metaPath = releasesMetaPath(app, env) + "/" + releaseId;
    Map<String, Object> meta = new HashMap<>();
    meta.put("id", releaseId);
    meta.put("author", author);
    meta.put("time", Instant.now().toString());
    meta.put("fromGray", grayId);
    byte[] metaBytes = mapper.writeValueAsBytes(meta);
    ensurePathExists(releasesMetaPath(app, env));
    CuratorOp metaOp = client.transactionOp().create().forPath(metaPath, metaBytes);
    ops.add(metaOp);

    // execute transaction
    client.transaction().forOperations(ops.toArray(new CuratorOp[0]));

    // 删除 gray 节点（非事务部分，若删除失败可再次尝试）
    deleteRecursively(grayBase);

    return releaseId;
  }

  // 回滚到某个 releaseId（将 releases/{releaseId} 快照写回正式路径）
  public void rollbackToRelease(String app, String env, String releaseId, String author)
      throws Exception {
    String releaseBase = String.format("%s/%s/%s/releases/%s", configRootPath, app, env, releaseId);
    List<String> keys = client.getChildren().forPath(releaseBase);
    if (keys == null || keys.isEmpty()) {
      throw new IllegalArgumentException("release snapshot empty");
    }
    List<CuratorOp> ops = new ArrayList<>();
    for (String key : keys) {
      String snapshotPath = releaseBase + "/" + key;
      byte[] snap = client.getData().forPath(snapshotPath);
      String targetPath = keyPath(app, env, key);
      ensurePathExists(String.format("%s/%s/%s", configRootPath, app, env));
      if (client.checkExists().forPath(targetPath) != null) {
        CuratorOp setOp = client.transactionOp().setData().forPath(targetPath, snap);
        ops.add(setOp);
      } else {
        CuratorOp createOp = client.transactionOp().create().forPath(targetPath, snap);
        ops.add(createOp);
      }
    }
    // add rollback meta entry
    String rbId = "rollback-" + Instant.now().toEpochMilli();

    Map<String, Object> meta = new HashMap<>();
    meta.put("id", rbId);
    meta.put("author", author);
    meta.put("time", Instant.now().toString());
    meta.put("rollbackFrom", releaseId);
    byte[] metaBytes = mapper.writeValueAsBytes(meta);
    ensurePathExists(releasesMetaPath(app, env));
    String metaPath = releasesMetaPath(app, env) + "/" + rbId;
    CuratorOp metaOp = client.transactionOp().create().forPath(metaPath, metaBytes);
    ops.add(metaOp);

    client.transaction().forOperations(ops.toArray(new CuratorOp[0]));
  }

  private void deleteRecursively(String path) {
    try {
      if (client.checkExists().forPath(path) != null) {
        List<String> children = client.getChildren().forPath(path);
        if (children != null) {
          for (String c : children) {
            deleteRecursively(path + "/" + c);
          }
        }
        client.delete().forPath(path);
      }
    } catch (Exception e) {
      // 忽略或记录
      logger.error("deleteRecursively error:{}", e.getMessage());
    }
  }

  private void ensurePathExists(String path) throws Exception {
    if (client.checkExists().forPath(path) == null) {
      client.create().creatingParentsIfNeeded().forPath(path, new byte[0]);
    }
  }

  @Override
  public void close() throws Exception {
    try {
      cache.close();
    } catch (Exception e) {
      // ignore
    }
  }
}
