package com.codeflow.result.combiner;

import static com.codeflow.result.Result.narrow;
import static com.codeflow.result.Result.ok;
import static com.codeflow.result.Result.trying;
import static java.util.Objects.requireNonNull;

import com.codeflow.result.Cause;
import com.codeflow.result.Functions.ThrowingFunction;
import com.codeflow.result.Functions.ThrowingSupplier;
import com.codeflow.result.Result;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Starts a two-stage combination of {@link Result} values.
 *
 * <p>The combiner retains an initial result and provides operations for producing a second result.
 * Each operation short-circuits when the initial result is an error: the second result receives the
 * same error and the supplied function or supplier is not invoked.
 *
 * @param <T> the success-value type of the initial result
 * @param initialValue the initial result; must not be {@code null}
 */
public record Combiner<T>(Result<T> initialValue) {

  /**
   * Validates the initial result.
   *
   * @throws NullPointerException if {@code initialValue} is {@code null}
   */
  public Combiner {
    requireNonNull(initialValue, "Initial value of combiner cannot be null.");
  }

  /**
   * Creates a combiner from an initial result.
   *
   * @param initialValue the initial result; must not be {@code null}
   * @param <T> the success-value type
   * @return a new combiner
   * @throws NullPointerException if {@code initialValue} is {@code null}
   */
  public static <T> Combiner<T> begin(Result<? extends T> initialValue) {
    return new Combiner<>(narrow(initialValue));
  }

  /**
   * Produces the second result by invoking a supplier if the initial result is successful.
   *
   * <p>If the initial result is an error, its error is propagated and the supplier is not invoked.
   *
   * @param fn supplier of the second value
   * @param <O> the second success-value type
   * @return a two-result combiner
   */
  public <O> Combiner2<T, O> andThen(Supplier<? extends O> fn) {
    requireNonNull(fn);
    return switch (initialValue) {
      case Result.Ok<T> value -> new Combiner2<>(value, ok(fn.get()));
      case Result.Err<T> err -> new Combiner2<>(err, err.into());
    };
  }

  /**
   * Maps the initial success value to the second result.
   *
   * <p>If the initial result is an error, that error is propagated without invoking the function.
   *
   * @param fn function applied to the initial success value
   * @param <O> the second success-value type
   * @return a two-result combiner
   */
  public <O> Combiner2<T, O> andThen(Function<? super T, ? extends O> fn) {
    return new Combiner2<>(initialValue, initialValue.then(fn));
  }

  /**
   * Produces the second result by invoking a supplier if the initial result is successful.
   *
   * <p>If the initial result is an error, its error is propagated and the supplier is not invoked.
   *
   * @param fn supplier of the second result
   * @param <O> the second success-value type
   * @return a two-result combiner
   */
  public <O> Combiner2<T, O> andThenApply(Supplier<? extends Result<? extends O>> fn) {
    requireNonNull(fn);
    return switch (initialValue) {
      case Result.Ok<T> value -> new Combiner2<>(value, narrow(fn.get()));
      case Result.Err<T> err -> new Combiner2<>(err, err.into());
    };
  }

  /**
   * Maps the initial success value to another {@link Result}.
   *
   * <p>If the initial result is an error, that error is propagated without invoking the function.
   *
   * @param fn function that returns the second result
   * @param <O> the second success-value type
   * @return a two-result combiner
   */
  public <O> Combiner2<T, O> andThenApply(Function<? super T, ? extends Result<? extends O>> fn) {
    return new Combiner2<>(initialValue, initialValue.thenApply(fn));
  }

  /**
   * Produces the second result by invoking a potentially throwing supplier if the initial result is
   * successful.
   *
   * <p>If the initial result is an error, its error is propagated and the supplier is not invoked.
   * Exceptions thrown by the supplier are handled by {@link Result#trying(ThrowingSupplier)}.
   *
   * @param fn potentially throwing supplier
   * @param <O> the second success-value type
   * @return a two-result combiner
   */
  public <O> Combiner2<T, O> andThenTrying(ThrowingSupplier<? extends O> fn) {
    requireNonNull(fn);
    return switch (initialValue) {
      case Result.Ok<T> value -> new Combiner2<>(value, trying(fn));
      case Result.Err<T> err -> new Combiner2<>(err, err.into());
    };
  }

  /**
   * Produces the second result by invoking a potentially throwing supplier if the initial result is
   * successful.
   *
   * <p>If the initial result is an error, its error is propagated and the supplier is not invoked.
   * Exceptions thrown by the supplier are handled by {@link Result#trying(ThrowingSupplier)}.
   *
   * @param fn potentially throwing supplier
   * @param <O> the second success-value type
   * @return a two-result combiner
   */
  public <O> Combiner2<T, O> andThenTrying(
      ThrowingSupplier<? extends O> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    requireNonNull(fn);
    requireNonNull(exceptionMapper);
    return switch (initialValue) {
      case Result.Ok<T> value -> new Combiner2<>(value, trying(fn, exceptionMapper));
      case Result.Err<T> err -> new Combiner2<>(err, err.into());
    };
  }

  /**
   * Applies an operation that may throw an exception to the initial success value.
   *
   * <p>If the initial result is an error, that error is propagated without invoking the operation.
   * Exceptions thrown by the operation are handled according to {@link Result#thenTrying}.
   *
   * @param fn potentially throwing operation
   * @param <O> the second success-value type
   * @return a two-result combiner
   */
  public <O> Combiner2<T, O> andThenTrying(ThrowingFunction<T, ? extends O> fn) {
    return new Combiner2<>(initialValue, initialValue.thenTrying(fn));
  }

  /**
   * Applies a potentially throwing operation to the initial success value, using an exception
   * mapper.
   *
   * <p>If the initial result is an error, that error is propagated without invoking the operation.
   *
   * @param fn potentially throwing operation
   * @param exceptionMapper converts a thrown exception into a {@link Cause}
   * @param <O> the second success-value type
   * @return a two-result combiner
   */
  public <O> Combiner2<T, O> andThenTrying(
      ThrowingFunction<T, ? extends O> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    requireNonNull(fn);
    requireNonNull(exceptionMapper);
    return switch (initialValue) {
      case Result.Ok<T> value -> new Combiner2<>(value, value.thenTrying(fn, exceptionMapper));
      case Result.Err<T> err -> new Combiner2<>(err, err.into());
    };
  }
}
