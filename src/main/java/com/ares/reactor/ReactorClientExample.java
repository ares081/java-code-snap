package com.ares.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactorClientExample {

  private static final Logger log = LoggerFactory.getLogger(ReactorClientExample.class);

  public static void main(String[] args) throws IOException, InterruptedException {
    try (SocketChannel socketChannel = SocketChannel.open()) {
      // Connect in blocking mode
      socketChannel.configureBlocking(true);
      if (!socketChannel.connect(new InetSocketAddress("127.0.0.1", 8908))) {
        log.error("Failed to connect to server");
        return;
      }

      ByteBuffer buffer = ByteBuffer.allocate(64);
      String msg = "this is client !!!\n";

      for (int i = 0; i < 10; i++) {
        try {
          // Send data
          buffer.clear();
          buffer.put(msg.getBytes(StandardCharsets.UTF_8));
          buffer.flip();
          while (buffer.hasRemaining()) {
            int written = socketChannel.write(buffer);
            if (written < 0) {
              throw new IOException("Connection closed by server");
            }
          }
          // Read response
          buffer.clear();
          int totalRead = 0;
          while (totalRead == 0) {
            int read = socketChannel.read(buffer);
            if (read == -1) {
              throw new IOException("Connection closed by server");
            }
            if (read > 0) {
              totalRead += read;
            }
            Thread.sleep(100); // Small delay to avoid busy waiting
          }

          buffer.flip();
          String result = StandardCharsets.UTF_8.decode(buffer).toString();
          log.info("Message {} - Response from server: {}", i, result);

          Thread.sleep(500); // Add delay between messages
        } catch (IOException e) {
          log.error("Error during communication: {}", e.getMessage());
          break;
        }
      }
    } catch (IOException e) {
      log.error("Connection error: {}", e.getMessage());
    }
  }
}
