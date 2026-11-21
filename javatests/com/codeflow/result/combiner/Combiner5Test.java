package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeflow.result.ExceptionCause;
import com.codeflow.result.Functions.Function5;
import com.codeflow.result.Functions.ThrowingFunction5;
import com.codeflow.result.Functions.ThrowingSupplier;
import com.codeflow.result.Result;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class Combiner5Test {

  // --- Constructor & Validation Tests ---

  @Test
  void shouldThrowNullPointerExceptionWhenFirstResultIsNull() {
    assertThatThrownBy(
            () -> new Combiner5<>(null, ok("second"), ok("third"), ok("fourth"), ok("fifth")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("First result cannot be null");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenFifthResultIsNull() {
    assertThatThrownBy(
            () -> new Combiner5<>(ok("first"), ok("second"), ok("third"), ok("fourth"), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Fifth result cannot be null");
  }

  // --- andThen Tests ---

  @Test
  void shouldExecuteAndThenSupplierWhenAllOk() {
    Combiner5<String, Integer, Boolean, Double, Character> combiner =
        new Combiner5<>(ok("a"), ok(1), ok(true), ok(2.0), ok('x'));
    Combiner6<String, Integer, Boolean, Double, Character, String> result =
        combiner.andThen(() -> "sixth");

    assertThat(result.result1()).isEqualTo(ok("a"));
    assertThat(result.result6()).isEqualTo(ok("sixth"));
  }

  @Test
  void shouldShortCircuitAndThenSupplierWhenAnyErr() {
    boolean[] invoked = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner5<String, Integer, Boolean, Double, Character> combiner =
        new Combiner5<>(errResult, ok(1), ok(true), ok(2.0), ok('x'));

    Combiner6<String, Integer, Boolean, Double, Character, String> result =
        combiner.andThen(
            () -> {
              invoked[0] = true;
              return "sixth";
            });

    assertThat(result.result1()).isEqualTo(errResult);
    assertThat(result.result6()).isInstanceOf(Result.Err.class);
    assertThat(invoked[0]).isFalse();
  }

  @Test
  void shouldRejectNullSupplierInAndThenConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner5<String, Integer, Boolean, Double, Character> okCombiner =
        new Combiner5<>(ok("a"), ok(1), ok(true), ok(2.0), ok('x'));
    Combiner5<String, Integer, Boolean, Double, Character> errCombiner =
        new Combiner5<>(errResult, ok(1), ok(true), ok(2.0), ok('x'));

    assertThatThrownBy(() -> okCombiner.andThen((Supplier<String>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.andThen((Supplier<String>) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldExecuteAndThenFunction5WhenAllOk() {
    Combiner5<String, String, String, String, String> combiner =
        new Combiner5<>(ok("a"), ok("b"), ok("c"), ok("d"), ok("e"));
    Combiner6<String, String, String, String, String, String> result =
        combiner.andThen((a, b, c, d, e) -> a + b + c + d + e);

    assertThat(result.result6()).isEqualTo(ok("abcde"));
  }

  @Test
  void shouldRejectNullFunction5InAndThenConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner5<String, String, String, String, String> okCombiner =
        new Combiner5<>(ok("a"), ok("b"), ok("c"), ok("d"), ok("e"));
    Combiner5<String, String, String, String, String> errCombiner =
        new Combiner5<>(errResult, ok("b"), ok("c"), ok("d"), ok("e"));

    assertThatThrownBy(
            () ->
                okCombiner.andThen(
                    (Function5<String, String, String, String, String, String>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () ->
                errCombiner.andThen(
                    (Function5<String, String, String, String, String, String>) null))
        .isInstanceOf(NullPointerException.class);
  }

  // --- andThenApply Tests ---

  @Test
  void shouldExecuteAndThenApplyFunction5WhenAllOk() {
    Combiner5<Integer, Integer, Integer, Integer, Integer> combiner =
        new Combiner5<>(ok(1), ok(2), ok(3), ok(4), ok(5));
    Combiner6<Integer, Integer, Integer, Integer, Integer, Integer> result =
        combiner.andThenApply((a, b, c, d, e) -> ok(a + b + c + d + e));

    assertThat(result.result6()).isEqualTo(ok(15));
  }

  @Test
  void shouldRejectNullFunction5InAndThenApplyConsistentlyOnOkAndErr() {
    Result<Integer> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner5<Integer, Integer, Integer, Integer, Integer> okCombiner =
        new Combiner5<>(ok(1), ok(2), ok(3), ok(4), ok(5));
    Combiner5<Integer, Integer, Integer, Integer, Integer> errCombiner =
        new Combiner5<>(errResult, ok(2), ok(3), ok(4), ok(5));

    assertThatThrownBy(
            () ->
                okCombiner.andThenApply(
                    (Function5<Integer, Integer, Integer, Integer, Integer, Result<Integer>>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () ->
                errCombiner.andThenApply(
                    (Function5<Integer, Integer, Integer, Integer, Integer, Result<Integer>>) null))
        .isInstanceOf(NullPointerException.class);
  }

  // --- andThenTrying Tests ---

  @Test
  void shouldExecuteAndThenTryingSupplierSuccess() {
    Combiner5<String, Integer, Boolean, Double, Character> combiner =
        new Combiner5<>(ok("a"), ok(1), ok(true), ok(2.0), ok('x'));
    Combiner6<String, Integer, Boolean, Double, Character, String> result =
        combiner.andThenTrying(() -> "success");

    assertThat(result.result6()).isEqualTo(ok("success"));
  }

  @Test
  void shouldRejectNullArgumentsInAndThenTryingSupplierConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner5<String, Integer, Boolean, Double, Character> okCombiner =
        new Combiner5<>(ok("a"), ok(1), ok(true), ok(2.0), ok('x'));
    Combiner5<String, Integer, Boolean, Double, Character> errCombiner =
        new Combiner5<>(errResult, ok(1), ok(true), ok(2.0), ok('x'));

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
  void shouldExecuteAndThenTryingFunction5Success() {
    Combiner5<Integer, Integer, Integer, Integer, Integer> combiner =
        new Combiner5<>(ok(10), ok(2), ok(1), ok(1), ok(1));
    Combiner6<Integer, Integer, Integer, Integer, Integer, Integer> result =
        combiner.andThenTrying((a, b, c, d, e) -> (a / b) + c + d + e);

    assertThat(result.result6()).isEqualTo(ok(8));
  }

  @Test
  void shouldRejectNullArgumentsInAndThenTryingFunction5ConsistentlyOnOkAndErr() {
    Result<Integer> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner5<Integer, Integer, Integer, Integer, Integer> okCombiner =
        new Combiner5<>(ok(10), ok(2), ok(1), ok(1), ok(1));
    Combiner5<Integer, Integer, Integer, Integer, Integer> errCombiner =
        new Combiner5<>(errResult, ok(2), ok(1), ok(1), ok(1));

    assertThatThrownBy(
            () ->
                okCombiner.andThenTrying(
                    (ThrowingFunction5<Integer, Integer, Integer, Integer, Integer, Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () ->
                errCombiner.andThenTrying(
                    (ThrowingFunction5<Integer, Integer, Integer, Integer, Integer, Integer>) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> okCombiner.andThenTrying((a, b, c, d, e) -> a + b + c + d + e, null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.andThenTrying((a, b, c, d, e) -> a + b + c + d + e, null))
        .isInstanceOf(NullPointerException.class);
  }

  // --- peek Tests ---

  @Test
  void shouldExecutePeekWhenAllOk() {
    boolean[] executed = {false};
    Combiner5<String, Integer, Boolean, Double, Character> combiner =
        new Combiner5<>(ok("foo"), ok(42), ok(true), ok(3.14), ok('z'));

    Combiner5<String, Integer, Boolean, Double, Character> returned =
        combiner.peekOk(
            (s, i, _, _, c) -> {
              executed[0] = true;
              assertThat(s).isEqualTo("foo");
              assertThat(i).isEqualTo(42);
              assertThat(c).isEqualTo('z');
            });

    assertThat(executed[0]).isTrue();
    assertThat(returned).isEqualTo(combiner);
  }

  @Test
  void shouldSkipPeekWhenAnyResultIsErr() {
    boolean[] executed = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner5<String, Integer, Boolean, Double, Character> combiner =
        new Combiner5<>(errResult, ok(42), ok(true), ok(3.14), ok('z'));

    combiner.peekOk((_, _, _, _, _) -> executed[0] = true);

    assertThat(executed[0]).isFalse();
  }

  @Test
  void shouldRejectNullConsumer5InPeekConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner5<String, Integer, Boolean, Double, Character> okCombiner =
        new Combiner5<>(ok("foo"), ok(42), ok(true), ok(3.14), ok('z'));
    Combiner5<String, Integer, Boolean, Double, Character> errCombiner =
        new Combiner5<>(errResult, ok(42), ok(true), ok(3.14), ok('z'));

    assertThatThrownBy(() -> okCombiner.peekOk(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.peekOk(null)).isInstanceOf(NullPointerException.class);
  }

  // --- Terminal then, thenApply, thenTrying Tests ---

  @Test
  void shouldEvaluateThenWhenAllOk() {
    Combiner5<Integer, Integer, Integer, Integer, Integer> combiner =
        new Combiner5<>(ok(1), ok(2), ok(3), ok(4), ok(5));
    Result<Integer> result = combiner.then((a, b, c, d, e) -> a + b + c + d + e);

    assertThat(result).isEqualTo(ok(15));
  }

  @Test
  void shouldPropagateFirstErrInThen() {
    Result<Integer> firstErr = err(new ExceptionCause(new RuntimeException("first")));
    Result<Integer> secondErr = err(new ExceptionCause(new RuntimeException("second")));
    Combiner5<Integer, Integer, Integer, Integer, Integer> combiner =
        new Combiner5<>(firstErr, secondErr, ok(3), ok(4), ok(5));

    assertThat(combiner.then((a, b, c, d, e) -> a + b + c + d + e)).isEqualTo(firstErr);
  }

  @Test
  void shouldEvaluateThenApplyWhenAllOk() {
    Combiner5<String, String, String, String, String> combiner =
        new Combiner5<>(ok("a"), ok("b"), ok("c"), ok("d"), ok("e"));
    Result<String> result = combiner.thenApply((a, b, c, d, e) -> ok(a + b + c + d + e));

    assertThat(result).isEqualTo(ok("abcde"));
  }

  @Test
  void shouldEvaluateThenTryingSuccess() {
    Combiner5<String, String, String, String, String> combiner =
        new Combiner5<>(ok("1"), ok("2"), ok("3"), ok("4"), ok("5"));
    Result<Integer> result =
        combiner.thenTrying(
            (a, b, c, d, e) ->
                Integer.parseInt(a)
                    + Integer.parseInt(b)
                    + Integer.parseInt(c)
                    + Integer.parseInt(d)
                    + Integer.parseInt(e));

    assertThat(result).isEqualTo(ok(15));
  }
}
