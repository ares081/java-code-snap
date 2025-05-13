package com.ares.reactor.multi;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.SneakyThrows;

public class EchoServer {

  private static final Logger log = LoggerFactory.getLogger(EchoServer.class);

  @SneakyThrows
  public static void main(String[] args) throws IOException {
    ServerBootstrap bootstrap = new ServerBootstrap()
        .port(8908)
        .subReactorCount(1)
        .channelHandler(EchoServerHandler::new);
    bootstrap.start();
    log.info("Echo server started on port: {}", 8908);
  }
}
