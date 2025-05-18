package com.ares.jpa;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Collection;

public class FilterPredicateBuilder<T> {

  private final Root<T> root;
  private final CriteriaQuery<?> query;
  private final CriteriaBuilder criteriaBuilder;

  public FilterPredicateBuilder(Root<T> root, CriteriaQuery<?> query,
      CriteriaBuilder criteriaBuilder) {
    this.root = root;
    this.query = query;
    this.criteriaBuilder = criteriaBuilder;
  }

  /**
   * 创建一个等于条件
   *
   * @param attribute 属性名
   * @param value     值
   * @return Predicate
   */
  public Predicate equals(String attribute, Object value) {
    return criteriaBuilder.equal(root.get(attribute), value);
  }

  /**
   * 创建一个不等于条件
   *
   * @param attribute 属性名
   * @param value     值
   * @return Predicate
   */
  public Predicate notEquals(String attribute, Object value) {
    return criteriaBuilder.notEqual(root.get(attribute), value);
  }

  /**
   * 创建一个大于条件
   *
   * @param attribute 属性名
   * @param value     值
   * @param <Y>       比较值类型
   * @return Predicate
   */
  public <Y extends Comparable<? super Y>> Predicate greaterThan(String attribute, Y value) {
    return criteriaBuilder.greaterThan(root.get(attribute), value);
  }

  /**
   * 创建一个大于等于条件
   *
   * @param attribute 属性名
   * @param value     值
   * @param <Y>       比较值类型
   * @return Predicate
   */
  public <Y extends Comparable<? super Y>> Predicate greaterThanOrEquals(String attribute,
      Y value) {
    return criteriaBuilder.greaterThanOrEqualTo(root.get(attribute), value);
  }

  /**
   * 创建一个小于条件
   *
   * @param attribute 属性名
   * @param value     值
   * @param <Y>       比较值类型
   * @return Predicate
   */
  public <Y extends Comparable<? super Y>> Predicate lessThan(String attribute, Y value) {
    return criteriaBuilder.lessThan(root.get(attribute), value);
  }

  /**
   * 创建一个小于等于条件
   *
   * @param attribute 属性名
   * @param value     值
   * @param <Y>       比较值类型
   * @return Predicate
   */
  public <Y extends Comparable<? super Y>> Predicate lessThanOrEquals(String attribute, Y value) {
    return criteriaBuilder.lessThanOrEqualTo(root.get(attribute), value);
  }

  /**
   * 创建一个Like条件
   *
   * @param attribute 属性名
   * @param value     值
   * @return Predicate
   */
  public Predicate like(String attribute, String value) {
    return criteriaBuilder.like(root.get(attribute), value);
  }

  /**
   * 创建一个包含条件（like %value%）
   *
   * @param attribute 属性名
   * @param value     值
   * @return Predicate
   */
  public Predicate contains(String attribute, String value) {
    return criteriaBuilder.like(root.get(attribute), "%" + value + "%");
  }

  /**
   * 创建一个开始于条件（like value%）
   *
   * @param attribute 属性名
   * @param value     值
   * @return Predicate
   */
  public Predicate startsWith(String attribute, String value) {
    return criteriaBuilder.like(root.get(attribute), value + "%");
  }

  /**
   * 创建一个结束于条件（like %value）
   *
   * @param attribute 属性名
   * @param value     值
   * @return Predicate
   */
  public Predicate endsWith(String attribute, String value) {
    return criteriaBuilder.like(root.get(attribute), "%" + value);
  }

  /**
   * 创建一个In条件
   *
   * @param attribute 属性名
   * @param values    值集合
   * @return Predicate
   */
  public Predicate in(String attribute, Collection<?> values) {
    CriteriaBuilder.In<Object> inClause = criteriaBuilder.in(root.get(attribute));
    for (Object value : values) {
      inClause.value(value);
    }
    return inClause;
  }

  /**
   * 创建一个Between条件
   *
   * @param attribute 属性名
   * @param start     起始值
   * @param end       结束值
   * @param <Y>       比较值类型
   * @return Predicate
   */
  public <Y extends Comparable<? super Y>> Predicate between(String attribute, Y start, Y end) {
    return criteriaBuilder.between(root.get(attribute), start, end);
  }

  /**
   * 创建一个IsNull条件
   *
   * @param attribute 属性名
   * @return Predicate
   */
  public Predicate isNull(String attribute) {
    return criteriaBuilder.isNull(root.get(attribute));
  }

  /**
   * 创建一个IsNotNull条件
   *
   * @param attribute 属性名
   * @return Predicate
   */
  public Predicate isNotNull(String attribute) {
    return criteriaBuilder.isNotNull(root.get(attribute));
  }

  /**
   * 创建一个And条件
   *
   * @param predicates Predicate列表
   * @return Predicate
   */
  public Predicate and(Predicate... predicates) {
    return criteriaBuilder.and(predicates);
  }

  /**
   * 创建一个Or条件
   *
   * @param predicates Predicate列表
   * @return Predicate
   */
  public Predicate or(Predicate... predicates) {
    return criteriaBuilder.or(predicates);
  }

  public Expression<Long> count(String attribute) {
    return criteriaBuilder.count(root.get(attribute));
  }
}
