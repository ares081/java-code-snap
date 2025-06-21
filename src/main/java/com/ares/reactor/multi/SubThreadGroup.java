package com.ares.reactor.multi;

import com.ares.reactor.ChannelHandler;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SubThreadGroup {

  private final List<SubReactor> children;
  private final AtomicInteger next = new AtomicInteger(0);

  public SubThreadGroup(int subThread) throws IOException {
    this.children = new ArrayList<>(subThread);
    for (int i = 0; i < subThread; i++) {
      SubReactor subReactor = new SubReactor(subThread);
      this.children.add(subReactor);
      subReactor.start(i);
    }
  }

  public void registerChannel(SocketChannel channel, ChannelHandler handler) throws IOException {
    int idx = next.getAndIncrement() % children.size();
    SubReactor subReactor = children.get(idx);
    subReactor.registerChannel(channel, SelectionKey.OP_READ, handler);
  }
}
