package com.codeflow.result;

import static java.util.Objects.requireNonNull;

import com.codeflow.result.Functions.ThrowingFunction;
import com.codeflow.result.Functions.ThrowingSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A container object which may contain either a success value (Ok) or an error value (Err).
 * Inspired by Rust's {@code Result} type, this sealed interface provides a type-safe way to handle
 * operations that can fail without throwing exceptions.
 *
 * @param <T> The type of the success value.
 */
public sealed interface Result<T> permits Result.Ok, Result.Err {

  /**
   * Represents a successful outcome containing a value.
   *
   * @param <T> The success type.
   * @param value The success value, must not be null.
   */
  record Ok<T>(T value) implements Result<T> {
    public Ok {
      requireNonNull(value);
      if (value instanceof Result) {
        throw new IllegalArgumentException("Cannot wrap Result in Ok");
      }
    }
  }

  /**
   * Represents a failed outcome containing an error.
   *
   * @param <T> The success type.
   * @param error The error value, must not be null.
   */
  record Err<T>(Cause error) implements Result<T> {
    public Err {
      requireNonNull(error);
    }

    @SuppressWarnings("unchecked")
    public <U> Err<U> into() {
      return (Err<U>) this;
    }
  }

  /**
   * Maps the success value to a new value using the provided mapper function. If this is an {@link
   * Err}, the error is propagated unchanged.
   *
   * @param fn The function to apply to the success value.
   * @param <U> The new success type.
   * @return A new Result containing the mapped value or the original error.
   */
  default <U> Result<U> then(Function<? super T, ? extends U> fn) {
    requireNonNull(fn);
    return switch (this) {
      case Ok(var v) -> ok(fn.apply(v));
      case Err<T> error -> error.into();
    };
  }

  /**
   * Maps the success value to a new Result. This is equivalent to "flatMap". If this is an {@link
   * Err}, the error is propagated unchanged.
   *
   * @param fn The function to apply to the success value.
   * @param <U> The new success type.
   * @return The Result produced by the mapper, or the original error.
   */
  default <U> Result<U> thenApply(Function<? super T, ? extends Result<? extends U>> fn) {
    requireNonNull(fn);
    return switch (this) {
      case Ok(var v) -> narrow(fn.apply(v));
      case Err<T> error -> error.into();
    };
  }

  /**
   * Maps the success value using an operation that may throw an exception. If this is an {@link
   * Err}, the error is propagated unchanged. If the operation throws an exception, it is mapped
   * into an error value.
   *
   * <p>Exceptions thrown while constructing the returned {@link Ok}, such as invariant violations
   * caused by returning {@code null} or another {@code Result}, are not mapped into {@link Err}.
   *
   * @param fn The operation to apply to the success value.
   * @param exceptionMapper The function used to transform the thrown exception into an error value.
   * @param <U> The new success type.
   * @return An {@link Ok} containing the mapped value, the original {@link Err}, or an {@link Err}
   *     containing the mapped exception.
   */
  default <U> Result<U> thenTrying(
      ThrowingFunction<? super T, ? extends U> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    requireNonNull(fn);
    requireNonNull(exceptionMapper);
    return switch (this) {
      case Ok(var value) -> {
        U result;
        try {
          result = fn.apply(value);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Execution interrupted", e);
        } catch (Exception e) {
          yield err(exceptionMapper.apply(e));
        }
        yield ok(result);
      }
      case Err<T> error -> error.into();
    };
  }

  /**
   * Maps the success value using an operation that may throw an exception. If this is an {@link
   * Err}, the error is propagated unchanged. If the operation throws an exception, it is mapped
   * into an error value.
   *
   * <p>Exceptions thrown while constructing the returned {@link Ok}, such as invariant violations
   * caused by returning {@code null} or another {@code Result}, are not mapped into {@link Err}.
   *
   * @param fn The operation to apply to the success value.
   * @param <U> The new success type.
   * @return An {@link Ok} containing the mapped value, the original {@link Err}, or an {@link Err}
   *     containing the mapped exception.
   */
  default <U> Result<U> thenTrying(ThrowingFunction<? super T, ? extends U> fn) {
    return thenTrying(fn, DEFAULT_EXCEPTION_HANDLER);
  }

  /**
   * Maps the error value to a new error value using the provided mapper function. If this is an
   * {@link Ok}, the success is propagated unchanged.
   *
   * @param fn The function to apply to the error value.
   * @return A new Result containing the original success or the mapped error.
   */
  default Result<T> onError(Function<Cause, Result<T>> fn) {
    requireNonNull(fn);
    if (this instanceof Err(var reason)) {
      return requireNonNull(fn.apply(reason));
    }
    return this;
  }

  /**
   * Performs an action on the success value if present, then returns this Result. Useful for side
   * effects like logging or debugging.
   *
   * @param consumer The action to perform on the success value.
   * @return This Result instance.
   */
  default Result<T> peekOk(Consumer<? super T> consumer) {
    requireNonNull(consumer);
    if (this instanceof Ok(T value)) {
      consumer.accept(value);
    }
    return this;
  }

  /**
   * Performs an action on the error value if present, then returns this Result. Useful for side
   * effects like logging or debugging.
   *
   * @param consumer The action to perform on the error cause value.
   * @return This Result instance.
   */
  default Result<T> peekErr(Consumer<? super Cause> consumer) {
    requireNonNull(consumer);
    if (this instanceof Err(Cause value)) {
      consumer.accept(value);
    }
    return this;
  }

  /**
   * Consumes the success value if present; does nothing if this is an {@link Err}.
   *
   * @param consumer The action to perform on the success value.
   */
  default void ifOk(Consumer<? super T> consumer) {
    requireNonNull(consumer);
    if (this instanceof Ok(T value)) {
      consumer.accept(value);
    }
  }

  /**
   * Consumes the error value if present; does nothing if this is an {@link Ok}.
   *
   * @param consumer The action to perform on the error reason.
   */
  default void ifErr(Consumer<? super Cause> consumer) {
    requireNonNull(consumer);
    if (this instanceof Err(Cause cause)) {
      consumer.accept(cause);
    }
  }

  /**
   * Returns the success value if this is an {@link Ok}; otherwise, throws an {@link
   * IllegalStateException}.
   *
   * @return The success value.
   * @throws IllegalStateException if this Result is an {@link Err}.
   */
  default T unwrap() {
    return switch (this) {
      case Ok(var value) -> value;
      case Err(var error) -> throw new IllegalStateException("Cannot unwrap Err value: " + error);
    };
  }

  /**
   * Returns the success value if this is an {@link Ok}; otherwise, returns the provided fallback
   * value.
   *
   * @param fallback The value to return if this Result is an {@link Err}.
   * @return The success value or the fallback value.
   */
  default T unwrapOr(T fallback) {
    requireNonNull(fallback);
    return switch (this) {
      case Ok(var value) -> value;
      case Err<?> _ -> fallback;
    };
  }

  /**
   * Returns the success value if this is an {@link Ok}; otherwise, computes a fallback value from
   * the error.
   *
   * @param fallbackFn The function used to compute a fallback value from the error.
   * @return The success value or the computed fallback value.
   */
  default T unwrapOrElse(Function<Cause, ? extends T> fallbackFn) {
    requireNonNull(fallbackFn);
    return switch (this) {
      case Ok(var value) -> value;
      case Err(var reason) -> requireNonNull(fallbackFn.apply(reason));
    };
  }

  /**
   * Returns the success value if this is an {@link Ok}; otherwise, computes a fallback value from
   * the error.
   *
   * @param fallbackFn The function used to compute a fallback value from the error.
   * @return The success value or the computed fallback value.
   */
  default T unwrapOrElse(Supplier<? extends T> fallbackFn) {
    requireNonNull(fallbackFn);
    return switch (this) {
      case Ok(var value) -> value;
      case Err<?> _ -> requireNonNull(fallbackFn.get());
    };
  }

  /**
   * Returns the success value if this is an {@link Ok}; otherwise, throws an exception produced by
   * the provided function.
   *
   * @param fn The function to produce an exception from the error cause.
   * @param <X> The type of the exception to be thrown.
   * @return The success value.
   * @throws X if this Result is an {@link Err}.
   */
  default <X extends Throwable> T unwrapOrElseThrow(Function<Cause, ? extends X> fn) throws X {
    requireNonNull(fn);
    return switch (this) {
      case Ok(var value) -> value;
      case Err(var reason) -> throw fn.apply(reason);
    };
  }

  /**
   * Safely narrows a Result with wildcard types to a specific Result type.
   *
   * @param result The result to narrow.
   * @param <T> The target success type.
   * @return The narrowed Result.
   */
  @SuppressWarnings("unchecked")
  static <T> Result<T> narrow(Result<? extends T> result) {
    requireNonNull(result);
    return (Result<T>) result;
  }

  /**
   * Creates a successful Result containing the given value.
   *
   * @param value The success value. Must not be null and must not be another Result.
   * @param <T> The success type.
   * @return A successful Result.
   * @throws NullPointerException if value is null.
   * @throws IllegalArgumentException if value is a Result.
   */
  static <T> Result<T> ok(T value) {
    return new Ok<>(value);
  }

  /**
   * Creates a failed Result containing the given error.
   *
   * @param error The error value. Must not be null and must not be another Result.
   * @param <T> The success type.
   * @return A failed Result.
   * @throws NullPointerException if error is null.
   * @throws IllegalArgumentException if error is a Result.
   */
  static <T> Result<T> err(Cause error) {
    return new Err<>(error);
  }

  /**
   * Executes an operation and wraps its result in an {@link Ok}; if the operation throws an
   * exception, maps the exception into an error value and returns an {@link Err}.
   *
   * <p>Exceptions thrown while constructing the returned {@link Ok}, such as invariant violations
   * caused by returning {@code null} or another {@code Result}, are not mapped into {@link Err}.
   *
   * @param supplier The operation to execute.
   * @param <T> The success type.
   * @return An {@link Ok} containing the operation result, or an {@link Err} containing the mapped
   *     exception.
   */
  static <T> Result<T> trying(ThrowingSupplier<? extends T> supplier) {
    return trying(supplier, DEFAULT_EXCEPTION_HANDLER);
  }

  /**
   * Executes an operation and wraps its result in an {@link Ok}; if the operation throws an
   * exception, maps the exception into an error value and returns an {@link Err}.
   *
   * <p>Exceptions thrown while constructing the returned {@link Ok}, such as invariant violations
   * caused by returning {@code null} or another {@code Result}, are not mapped into {@link Err}.
   *
   * @param supplier The operation to execute.
   * @param exceptionMapper The function used to transform the thrown exception into an error value.
   * @param <T> The success type.
   * @return An {@link Ok} containing the operation result, or an {@link Err} containing the mapped
   *     exception.
   */
  static <T> Result<T> trying(
      ThrowingSupplier<? extends T> supplier,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    requireNonNull(supplier);
    requireNonNull(exceptionMapper);
    T result;
    try {
      result = supplier.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Execution interrupted", e);
    } catch (Exception e) {
      return err(exceptionMapper.apply(e));
    }
    return ok(result);
  }

  Function<? super Exception, ? extends Cause> DEFAULT_EXCEPTION_HANDLER = ExceptionCause::new;
}
