package com.ares.reactor.signal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reactor implements Runnable {

  private final Logger log = LoggerFactory.getLogger(Reactor.class);
  private final Selector selector;

  public Reactor(int port) throws IOException {
    selector = Selector.open();
    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    serverSocketChannel.socket().bind(new InetSocketAddress(port));
    serverSocketChannel.configureBlocking(false);

    // 注册keys
    SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    key.attach(new Acceptor(selector, serverSocketChannel));
  }

  @Override
  public void run() {
    try {
      while (!Thread.interrupted()) {
        selector.select();
        Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
        while (selectedKeys.hasNext()) {
          SelectionKey key = selectedKeys.next();
          selectedKeys.remove();
          if (!key.isValid()) {
            continue;
          }
          dispatch(key);
        }
      }
    } catch (Exception e) {
      log.error("selector error", e);
    }
  }

  private void dispatch(SelectionKey key) throws IOException {
    Runnable r = (Runnable) key.attachment();
    if (r != null) {
      r.run();
    }
  }
}
