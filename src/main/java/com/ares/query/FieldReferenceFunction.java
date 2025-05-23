package com.ares.query;

public interface FieldReferenceFunction<T, R> extends SerializableFunction<T, R> {

  String name();
}
