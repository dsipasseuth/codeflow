package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeflow.result.ExceptionCause;
import com.codeflow.result.Functions.ThrowingBiFunction;
import com.codeflow.result.Functions.ThrowingSupplier;
import com.codeflow.result.Result;
import java.util.function.BiFunction;
import java.util.function.Supplier;
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
  void shouldShortCircuitAndThenSupplierWhenAnyErr() {
    boolean[] invoked = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<String, Integer> combiner = new Combiner2<>(errResult, ok(1));

    Combiner3<String, Integer, String> result =
        combiner.andThen(
            () -> {
              invoked[0] = true;
              return "third";
            });

    assertThat(result.result1()).isEqualTo(errResult);
    assertThat(result.result2()).isEqualTo(ok(1));
    assertThat(result.result3()).isInstanceOf(Result.Err.class);
    assertThat(invoked[0]).isFalse();
  }

  @Test
  void shouldRejectNullSupplierInAndThenConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<String, Integer> okCombiner = new Combiner2<>(ok("a"), ok(1));
    Combiner2<String, Integer> errCombiner = new Combiner2<>(errResult, ok(1));

    assertThatThrownBy(() -> okCombiner.andThen((Supplier<String>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.andThen((Supplier<String>) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldExecuteAndThenBiFunctionWhenBothOk() {
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("hello"), ok(3));
    Combiner3<String, Integer, String> result = combiner.andThen(String::repeat);

    assertThat(result.result1()).isEqualTo(ok("hello"));
    assertThat(result.result2()).isEqualTo(ok(3));
    assertThat(result.result3()).isEqualTo(ok("hellohellohello"));
  }

  @Test
  void shouldRejectNullBiFunctionInAndThenConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<String, Integer> okCombiner = new Combiner2<>(ok("a"), ok(1));
    Combiner2<String, Integer> errCombiner = new Combiner2<>(errResult, ok(1));

    assertThatThrownBy(() -> okCombiner.andThen((BiFunction<String, Integer, String>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.andThen((BiFunction<String, Integer, String>) null))
        .isInstanceOf(NullPointerException.class);
  }

  // --- andThenApply Tests ---

  @Test
  void shouldExecuteAndThenApplyBiFunctionWhenBothOk() {
    Combiner2<Integer, Integer> combiner = new Combiner2<>(ok(2), ok(3));
    Combiner3<Integer, Integer, Integer> result = combiner.andThenApply((a, b) -> ok(a * b));

    assertThat(result.result3()).isEqualTo(ok(6));
  }

  @Test
  void shouldRejectNullBiFunctionInAndThenApplyConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<String, Integer> okCombiner = new Combiner2<>(ok("a"), ok(1));
    Combiner2<String, Integer> errCombiner = new Combiner2<>(errResult, ok(1));

    assertThatThrownBy(
            () -> okCombiner.andThenApply((BiFunction<String, Integer, Result<String>>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () -> errCombiner.andThenApply((BiFunction<String, Integer, Result<String>>) null))
        .isInstanceOf(NullPointerException.class);
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
  void shouldRejectNullArgumentsInAndThenTryingSupplierConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<String, Integer> okCombiner = new Combiner2<>(ok("a"), ok(1));
    Combiner2<String, Integer> errCombiner = new Combiner2<>(errResult, ok(1));

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

  @Test
  void shouldRejectNullArgumentsInAndThenTryingBiFunctionConsistentlyOnOkAndErr() {
    Result<Integer> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<Integer, Integer> okCombiner = new Combiner2<>(ok(10), ok(2));
    Combiner2<Integer, Integer> errCombiner = new Combiner2<>(errResult, ok(2));

    assertThatThrownBy(
            () -> okCombiner.andThenTrying((ThrowingBiFunction<Integer, Integer, Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () -> errCombiner.andThenTrying((ThrowingBiFunction<Integer, Integer, Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> okCombiner.andThenTrying(Integer::sum, null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.andThenTrying(Integer::sum, null))
        .isInstanceOf(NullPointerException.class);
  }

  // --- peek (Side-effect) Tests ---

  @Test
  void shouldExecutePeekWhenBothOk() {
    boolean[] executed = {false};
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("foo"), ok(42));

    Combiner2<String, Integer> returned =
        combiner.peekOk(
            (s, i) -> {
              executed[0] = true;
              assertThat(s).isEqualTo("foo");
              assertThat(i).isEqualTo(42);
            });

    assertThat(executed[0]).isTrue();
    assertThat(returned).isEqualTo(combiner);
  }

  @Test
  void shouldSkipPeekWhenFirstResultIsErr() {
    boolean[] executed = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<String, Integer> combiner = new Combiner2<>(errResult, ok(42));

    combiner.peekOk((_, _) -> executed[0] = true);

    assertThat(executed[0]).isFalse();
  }

  @Test
  void shouldSkipPeekWhenSecondResultIsErr() {
    boolean[] executed = {false};
    Result<Integer> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<String, Integer> combiner = new Combiner2<>(ok("foo"), errResult);

    combiner.peekOk((_, _) -> executed[0] = true);

    assertThat(executed[0]).isFalse();
  }

  @Test
  void shouldRejectNullConsumerInPeekConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner2<String, Integer> okCombiner = new Combiner2<>(ok("foo"), ok(42));
    Combiner2<String, Integer> errCombiner = new Combiner2<>(errResult, ok(42));

    assertThatThrownBy(() -> okCombiner.peekOk(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.peekOk(null)).isInstanceOf(NullPointerException.class);
  }

  // --- Terminal then, thenApply, thenTrying Tests ---

  @Test
  void shouldEvaluateThenWhenBothOk() {
    Combiner2<Integer, Integer> combiner = new Combiner2<>(ok(3), ok(4));
    Result<Integer> result = combiner.then(Integer::sum);

    assertThat(result).isEqualTo(ok(7));
  }

  @Test
  void shouldPropagateFirstErrInThen() {
    Result<Integer> firstErr = err(new ExceptionCause(new RuntimeException("first")));
    Result<Integer> secondErr = err(new ExceptionCause(new RuntimeException("second")));
    Combiner2<Integer, Integer> combiner = new Combiner2<>(firstErr, secondErr);

    assertThat(combiner.then(Integer::sum)).isEqualTo(firstErr);
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
            _ -> new ExceptionCause(new RuntimeException("Parse failed")));

    assertThat(result).isInstanceOf(Result.Err.class);
  }
}
