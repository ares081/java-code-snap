package com.ares.reactor.multi;

import com.ares.reactor.ChannelHandler;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerBootstrapAcceptor {

  private final Logger log = LoggerFactory.getLogger(ServerBootstrapAcceptor.class);
  private final SubThreadGroup subThreadGroup;
  private final ChannelHandler channelHandler;

  public ServerBootstrapAcceptor(SubThreadGroup subThreadGroup, ChannelHandler channelHandler) {
    this.subThreadGroup = subThreadGroup;
    this.channelHandler = channelHandler;
  }

  public void handleAccept(SocketChannel socketChannel) throws IOException {
    log.info("Accepted connection from: {}", socketChannel.getRemoteAddress());
    subThreadGroup.registerChannel(socketChannel, channelHandler);
  }
}
