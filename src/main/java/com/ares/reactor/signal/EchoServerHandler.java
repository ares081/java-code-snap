package com.ares.reactor.signal;

import com.ares.reactor.ChannelHandler;
import com.ares.reactor.ChannelState;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServerHandler implements ChannelHandler {

  private final Logger log = LoggerFactory.getLogger(EchoServerHandler.class);

  private final SocketChannel socketChannel;
  private final SelectionKey selectionKey;
  ByteBuffer input = ByteBuffer.allocate(1024);
  ByteBuffer output = ByteBuffer.allocate(1024);

  ChannelState state = ChannelState.READING;

  public EchoServerHandler(Selector selector, SocketChannel socketChannel) throws IOException {
    this.socketChannel = socketChannel;
    this.socketChannel.configureBlocking(false);
    this.selectionKey = this.socketChannel.register(selector, 0);
    this.selectionKey.attach(this);
    this.selectionKey.interestOps(SelectionKey.OP_READ);
    selector.wakeup();
  }


  @Override
  public void run() {
    try {
      if (state == ChannelState.READING) {
        handleRead(selectionKey);
      } else if (state == ChannelState.SENDING) {
        handleWrite(selectionKey);
      }
    } catch (IOException e) {
      log.error("read or write error", e);
    }
  }

  @Override
  public void handleRead(SelectionKey selectionKey) throws IOException {
    input.clear();
    int read = socketChannel.read(input);

    if (read == -1) {
      socketChannel.close();
      selectionKey.cancel();
      return;
    }

    if (read <= 0) {
      state = ChannelState.SENDING;
      selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    // 处理数据
    input.flip();
    byte[] bytes = new byte[input.remaining()];
    input.get(bytes);

    String str = new String(bytes, StandardCharsets.UTF_8);
    log.info("read : {}", str);
    state = ChannelState.SENDING;
    selectionKey.interestOps(SelectionKey.OP_WRITE);
    input.clear();
  }

  @Override
  public void handleWrite(SelectionKey selectionKey) throws IOException {
    String msg = "this is server !!!\n";
    output.clear();
    output.flip();
    output = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
    socketChannel.write(output);
    state = ChannelState.READING;
    selectionKey.interestOps(SelectionKey.OP_READ);
    output.clear();
  }
}
