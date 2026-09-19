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

  /**
   * Represents an operation that accepts two values, returns another value, and may throw an
   * exception.
   *
   * @param <T1> The first input type.
   * @param <T2> The second input type.
   * @param <U> The returned value type.
   */
  @FunctionalInterface
  public interface ThrowingBiFunction<T1, T2, U> {
    /**
     * Applies this function to the given value, potentially throwing an exception.
     *
     * @param t1 The first input value.
     * @param t2 The second input value.
     * @return The function result.
     * @throws Exception if the operation fails.
     */
    U apply(T1 t1, T2 t2) throws Exception;
  }

  /**
   * Represents an operation that accepts three values, returns another value, and may throw an
   * exception.
   *
   * @param <T1> The first input type.
   * @param <T2> The second input type.
   * @param <T3> The third input type.
   * @param <R> The returned value type.
   */
  @FunctionalInterface
  public interface ThrowingFunction3<T1, T2, T3, R> {
    /**
     * Applies this function to the given values, potentially throwing an exception.
     *
     * @param t1 The first input value.
     * @param t2 The second input value.
     * @param t3 The third input value.
     * @return The function result.
     * @throws Exception if the operation fails.
     */
    R apply(T1 t1, T2 t2, T3 t3) throws Exception;
  }

  /**
   * Represents an operation that accepts three input arguments and returns no result.
   *
   * @param <T1> The type of the first argument to the operation.
   * @param <T2> The type of the second argument to the operation.
   * @param <T3> The type of the third argument to the operation.
   */
  @FunctionalInterface
  public interface Consumer3<T1, T2, T3> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t1 the first input argument
     * @param t2 the second input argument
     * @param t3 the third input argument
     */
    void accept(T1 t1, T2 t2, T3 t3);
  }

  /**
   * Represents an operation that accepts three arguments and produces a result.
   *
   * @param <T1> the type of the first argument to the function
   * @param <T2> the type of the second argument to the function
   * @param <T3> the type of the third argument to the function
   * @param <R> the type of the result of the function
   */
  @FunctionalInterface
  public interface Function3<T1, T2, T3, R> {

    static <T1, T2, T3, R> Function3<T1, T2, T3, R> safe(ThrowingFunction3<T1, T2, T3, R> fn) {
      return (t1, t2, t3) -> {
        try {
          return fn.apply(t1, t2, t3);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      };
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param t1 the first function argument
     * @param t2 the second function argument
     * @param t3 the third function argument
     * @return the function result
     */
    R apply(T1 t1, T2 t2, T3 t3);
  }

  /**
   * Represents an operation that accepts four values, returns another value, and may throw an
   * exception.
   *
   * @param <T1> The first input type.
   * @param <T2> The second input type.
   * @param <T3> The third input type.
   * @param <T4> The fourth input type.
   * @param <R> The returned value type.
   */
  @FunctionalInterface
  public interface ThrowingFunction4<T1, T2, T3, T4, R> {
    /**
     * Applies this function to the given values, potentially throwing an exception.
     *
     * @param t1 The first input value.
     * @param t2 The second input value.
     * @param t3 The third input value.
     * @param t4 The fourth input value.
     * @return The function result.
     * @throws Exception if the operation fails.
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4) throws Exception;
  }

  /**
   * Represents an operation that accepts four arguments and produces a result.
   *
   * @param <T1> the type of the first argument to the function
   * @param <T2> the type of the second argument to the function
   * @param <T3> the type of the third argument to the function
   * @param <T4> the type of the fourth argument to the function
   * @param <R> the type of the result of the function
   */
  @FunctionalInterface
  public interface Function4<T1, T2, T3, T4, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t1 the first function argument
     * @param t2 the second function argument
     * @param t3 the third function argument
     * @param t4 the fourth function argument
     * @return the function result
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4);
  }

  /**
   * Represents an operation that accepts four input arguments and returns no result.
   *
   * @param <T1> The type of the first argument to the operation.
   * @param <T2> The type of the second argument to the operation.
   * @param <T3> The type of the third argument to the operation.
   * @param <T4> The type of the fourth argument to the operation.
   */
  @FunctionalInterface
  public interface Consumer4<T1, T2, T3, T4> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t1 the first input argument
     * @param t2 the second input argument
     * @param t3 the third input argument
     * @param t4 the fourth input argument
     */
    void accept(T1 t1, T2 t2, T3 t3, T4 t4);
  }

  /**
   * Represents an operation that accepts five values, returns another value, and may throw an
   * exception.
   *
   * @param <T1> The first input type.
   * @param <T2> The second input type.
   * @param <T3> The third input type.
   * @param <T4> The fourth input type.
   * @param <T5> The fifth input type.
   * @param <R> The returned value type.
   */
  @FunctionalInterface
  public interface ThrowingFunction5<T1, T2, T3, T4, T5, R> {
    /**
     * Applies this function to the given values, potentially throwing an exception.
     *
     * @param t1 The first input value.
     * @param t2 The second input value.
     * @param t3 The third input value.
     * @param t4 The fourth input value.
     * @param t5 The fifth input value.
     * @return The function result.
     * @throws Exception if the operation fails.
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) throws Exception;
  }

  /**
   * Represents an operation that accepts five arguments and produces a result.
   *
   * @param <T1> the type of the first argument to the function
   * @param <T2> the type of the second argument to the function
   * @param <T3> the type of the third argument to the function
   * @param <T4> the type of the fourth argument to the function
   * @param <T5> the type of the fifth argument to the function
   * @param <R> the type of the result of the function
   */
  @FunctionalInterface
  public interface Function5<T1, T2, T3, T4, T5, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t1 the first function argument
     * @param t2 the second function argument
     * @param t3 the third function argument
     * @param t4 the fourth function argument
     * @param t5 the fifth function argument
     * @return the function result
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
  }

  /**
   * Represents an operation that accepts five input arguments and returns no result.
   *
   * @param <T1> The type of the first argument to the operation.
   * @param <T2> The type of the second argument to the operation.
   * @param <T3> The type of the third argument to the operation.
   * @param <T4> The type of the fourth argument to the operation.
   * @param <T5> The type of the fifth argument to the operation.
   */
  @FunctionalInterface
  public interface Consumer5<T1, T2, T3, T4, T5> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t1 the first input argument
     * @param t2 the second input argument
     * @param t3 the third input argument
     * @param t4 the fourth input argument
     * @param t5 the fifth input argument
     */
    void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
  }

  /**
   * Represents an operation that accepts six values, returns another value, and may throw an
   * exception.
   *
   * @param <T1> The first input type.
   * @param <T2> The second input type.
   * @param <T3> The third input type.
   * @param <T4> The fourth input type.
   * @param <T5> The fifth input type.
   * @param <T6> The sixth input type.
   * @param <R> The returned value type.
   */
  @FunctionalInterface
  public interface ThrowingFunction6<T1, T2, T3, T4, T5, T6, R> {
    /**
     * Applies this function to the given values, potentially throwing an exception.
     *
     * @param t1 The first input value.
     * @param t2 The second input value.
     * @param t3 The third input value.
     * @param t4 The fourth input value.
     * @param t5 The fifth input value.
     * @param t6 The sixth input value.
     * @return The function result.
     * @throws Exception if the operation fails.
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) throws Exception;
  }

  /**
   * Represents an operation that accepts six arguments and produces a result.
   *
   * @param <T1> the type of the first argument to the function
   * @param <T2> the type of the second argument to the function
   * @param <T3> the type of the third argument to the function
   * @param <T4> the type of the fourth argument to the function
   * @param <T5> the type of the fifth argument to the function
   * @param <T6> the type of the sixth argument to the function
   * @param <R> the type of the result of the function
   */
  @FunctionalInterface
  public interface Function6<T1, T2, T3, T4, T5, T6, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t1 the first function argument
     * @param t2 the second function argument
     * @param t3 the third function argument
     * @param t4 the fourth function argument
     * @param t5 the fifth function argument
     * @param t6 the sixth function argument
     * @return the function result
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);
  }

  /**
   * Represents an operation that accepts six input arguments and returns no result.
   *
   * @param <T1> The type of the first argument to the operation.
   * @param <T2> The type of the second argument to the operation.
   * @param <T3> The type of the third argument to the operation.
   * @param <T4> The type of the fourth argument to the operation.
   * @param <T5> The type of the fifth argument to the operation.
   * @param <T6> The type of the sixth argument to the operation.
   */
  @FunctionalInterface
  public interface Consumer6<T1, T2, T3, T4, T5, T6> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t1 the first input argument
     * @param t2 the second input argument
     * @param t3 the third input argument
     * @param t4 the fourth input argument
     * @param t5 the fifth input argument
     * @param t6 the sixth input argument
     */
    void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);
  }
}
