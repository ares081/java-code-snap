package com.ares.query;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FieldReference {

  private static final ConcurrentMap<String, String> FIELD_NAME_CACHE = new ConcurrentHashMap<>();

  public static <T, R> FieldReferenceFunction<T, R> of(SerializableFunction<T, R> function) {
    return new FieldReferenceFunction<>() {
      @Override
      public R apply(T t) {
        return function.apply(t);
      }

      @Override
      public String name() {
        return extractFieldName(function);
      }
    };
  }

  private static String extractFieldName(SerializableFunction<?, ?> function) {
    String key = function.getClass().getName();
    return FIELD_NAME_CACHE.computeIfAbsent(key, k -> {
      try {
        Method writeReplace = function.getClass().getDeclaredMethod("writeReplace");
        writeReplace.setAccessible(true);
        SerializedLambda lambda = (SerializedLambda) writeReplace.invoke(function);
        String methodName = lambda.getImplMethodName();
        if (methodName.startsWith("get") && methodName.length() > 3) {
          return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
          return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        } else {
          return methodName;
        }
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(
            "Failed to extract field name from method reference. Make sure you're using a method reference (::)", e);
      }
    });
  }
}
