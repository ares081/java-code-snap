package com.ares.reactor.multi;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Reactor implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(Reactor.class);

  protected final Selector selector;

  public Reactor() throws IOException {
    selector = Selector.open();
  }

  @Override
  public  void run() {
    while (true) {
      try {
        if (selector.select() == 0) {
          continue;
        }
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()) {
          SelectionKey selectionKey = iterator.next();
          // 必须先移除，避免重复处理
          iterator.remove();
          if (!selectionKey.isValid()) {
            continue;
          }
          try {
            dispatch(selectionKey);
          } catch (IOException e) {
            logger.error("Error handling key: " + selectionKey, e);
            selectionKey.cancel();
            try {
              selectionKey.channel().close();
            } catch (IOException ex) {
              logger.error("Error closing channel", ex);
            }
          }
        }
      } catch (Exception e) {
        logger.error("selector error", e);
      }
    }
  }

  protected abstract void dispatch(SelectionKey selectionKey) throws IOException;
}
