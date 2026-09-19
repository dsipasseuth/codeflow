package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeflow.result.ExceptionCause;
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

  // --- also Tests ---

  @Test
  void shouldExecuteAlsoWhenAllOk() {
    boolean[] executed = {false};
    Combiner6<String, Integer, Boolean, Double, Character, Long> combiner =
        new Combiner6<>(ok("foo"), ok(42), ok(true), ok(3.14), ok('z'), ok(100L));

    Combiner6<String, Integer, Boolean, Double, Character, Long> returned =
        combiner.also(
            (s, i, b, d, c, l) -> {
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

    assertThatThrownBy(() -> combiner.also(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Consumer cannot be null");
  }

  @Test
  void shouldSkipAlsoWhenAnyResultIsErr() {
    boolean[] executed = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner6<String, Integer, Boolean, Double, Character, Long> combiner =
        new Combiner6<>(errResult, ok(42), ok(true), ok(3.14), ok('z'), ok(100L));

    combiner.also((s, i, b, d, c, l) -> executed[0] = true);

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
  void shouldEvaluateThenApplyWhenAllOk() {
    Combiner6<String, String, String, String, String, String> combiner =
        new Combiner6<>(ok("a"), ok("b"), ok("c"), ok("d"), ok("e"), ok("f"));
    Result<String> result = combiner.thenApply((a, b, c, d, e, f) -> ok(a + b + c + d + e + f));

    assertThat(result).isEqualTo(ok("abcdef"));
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
            (a, b, c, d, e, f) -> Integer.parseInt(a),
            ex -> new ExceptionCause(new RuntimeException("Parsing failed")));

    assertThat(result).isInstanceOf(Result.Err.class);
  }
}
