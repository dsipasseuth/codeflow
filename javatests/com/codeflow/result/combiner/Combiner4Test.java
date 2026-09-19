package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeflow.result.ExceptionCause;
import com.codeflow.result.Result;
import org.junit.jupiter.api.Test;

class Combiner4Test {

  // --- Constructor & Validation Tests ---

  @Test
  void shouldThrowNullPointerExceptionWhenFirstResultIsNull() {
    assertThatThrownBy(() -> new Combiner4<>(null, ok("second"), ok("third"), ok("fourth")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("First result cannot be null");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenSecondResultIsNull() {
    assertThatThrownBy(() -> new Combiner4<>(ok("first"), null, ok("third"), ok("fourth")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Second result cannot be null");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenThirdResultIsNull() {
    assertThatThrownBy(() -> new Combiner4<>(ok("first"), ok("second"), null, ok("fourth")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Third result cannot be null");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenFourthResultIsNull() {
    assertThatThrownBy(() -> new Combiner4<>(ok("first"), ok("second"), ok("third"), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Fourth result cannot be null");
  }

  // --- andThen Tests (Supplier & QuadFunction) ---

  @Test
  void shouldExecuteAndThenSupplierWhenAllOk() {
    Combiner4<String, Integer, Boolean, Double> combiner =
        new Combiner4<>(ok("a"), ok(1), ok(true), ok(2.0));
    Combiner5<String, Integer, Boolean, Double, String> result = combiner.andThen(() -> "fifth");

    assertThat(result.result1()).isEqualTo(ok("a"));
    assertThat(result.result2()).isEqualTo(ok(1));
    assertThat(result.result3()).isEqualTo(ok(true));
    assertThat(result.result4()).isEqualTo(ok(2.0));
    assertThat(result.result5()).isEqualTo(ok("fifth"));
  }

  @Test
  void shouldExecuteAndThenQuadFunctionWhenAllOk() {
    Combiner4<String, String, String, String> combiner =
        new Combiner4<>(ok("a"), ok("b"), ok("c"), ok("d"));
    Combiner5<String, String, String, String, String> result =
        combiner.andThen((a, b, c, d) -> a + b + c + d);

    assertThat(result.result1()).isEqualTo(ok("a"));
    assertThat(result.result2()).isEqualTo(ok("b"));
    assertThat(result.result3()).isEqualTo(ok("c"));
    assertThat(result.result4()).isEqualTo(ok("d"));
    assertThat(result.result5()).isEqualTo(ok("abcd"));
  }

  // --- andThenApply Tests ---

  @Test
  void shouldExecuteAndThenApplySupplierWhenAllOk() {
    Combiner4<String, Integer, Boolean, Double> combiner =
        new Combiner4<>(ok("a"), ok(1), ok(true), ok(2.0));
    Combiner5<String, Integer, Boolean, Double, String> result =
        combiner.andThenApply(() -> ok("applied"));

    assertThat(result.result5()).isEqualTo(ok("applied"));
  }

  @Test
  void shouldExecuteAndThenApplyQuadFunctionWhenAllOk() {
    Combiner4<Integer, Integer, Integer, Integer> combiner =
        new Combiner4<>(ok(1), ok(2), ok(3), ok(4));
    Combiner5<Integer, Integer, Integer, Integer, Integer> result =
        combiner.andThenApply((a, b, c, d) -> ok(a + b + c + d));

    assertThat(result.result5()).isEqualTo(ok(10));
  }

  // --- andThenTrying Tests ---

  @Test
  void shouldExecuteAndThenTryingSupplierSuccess() {
    Combiner4<String, Integer, Boolean, Double> combiner =
        new Combiner4<>(ok("a"), ok(1), ok(true), ok(2.0));
    Combiner5<String, Integer, Boolean, Double, String> result =
        combiner.andThenTrying(() -> "success");

    assertThat(result.result5()).isEqualTo(ok("success"));
  }

  @Test
  void shouldExecuteAndThenTryingSupplierFailureWithDefaultHandler() {
    Combiner4<String, Integer, Boolean, Double> combiner =
        new Combiner4<>(ok("a"), ok(1), ok(true), ok(2.0));
    Combiner5<String, Integer, Boolean, Double, String> result =
        combiner.andThenTrying(
            () -> {
              throw new RuntimeException("supplier error");
            });

    assertThat(result.result5()).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldExecuteAndThenTryingSupplierFailureWithCustomMapper() {
    Combiner4<String, Integer, Boolean, Double> combiner =
        new Combiner4<>(ok("a"), ok(1), ok(true), ok(2.0));
    Combiner5<String, Integer, Boolean, Double, String> result =
        combiner.andThenTrying(
            () -> {
              throw new IllegalStateException("bad state");
            },
            ex -> new ExceptionCause(new RuntimeException("mapped: " + ex.getMessage())));

    assertThat(result.result5()).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldExecuteAndThenTryingQuadFunctionSuccess() {
    Combiner4<Integer, Integer, Integer, Integer> combiner =
        new Combiner4<>(ok(20), ok(2), ok(3), ok(5));
    Combiner5<Integer, Integer, Integer, Integer, Integer> result =
        combiner.andThenTrying((a, b, c, d) -> (a / b) + c + d);

    assertThat(result.result5()).isEqualTo(ok(18));
  }

  @Test
  void shouldExecuteAndThenTryingQuadFunctionFailureWithCustomMapper() {
    Combiner4<Integer, Integer, Integer, Integer> combiner =
        new Combiner4<>(ok(20), ok(0), ok(3), ok(5));
    Combiner5<Integer, Integer, Integer, Integer, Integer> result =
        combiner.andThenTrying(
            (a, b, c, d) -> (a / b) + c + d,
            ex -> new ExceptionCause(new RuntimeException("Math error: " + ex.getMessage())));

    assertThat(result.result5()).isInstanceOf(Result.Err.class);
  }

  // --- also (Side-effect) Tests ---

  @Test
  void shouldExecuteAlsoWhenAllOk() {
    boolean[] executed = {false};
    Combiner4<String, Integer, Boolean, Double> combiner =
        new Combiner4<>(ok("foo"), ok(42), ok(true), ok(3.14));

    Combiner4<String, Integer, Boolean, Double> returned =
        combiner.also(
            (s, i, b, d) -> {
              executed[0] = true;
              assertThat(s).isEqualTo("foo");
              assertThat(i).isEqualTo(42);
              assertThat(b).isTrue();
              assertThat(d).isEqualTo(3.14);
            });

    assertThat(executed[0]).isTrue();
    assertThat(returned).isEqualTo(combiner);
  }

  @Test
  void shouldSkipAlsoWhenAnyResultIsErr() {
    boolean[] executed = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner4<String, Integer, Boolean, Double> combiner =
        new Combiner4<>(errResult, ok(42), ok(true), ok(3.14));

    combiner.also((s, i, b, d) -> executed[0] = true);

    assertThat(executed[0]).isFalse();
  }

  // --- Terminal then, thenApply, thenTrying Tests ---

  @Test
  void shouldEvaluateThenWhenAllOk() {
    Combiner4<Integer, Integer, Integer, Integer> combiner =
        new Combiner4<>(ok(1), ok(2), ok(3), ok(4));
    Result<Integer> result = combiner.then((a, b, c, d) -> a + b + c + d);

    assertThat(result).isEqualTo(ok(10));
  }

  @Test
  void shouldEvaluateThenApplyWhenAllOk() {
    Combiner4<String, String, String, String> combiner =
        new Combiner4<>(ok("a"), ok("b"), ok("c"), ok("d"));
    Result<String> result = combiner.thenApply((a, b, c, d) -> ok(a + b + c + d));

    assertThat(result).isEqualTo(ok("abcd"));
  }

  @Test
  void shouldEvaluateThenTryingSuccess() {
    Combiner4<String, String, String, String> combiner =
        new Combiner4<>(ok("1"), ok("2"), ok("3"), ok("4"));
    Result<Integer> result =
        combiner.thenTrying(
            (a, b, c, d) ->
                Integer.parseInt(a)
                    + Integer.parseInt(b)
                    + Integer.parseInt(c)
                    + Integer.parseInt(d));

    assertThat(result).isEqualTo(ok(10));
  }

  @Test
  void shouldEvaluateThenTryingFailureWithCustomMapper() {
    Combiner4<String, String, String, String> combiner =
        new Combiner4<>(ok("invalid"), ok("2"), ok("3"), ok("4"));
    Result<Integer> result =
        combiner.thenTrying(
            (a, b, c, d) ->
                Integer.parseInt(a)
                    + Integer.parseInt(b)
                    + Integer.parseInt(c)
                    + Integer.parseInt(d),
            ex -> new ExceptionCause(new RuntimeException("Parse failed")));

    assertThat(result).isInstanceOf(Result.Err.class);
  }
}
