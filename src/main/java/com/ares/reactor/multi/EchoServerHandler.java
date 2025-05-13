package com.ares.reactor.multi;

import com.ares.reactor.BufferPool;
import com.ares.reactor.ChannelHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServerHandler implements ChannelHandler {

  private final Logger log = LoggerFactory.getLogger(EchoServerHandler.class);
  private volatile boolean readComplete = false;
  private final BufferPool bufferPool;

  public EchoServerHandler(int bufferSize, int maxBufferPool) {
    this.bufferPool = new BufferPool(bufferSize, maxBufferPool);
  }

  @Override
  public synchronized void handleRead(SelectionKey key) throws IOException {
    if (!key.isValid()) {
      return;
    }

    if (readComplete) {
      return;
    }

    log.info("+++++++++++++++++++++EchoServerHandler handleRead, selectorKey: {}", key);
    ByteBuffer input = bufferPool.acquire();
    try {
      SocketChannel channel = (SocketChannel) key.channel();

      input.clear();
      int read = channel.read(input);

      if (read == -1) {
        channel.close();
        key.cancel();
        log.info("read completed !!!");
        return;
      }

      input.flip();
      byte[] bytes = new byte[input.remaining()];
      input.get(bytes);
      String message = new String(bytes, StandardCharsets.UTF_8);
      log.info("********************EchoServerHandler handleRead, message: {}", message);
      readComplete = true;
      if (key.isValid()) {
        key.interestOps(SelectionKey.OP_WRITE);
        key.selector().wakeup();
      }
      input.clear();
    } finally {
      bufferPool.release(input);
    }

  }

  @Override
  public synchronized void handleWrite(SelectionKey key) throws IOException {
    if (!key.isValid()) {
      return;
    }
    if (!readComplete) {
      return; // 如果还没读取完成，直接返回
    }
    log.info("++++++++++++++++++++++++EchoServerHandler handleWrite, selectorKey: {}", key);
    ByteBuffer output = bufferPool.acquire();
    try {
      SocketChannel channel = (SocketChannel) key.channel();
      output.clear();
      String message = "this is server !!!\n";
      output.put(message.getBytes(StandardCharsets.UTF_8));
      output.flip();
      channel.write(output);

      output.clear();
      readComplete = false;

      if (key.isValid()) {
        key.interestOps(SelectionKey.OP_READ);
        key.selector().wakeup();
      }
    } catch (IOException e) {
      log.error("write failed", e);
    } finally {
      bufferPool.release(output);
    }
  }

  @Override
  public void run() {

  }
}
