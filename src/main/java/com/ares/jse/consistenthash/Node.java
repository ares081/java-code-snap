package com.ares.jse.consistenthash;

import java.util.Objects;
import lombok.Getter;


@Getter
public class Node {

  private final String id;
  private final String name;
  private final String ip;
  private final int hashCode;

  public Node(String id, String name, String ip) {
    this.id = id;
    this.name = name;
    this.ip = ip;
    this.hashCode = Objects.hash(id, name, ip);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Node that = (Node) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(name, that.name) &&
        Objects.equals(ip, that.ip);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public String toString() {
    return id;
  }
}