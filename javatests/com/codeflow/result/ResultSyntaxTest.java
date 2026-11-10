package com.codeflow.result;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.narrow;
import static com.codeflow.result.Result.ok;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Result Syntax Specification")
class ResultSyntaxTest {

  @Test
  @DisplayName("then should accept a mapper consuming a supertype and producing a subtype")
  void thenShouldRespectPecs() {
    Function<Number, Integer> numberToInteger = Number::intValue;

    Result<Number> result = ok(42).then(numberToInteger);

    assertEquals(ok(42), result);
  }

  @Test
  @DisplayName("then should support fluent widening from concrete values to interface types")
  void thenShouldSupportFluentWideningToInterfaceTypes() {
    Result<List<String>> result =
        ok("apple")
            .then(
                value -> {
                  ArrayList<String> values = new ArrayList<>();
                  values.add(value);
                  return values;
                });

    assertEquals(ok(List.of("apple")), result);
  }

  @Test
  @DisplayName("thenApply should accept a mapper consuming a supertype")
  void thenApplyShouldAcceptMapperConsumingSupertype() {
    Function<CharSequence, Result<Integer>> length = value -> ok(value.length());

    Result<Integer> result = ok("apple").thenApply(length);

    assertEquals(ok(5), result);
  }

  @Test
  @DisplayName("thenApply should support covariant success results")
  void thenApplyShouldSupportCovariantSuccessResults() {
    Result<List<String>> result =
        ok("apple")
            .thenApply(
                value -> {
                  ArrayList<String> values = new ArrayList<>();
                  values.add(value);
                  return ok(values);
                });

    assertEquals(ok(List.of("apple")), result);
  }

  @Test
  @DisplayName("thenTrying should accept throwing mapper consuming supertype and producing subtype")
  void thenTryingShouldRespectPecs() {
    Result<Number> result = ok(42).thenTrying(Number::longValue);
    assertEquals(ok(42L), result);
  }

  @Test
  @DisplayName("thenTrying with exception mapper should accept supertype consumers for exception")
  void thenTryingExceptionMapperShouldRespectPecs() {
    Result<Integer> result =
        ok("abc")
            .thenTrying(
                Integer::parseInt,
                (Exception ex) -> new FakeCause("caught: " + ex.getClass().getSimpleName()));

    assertEquals(err(new FakeCause("caught: NumberFormatException")), result);
  }

  @Test
  @DisplayName("trying should accept supplier producing subtype of requested success type")
  void tryingShouldAcceptSupplierProducingSubtype() {
    Result<List<String>> result =
        Result.trying(
            () -> {
              ArrayList<String> values = new ArrayList<>();
              values.add("apple");
              return values;
            });

    assertEquals(ok(List.of("apple")), result);
  }

  @Test
  @DisplayName("trying with exception mapper should accept supertype exception consumers")
  void tryingExceptionMapperShouldRespectPecs() {
    Result<Integer> result =
        Result.trying(
            () -> {
              throw new IOException("fail");
            },
            (Exception ex) -> new FakeCause(ex.getMessage()));

    assertEquals(err(new FakeCause("fail")), result);
  }

  @Test
  @DisplayName("also should accept consumer of a supertype")
  void alsoShouldAcceptConsumerOfSupertype() {
    List<Number> seen = new ArrayList<>();
    Consumer<Number> consumer = seen::add;

    Result<Integer> result = ok(42).also(consumer);

    assertEquals(ok(42), result);
    assertEquals(List.of(42), seen);
  }

  @Test
  @DisplayName("ifOk should accept consumer of a supertype")
  void ifOkShouldAcceptConsumerOfSupertype() {
    List<Number> seen = new ArrayList<>();
    Consumer<Number> consumer = seen::add;

    ok(42).ifOk(consumer);

    assertEquals(List.of(42), seen);
  }

  @Test
  @DisplayName("ifErr should accept consumer of a supertype cause")
  void ifErrShouldAcceptConsumerOfSupertype() {
    List<Object> seen = new ArrayList<>();
    Consumer<Object> consumer = seen::add;

    Result.<Integer>err(new FakeCause("error")).ifErr(consumer);

    assertEquals(1, seen.size());
  }

  @Test
  @DisplayName(
      "unwrapOrElse should accept fallback consuming supertype error and producing subtype value")
  void unwrapOrElseShouldRespectPecs() {
    Function<Cause, Integer> fallback = __ -> 1;

    Number value = Result.<Number>err(new FakeCause("test")).unwrapOrElse(fallback);

    assertEquals(1, value);
  }

  @Test
  @DisplayName("narrow should allow assigning wildcard Result to concrete Result type")
  void narrowShouldSupportWildcardResultAssignment() {
    Result<? extends ArrayList<String>> wildcard = ok(new ArrayList<>(List.of("apple")));

    Result<List<String>> result = narrow(wildcard);

    assertEquals(ok(List.of("apple")), result);
  }

  @Test
  @DisplayName("Result should support fluent syntax with method references")
  void resultShouldSupportFluentSyntaxWithMethodReferences() {
    Result<List<Integer>> result =
        ok(List.of("apple", "banana", "pineapple"))
            .then(values -> values.stream().map(String::length).toList());

    assertEquals(ok(List.of(5, 6, 9)), result);
  }

  @Test
  @DisplayName("Result should support exhaustive pattern matching syntax")
  void resultShouldSupportExhaustivePatternMatchingSyntax() {
    Result<Integer> result = ok(42);

    String message =
        switch (result) {
          case Result.Ok(var value) -> "ok: " + value;
          case Result.Err(var error) -> "err: " + error;
        };

    assertEquals("ok: 42", message);
  }
}
