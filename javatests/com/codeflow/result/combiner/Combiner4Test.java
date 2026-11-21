package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeflow.result.ExceptionCause;
import com.codeflow.result.Functions.Function4;
import com.codeflow.result.Functions.ThrowingFunction4;
import com.codeflow.result.Functions.ThrowingSupplier;
import com.codeflow.result.Result;
import java.util.function.Supplier;
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

  // --- andThen Tests (Supplier & Function4) ---

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
  void shouldShortCircuitAndThenSupplierWhenAnyErr() {
    boolean[] invoked = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner4<String, Integer, Boolean, Double> combiner =
        new Combiner4<>(errResult, ok(1), ok(true), ok(2.0));

    Combiner5<String, Integer, Boolean, Double, String> result =
        combiner.andThen(
            () -> {
              invoked[0] = true;
              return "fifth";
            });

    assertThat(result.result1()).isEqualTo(errResult);
    assertThat(result.result5()).isInstanceOf(Result.Err.class);
    assertThat(invoked[0]).isFalse();
  }

  @Test
  void shouldRejectNullSupplierInAndThenConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner4<String, Integer, Boolean, Double> okCombiner =
        new Combiner4<>(ok("a"), ok(1), ok(true), ok(2.0));
    Combiner4<String, Integer, Boolean, Double> errCombiner =
        new Combiner4<>(errResult, ok(1), ok(true), ok(2.0));

    assertThatThrownBy(() -> okCombiner.andThen((Supplier<String>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.andThen((Supplier<String>) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldExecuteAndThenFunction4WhenAllOk() {
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

  @Test
  void shouldRejectNullFunction4InAndThenConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner4<String, String, String, String> okCombiner =
        new Combiner4<>(ok("a"), ok("b"), ok("c"), ok("d"));
    Combiner4<String, String, String, String> errCombiner =
        new Combiner4<>(errResult, ok("b"), ok("c"), ok("d"));

    assertThatThrownBy(
            () -> okCombiner.andThen((Function4<String, String, String, String, String>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () -> errCombiner.andThen((Function4<String, String, String, String, String>) null))
        .isInstanceOf(NullPointerException.class);
  }

  // --- andThenApply Tests ---

  @Test
  void shouldExecuteAndThenApplyFunction4WhenAllOk() {
    Combiner4<Integer, Integer, Integer, Integer> combiner =
        new Combiner4<>(ok(1), ok(2), ok(3), ok(4));
    Combiner5<Integer, Integer, Integer, Integer, Integer> result =
        combiner.andThenApply((a, b, c, d) -> ok(a + b + c + d));

    assertThat(result.result5()).isEqualTo(ok(10));
  }

  @Test
  void shouldRejectNullFunction4InAndThenApplyConsistentlyOnOkAndErr() {
    Result<Integer> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner4<Integer, Integer, Integer, Integer> okCombiner =
        new Combiner4<>(ok(1), ok(2), ok(3), ok(4));
    Combiner4<Integer, Integer, Integer, Integer> errCombiner =
        new Combiner4<>(errResult, ok(2), ok(3), ok(4));

    assertThatThrownBy(
            () ->
                okCombiner.andThenApply(
                    (Function4<Integer, Integer, Integer, Integer, Result<Integer>>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () ->
                errCombiner.andThenApply(
                    (Function4<Integer, Integer, Integer, Integer, Result<Integer>>) null))
        .isInstanceOf(NullPointerException.class);
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
  void shouldRejectNullArgumentsInAndThenTryingSupplierConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner4<String, Integer, Boolean, Double> okCombiner =
        new Combiner4<>(ok("a"), ok(1), ok(true), ok(2.0));
    Combiner4<String, Integer, Boolean, Double> errCombiner =
        new Combiner4<>(errResult, ok(1), ok(true), ok(2.0));

    assertThatThrownBy(() -> okCombiner.andThenTrying((ThrowingSupplier<String>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.andThenTrying((ThrowingSupplier<String>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> okCombiner.andThenTrying(() -> "ok", null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.andThenTrying(() -> "ok", null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldExecuteAndThenTryingFunction4Success() {
    Combiner4<Integer, Integer, Integer, Integer> combiner =
        new Combiner4<>(ok(20), ok(2), ok(3), ok(5));
    Combiner5<Integer, Integer, Integer, Integer, Integer> result =
        combiner.andThenTrying((a, b, c, d) -> (a / b) + c + d);

    assertThat(result.result5()).isEqualTo(ok(18));
  }

  @Test
  void shouldExecuteAndThenTryingFunction4FailureWithCustomMapper() {
    Combiner4<Integer, Integer, Integer, Integer> combiner =
        new Combiner4<>(ok(20), ok(0), ok(3), ok(5));
    Combiner5<Integer, Integer, Integer, Integer, Integer> result =
        combiner.andThenTrying(
            (a, b, c, d) -> (a / b) + c + d,
            ex -> new ExceptionCause(new RuntimeException("Math error: " + ex.getMessage())));

    assertThat(result.result5()).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldRejectNullArgumentsInAndThenTryingFunction4ConsistentlyOnOkAndErr() {
    Result<Integer> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner4<Integer, Integer, Integer, Integer> okCombiner =
        new Combiner4<>(ok(20), ok(2), ok(3), ok(5));
    Combiner4<Integer, Integer, Integer, Integer> errCombiner =
        new Combiner4<>(errResult, ok(2), ok(3), ok(5));

    assertThatThrownBy(
            () ->
                okCombiner.andThenTrying(
                    (ThrowingFunction4<Integer, Integer, Integer, Integer, Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () ->
                errCombiner.andThenTrying(
                    (ThrowingFunction4<Integer, Integer, Integer, Integer, Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> okCombiner.andThenTrying((a, b, c, d) -> a + b + c + d, null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.andThenTrying((a, b, c, d) -> a + b + c + d, null))
        .isInstanceOf(NullPointerException.class);
  }

  // --- peek (Side-effect) Tests ---

  @Test
  void shouldExecutePeekWhenAllOk() {
    boolean[] executed = {false};
    Combiner4<String, Integer, Boolean, Double> combiner =
        new Combiner4<>(ok("foo"), ok(42), ok(true), ok(3.14));

    Combiner4<String, Integer, Boolean, Double> returned =
        combiner.peekOk(
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
  void shouldSkipPeekWhenAnyResultIsErr() {
    boolean[] executed = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner4<String, Integer, Boolean, Double> combiner =
        new Combiner4<>(errResult, ok(42), ok(true), ok(3.14));

    combiner.peekOk((_, _, _, _) -> executed[0] = true);

    assertThat(executed[0]).isFalse();
  }

  @Test
  void shouldRejectNullConsumer4InPeekConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner4<String, Integer, Boolean, Double> okCombiner =
        new Combiner4<>(ok("foo"), ok(42), ok(true), ok(3.14));
    Combiner4<String, Integer, Boolean, Double> errCombiner =
        new Combiner4<>(errResult, ok(42), ok(true), ok(3.14));

    assertThatThrownBy(() -> okCombiner.peekOk(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.peekOk(null)).isInstanceOf(NullPointerException.class);
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
  void shouldPropagateFirstErrInThen() {
    Result<Integer> firstErr = err(new ExceptionCause(new RuntimeException("first")));
    Result<Integer> secondErr = err(new ExceptionCause(new RuntimeException("second")));
    Combiner4<Integer, Integer, Integer, Integer> combiner =
        new Combiner4<>(firstErr, secondErr, ok(3), ok(4));

    assertThat(combiner.then((a, b, c, d) -> a + b + c + d)).isEqualTo(firstErr);
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
            _ -> new ExceptionCause(new RuntimeException("Parse failed")));

    assertThat(result).isInstanceOf(Result.Err.class);
  }
}
