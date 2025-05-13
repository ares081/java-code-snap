package com.ares.reactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface ChannelHandler extends Runnable {

  void handleRead(SelectionKey key) throws IOException;

  void handleWrite(SelectionKey key) throws IOException;
}
