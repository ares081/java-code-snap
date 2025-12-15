package com.ares.distribute.zookeeper.config;

import com.ares.distribute.zookeeper.CuratorClientFactory;
import com.ares.distribute.zookeeper.CuratorProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.apache.curator.framework.CuratorFramework;

public class ConfigServiceExample {

  public static void main(String[] args) throws Exception {

    CuratorProperties properties = new CuratorProperties();
    properties.setAddress("127.0.0.1:2181");
    properties.setSessionTimeout(3000);
    properties.setConnectionTimeout(1000);
    properties.setMaxRetries(3);
    properties.setSleepTime(2000);
    properties.setAuthDigest("admin:admin");
    String instanceId = "instance-" + System.currentTimeMillis();

    CuratorFramework client = CuratorClientFactory.create(properties);

    LeaderService leader = new LeaderService(client, "/config/leader", instanceId);
    leader.start();
    String app = "application";
    ConfigService config = new ConfigService(client, "application");

    Scanner sc = new Scanner(System.in);
    System.out.println("Commands: put release | create-gray | confirm-gray | rollback | get | exit");
    while (true) {
      System.out.print("> ");
      String line = sc.nextLine();
      if (line == null) break;
      String[] parts = line.split("\\s+");
      if (parts.length == 0) continue;
      String cmd = parts[0];
      try {
        if (cmd.equalsIgnoreCase("put")) {
          // put release key value
          if (leader.checkLeader()) {
            System.out.println("not leader, cannot put");
            continue;
          }
          String env = parts[1];
          String key = parts[2];
          String value = parts[3];
          Map<String, String> kv = new HashMap<>();
          kv.put(key, value);
          String releaseId = leader.runIfLeader(() -> {
            try {
              return config.putRelease(app, env, kv, instanceId);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
          System.out.println("releaseId=" + releaseId);
        } else if (cmd.equalsIgnoreCase("create-gray")) {
          String env = parts[1];
          String grayId = parts[2];
          String key = parts[3];
          String value = parts[4];
          Map<String, String> kv = new HashMap<>();
          kv.put(key, value);
          config.createGrayRelease(app, env, grayId, kv, instanceId);
          System.out.println("gray created");
        } else if (cmd.equalsIgnoreCase("confirm-gray")) {
          if (leader.checkLeader()) {
            System.out.println("not leader, cannot confirm");
            continue;
          }
          String env = parts[1];
          String grayId = parts[2];
          String releaseId = leader.runIfLeader(() -> {
            try {
              return config.confirmGrayRelease(app, env, grayId, instanceId);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
          System.out.println("confirmed releaseId=" + releaseId);
        } else if (cmd.equalsIgnoreCase("rollback")) {
          if (leader.checkLeader()) {
            System.out.println("not leader, cannot rollback");
            continue;
          }
          String env = parts[1];
          String releaseId = parts[2];
          leader.runIfLeader(() -> {
            try {
              config.rollbackToRelease(app, env, releaseId, instanceId);
              return null;
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
          System.out.println("rollback done");
        } else if (cmd.equalsIgnoreCase("get")) {
          String env = parts[1];
          String key = parts[2];
          System.out.println("value: " + config.get(app, env, key).orElse("[empty]"));
        } else if (cmd.equalsIgnoreCase("exit")) {
          break;
        } else {
          System.out.println("unknown cmd");
        }
      } catch (Exception ex) {
        System.err.println("error: " + ex.getMessage());
        ex.printStackTrace();
      }
    }

    config.close();
    leader.close();
    client.close();
  }

}
