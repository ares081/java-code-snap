package com.ares.reactor.multi;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.concurrency.DefaultThreadFactory;
import com.ares.concurrency.threadpool.DefaultThreadPoolExecutor;
import com.ares.reactor.ChannelHandler;

public class SubReactor extends Reactor {

  private final Logger log = LoggerFactory.getLogger(SubReactor.class);

  static final int core = Runtime.getRuntime().availableProcessors();
  private final ExecutorService executor;

  public SubReactor(Integer coreThread) throws IOException {
    super();
    if (coreThread == null) {
      coreThread = core;
    }
    this.executor = new DefaultThreadPoolExecutor(coreThread, 65535,
        new DefaultThreadFactory("sub-reactor-pool"));
  }

  @Override
  protected synchronized void dispatch(SelectionKey selectionKey) throws IOException {
    if (!selectionKey.isValid()) {
      return;
    }

    log.info("sub-reactor selectionKey: {}", selectionKey);
    executor.execute(() -> {
      try {
        if (!selectionKey.isValid()) {
          return;
        }
        ChannelHandler handler = (ChannelHandler) selectionKey.attachment();
        if (selectionKey.isReadable()) {
          handler.handleRead(selectionKey);
        } else if (selectionKey.isWritable()) {
          handler.handleWrite(selectionKey);
        }
      } catch (IOException e) {
        log.error("Handler processing error", e);
        try {
          selectionKey.cancel();
          selectionKey.channel().close();
        } catch (IOException ex) {
          log.error("Error closing channel", ex);
        }
      }
    });
  }

  public void registerChannel(SocketChannel channel, int ops, ChannelHandler handler)
      throws IOException {
    channel.configureBlocking(false);
    channel.register(this.selector, ops, handler);
    this.selector.wakeup();
  }

  public void start(int threadId) {
    new Thread(this, "sub-reactor-thread-" + threadId).start();
  }
}
