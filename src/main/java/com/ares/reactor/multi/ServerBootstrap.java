package com.ares.reactor.multi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.reactor.ChannelHandler;

public class ServerBootstrap {

  private final Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

  private int port = 8080;
  private int coreThread = Runtime.getRuntime().availableProcessors();
  private Supplier<ChannelHandler> handler;

  public ServerBootstrap port(int port) {
    this.port = port;
    return this;
  }

  public ServerBootstrap subReactorCount(int coreThread) {
    this.coreThread = coreThread;
    return this;
  }

  public ServerBootstrap channelHandler(Supplier<ChannelHandler> handler) {
    this.handler = handler;
    return this;
  }

  public void start() throws IOException {
    if (handler == null) {
      throw new IllegalStateException("ChannelHandler supplier is not set");
    }

    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    serverSocketChannel.bind(new InetSocketAddress(port));

    ChannelHandler channelHandler = handler.get();
    SubThreadGroup threadGroup = new SubThreadGroup(coreThread);
    ServerBootstrapAcceptor acceptor = new ServerBootstrapAcceptor(threadGroup, channelHandler);
    MainReactor mainReactor = new MainReactor(serverSocketChannel, acceptor);
    mainReactor.start();
  }
}
