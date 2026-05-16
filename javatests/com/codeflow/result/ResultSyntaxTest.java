package com.codeflow.result;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
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

    Result<Number, String> result = Result.<Integer, String>ok(42).then(numberToInteger);

    assertEquals(new Result.Ok<>(42), result);
  }

  @Test
  @DisplayName("then should support fluent widening from concrete values to interface types")
  void thenShouldSupportFluentWideningToInterfaceTypes() {
    Result<List<String>, String> result =
        Result.<String, String>ok("apple")
            .then(
                value -> {
                  ArrayList<String> values = new ArrayList<>();
                  values.add(value);
                  return values;
                });

    assertEquals(new Result.Ok<>(List.of("apple")), result);
  }

  @Test
  @DisplayName("thenApply should accept a mapper consuming a supertype")
  void thenApplyShouldAcceptMapperConsumingSupertype() {
    Function<CharSequence, Result<Integer, String>> length = value -> Result.ok(value.length());

    Result<Integer, String> result = Result.<String, String>ok("apple").thenApply(length);

    assertEquals(new Result.Ok<>(5), result);
  }

  @Test
  @DisplayName("thenApply should support covariant success results")
  void thenApplyShouldSupportCovariantSuccessResults() {
    Result<List<String>, RuntimeException> result =
        Result.<String, RuntimeException>ok("apple")
            .thenApply(
                value -> {
                  ArrayList<String> values = new ArrayList<>();
                  values.add(value);
                  return Result.<ArrayList<String>, IllegalArgumentException>ok(values);
                });

    assertEquals(new Result.Ok<>(List.of("apple")), result);
  }

  @Test
  @DisplayName("thenApply should support covariant Err results")
  void thenApplyShouldSupportCovariantErrResults() {
    Result<List<String>, RuntimeException> result =
        Result.<String, RuntimeException>ok("apple")
            .thenApply(
                value ->
                    Result.<ArrayList<String>, IllegalArgumentException>err(
                        new IllegalArgumentException("bad value")));

    assertInstanceOf(Result.Err.class, result);
    if (result instanceof Result.Err(var error)) {
      assertEquals("bad value", error.getMessage());
    }
  }

  @Test
  @DisplayName("thenTrying should accept throwing mapper consuming supertype and producing subtype")
  void thenTryingShouldRespectPecs() {
    Result<Number, String> result =
        Result.<Integer, String>ok(42).thenTrying(Number::longValue, Exception::getMessage);

    assertEquals(new Result.Ok<>(42L), result);
  }

  @Test
  @DisplayName("trying should accept supplier producing subtype of requested success type")
  void tryingShouldAcceptSupplierProducingSubtype() {
    Result<List<String>, String> result =
        Result.trying(
            () -> {
              ArrayList<String> values = new ArrayList<>();
              values.add("apple");
              return values;
            },
            Exception::getMessage);

    assertEquals(new Result.Ok<>(List.of("apple")), result);
  }

  @Test
  @DisplayName("also should accept consumer of a supertype")
  void alsoShouldAcceptConsumerOfSupertype() {
    List<Number> seen = new ArrayList<>();
    Consumer<Number> consumer = seen::add;

    Result<Integer, String> result = Result.<Integer, String>ok(42).also(consumer);

    assertEquals(new Result.Ok<>(42), result);
    assertEquals(List.of(42), seen);
  }

  @Test
  @DisplayName("ifOk should accept consumer of a supertype")
  void ifOkShouldAcceptConsumerOfSupertype() {
    List<Number> seen = new ArrayList<>();
    Consumer<Number> consumer = seen::add;

    Result.<Integer, String>ok(42).ifOk(consumer);

    assertEquals(List.of(42), seen);
  }

  @Test
  @DisplayName(
      "unwrapOrElse should accept fallback consuming supertype error and producing subtype value")
  void unwrapOrElseShouldRespectPecs() {
    Function<Exception, Integer> fallback = exception -> exception.getMessage().length();

    Number value =
        Result.<Number, IOException>err(new FileNotFoundException("missing"))
            .unwrapOrElse(fallback);

    assertEquals(7, value);
  }

  @Test
  @DisplayName("narrow should allow assigning wildcard Result to concrete Result type")
  void narrowShouldSupportWildcardResultAssignment() {
    Result<? extends ArrayList<String>, ? extends IllegalArgumentException> wildcard =
        Result.ok(new ArrayList<>(List.of("apple")));

    Result<List<String>, RuntimeException> result = Result.narrow(wildcard);

    assertEquals(new Result.Ok<>(List.of("apple")), result);
  }

  @Test
  @DisplayName("Result should support fluent syntax with method references")
  void resultShouldSupportFluentSyntaxWithMethodReferences() {
    Result<List<Integer>, String> result =
        Result.<List<String>, String>ok(List.of("apple", "banana", "pineapple"))
            .then(values -> values.stream().map(String::length).toList());

    assertEquals(new Result.Ok<>(List.of(5, 6, 9)), result);
  }

  @Test
  @DisplayName("Result should support exhaustive pattern matching syntax")
  void resultShouldSupportExhaustivePatternMatchingSyntax() {
    Result<Integer, String> result = Result.ok(42);

    String message =
        switch (result) {
          case Result.Ok(var value) -> "ok: " + value;
          case Result.Err(var error) -> "err: " + error;
        };

    assertEquals("ok: 42", message);
  }
}
