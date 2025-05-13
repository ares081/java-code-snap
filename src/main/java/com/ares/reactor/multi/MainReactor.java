package com.ares.reactor.multi;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainReactor extends Reactor {

  private final Logger log = LoggerFactory.getLogger(MainReactor.class);

  private final ServerSocketChannel serverSocketChannel;
  private final ServerBootstrapAcceptor acceptor;

  public MainReactor(ServerSocketChannel serverSocketChannel, ServerBootstrapAcceptor acceptor)
      throws IOException {
    super();
    this.serverSocketChannel = serverSocketChannel;
    serverSocketChannel.configureBlocking(false);
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    this.acceptor = acceptor;
    log.info("register key type: {}", selector.selectedKeys().toString());
  }

  @Override
  protected void dispatch(SelectionKey key) {
    if (key.isAcceptable()) {
      try {
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel != null) {
          acceptor.handleAccept(socketChannel);
        }
      } catch (IOException e) {
        log.error("get socket channel error", e);
      }
    }
  }

  public void start() {
    new Thread(this, "main-reactor-thread").start();
  }
}
