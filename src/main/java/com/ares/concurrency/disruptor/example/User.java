package com.ares.concurrency.disruptor.example;

public class User {

  private Long userId;
  private String name;

  public User(Long userId, String name) {
    this.userId = userId;
    this.name = name;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "User{" +
        "userId=" + userId +
        ", name='" + name + '\'' +
        '}';
  }
}
