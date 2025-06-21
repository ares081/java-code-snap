package com.ares.query;

import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

public class JpaQueryHelper {

  /**
   * 创建空的Specification
   *
   * @param <T> 实体类型
   * @return 空的Specification
   */
  public static <T> Specification<T> none() {
    return (root, query, criteria) -> null;
  }

  /**
   * 创建一个Specification
   *
   * @param func predicate构建函数
   * @param <T>  实体类型
   * @return Specification
   */
  public static <T> Specification<T> specPredicate(
      Function<FilterPredicateBuilder<T>, Predicate> func) {
    return (root, query, criteria) -> {
      FilterPredicateBuilder<T> builder = new FilterPredicateBuilder<>(root, query, criteria);
      return func.apply(builder);
    };
  }


  /**
   * 创建一个可选的Specification，仅当条件成立时才应用
   *
   * @param condition 条件
   * @param spec      当条件成立时应用的Specification
   * @param <T>       实体类型
   * @return Specification
   */
  private static <T> Specification<T> optional(boolean condition, Specification<T> spec) {
    return condition ? spec : none();
  }

  /**
   * 组合多个Specification，使用AND连接
   *
   * @param specs Specification列表
   * @param <T>   实体类型
   * @return 组合后的Specification
   */
  @SafeVarargs
  public static <T> Specification<T> and(Specification<T>... specs) {
    return (root, query, criteria) -> {
      List<Predicate> predicates = Arrays.stream(specs)
          .filter(Objects::nonNull)
          .map(spec -> spec.toPredicate(root, query, criteria))
          .filter(Objects::nonNull)
          .toList();
      return predicates.isEmpty() ? null : criteria.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * 组合多个Specification，使用OR连接¬
   *
   * @param specs Specification列表
   * @param <T>   实体类型
   * @return 组合后的Specification
   */
  @SafeVarargs
  public static <T> Specification<T> or(Specification<T>... specs) {
    return (root, query, criteria) -> {
      List<Predicate> predicates = Arrays.stream(specs)
          .filter(Objects::nonNull)
          .map(spec -> spec.toPredicate(root, query, criteria))
          .filter(Objects::nonNull)
          .toList();
      return predicates.isEmpty() ? null : criteria.or(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * 创建一个等于条件的Specification
   *
   * @param attribute 属性名
   * @param value     值
   * @param <T>       实体类型
   * @return Specification
   */
  public static <T> Specification<T> equals(String attribute, Object value) {
    return optional(value != null, specPredicate(builder -> builder.equals(attribute, value)));
  }

  /**
   * 创建一个不等于条件的Specification
   *
   * @param attribute 属性名
   * @param value     值
   * @param <T>       实体类型
   * @return Specification
   */
  public static <T> Specification<T> notEquals(String attribute, Object value) {
    return optional(value != null, specPredicate(builder -> builder.notEquals(attribute, value)));
  }

  /**
   * 创建一个大于条件的Specification
   *
   * @param attribute 属性名
   * @param value     值
   * @param <T>       实体类型
   * @param <Y>       比较值类型
   * @return Specification
   */
  public static <T, Y extends Comparable<? super Y>> Specification<T> greaterThan(String attribute,
      Y value) {
    return optional(value != null, specPredicate(builder -> builder.greaterThan(attribute, value)));
  }

  /**
   * 创建一个大于等于条件的Specification
   *
   * @param attribute 属性名
   * @param value     值
   * @param <T>       实体类型
   * @param <Y>       比较值类型
   * @return Specification
   */
  public static <T, Y extends Comparable<? super Y>> Specification<T> greaterThanOrEquals(
      String attribute, Y value) {
    return optional(value != null,
        specPredicate(builder -> builder.greaterThanOrEquals(attribute, value)));
  }

  /**
   * 创建一个小于条件的Specification
   *
   * @param attribute 属性名
   * @param value     值
   * @param <T>       实体类型
   * @param <Y>       比较值类型
   * @return Specification
   */
  public static <T, Y extends Comparable<? super Y>> Specification<T> lessThan(String attribute,
      Y value) {
    return optional(value != null, specPredicate(builder -> builder.lessThan(attribute, value)));
  }

  /**
   * 创建一个小于等于条件的Specification
   *
   * @param attribute 属性名
   * @param value     值
   * @param <T>       实体类型
   * @param <Y>       比较值类型
   * @return Specification
   */
  public static <T, Y extends Comparable<? super Y>> Specification<T> lessThanOrEquals(
      String attribute, Y value) {
    return optional(value != null,
        specPredicate(builder -> builder.lessThanOrEquals(attribute, value)));
  }

  /**
   * 创建一个Like条件的Specification
   *
   * @param attribute 属性名
   * @param value     值
   * @param <T>       实体类型
   * @return Specification
   */
  public static <T> Specification<T> like(String attribute, String value) {
    return optional(value != null && !value.isEmpty(),
        specPredicate(builder -> builder.like(attribute, value)));
  }

  /**
   * 创建一个包含条件的Specification（like %value%）
   *
   * @param attribute 属性名
   * @param value     值
   * @param <T>       实体类型
   * @return Specification
   */
  public static <T> Specification<T> contains(String attribute, String value) {
    return optional(value != null && !value.isEmpty(),
        specPredicate(builder -> builder.contains(attribute, value)));
  }

  /**
   * 创建一个开始于条件的Specification（like value%）
   *
   * @param attribute 属性名
   * @param value     值
   * @param <T>       实体类型
   * @return Specification
   */
  public static <T> Specification<T> startsWith(String attribute, String value) {
    return optional(value != null && !value.isEmpty(),
        specPredicate(builder -> builder.startsWith(attribute, value)));
  }

  /**
   * 创建一个结束于条件的Specification（like %value）
   *
   * @param attribute 属性名
   * @param value     值
   * @param <T>       实体类型
   * @return Specification
   */
  public static <T> Specification<T> endsWith(String attribute, String value) {
    return optional(value != null && !value.isEmpty(),
        specPredicate(builder -> builder.endsWith(attribute, value)));
  }

  /**
   * 创建一个In条件的Specification
   *
   * @param attribute 属性名
   * @param values    值集合
   * @param <T>       实体类型
   * @return Specification
   */
  public static <T> Specification<T> in(String attribute, Collection<?> values) {
    return optional(values != null && !values.isEmpty(),
        specPredicate(builder -> builder.in(attribute, Objects.requireNonNull(values))));
  }

  /**
   * 创建一个Between条件的Specification
   *
   * @param attribute 属性名
   * @param start     起始值
   * @param end       结束值
   * @param <T>       实体类型
   * @param <Y>       比较值类型
   * @return Specification
   */
  public static <T, Y extends Comparable<? super Y>> Specification<T> between(String attribute,
      Y start, Y end) {
    if (start != null && end != null) {
      return specPredicate(builder -> builder.between(attribute, start, end));
    } else if (start != null) {
      return specPredicate(builder -> builder.greaterThanOrEquals(attribute, start));
    } else if (end != null) {
      return specPredicate(builder -> builder.lessThanOrEquals(attribute, end));
    } else {
      return none();
    }
  }

  /**
   * 创建一个IsNull条件的Specification
   *
   * @param attribute 属性名
   * @param <T>       实体类型
   * @return Specification
   */
  public static <T> Specification<T> isNull(String attribute) {
    return specPredicate(builder -> builder.isNull(attribute));
  }

  /**
   * 创建一个IsNotNull条件的Specification
   *
   * @param attribute 属性名
   * @param <T>       实体类型
   * @return Specification
   */
  public static <T> Specification<T> isNotNull(String attribute) {
    return specPredicate(builder -> builder.isNotNull(attribute));
  }

}
