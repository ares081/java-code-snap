package com.ares.jse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class ListHelper {

  // 按对象field去重
  public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
    ConcurrentHashMap<Object, Boolean> map = new ConcurrentHashMap<>();
    return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }

  // 匹配两个列表，按 keyFunction 提取 key 做关联，返回匹配的 Pair 对
  public static <A, B, K> List<Pair<A, B>> match(List<A> list1, List<B> list2,
      Function<A, K> keyExtractor1, Function<B, K> keyExtractor2) {
    return doMatch(list1, list2, keyExtractor1, keyExtractor2);
  }

  public static <A, B, K> List<Pair<A, B>> matchDistinct(List<A> list1, List<B> list2,
      Function<A, K> keyExtractor1, Function<B, K> keyExtractor2) {
    List<Pair<A, B>> list = doMatch(list1, list2, keyExtractor1, keyExtractor2);
    return list.stream().filter(Objects::nonNull)
        .filter(distinctByKey(Pair::second)).toList();
  }

  private static <A, B, K> List<Pair<A, B>> doMatch(List<A> list1, List<B> list2,
      Function<A, K> keyExtractor1, Function<B, K> keyExtractor2) {
    Map<K, List<B>> map2 = new HashMap<>();
    for (B b : list2) {
      K key = keyExtractor2.apply(b);
      map2.computeIfAbsent(key, k -> new ArrayList<>()).add(b);
    }

    List<Pair<A, B>> result = new ArrayList<>();
    for (A a : list1) {
      K key = keyExtractor1.apply(a);
      List<B> bs = map2.get(key);
      if (bs != null) {
        for (B b : bs) {
          result.add(new Pair<>(a, b));
        }
      }
    }
    return new ArrayList<>(result);
  }

  // 泛型Pair，重写equals/hashCode以便于去重
  public record Pair<X, Y>(X first, Y second) {

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Pair<?, ?> pair)) {
        return false;
      }
      return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
      return Objects.hash(first, second);
    }
  }
}
