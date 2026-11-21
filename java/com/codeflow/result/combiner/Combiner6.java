package com.codeflow.result.combiner;

import static com.codeflow.result.Result.DEFAULT_EXCEPTION_HANDLER;
import static com.codeflow.result.combiner.CombinerUtils.combine;
import static com.codeflow.result.combiner.CombinerUtils.combineResult;
import static java.util.Objects.requireNonNull;

import com.codeflow.result.Cause;
import com.codeflow.result.Functions.Consumer6;
import com.codeflow.result.Functions.Function6;
import com.codeflow.result.Functions.ThrowingFunction6;
import com.codeflow.result.Result;
import java.util.function.Function;

/**
 * Holds six {@link Result} values and provides operations for combining their successful values.
 *
 * <p>As the maximum arity combiner, {@code Combiner6} provides terminal operations via the {@code
 * then} methods to combine the six values into a single result, or the {@code peekOk} method to
 * perform a side-effect action.
 *
 * <p>The actual combination and result-handling behaviour is delegated to {@link CombinerUtils}.
 *
 * @param <T1> the value type of the first result
 * @param <T2> the value type of the second result
 * @param <T3> the value type of the third result
 * @param <T4> the value type of the fourth result
 * @param <T5> the value type of the fifth result
 * @param <T6> the value type of the sixth result
 * @param result1 the first result; must not be {@code null}
 * @param result2 the second result; must not be {@code null}
 * @param result3 the third result; must not be {@code null}
 * @param result4 the fourth result; must not be {@code null}
 * @param result5 the fifth result; must not be {@code null}
 * @param result6 the sixth result; must not be {@code null}
 */
public record Combiner6<T1, T2, T3, T4, T5, T6>(
    Result<T1> result1,
    Result<T2> result2,
    Result<T3> result3,
    Result<T4> result4,
    Result<T5> result5,
    Result<T6> result6) {

  /**
   * Creates a combiner for six non-null results.
   *
   * @throws NullPointerException if any result is {@code null}
   */
  public Combiner6 {
    requireNonNull(result1, "First result cannot be null");
    requireNonNull(result2, "Second result cannot be null");
    requireNonNull(result3, "Third result cannot be null");
    requireNonNull(result4, "Fourth result cannot be null");
    requireNonNull(result5, "Fifth result cannot be null");
    requireNonNull(result6, "Sixth result cannot be null");
  }

  /**
   * Performs an action with the six values and returns this combiner.
   *
   * <p>The action is invoked only when all six results are {@code Ok}. If any result is not
   * successful, the action is skipped.
   *
   * @param consumer action to perform with the six values; must not be {@code null}
   * @return this combiner
   * @throws NullPointerException if {@code consumer} is {@code null}
   */
  public Combiner6<T1, T2, T3, T4, T5, T6> peekOk(
      Consumer6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6> consumer) {
    requireNonNull(consumer);
    if (result1 instanceof Result.Ok(var v1)
        && result2 instanceof Result.Ok(var v2)
        && result3 instanceof Result.Ok(var v3)
        && result4 instanceof Result.Ok(var v4)
        && result5 instanceof Result.Ok(var v5)
        && result6 instanceof Result.Ok(var v6)) {
      consumer.accept(v1, v2, v3, v4, v5, v6);
    }
    return this;
  }

  /**
   * Combines the six results into one result using a value-producing function.
   *
   * @param fn function applied to the six values
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> then(
      Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R>
          fn) {
    return combine(result1, result2, result3, result4, result5, result6, fn);
  }

  /**
   * Combines the six results into one result using a function that returns a {@link Result}.
   *
   * <p>The callback supplies the result wrapper itself, rather than a plain value.
   *
   * @param fn function applied to the six values, returning a result
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> thenApply(
      Function6<
              ? super T1,
              ? super T2,
              ? super T3,
              ? super T4,
              ? super T5,
              ? super T6,
              ? extends Result<? extends R>>
          fn) {
    return combineResult(result1, result2, result3, result4, result5, result6, fn);
  }

  /**
   * Combines the six results using a function that may throw an exception.
   *
   * <p>Thrown exceptions are handled by the default exception handler.
   *
   * @param fn function applied to the six values
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> thenTrying(
      ThrowingFunction6<
              ? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R>
          fn) {
    return combine(
        result1, result2, result3, result4, result5, result6, fn, DEFAULT_EXCEPTION_HANDLER);
  }

  /**
   * Combines the six results using a function that may throw an exception, with a custom
   * exception-to-cause mapper.
   *
   * @param fn function applied to the six values
   * @param exceptionMapper maps a caught exception to a {@link Cause}
   * @param <R> the resulting value type
   * @return the combined result
   */
  public <R> Result<R> thenTrying(
      ThrowingFunction6<
              ? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R>
          fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    return combine(result1, result2, result3, result4, result5, result6, fn, exceptionMapper);
  }
}
