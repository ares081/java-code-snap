package com.ares.reactor.signal;

import java.io.IOException;

public class EchoServerExample {

  public static void main(String[] args) throws IOException {

    new Reactor(8908).run();
  }

}
