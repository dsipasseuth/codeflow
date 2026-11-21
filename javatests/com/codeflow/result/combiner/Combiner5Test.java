package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeflow.result.ExceptionCause;
import com.codeflow.result.Result;
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
  void shouldExecuteAndThenPentaFunctionWhenAllOk() {
    Combiner5<String, String, String, String, String> combiner =
        new Combiner5<>(ok("a"), ok("b"), ok("c"), ok("d"), ok("e"));
    Combiner6<String, String, String, String, String, String> result =
        combiner.andThen((a, b, c, d, e) -> a + b + c + d + e);

    assertThat(result.result6()).isEqualTo(ok("abcde"));
  }

  // --- andThenApply Tests ---

  @Test
  void shouldExecuteAndThenApplySupplierWhenAllOk() {
    Combiner5<String, Integer, Boolean, Double, Character> combiner =
        new Combiner5<>(ok("a"), ok(1), ok(true), ok(2.0), ok('x'));
    Combiner6<String, Integer, Boolean, Double, Character, String> result =
        combiner.andThenApply(() -> ok("applied"));

    assertThat(result.result6()).isEqualTo(ok("applied"));
  }

  @Test
  void shouldExecuteAndThenApplyPentaFunctionWhenAllOk() {
    Combiner5<Integer, Integer, Integer, Integer, Integer> combiner =
        new Combiner5<>(ok(1), ok(2), ok(3), ok(4), ok(5));
    Combiner6<Integer, Integer, Integer, Integer, Integer, Integer> result =
        combiner.andThenApply((a, b, c, d, e) -> ok(a + b + c + d + e));

    assertThat(result.result6()).isEqualTo(ok(15));
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
  void shouldExecuteAndThenTryingPentaFunctionSuccess() {
    Combiner5<Integer, Integer, Integer, Integer, Integer> combiner =
        new Combiner5<>(ok(10), ok(2), ok(1), ok(1), ok(1));
    Combiner6<Integer, Integer, Integer, Integer, Integer, Integer> result =
        combiner.andThenTrying((a, b, c, d, e) -> (a / b) + c + d + e);

    assertThat(result.result6()).isEqualTo(ok(8));
  }

  // --- also Tests ---

  @Test
  void shouldExecuteAlsoWhenAllOk() {
    boolean[] executed = {false};
    Combiner5<String, Integer, Boolean, Double, Character> combiner =
        new Combiner5<>(ok("foo"), ok(42), ok(true), ok(3.14), ok('z'));

    Combiner5<String, Integer, Boolean, Double, Character> returned =
        combiner.also(
            (s, i, b, d, c) -> {
              executed[0] = true;
              assertThat(s).isEqualTo("foo");
              assertThat(i).isEqualTo(42);
              assertThat(c).isEqualTo('z');
            });

    assertThat(executed[0]).isTrue();
    assertThat(returned).isEqualTo(combiner);
  }

  @Test
  void shouldSkipAlsoWhenAnyResultIsErr() {
    boolean[] executed = {false};
    Result<String> errResult = err(new ExceptionCause(new RuntimeException("fail")));
    Combiner5<String, Integer, Boolean, Double, Character> combiner =
        new Combiner5<>(errResult, ok(42), ok(true), ok(3.14), ok('z'));

    combiner.also((s, i, b, d, c) -> executed[0] = true);

    assertThat(executed[0]).isFalse();
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
