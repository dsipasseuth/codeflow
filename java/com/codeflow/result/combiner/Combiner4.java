package com.codeflow.result.combiner;

import static com.codeflow.result.Result.DEFAULT_EXCEPTION_HANDLER;
import static com.codeflow.result.Result.narrow;
import static com.codeflow.result.combiner.CombinerUtils.combine;
import static com.codeflow.result.combiner.CombinerUtils.combineResult;
import static java.util.Objects.requireNonNull;

import com.codeflow.result.Cause;
import com.codeflow.result.Functions.Consumer4;
import com.codeflow.result.Functions.Function4;
import com.codeflow.result.Functions.ThrowingFunction4;
import com.codeflow.result.Functions.ThrowingSupplier;
import com.codeflow.result.Result;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Holds four {@link Result} values and provides operations for combining their successful values.
 *
 * <p>Use the {@code andThen} methods to extend this combination with a fifth result, or use the
 * {@code then} methods to combine the four values into a single result. The {@code peekValues}
 * method performs an action with the four values while retaining this combiner.
 *
 * <p>The actual combination and result-handling behaviour is delegated to {@link CombinerUtils}.
 *
 * @param <T1> the value type of the first result
 * @param <T2> the value type of the second result
 * @param <T3> the value type of the third result
 * @param <T4> the value type of the fourth result
 * @param result1 the first result; must not be {@code null}
 * @param result2 the second result; must not be {@code null}
 * @param result3 the third result; must not be {@code null}
 * @param result4 the fourth result; must not be {@code null}
 */
public record Combiner4<T1, T2, T3, T4>(
    Result<T1> result1, Result<T2> result2, Result<T3> result3, Result<T4> result4) {

  /**
   * Creates a combiner for four non-null results.
   *
   * @throws NullPointerException if any result is {@code null}
   */
  public Combiner4 {
    requireNonNull(result1, "First result cannot be null");
    requireNonNull(result2, "Second result cannot be null");
    requireNonNull(result3, "Third result cannot be null");
    requireNonNull(result4, "Fourth result cannot be null");
  }

  /**
   * Extends this combination with a fifth value supplied independently of the four input values.
   *
   * @param fn supplier of the fifth value
   * @param <T5> the fifth value type
   * @return a combiner containing the original four results and the produced result
   */
  public <T5> Combiner5<T1, T2, T3, T4, T5> andThen(Supplier<? extends T5> fn) {
    requireNonNull(fn);
    return new Combiner5<>(result1, result2, result3, result4, then((_, _, _, _) -> fn.get()));
  }

  /**
   * Extends this combination with a fifth value produced from the four successful input values.
   *
   * @param fn function that combines the four values into a fifth value
   * @param <T5> the fifth value type
   * @return a combiner containing the original four results and the produced result
   */
  public <T5> Combiner5<T1, T2, T3, T4, T5> andThen(
      Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends T5> fn) {
    return new Combiner5<>(result1, result2, result3, result4, then(fn));
  }

  /**
   * Extends this combination with a fifth result supplied independently of the four input values.
   *
   * @param fn supplier of the fifth result
   * @param <T5> the fifth value type
   * @return a combiner containing the original four results and the supplied result
   */
  public <T5> Combiner5<T1, T2, T3, T4, T5> andThenApply(Supplier<Result<? extends T5>> fn) {
    requireNonNull(fn);
    return new Combiner5<>(
        result1, result2, result3, result4, thenApply((_, _, _, _) -> narrow(fn.get())));
  }

  /**
   * Extends this combination with a fifth result returned by a function of the four input values.
   *
   * @param fn function that combines the four values into a result
   * @param <T5> the fifth value type
   * @return a combiner containing the original four results and the returned result
   */
  public <T5> Combiner5<T1, T2, T3, T4, T5> andThenApply(
      Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends Result<? extends T5>>
          fn) {
    return new Combiner5<>(result1, result2, result3, result4, thenApply(fn));
  }

  /**
   * Extends this combination with a fifth value produced by a supplier that may throw an exception.
   *
   * <p>Thrown exceptions are handled by the default exception handler.
   *
   * @param fn supplier of the fifth value
   * @param <T5> the fifth value type
   * @return a combiner containing the original four results and the produced result
   */
  public <T5> Combiner5<T1, T2, T3, T4, T5> andThenTrying(ThrowingSupplier<? extends T5> fn) {
    requireNonNull(fn);
    return new Combiner5<>(
        result1, result2, result3, result4, thenTrying((_, _, _, _) -> fn.get()));
  }

  /**
   * Extends this combination with a fifth value produced by a supplier that may throw an exception.
   *
   * @param fn supplier of the fifth value
   * @param exceptionMapper maps a caught exception to a {@link Cause}
   * @param <T5> the fifth value type
   * @return a combiner containing the original four results and the produced result
   */
  public <T5> Combiner5<T1, T2, T3, T4, T5> andThenTrying(
      ThrowingSupplier<? extends T5> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    requireNonNull(fn);
    requireNonNull(exceptionMapper);
    return new Combiner5<>(
        result1, result2, result3, result4, thenTrying((_, _, _, _) -> fn.get(), exceptionMapper));
  }

  /**
   * Extends this combination with a fifth value produced from the four input values by a function
   * that may throw an exception.
   *
   * <p>Thrown exceptions are handled by the default exception handler.
   *
   * @param fn function that combines the four values into a fifth value
   * @param <T5> the fifth value type
   * @return a combiner containing the original four results and the produced result
   */
  public <T5> Combiner5<T1, T2, T3, T4, T5> andThenTrying(
      ThrowingFunction4<? super T1, ? super T2, ? super T3, ? super T4, ? extends T5> fn) {
    return new Combiner5<>(result1, result2, result3, result4, thenTrying(fn));
  }

  /**
   * Extends this combination with a fifth value produced from the four input values by a function
   * that may throw an exception.
   *
   * @param fn function that combines the four values into a fifth value
   * @param exceptionMapper maps a caught exception to a {@link Cause}
   * @param <T5> the fifth value type
   * @return a combiner containing the original four results and the produced result
   */
  public <T5> Combiner5<T1, T2, T3, T4, T5> andThenTrying(
      ThrowingFunction4<? super T1, ? super T2, ? super T3, ? super T4, ? extends T5> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    return new Combiner5<>(result1, result2, result3, result4, thenTrying(fn, exceptionMapper));
  }

  /**
   * Performs an action with the four values and returns this combiner.
   *
   * <p>The action is invoked only when all four results are {@code Ok}. If any result is not
   * successful, the action is skipped.
   *
   * @param fn action to perform with the four values
   * @return this combiner
   */
  public Combiner4<T1, T2, T3, T4> peekValues(
      Consumer4<? super T1, ? super T2, ? super T3, ? super T4> fn) {
    requireNonNull(fn);
    if (result1 instanceof Result.Ok(var v1)
        && result2 instanceof Result.Ok(var v2)
        && result3 instanceof Result.Ok(var v3)
        && result4 instanceof Result.Ok(var v4)) {
      fn.accept(v1, v2, v3, v4);
    }
    return this;
  }

  /**
   * Combines the four results into one result using a value-producing function.
   *
   * @param fn function applied to the four values
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> then(
      Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
    return combine(result1, result2, result3, result4, fn);
  }

  /**
   * Combines the four results into one result using a function that returns a {@link Result}.
   *
   * <p>The callback supplies the result wrapper itself, rather than a plain value.
   *
   * @param fn function applied to the four values, returning a result
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> thenApply(
      Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends Result<? extends R>> fn) {
    return combineResult(result1, result2, result3, result4, fn);
  }

  /**
   * Combines the four results using a function that may throw an exception.
   *
   * <p>Thrown exceptions are handled by the default exception handler.
   *
   * @param fn function applied to the four values
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> thenTrying(
      ThrowingFunction4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
    return combine(result1, result2, result3, result4, fn, DEFAULT_EXCEPTION_HANDLER);
  }

  /**
   * Combines the four results using a function that may throw an exception, with a custom
   * exception-to-cause mapper.
   *
   * @param fn function applied to the four values
   * @param exceptionMapper maps a caught exception to a {@link Cause}
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> thenTrying(
      ThrowingFunction4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    return combine(result1, result2, result3, result4, fn, exceptionMapper);
  }
}
