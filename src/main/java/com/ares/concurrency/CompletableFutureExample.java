package com.ares.concurrency;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureExample {

  public static void main(String[] args) {
    CompletableFuture<String> cf = CompletableFuture
        .supplyAsync(() -> "Hello CompletableFuture")
        .thenApply(String::toUpperCase)
        .thenApply(String::trim);

    cf.thenAccept(System.out::println);

    CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> "cf1");
    CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> "cf2");
    CompletableFuture<String> cf3 = CompletableFuture.supplyAsync(() -> "cf3");
    CompletableFuture.allOf(cf1, cf2, cf3).thenRun(() -> {
      String res1 = cf1.join();
      String res2 = cf2.join();
      String res3 = cf3.join();
      System.out.println("result=" + res1 + "," + res2 + "," + res3);
    });

    CompletableFuture cf4 = CompletableFuture.completedFuture("Hello CompletableFuture").thenApply(String::toUpperCase);

    CompletableFuture.supplyAsync(() -> {
      double s = 3 / 0;
      return s;
    }).exceptionally(ex -> {
      System.out.println(ex.getMessage());
      return 0d;
    }).thenAccept(System.out::println);


  }

}
