package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeflow.result.ExceptionCause;
import com.codeflow.result.Result;
import org.junit.jupiter.api.Test;

class Combiner2Test {

  // --- Constructor & Validation Tests ---

  @Test
  void shouldThrowNullPointerExceptionWhenFirstResultIsNull() {
    assertThatThrownBy(() -> new Combiner2<>(null, ok("second")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("First result cannot be null");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenSecondResultIsNull() {
    assertThatThrownBy(() -> new Combiner2<>(ok("first"), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Second result cannot be null");
  }

  // --- andThen Tests (Supplier & BiFunction) ---

  @Test
  void shouldExecuteAndThenSupplierWhenBothOk() {
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("a"), ok(1));
    Combiner3<String, Integer, String> result = combiner.andThen(() -> "third");

    assertThat(result.result1()).isEqualTo(ok("a"));
    assertThat(result.result2()).isEqualTo(ok(1));
    assertThat(result.result3()).isEqualTo(ok("third"));
  }

  @Test
  void shouldExecuteAndThenBiFunctionWhenBothOk() {
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("hello"), ok(3));
    Combiner3<String, Integer, String> result = combiner.andThen(String::repeat);

    assertThat(result.result1()).isEqualTo(ok("hello"));
    assertThat(result.result2()).isEqualTo(ok(3));
    assertThat(result.result3()).isEqualTo(ok("hellohellohello"));
  }

  // --- andThenApply Tests ---

  @Test
  void shouldExecuteAndThenApplySupplierWhenBothOk() {
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("a"), ok(1));
    Combiner3<String, Integer, String> result = combiner.andThenApply(() -> ok("applied"));

    assertThat(result.result3()).isEqualTo(ok("applied"));
  }

  @Test
  void shouldExecuteAndThenApplyBiFunctionWhenBothOk() {
    Combiner2<Integer, Integer> combiner = new Combiner2<>(ok(2), ok(3));
    Combiner3<Integer, Integer, Integer> result = combiner.andThenApply((a, b) -> ok(a * b));

    assertThat(result.result3()).isEqualTo(ok(6));
  }

  // --- andThenTrying Tests ---

  @Test
  void shouldExecuteAndThenTryingSupplierSuccess() {
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("a"), ok(1));
    Combiner3<String, Integer, String> result = combiner.andThenTrying(() -> "success");

    assertThat(result.result3()).isEqualTo(ok("success"));
  }

  @Test
  void shouldExecuteAndThenTryingSupplierFailureWithDefaultHandler() {
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("a"), ok(1));
    Combiner3<String, Integer, String> result =
        combiner.andThenTrying(
            () -> {
              throw new RuntimeException("supplier error");
            });

    assertThat(result.result3()).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldExecuteAndThenTryingSupplierFailureWithCustomMapper() {
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("a"), ok(1));
    Combiner3<String, Integer, String> result =
        combiner.andThenTrying(
            () -> {
              throw new IllegalStateException("bad state");
            },
            ex -> new ExceptionCause(new RuntimeException("mapped: " + ex.getMessage())));

    assertThat(result.result3()).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldExecuteAndThenTryingBiFunctionSuccess() {
    Combiner2<Integer, Integer> combiner = new Combiner2<>(ok(10), ok(2));
    Combiner3<Integer, Integer, Integer> result = combiner.andThenTrying((a, b) -> a / b);

    assertThat(result.result3()).isEqualTo(ok(5));
  }

  @Test
  void shouldExecuteAndThenTryingBiFunctionFailureWithCustomMapper() {
    Combiner2<Integer, Integer> combiner = new Combiner2<>(ok(10), ok(0));
    Combiner3<Integer, Integer, Integer> result =
        combiner.andThenTrying(
            (a, b) -> a / b,
            ex -> new ExceptionCause(new RuntimeException("Math error: " + ex.getMessage())));

    assertThat(result.result3()).isInstanceOf(Result.Err.class);
  }

  // --- also (Side-effect) Tests ---

  @Test
  void shouldExecuteAlsoWhenBothOk() {
    boolean[] executed = {false};
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("foo"), ok(42));

    Combiner2<String, Integer> returned =
        combiner.also(
            (s, i) -> {
              executed[0] = true;
              assertThat(s).isEqualTo("foo");
              assertThat(i).isEqualTo(42);
            });

    assertThat(executed[0]).isTrue();
    assertThat(returned).isEqualTo(combiner);
  }

  @Test
  void shouldSkipAlsoWhenFirstResultIsErr() {
    boolean[] executed = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<String, Integer> combiner = new Combiner2<>(errResult, ok(42));

    combiner.also((s, i) -> executed[0] = true);

    assertThat(executed[0]).isFalse();
  }

  @Test
  void shouldSkipAlsoWhenSecondResultIsErr() {
    boolean[] executed = {false};
    Result<Integer> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("foo"), errResult);

    combiner.also((s, i) -> executed[0] = true);

    assertThat(executed[0]).isFalse();
  }

  // --- Terminal then, thenApply, thenTrying Tests ---

  @Test
  void shouldEvaluateThenWhenBothOk() {
    Combiner2<Integer, Integer> combiner = new Combiner2<>(ok(3), ok(4));
    Result<Integer> result = combiner.then(Integer::sum);

    assertThat(result).isEqualTo(ok(7));
  }

  @Test
  void shouldEvaluateThenApplyWhenBothOk() {
    Combiner2<String, String> combiner = new Combiner2<>(ok("hello"), ok("world"));
    Result<String> result = combiner.thenApply((a, b) -> ok(a + " " + b));

    assertThat(result).isEqualTo(ok("hello world"));
  }

  @Test
  void shouldEvaluateThenTryingSuccess() {
    Combiner2<String, String> combiner = new Combiner2<>(ok("10"), ok("20"));
    Result<Integer> result =
        combiner.thenTrying((a, b) -> Integer.parseInt(a) + Integer.parseInt(b));

    assertThat(result).isEqualTo(ok(30));
  }

  @Test
  void shouldEvaluateThenTryingFailureWithCustomMapper() {
    Combiner2<String, String> combiner = new Combiner2<>(ok("not-a-number"), ok("20"));
    Result<Integer> result =
        combiner.thenTrying(
            (a, b) -> Integer.parseInt(a) + Integer.parseInt(b),
            ex -> new ExceptionCause(new RuntimeException("Parse failed")));

    assertThat(result).isInstanceOf(Result.Err.class);
  }
}
