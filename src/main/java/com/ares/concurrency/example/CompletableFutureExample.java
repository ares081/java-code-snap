package com.ares.concurrency.example;

import com.ares.concurrency.DefaultThreadFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

public class CompletableFutureExample {

  private static final ThreadFactory threadFactory = new DefaultThreadFactory("forkjoin");

  public static void main(String[] args) {
    CompletableFuture<String> future = new CompletableFuture<>();
    future.thenAccept(System.out::println);
  }
}
