package com.codeflow.result;

/** Functional interfaces used by Result operations that can throw checked exceptions. */
public final class Functions {

  private Functions() {}

  /**
   * Represents an operation that returns a value and may throw an exception.
   *
   * @param <T> The returned value type.
   */
  @FunctionalInterface
  public interface ThrowingSupplier<T> {
    T get() throws Exception;
  }

  /**
   * Represents an operation that accepts a value, returns another value, and may throw an
   * exception.
   *
   * @param <T> The input type.
   * @param <U> The returned value type.
   */
  @FunctionalInterface
  public interface ThrowingFunction<T, U> {
    U apply(T value) throws Exception;
  }
}
