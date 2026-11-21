package com.codeflow.result.combiner;

import static com.codeflow.result.Result.DEFAULT_EXCEPTION_HANDLER;
import static com.codeflow.result.Result.narrow;
import static com.codeflow.result.combiner.CombinerUtils.combine;
import static com.codeflow.result.combiner.CombinerUtils.combineResult;
import static java.util.Objects.requireNonNull;

import com.codeflow.result.Cause;
import com.codeflow.result.Functions.ThrowingBiFunction;
import com.codeflow.result.Functions.ThrowingSupplier;
import com.codeflow.result.Result;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Holds two {@link Result} values and provides operations for combining their successful values.
 *
 * <p>Use {@code andThen} methods to extend this pair with a third result, or use {@code then}
 * methods to combine the pair into a single result. The {@code also} method allows a side effect to
 * be performed on the pair while retaining this combiner.
 *
 * <p>Combination and failure propagation are delegated to {@link CombinerUtils}.
 *
 * @param <T1> the value type of the first result
 * @param <T2> the value type of the second result
 * @param result1 the first result; must not be {@code null}
 * @param result2 the second result; must not be {@code null}
 */
public record Combiner2<T1, T2>(Result<T1> result1, Result<T2> result2) {

  /**
   * Creates a combiner for two non-null results.
   *
   * @throws NullPointerException if either result is {@code null}
   */
  public Combiner2 {
    requireNonNull(result1, "First result cannot be null");
    requireNonNull(result2, "Second result cannot be null");
  }

  /**
   * Extends this pair with a third result produced by a supplier.
   *
   * <p>The supplier does not receive either input value.
   *
   * @param fn supplier of the third value
   * @param <T3> the third value type
   * @return a combiner containing the original two results and the produced result
   */
  public <T3> Combiner3<T1, T2, T3> andThen(Supplier<? extends T3> fn) {
    requireNonNull(fn);
    return new Combiner3<>(result1, result2, then((_, _) -> fn.get()));
  }

  /**
   * Extends this pair with a third result produced from both input values.
   *
   * @param fn function that combines the two values into a third value
   * @param <T3> the third value type
   * @return a combiner containing the original two results and the produced result
   */
  public <T3> Combiner3<T1, T2, T3> andThen(BiFunction<? super T1, ? super T2, ? extends T3> fn) {
    return new Combiner3<>(result1, result2, then(fn));
  }

  /**
   * Extends this pair with a third result supplied as a {@link Result}.
   *
   * <p>The supplier does not receive either input value.
   *
   * @param fn supplier of the third result
   * @param <T3> the third value type
   * @return a combiner containing the original two results and the supplied result
   */
  public <T3> Combiner3<T1, T2, T3> andThenApply(Supplier<Result<? extends T3>> fn) {
    requireNonNull(fn);
    return new Combiner3<>(result1, result2, thenApply((_, _) -> narrow(fn.get())));
  }

  /**
   * Extends this pair with a third result returned by a function of both values.
   *
   * @param fn function that combines the two values into a result
   * @param <T3> the third value type
   * @return a combiner containing the original two results and the returned result
   */
  public <T3> Combiner3<T1, T2, T3> andThenApply(
      BiFunction<? super T1, ? super T2, ? extends Result<? extends T3>> fn) {
    return new Combiner3<>(result1, result2, thenApply(fn));
  }

  /**
   * Extends this pair with a third value produced by a supplier that may throw.
   *
   * <p>Thrown exceptions are handled by the default exception handler.
   *
   * @param fn supplier of the third value
   * @param <T3> the third value type
   * @return a combiner containing the original two results and the produced result
   */
  public <T3> Combiner3<T1, T2, T3> andThenTrying(ThrowingSupplier<? extends T3> fn) {
    requireNonNull(fn);
    return new Combiner3<>(result1, result2, thenTrying((_, _) -> fn.get()));
  }

  /**
   * Extends this pair with a third value produced by a supplier that may throw.
   *
   * @param fn supplier of the third value
   * @param exceptionMapper maps a caught exception to a {@link Cause}
   * @param <T3> the third value type
   * @return a combiner containing the original two results and the produced result
   */
  public <T3> Combiner3<T1, T2, T3> andThenTrying(
      ThrowingSupplier<? extends T3> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    requireNonNull(fn);
    requireNonNull(exceptionMapper);
    return new Combiner3<>(result1, result2, thenTrying((_, _) -> fn.get(), exceptionMapper));
  }

  /**
   * Extends this pair with a third value produced from both inputs by a function that may throw.
   *
   * <p>Thrown exceptions are handled by the default exception handler.
   *
   * @param fn function that combines the two values into a third value
   * @param <T3> the third value type
   * @return a combiner containing the original two results and the produced result
   */
  public <T3> Combiner3<T1, T2, T3> andThenTrying(
      ThrowingBiFunction<? super T1, ? super T2, ? extends T3> fn) {
    return new Combiner3<>(result1, result2, thenTrying(fn));
  }

  /**
   * Extends this pair with a third value produced from both inputs by a function that may throw.
   *
   * @param fn function that combines the two values into a third value
   * @param exceptionMapper maps a caught exception to a {@link Cause}
   * @param <T3> the third value type
   * @return a combiner containing the original two results and the produced result
   */
  public <T3> Combiner3<T1, T2, T3> andThenTrying(
      ThrowingBiFunction<? super T1, ? super T2, ? extends T3> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    return new Combiner3<>(result1, result2, thenTrying(fn, exceptionMapper));
  }

  /**
   * Performs an action with both successful values and returns this combiner.
   *
   * <p>The action is invoked only when both results are {@code Ok}. If either result is not
   * successful, the action is skipped.
   *
   * @param fn action to perform with the two values
   * @return this combiner
   */
  public Combiner2<T1, T2> also(BiConsumer<? super T1, ? super T2> fn) {
    requireNonNull(fn);
    if (result1 instanceof Result.Ok(var v1) && result2 instanceof Result.Ok(var v2)) {
      fn.accept(v1, v2);
    }
    return this;
  }

  /**
   * Combines the two results into one result using a value-producing function.
   *
   * @param fn function applied to the two values
   * @param <T3> the resulting value type
   * @return the combined result
   */
  public <T3> Result<T3> then(BiFunction<? super T1, ? super T2, ? extends T3> fn) {
    return combine(result1, result2, fn);
  }

  /**
   * Combines the two results into one result using a function that returns a {@link Result}.
   *
   * <p>Unlike {@link #then(BiFunction)}, the callback supplies the result wrapper itself.
   *
   * @param fn function applied to the two values, returning a result
   * @param <T3> the resulting value type
   * @return the combined result
   */
  public <T3> Result<T3> thenApply(
      BiFunction<? super T1, ? super T2, ? extends Result<? extends T3>> fn) {
    return combineResult(result1, result2, fn);
  }

  /**
   * Combines the two results using a function that may throw.
   *
   * <p>Thrown exceptions are handled by the default exception handler.
   *
   * @param fn function applied to the two values
   * @param <T3> the resulting value type
   * @return the combined result
   */
  public <T3> Result<T3> thenTrying(ThrowingBiFunction<? super T1, ? super T2, ? extends T3> fn) {
    return combine(result1, result2, fn, DEFAULT_EXCEPTION_HANDLER);
  }

  /**
   * Combines the two results using a function that may throw, with a custom exception-to-cause
   * mapper.
   *
   * @param fn function applied to the two values
   * @param exceptionMapper maps a caught exception to a {@link Cause}
   * @param <T3> the resulting value type
   * @return the combined result
   */
  public <T3> Result<T3> thenTrying(
      ThrowingBiFunction<? super T1, ? super T2, ? extends T3> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    return combine(result1, result2, fn, exceptionMapper);
  }
}
