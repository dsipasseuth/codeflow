package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static com.codeflow.result.combiner.Combiner.begin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeflow.result.ExceptionCause;
import com.codeflow.result.Functions.ThrowingFunction;
import com.codeflow.result.Functions.ThrowingSupplier;
import com.codeflow.result.Result;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class CombinerTest {

  // --- Constructor & Validation Tests ---

  @Test
  void shouldThrowNullPointerExceptionWhenInitialResultIsNull() {
    assertThatThrownBy(() -> new Combiner<>(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Initial value of combiner cannot be null.");
  }

  // --- Begin Factory Tests ---

  @Test
  void shouldBeginWithResult() {
    Combiner<String> combiner = begin(ok("initial"));
    assertThat(combiner.initialValue()).isEqualTo(ok("initial"));
  }

  @Test
  void shouldThrowNullPointerExceptionWhenBeginInitialResultIsNull() {
    assertThatThrownBy(() -> begin(null)).isInstanceOf(NullPointerException.class);
  }

  // --- andThen (Supplier & Function) Tests ---

  @Test
  void shouldExecuteAndThenSupplierWhenOk() {
    Combiner2<String, Integer> result = begin(ok("hello")).andThen(() -> 42);

    assertThat(result.result1()).isEqualTo(ok("hello"));
    assertThat(result.result2()).isEqualTo(ok(42));
  }

  @Test
  void shouldShortCircuitAndThenSupplierWhenErr() {
    boolean[] invoked = {false};
    Result<String> initial = err(new ExceptionCause(new RuntimeException("fail")));

    Combiner2<String, Integer> result =
        begin(initial)
            .andThen(
                () -> {
                  invoked[0] = true;
                  return 42;
                });

    assertThat(result.result1()).isEqualTo(initial);
    assertThat(result.result2()).isInstanceOf(Result.Err.class);
    assertThat(invoked[0]).isFalse(); // Supplier should not be executed
  }

  @Test
  void shouldRejectNullSupplierInAndThenConsistentlyOnOkAndErr() {
    Result<String> initialErr = err(new ExceptionCause(new RuntimeException("fail")));

    assertThatThrownBy(() -> begin(ok("hello")).andThen((Supplier<Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> begin(initialErr).andThen((Supplier<Integer>) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldExecuteAndThenFunctionWhenOk() {
    Combiner2<String, Integer> result = begin(ok("hello")).andThen(String::length);

    assertThat(result.result1()).isEqualTo(ok("hello"));
    assertThat(result.result2()).isEqualTo(ok(5));
  }

  @Test
  void shouldShortCircuitAndThenFunctionWhenErr() {
    boolean[] invoked = {false};
    Result<String> initial = err(new ExceptionCause(new RuntimeException("fail")));

    Combiner2<String, Integer> result =
        begin(initial)
            .andThen(
                s -> {
                  invoked[0] = true;
                  return s.length();
                });

    assertThat(result.result1()).isEqualTo(initial);
    assertThat(result.result2()).isInstanceOf(Result.Err.class);
    assertThat(invoked[0]).isFalse();
  }

  @Test
  void shouldRejectNullFunctionInAndThenConsistentlyOnOkAndErr() {
    Result<String> initialErr = err(new ExceptionCause(new RuntimeException("fail")));

    assertThatThrownBy(() -> begin(ok("hello")).andThen((Function<String, Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> begin(initialErr).andThen((Function<String, Integer>) null))
        .isInstanceOf(NullPointerException.class);
  }

  // --- andThenApply Tests ---

  @Test
  void shouldExecuteAndThenApplySupplierWhenOk() {
    Combiner2<String, Integer> result = begin(ok("hello")).andThenApply(() -> ok(100));

    assertThat(result.result1()).isEqualTo(ok("hello"));
    assertThat(result.result2()).isEqualTo(ok(100));
  }

  @Test
  void shouldShortCircuitAndThenApplySupplierWhenErr() {
    boolean[] invoked = {false};
    Result<String> initial = err(new ExceptionCause(new RuntimeException("fail")));

    Combiner2<String, Integer> result =
        begin(initial)
            .andThenApply(
                () -> {
                  invoked[0] = true;
                  return ok(100);
                });

    assertThat(result.result1()).isEqualTo(initial);
    assertThat(result.result2()).isInstanceOf(Result.Err.class);
    assertThat(invoked[0]).isFalse();
  }

  @Test
  void shouldRejectNullSupplierInAndThenApplyConsistentlyOnOkAndErr() {
    Result<String> initialErr = err(new ExceptionCause(new RuntimeException("fail")));

    assertThatThrownBy(() -> begin(ok("hello")).andThenApply((Supplier<Result<Integer>>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> begin(initialErr).andThenApply((Supplier<Result<Integer>>) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldExecuteAndThenApplyFunctionWhenOk() {
    Combiner2<String, Integer> result = begin(ok("hello")).andThenApply(s -> ok(s.length()));

    assertThat(result.result1()).isEqualTo(ok("hello"));
    assertThat(result.result2()).isEqualTo(ok(5));
  }

  @Test
  void shouldShortCircuitAndThenApplyFunctionWhenErr() {
    boolean[] invoked = {false};
    Result<String> initial = err(new ExceptionCause(new RuntimeException("fail")));

    Combiner2<String, Integer> result =
        begin(initial)
            .andThenApply(
                s -> {
                  invoked[0] = true;
                  return ok(s.length());
                });

    assertThat(result.result1()).isEqualTo(initial);
    assertThat(result.result2()).isEqualTo(result.result1());
    assertThat(result.result2()).isInstanceOf(Result.Err.class);
    assertThat(invoked[0]).isFalse();
  }

  @Test
  void shouldRejectNullFunctionInAndThenApplyConsistentlyOnOkAndErr() {
    Result<String> initialErr = err(new ExceptionCause(new RuntimeException("fail")));

    assertThatThrownBy(
            () -> begin(ok("hello")).andThenApply((Function<String, Result<Integer>>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () -> begin(initialErr).andThenApply((Function<String, Result<Integer>>) null))
        .isInstanceOf(NullPointerException.class);
  }

  // --- andThenTrying Tests ---

  @Test
  void shouldExecuteAndThenTryingSupplierSuccess() {
    Combiner2<String, Integer> result = begin(ok("start")).andThenTrying(() -> 99);

    assertThat(result.result1()).isEqualTo(ok("start"));
    assertThat(result.result2()).isEqualTo(ok(99));
  }

  @Test
  void shouldExecuteAndThenTryingSupplierFailureWithDefaultHandler() {
    Combiner2<String, Integer> result =
        begin(ok("start"))
            .andThenTrying(
                () -> {
                  throw new IllegalArgumentException("bad arg");
                });

    assertThat(result.result1()).isEqualTo(ok("start"));
    assertThat(result.result2()).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldExecuteAndThenTryingSupplierFailureWithCustomMapper() {
    Combiner2<String, Integer> result =
        begin(ok("start"))
            .andThenTrying(
                () -> {
                  throw new IllegalStateException("custom error");
                },
                ex -> new ExceptionCause(new RuntimeException("mapped: " + ex.getMessage())));

    assertThat(result.result1()).isEqualTo(ok("start"));
    assertThat(result.result2()).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldShortCircuitAndThenTryingSupplierWhenErr() {
    boolean[] invoked = {false};
    Result<String> initial = err(new ExceptionCause(new RuntimeException("fail")));

    Combiner2<String, Integer> result =
        begin(initial)
            .andThenTrying(
                () -> {
                  invoked[0] = true;
                  return 99;
                });

    assertThat(result.result1()).isEqualTo(initial);
    assertThat(result.result2()).isInstanceOf(Result.Err.class);
    assertThat(invoked[0]).isFalse();
  }

  @Test
  void shouldRejectNullArgumentsInAndThenTryingSupplierConsistentlyOnOkAndErr() {
    Result<String> initialErr = err(new ExceptionCause(new RuntimeException("fail")));

    assertThatThrownBy(() -> begin(ok("start")).andThenTrying((ThrowingSupplier<Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> begin(initialErr).andThenTrying((ThrowingSupplier<Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> begin(ok("start")).andThenTrying(() -> 99, null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> begin(initialErr).andThenTrying(() -> 99, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldExecuteAndThenTryingFunctionSuccess() {
    Combiner2<String, Integer> result = begin(ok("123")).andThenTrying(Integer::parseInt);

    assertThat(result.result1()).isEqualTo(ok("123"));
    assertThat(result.result2()).isEqualTo(ok(123));
  }

  @Test
  void shouldExecuteAndThenTryingFunctionFailureWithCustomMapper() {
    Combiner2<String, Integer> result =
        begin(ok("not-a-number"))
            .andThenTrying(
                Integer::parseInt,
                ex -> new ExceptionCause(new RuntimeException("Parsing failed")));

    assertThat(result.result1()).isEqualTo(ok("not-a-number"));
    assertThat(result.result2()).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldShortCircuitAndThenTryingFunctionWhenErr() {
    boolean[] invoked = {false};
    Result<String> initial = err(new ExceptionCause(new RuntimeException("fail")));

    Combiner2<String, Integer> result =
        begin(initial)
            .andThenTrying(
                s -> {
                  invoked[0] = true;
                  return Integer.parseInt(s);
                });

    assertThat(result.result1()).isEqualTo(initial);
    assertThat(result.result2()).isInstanceOf(Result.Err.class);
    assertThat(invoked[0]).isFalse();
  }

  @Test
  void shouldRejectNullArgumentsInAndThenTryingFunctionConsistentlyOnOkAndErr() {
    Result<String> initialErr = err(new ExceptionCause(new RuntimeException("fail")));

    assertThatThrownBy(
            () -> begin(ok("123")).andThenTrying((ThrowingFunction<String, Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () -> begin(initialErr).andThenTrying((ThrowingFunction<String, Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> begin(ok("123")).andThenTrying(Integer::parseInt, null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> begin(initialErr).andThenTrying(Integer::parseInt, null))
        .isInstanceOf(NullPointerException.class);
  }
}
