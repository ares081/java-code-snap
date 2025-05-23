package com.ares.query;

import java.util.Collection;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.json.JsonData;

public class ElasticQueryBuilder<T> extends BoolQuery.Builder {

  public static <T> ElasticQueryBuilder<T> of() {
    return new ElasticQueryBuilder<>();
  }

  public <V> ElasticQueryBuilder<T> withTerm(FieldReferenceFunction<T, ?> function, V value) {
    if (value != null) {
      String field = function.name();
      must(q -> q.term(t -> t.field(field).value(FieldValue.of(value))));
    }
    return this;
  }

  public <V> ElasticQueryBuilder<T> withTerms(SerializableFunction<T, ?> function,
      Collection<V> values) {
    if (values != null && !values.isEmpty()) {
      String field = FieldReference.of(function).name();
      must(f -> f.terms(t -> t.field(field)
          .terms(v -> v.value(values.stream().map(FieldValue::of).toList()))));
    }
    return this;
  }

  public <V> ElasticQueryBuilder<T> withRange(SerializableFunction<T, ?> function, V from, V to) {
    if (from != null || to != null) {
      String field = FieldReference.of(function).name();
      if (from != null) {
        must(q -> q.range(r -> r.field(field).gte(JsonData.of(from))));
      }
      if (to != null) {
        must(q -> q.range(r -> r.field(field).lte(JsonData.of(to))));
      }
    }
    return this;
  }

  public <V> ElasticQueryBuilder<T> withMatch(SerializableFunction<T, ?> function, V value) {
    if (value != null) {
      String field = FieldReference.of(function).name();
      must(q -> q.match(m -> m.field(field).query(value.toString())));
    }
    return this;
  }

  public <V> ElasticQueryBuilder<T> withMatchPhrase(SerializableFunction<T, ?> function, V value) {
    if (value != null) {
      String field = FieldReference.of(function).name();
      must(q -> q.matchPhrase(m -> m.field(field).query(value.toString())));
    }
    return this;
  }

  public <V> ElasticQueryBuilder<T> withMatchPhrasePrefix(SerializableFunction<T, ?> function,
      V value) {
    if (value != null) {
      String field = FieldReference.of(function).name();
      must(q -> q.matchPhrasePrefix(m -> m.field(field).query(value.toString())));
    }
    return this;
  }

  public <V> ElasticQueryBuilder<T> withMatchAll() {
    must(q -> q.matchAll(m -> m));
    return this;
  }

  public <V> ElasticQueryBuilder<T> withMatchNone() {
    must(q -> q.matchNone(m -> m));
    return this;
  }
}
