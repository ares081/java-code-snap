package com.ares.distribute.zookeeper;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用原生 zookeeper api
 */
public class Sync implements Watcher {

  private static final Logger logger = LoggerFactory.getLogger(Sync.class);
  protected final ZooKeeper zookeeper;
  protected final String root;
  protected static final Object mutex = new Object();


  Sync(String addr, String root) throws IOException {
    this.zookeeper = new ZooKeeper(addr, 3000, this);
    this.root = root;
  }

  @Override
  synchronized public void process(WatchedEvent watchedEvent) {
    logger.info(watchedEvent.toString());
    synchronized (mutex) {
      mutex.notify();
    }
  }

  static public class Barrier extends Sync {

    private final int size;
    private final String name;

    Barrier(String address, String root, int size) throws IOException {
      super(address, root);
      this.size = size;
      if (zookeeper == null) {
        throw new RuntimeException("zookeeper is null");
      }
      try {
        Stat stat = zookeeper.exists(root, false);
        if (stat == null) {
          zookeeper.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
      } catch (Exception e) {
        logger.error("create zookeeper error", e);
      }
      this.name = InetAddress.getLocalHost().getCanonicalHostName();
    }

    boolean enter() throws InterruptedException, KeeperException {
      zookeeper.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE,
          CreateMode.EPHEMERAL_SEQUENTIAL);

      while (true) {
        synchronized (mutex) {
          List<String> list = zookeeper.getChildren(root, true);
          if (list.size() < size) {
            mutex.wait();
          } else {
            return true;
          }
        }
      }
    }

    boolean leave() throws KeeperException, InterruptedException {
      zookeeper.delete(root + "/" + name, 0);
      while (true) {
        synchronized (mutex) {
          List<String> list = zookeeper.getChildren(root, true);
          if (!list.isEmpty()) {
            mutex.wait();
          } else {
            return true;
          }
        }
      }
    }
  }

  static public class Queue extends Sync {

    Queue(String address, String root) throws IOException, InterruptedException, KeeperException {
      super(address, root);
      if (zookeeper == null) {
        throw new RuntimeException("zookeeper is null");
      }

      Stat stat = zookeeper.exists(root, false);
      if (stat == null) {
        zookeeper.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
            CreateMode.PERSISTENT);
      }
    }

    boolean produce(int i) throws InterruptedException, KeeperException {
      ByteBuffer b = ByteBuffer.allocate(4);
      byte[] value;
      b.putInt(i);
      value = b.array();
      zookeeper.create(root + "/element", value, Ids.OPEN_ACL_UNSAFE,
          CreateMode.PERSISTENT_SEQUENTIAL);
      return true;
    }

    int consume() throws KeeperException, InterruptedException {
      int retvalue = -1;
      Stat stat = null;

      while (true) {
        synchronized (mutex) {
          List<String> list = zookeeper.getChildren(root, true);
          if (list.isEmpty()) {
            logger.info("consumer going to wait");
            mutex.wait();
          } else {
            int min = Integer.parseInt(list.get(0).substring(7));
            String minNode = list.get(0);
            for (String s : list) {
              int tempValue = Integer.parseInt(s.substring(7));
              if (tempValue < min) {
                min = tempValue;
                minNode = s;
              }
            }
            logger.info("Temporary value: {}/{}", root, minNode);
            byte[] b = zookeeper.getData(root + "/" + minNode, false, stat);
            zookeeper.delete(root + "/" + minNode, 0);
            ByteBuffer buffer = ByteBuffer.wrap(b);
            retvalue = buffer.getInt();
            return retvalue;
          }
        }
      }
    }
  }

  public static void main(String args[]) throws IOException, InterruptedException, KeeperException {
    if (args[0].equals("qTest")) {
      queueTest(args);
    } else {
      barrierTest(args);
    }
  }

  public static void queueTest(String args[])
      throws IOException, InterruptedException, KeeperException {
    Queue q = new Queue(args[1], "/app1");

    System.out.println("Input: " + args[1]);
    int i;
    int max = Integer.parseInt(args[2]);

    if (args[3].equals("p")) {
      System.out.println("Producer");
      for (i = 0; i < max; i++) {
        try {
          q.produce(10 + i);
        } catch (KeeperException | InterruptedException ignored) {

        }
      }
    } else {
      System.out.println("Consumer");

      for (i = 0; i < max; i++) {
        try {
          int r = q.consume();
          System.out.println("Item: " + r);
        } catch (KeeperException e) {
          i--;
        } catch (InterruptedException ignored) {
        }
      }
    }
  }

  public static void barrierTest(String args[]) throws IOException {
    Barrier b = new Barrier(args[1], "/b1", Integer.parseInt(args[2]));
    try {
      boolean flag = b.enter();
      System.out.println("Entered barrier: " + args[2]);
      if (!flag) {
        System.out.println("Error when entering the barrier");
      }
    } catch (KeeperException | InterruptedException ignored) {
    }

    Random rand = new Random();
    int r = rand.nextInt(100);
    for (int i = 0; i < r; i++) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ignored) {
      }
    }
    try {
      b.leave();
    } catch (KeeperException | InterruptedException ignored) {

    }
    logger.info("Left barrier");
  }

}
