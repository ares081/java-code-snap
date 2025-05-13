package com.ares.reactor.signal;

import com.ares.concurrency.threadpool.DefaultThreadPoolExecutor;
import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Acceptor implements Runnable {

  private final Logger log = LoggerFactory.getLogger(Acceptor.class);

  private final Selector selector;
  private final ServerSocketChannel serverSocketChannel;
  private final DefaultThreadPoolExecutor executor;

  public Acceptor(Selector selector, ServerSocketChannel serverSocketChannel) {
    this.selector = selector;
    this.serverSocketChannel = serverSocketChannel;
    this.executor = new DefaultThreadPoolExecutor("task-executor");
  }

  @Override
  public void run() {
    try {
      SocketChannel socketChannel = serverSocketChannel.accept();
      if (socketChannel != null) {
        executor.submit(() -> new EchoServerHandler(selector, socketChannel));
      }
    } catch (IOException e) {
      log.error("socket accept error", e);
    }
  }
}
