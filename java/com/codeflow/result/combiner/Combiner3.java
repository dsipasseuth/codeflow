package com.codeflow.result.combiner;

import static com.codeflow.result.Result.DEFAULT_EXCEPTION_HANDLER;
import static com.codeflow.result.Result.narrow;
import static com.codeflow.result.combiner.CombinerUtils.combine;
import static com.codeflow.result.combiner.CombinerUtils.combineResult;
import static java.util.Objects.requireNonNull;

import com.codeflow.result.Cause;
import com.codeflow.result.Functions.Consumer3;
import com.codeflow.result.Functions.Function3;
import com.codeflow.result.Functions.ThrowingFunction3;
import com.codeflow.result.Functions.ThrowingSupplier;
import com.codeflow.result.Result;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Holds three {@link Result} values and provides operations for combining their successful values.
 *
 * <p>Use the {@code andThen} methods to extend this combination with a fourth result, or use the
 * {@code then} methods to combine the three values into a single result. The {@code also} method
 * performs an action with the three values while retaining this combiner.
 *
 * <p>The actual combination and result-handling behaviour is delegated to {@link CombinerUtils}.
 *
 * @param <T1> the value type of the first result
 * @param <T2> the value type of the second result
 * @param <T3> the value type of the third result
 * @param result1 the first result; must not be {@code null}
 * @param result2 the second result; must not be {@code null}
 * @param result3 the third result; must not be {@code null}
 */
public record Combiner3<T1, T2, T3>(Result<T1> result1, Result<T2> result2, Result<T3> result3) {

  /**
   * Creates a combiner for three non-null results.
   *
   * @throws NullPointerException if any result is {@code null}
   */
  public Combiner3 {
    requireNonNull(result1, "First result cannot be null");
    requireNonNull(result2, "Second result cannot be null");
    requireNonNull(result3, "Third result cannot be null");
  }

  /**
   * Extends this combination with a fourth value supplied independently of the three input values.
   *
   * @param fn supplier of the fourth value
   * @param <T4> the fourth value type
   * @return a combiner containing the original three results and the produced result
   */
  public <T4> Combiner4<T1, T2, T3, T4> andThen(Supplier<? extends T4> fn) {
    requireNonNull(fn);
    return new Combiner4<>(result1, result2, result3, then((_, _, _) -> fn.get()));
  }

  /**
   * Extends this combination with a fourth value produced from the three successful input values.
   *
   * @param fn function that combines the three values into a fourth value
   * @param <T4> the fourth value type
   * @return a combiner containing the original three results and the produced result
   */
  public <T4> Combiner4<T1, T2, T3, T4> andThen(
      Function3<? super T1, ? super T2, ? super T3, ? extends T4> fn) {
    return new Combiner4<>(result1, result2, result3, then(fn));
  }

  /**
   * Extends this combination with a fourth result supplied independently of the three input values.
   *
   * @param fn supplier of the fourth result
   * @param <T4> the fourth value type
   * @return a combiner containing the original three results and the supplied result
   */
  public <T4> Combiner4<T1, T2, T3, T4> andThenApply(Supplier<Result<? extends T4>> fn) {
    requireNonNull(fn);
    return new Combiner4<>(result1, result2, result3, thenApply((_, _, _) -> narrow(fn.get())));
  }

  /**
   * Extends this combination with a fourth result returned by a function of the three input values.
   *
   * @param fn function that combines the three values into a result
   * @param <T4> the fourth value type
   * @return a combiner containing the original three results and the returned result
   */
  public <T4> Combiner4<T1, T2, T3, T4> andThenApply(
      Function3<? super T1, ? super T2, ? super T3, ? extends Result<? extends T4>> fn) {
    return new Combiner4<>(result1, result2, result3, thenApply(fn));
  }

  /**
   * Extends this combination with a fourth value produced by a supplier that may throw an
   * exception.
   *
   * <p>Thrown exceptions are handled by the default exception handler.
   *
   * @param fn supplier of the fourth value
   * @param <T4> the fourth value type
   * @return a combiner containing the original three results and the produced result
   */
  public <T4> Combiner4<T1, T2, T3, T4> andThenTrying(ThrowingSupplier<? extends T4> fn) {
    requireNonNull(fn);
    return new Combiner4<>(result1, result2, result3, thenTrying((_, _, _) -> fn.get()));
  }

  /**
   * Extends this combination with a fourth value produced by a supplier that may throw an
   * exception.
   *
   * @param fn supplier of the fourth value
   * @param exceptionMapper maps a caught exception to a {@link Cause}
   * @param <T4> the fourth value type
   * @return a combiner containing the original three results and the produced result
   */
  public <T4> Combiner4<T1, T2, T3, T4> andThenTrying(
      ThrowingSupplier<? extends T4> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    requireNonNull(fn);
    requireNonNull(exceptionMapper);
    return new Combiner4<>(
        result1, result2, result3, thenTrying((_, _, _) -> fn.get(), exceptionMapper));
  }

  /**
   * Extends this combination with a fourth value produced from the three input values by a function
   * that may throw an exception.
   *
   * <p>Thrown exceptions are handled by the default exception handler.
   *
   * @param fn function that combines the three values into a fourth value
   * @param <T4> the fourth value type
   * @return a combiner containing the original three results and the produced result
   */
  public <T4> Combiner4<T1, T2, T3, T4> andThenTrying(
      ThrowingFunction3<? super T1, ? super T2, ? super T3, ? extends T4> fn) {
    return new Combiner4<>(result1, result2, result3, thenTrying(fn));
  }

  /**
   * Extends this combination with a fourth value produced from the three input values by a function
   * that may throw an exception.
   *
   * @param fn function that combines the three values into a fourth value
   * @param exceptionMapper maps a caught exception to a {@link Cause}
   * @param <T4> the fourth value type
   * @return a combiner containing the original three results and the produced result
   */
  public <T4> Combiner4<T1, T2, T3, T4> andThenTrying(
      ThrowingFunction3<? super T1, ? super T2, ? super T3, ? extends T4> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    return new Combiner4<>(result1, result2, result3, thenTrying(fn, exceptionMapper));
  }

  /**
   * Performs an action with the three values and returns this combiner.
   *
   * <p>The action is invoked only when all three results are {@code Ok}. If any result is not
   * successful, the action is skipped.
   *
   * @param fn action to perform with the three values
   * @return this combiner
   */
  public Combiner3<T1, T2, T3> also(Consumer3<? super T1, ? super T2, ? super T3> fn) {
    requireNonNull(fn);
    if (result1 instanceof Result.Ok(var v1)
        && result2 instanceof Result.Ok(var v2)
        && result3 instanceof Result.Ok(var v3)) {
      fn.accept(v1, v2, v3);
    }
    return this;
  }

  /**
   * Combines the three results into one result using a value-producing function.
   *
   * @param fn function applied to the three values
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> then(Function3<? super T1, ? super T2, ? super T3, ? extends R> fn) {
    return combine(result1, result2, result3, fn);
  }

  /**
   * Combines the three results into one result using a function that returns a {@link Result}.
   *
   * <p>The callback supplies the result wrapper itself, rather than a plain value.
   *
   * @param fn function applied to the three values, returning a result
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> thenApply(
      Function3<? super T1, ? super T2, ? super T3, ? extends Result<? extends R>> fn) {
    return combineResult(result1, result2, result3, fn);
  }

  /**
   * Combines the three results using a function that may throw an exception.
   *
   * <p>Thrown exceptions are handled by the default exception handler.
   *
   * @param fn function applied to the three values
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> thenTrying(
      ThrowingFunction3<? super T1, ? super T2, ? super T3, ? extends R> fn) {
    return combine(result1, result2, result3, fn, DEFAULT_EXCEPTION_HANDLER);
  }

  /**
   * Combines the three results using a function that may throw an exception, with a custom
   * exception-to-cause mapper.
   *
   * @param fn function applied to the three values
   * @param exceptionMapper maps a caught exception to a {@link Cause}
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> thenTrying(
      ThrowingFunction3<? super T1, ? super T2, ? super T3, ? extends R> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    return combine(result1, result2, result3, fn, exceptionMapper);
  }
}
