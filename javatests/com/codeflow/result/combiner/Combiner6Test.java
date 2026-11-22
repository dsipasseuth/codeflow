package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeflow.result.ExceptionCause;
import com.codeflow.result.Functions.Function6;
import com.codeflow.result.Functions.ThrowingFunction6;
import com.codeflow.result.Result;
import org.junit.jupiter.api.Test;

class Combiner6Test {

  // --- Constructor & Validation Tests ---

  @Test
  void shouldThrowNullPointerExceptionWhenFirstResultIsNull() {
    assertThatThrownBy(() -> new Combiner6<>(null, ok("b"), ok("c"), ok("d"), ok("e"), ok("f")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("First result cannot be null");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenSixthResultIsNull() {
    assertThatThrownBy(() -> new Combiner6<>(ok("a"), ok("b"), ok("c"), ok("d"), ok("e"), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Sixth result cannot be null");
  }

  // --- peek Tests ---

  @Test
  void shouldExecutePeekWhenAllOk() {
    boolean[] executed = {false};
    Combiner6<String, Integer, Boolean, Double, Character, Long> combiner =
        new Combiner6<>(ok("foo"), ok(42), ok(true), ok(3.14), ok('z'), ok(100L));

    Combiner6<String, Integer, Boolean, Double, Character, Long> returned =
        combiner.peekValues(
            (s, i, _, _, _, l) -> {
              executed[0] = true;
              assertThat(s).isEqualTo("foo");
              assertThat(i).isEqualTo(42);
              assertThat(l).isEqualTo(100L);
            });

    assertThat(executed[0]).isTrue();
    assertThat(returned).isEqualTo(combiner);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenConsumerIsNull() {
    Combiner6<String, Integer, Boolean, Double, Character, Long> combiner =
        new Combiner6<>(ok("foo"), ok(42), ok(true), ok(3.14), ok('z'), ok(100L));

    assertThatThrownBy(() -> combiner.peekValues(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldRejectNullConsumerConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner6<String, Integer, Boolean, Double, Character, Long> okCombiner =
        new Combiner6<>(ok("foo"), ok(42), ok(true), ok(3.14), ok('z'), ok(100L));
    Combiner6<String, Integer, Boolean, Double, Character, Long> errCombiner =
        new Combiner6<>(errResult, ok(42), ok(true), ok(3.14), ok('z'), ok(100L));

    assertThatThrownBy(() -> okCombiner.peekValues(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errCombiner.peekValues(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldSkipPeekWhenAnyResultIsErr() {
    boolean[] executed = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner6<String, Integer, Boolean, Double, Character, Long> combiner =
        new Combiner6<>(errResult, ok(42), ok(true), ok(3.14), ok('z'), ok(100L));

    combiner.peekValues((_, _, _, _, _, _) -> executed[0] = true);

    assertThat(executed[0]).isFalse();
  }

  // --- Terminal then, thenApply, thenTrying Tests ---

  @Test
  void shouldEvaluateThenWhenAllOk() {
    Combiner6<Integer, Integer, Integer, Integer, Integer, Integer> combiner =
        new Combiner6<>(ok(1), ok(2), ok(3), ok(4), ok(5), ok(6));
    Result<Integer> result = combiner.then((a, b, c, d, e, f) -> a + b + c + d + e + f);

    assertThat(result).isEqualTo(ok(21));
  }

  @Test
  void shouldPropagateFirstErrInThen() {
    Result<Integer> firstErr = err(new ExceptionCause(new RuntimeException("first")));
    Result<Integer> secondErr = err(new ExceptionCause(new RuntimeException("second")));
    Combiner6<Integer, Integer, Integer, Integer, Integer, Integer> combiner =
        new Combiner6<>(firstErr, secondErr, ok(3), ok(4), ok(5), ok(6));

    assertThat(combiner.then((a, b, c, d, e, f) -> a + b + c + d + e + f)).isEqualTo(firstErr);
  }

  @Test
  void shouldRejectNullFnInThenConsistentlyOnOkAndErr() {
    Result<Integer> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner6<Integer, Integer, Integer, Integer, Integer, Integer> okCombiner =
        new Combiner6<>(ok(1), ok(2), ok(3), ok(4), ok(5), ok(6));
    Combiner6<Integer, Integer, Integer, Integer, Integer, Integer> errCombiner =
        new Combiner6<>(errResult, ok(2), ok(3), ok(4), ok(5), ok(6));

    assertThatThrownBy(
            () ->
                okCombiner.then(
                    (Function6<Integer, Integer, Integer, Integer, Integer, Integer, Integer>)
                        null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () ->
                errCombiner.then(
                    (Function6<Integer, Integer, Integer, Integer, Integer, Integer, Integer>)
                        null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldEvaluateThenApplyWhenAllOk() {
    Combiner6<String, String, String, String, String, String> combiner =
        new Combiner6<>(ok("a"), ok("b"), ok("c"), ok("d"), ok("e"), ok("f"));
    Result<String> result = combiner.thenApply((a, b, c, d, e, f) -> ok(a + b + c + d + e + f));

    assertThat(result).isEqualTo(ok("abcdef"));
  }

  @Test
  void shouldRejectNullFnInThenApplyConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner6<String, String, String, String, String, String> okCombiner =
        new Combiner6<>(ok("a"), ok("b"), ok("c"), ok("d"), ok("e"), ok("f"));
    Combiner6<String, String, String, String, String, String> errCombiner =
        new Combiner6<>(errResult, ok("b"), ok("c"), ok("d"), ok("e"), ok("f"));

    assertThatThrownBy(
            () ->
                okCombiner.thenApply(
                    (Function6<String, String, String, String, String, String, Result<String>>)
                        null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () ->
                errCombiner.thenApply(
                    (Function6<String, String, String, String, String, String, Result<String>>)
                        null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldEvaluateThenTryingSuccess() {
    Combiner6<String, String, String, String, String, String> combiner =
        new Combiner6<>(ok("1"), ok("2"), ok("3"), ok("4"), ok("5"), ok("6"));
    Result<Integer> result =
        combiner.thenTrying(
            (a, b, c, d, e, f) ->
                Integer.parseInt(a)
                    + Integer.parseInt(b)
                    + Integer.parseInt(c)
                    + Integer.parseInt(d)
                    + Integer.parseInt(e)
                    + Integer.parseInt(f));

    assertThat(result).isEqualTo(ok(21));
  }

  @Test
  void shouldEvaluateThenTryingFailureWithCustomMapper() {
    Combiner6<String, String, String, String, String, String> combiner =
        new Combiner6<>(ok("bad"), ok("2"), ok("3"), ok("4"), ok("5"), ok("6"));
    Result<Integer> result =
        combiner.thenTrying(
            (a, _, _, _, _, _) -> Integer.parseInt(a),
            _ -> new ExceptionCause(new RuntimeException("Parsing failed")));

    assertThat(result).isInstanceOf(Result.Err.class);
  }

  @Test
  void shouldRejectNullArgumentsInThenTryingConsistentlyOnOkAndErr() {
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner6<String, String, String, String, String, String> okCombiner =
        new Combiner6<>(ok("1"), ok("2"), ok("3"), ok("4"), ok("5"), ok("6"));
    Combiner6<String, String, String, String, String, String> errCombiner =
        new Combiner6<>(errResult, ok("2"), ok("3"), ok("4"), ok("5"), ok("6"));

    assertThatThrownBy(
            () ->
                okCombiner.thenTrying(
                    (ThrowingFunction6<String, String, String, String, String, String, Integer>)
                        null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () ->
                errCombiner.thenTrying(
                    (ThrowingFunction6<String, String, String, String, String, String, Integer>)
                        null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> okCombiner.thenTrying((a, _, _, _, _, _) -> Integer.parseInt(a), null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () -> errCombiner.thenTrying((a, _, _, _, _, _) -> Integer.parseInt(a), null))
        .isInstanceOf(NullPointerException.class);
  }
}
