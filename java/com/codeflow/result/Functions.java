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
    /**
     * Gets a result, potentially throwing an exception.
     *
     * @return The supplied value.
     * @throws Exception if the operation fails.
     */
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
    /**
     * Applies this function to the given value, potentially throwing an exception.
     *
     * @param value The input value.
     * @return The function result.
     * @throws Exception if the operation fails.
     */
    U apply(T value) throws Exception;
  }
}
