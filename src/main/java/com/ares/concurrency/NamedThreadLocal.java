package com.ares.concurrency;

import java.util.Objects;
import java.util.function.Supplier;

public class NamedThreadLocal<T> extends ThreadLocal<T> {

  private final String name;

  public NamedThreadLocal(String name) {
    this.name = name;
  }

  public static <S> ThreadLocal<S> withInitial(String name, Supplier<? extends S> supplier) {
    return new SuppliedNamedThreadLocal<>(name, supplier);
  }

  private static final class SuppliedNamedThreadLocal<T> extends NamedThreadLocal<T> {

    private final Supplier<? extends T> supplier;

    SuppliedNamedThreadLocal(String name, Supplier<? extends T> supplier) {
      super(name);
      this.supplier = Objects.requireNonNull(supplier);
    }

    @Override
    protected T initialValue() {
      return this.supplier.get();
    }
  }
}
