package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeflow.result.ExceptionCause;
import com.codeflow.result.Result;
import org.junit.jupiter.api.Test;

class Combiner3Test {

  // --- Constructor & Validation Tests ---

  @Test
  void shouldThrowNullPointerExceptionWhenFirstResultIsNull() {
    assertThatThrownBy(() -> new Combiner3<>(null, ok("second"), ok("third")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("First result cannot be null");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenSecondResultIsNull() {
    assertThatThrownBy(() -> new Combiner3<>(ok("first"), null, ok("third")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Second result cannot be null");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenThirdResultIsNull() {
    assertThatThrownBy(() -> new Combiner3<>(ok("first"), ok("second"), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Third result cannot be null");
  }

  // --- andThen Tests (Supplier & TriFunction) ---

  @Test
  void shouldExecuteAndThenSupplierWhenAllOk() {
    Combiner3<String, Integer, Boolean> combiner = new Combiner3<>(ok("a"), ok(1), ok(true));
    Combiner4<String, Integer, Boolean, String> result = combiner.andThen(() -> "fourth");

    assertThat(result.result1()).isEqualTo(ok("a"));
    assertThat(result.result2()).isEqualTo(ok(1));
    assertThat(result.result3()).isEqualTo(ok(true));
    assertThat(result.result4()).isEqualTo(ok("fourth"));
  }

  @Test
  void shouldExecuteAndThenTriFunctionWhenAllOk() {
    Combiner3<String, String, String> combiner = new Combiner3<>(ok("a"), ok("b"), ok("c"));
    Combiner4<String, String, String, String> result = combiner.andThen((a, b, c) -> a + b + c);

    assertThat(result.result1()).isEqualTo(ok("a"));
    assertThat(result.result2()).isEqualTo(ok("b"));
    assertThat(result.result3()).isEqualTo(ok("c"));
    assertThat(result.result4()).isEqualTo(ok("abc"));
  }

  // --- andThenApply Tests ---

  @Test
  void shouldExecuteAndThenApplySupplierWhenAllOk() {
    Combiner3<String, Integer, Boolean> combiner = new Combiner3<>(ok("a"), ok(1), ok(true));
    Combiner4<String, Integer, Boolean, String> result = combiner.andThenApply(() -> ok("applied"));

    assertThat(result.result4()).isEqualTo(ok("applied"));
  }

  @Test
  void shouldExecuteAndThenApplyTriFunctionWhenAllOk() {
    Combiner3<Integer, Integer, Integer> combiner = new Combiner3<>(ok(2), ok(3), ok(4));
    Combiner4<Integer, Integer, Integer, Integer> result =
        combiner.andThenApply((a, b, c) -> ok(a * b * c));

    assertThat(result.result4()).isEqualTo(ok(24));
  }

  // --- andThenTrying Tests ---

  @Test
  void shouldExecuteAndThenTryingSupplierSuccess() {
    Combiner3<String, Integer, Boolean> combiner = new Combiner3<>(ok("a"), ok(1), ok(true));
    Combiner4<String, Integer, Boolean, String> result = combiner.andThenTrying(() -> "success");

    assertThat(result.result4()).isEqualTo(ok("success"));
  }

  @Test
  void shouldExecuteAndThenTryingSupplierFailureWithDefaultHandler() {
    Combiner3<String, Integer, Boolean> combiner = new Combiner3<>(ok("a"), ok(1), ok(true));
    Combiner4<String, Integer, Boolean, String> result =
        combiner.andThenTrying(
            () -> {
              throw new RuntimeException("supplier error");
            });

    assertThat(result.result4()).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldExecuteAndThenTryingSupplierFailureWithCustomMapper() {
    Combiner3<String, Integer, Boolean> combiner = new Combiner3<>(ok("a"), ok(1), ok(true));
    Combiner4<String, Integer, Boolean, String> result =
        combiner.andThenTrying(
            () -> {
              throw new IllegalStateException("bad state");
            },
            ex -> new ExceptionCause(new RuntimeException("mapped: " + ex.getMessage())));

    assertThat(result.result4()).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldExecuteAndThenTryingTriFunctionSuccess() {
    Combiner3<Integer, Integer, Integer> combiner = new Combiner3<>(ok(10), ok(2), ok(1));
    Combiner4<Integer, Integer, Integer, Integer> result =
        combiner.andThenTrying((a, b, c) -> (a / b) + c);

    assertThat(result.result4()).isEqualTo(ok(6));
  }

  @Test
  void shouldExecuteAndThenTryingTriFunctionFailureWithCustomMapper() {
    Combiner3<Integer, Integer, Integer> combiner = new Combiner3<>(ok(10), ok(0), ok(1));
    Combiner4<Integer, Integer, Integer, Integer> result =
        combiner.andThenTrying(
            (a, b, c) -> (a / b) + c,
            ex -> new ExceptionCause(new RuntimeException("Math error: " + ex.getMessage())));

    assertThat(result.result4()).isInstanceOf(Result.Err.class);
  }

  // --- also (Side-effect) Tests ---

  @Test
  void shouldExecuteAlsoWhenAllOk() {
    boolean[] executed = {false};
    Combiner3<String, Integer, Boolean> combiner = new Combiner3<>(ok("foo"), ok(42), ok(true));

    Combiner3<String, Integer, Boolean> returned =
        combiner.also(
            (s, i, b) -> {
              executed[0] = true;
              assertThat(s).isEqualTo("foo");
              assertThat(i).isEqualTo(42);
              assertThat(b).isTrue();
            });

    assertThat(executed[0]).isTrue();
    assertThat(returned).isEqualTo(combiner);
  }

  @Test
  void shouldSkipAlsoWhenAnyResultIsErr() {
    boolean[] executed = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner3<String, Integer, Boolean> combiner = new Combiner3<>(errResult, ok(42), ok(true));

    combiner.also((s, i, b) -> executed[0] = true);

    assertThat(executed[0]).isFalse();
  }

  // --- Terminal then, thenApply, thenTrying Tests ---

  @Test
  void shouldEvaluateThenWhenAllOk() {
    Combiner3<Integer, Integer, Integer> combiner = new Combiner3<>(ok(3), ok(4), ok(5));
    Result<Integer> result = combiner.then((a, b, c) -> a + b + c);

    assertThat(result).isEqualTo(ok(12));
  }

  @Test
  void shouldEvaluateThenApplyWhenAllOk() {
    Combiner3<String, String, String> combiner =
        new Combiner3<>(ok("hello"), ok("middle"), ok("world"));
    Result<String> result = combiner.thenApply((a, b, c) -> ok(a + " " + b + " " + c));

    assertThat(result).isEqualTo(ok("hello middle world"));
  }

  @Test
  void shouldEvaluateThenTryingSuccess() {
    Combiner3<String, String, String> combiner = new Combiner3<>(ok("10"), ok("20"), ok("30"));
    Result<Integer> result =
        combiner.thenTrying(
            (a, b, c) -> Integer.parseInt(a) + Integer.parseInt(b) + Integer.parseInt(c));

    assertThat(result).isEqualTo(ok(60));
  }

  @Test
  void shouldEvaluateThenTryingFailureWithCustomMapper() {
    Combiner3<String, String, String> combiner =
        new Combiner3<>(ok("not-a-number"), ok("20"), ok("30"));
    Result<Integer> result =
        combiner.thenTrying(
            (a, b, c) -> Integer.parseInt(a) + Integer.parseInt(b) + Integer.parseInt(c),
            ex -> new ExceptionCause(new RuntimeException("Parse failed")));

    assertThat(result).isInstanceOf(Result.Err.class);
  }
}
