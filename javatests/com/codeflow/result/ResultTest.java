package com.codeflow.result;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Result Type Specification")
public class ResultTest {

  @Test
  @DisplayName("Ok should store and allow extraction of a value")
  public void testOkExtraction() {
    Result<Integer, String> ok = new Result.Ok<>(100);

    int value =
        switch (ok) {
          case Result.Ok(var v) -> v;
          case Result.Err(var e) -> fail("Expected Ok, got Err: " + e);
        };

    assertEquals(100, value);
  }

  @Test
  @DisplayName("Err should store and allow extraction of an error")
  public void testErrExtraction() {
    Result<Integer, String> err = new Result.Err<>("Timeout");

    if (err instanceof Result.Err(var msg)) {
      assertEquals("Timeout", msg);
    } else {
      fail("Expected Err type");
    }
  }

  @Test
  @DisplayName("Ok should throw NullPointerException if value is null")
  public void testOkNullCheck() {
    var exception =
        assertThrows(NullPointerException.class, () -> new Result.Ok<String, String>(null));

    assertEquals("Ok value cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("Ok should reject wrapping another Result")
  public void testOkRejectsWrappingResult() {
    Result<Integer, String> inner = Result.ok(42);

    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new Result.Ok<Result<Integer, String>, String>(inner));

    assertEquals("Cannot wrap Result in Ok", exception.getMessage());
  }

  @Test
  @DisplayName("Err should throw NullPointerException if error is null")
  public void testErrNullCheck() {
    var exception =
        assertThrows(NullPointerException.class, () -> new Result.Err<String, String>(null));

    assertEquals("Err value cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("Err should reject wrapping another Result")
  public void testErrRejectsWrappingResult() {
    Result<Integer, String> inner = Result.err("inner error");

    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new Result.Err<String, Result<Integer, String>>(inner));

    assertEquals("Cannot wrap Result in Err", exception.getMessage());
  }

  @Test
  @DisplayName("Sealed interface should support exhaustive switch")
  public void testExhaustiveness() {
    Result<Boolean, Boolean> result = new Result.Ok<>(true);

    boolean check =
        switch (result) {
          case Result.Ok(var v) -> v;
          case Result.Err(var e) -> !e;
        };

    assertTrue(check);
  }

  @Test
  @DisplayName("ok factory should create an Ok result")
  public void testOkFactory() {
    assertEquals(new Result.Ok<>("value"), Result.ok("value"));
  }

  @Test
  @DisplayName("err factory should create an Err result")
  public void testErrFactory() {
    assertEquals(new Result.Err<>("error"), Result.err("error"));
  }

  @Test
  @DisplayName("ok factory should reject null values")
  public void testOkFactoryRejectsNull() {
    var exception = assertThrows(NullPointerException.class, () -> Result.<String, String>ok(null));

    assertEquals("Ok value cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("ok factory should reject wrapping another Result")
  public void testOkFactoryRejectsWrappingResult() {
    Result<Integer, String> inner = Result.ok(42);

    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> Result.<Result<Integer, String>, String>ok(inner));

    assertEquals("Cannot wrap Result in Ok", exception.getMessage());
  }

  @Test
  @DisplayName("err factory should reject null errors")
  public void testErrFactoryRejectsNull() {
    var exception =
        assertThrows(NullPointerException.class, () -> Result.<String, String>err(null));

    assertEquals("Err value cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("err factory should reject wrapping another Result")
  public void testErrFactoryRejectsWrappingResult() {
    Result<Integer, String> inner = Result.err("inner error");

    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> Result.<String, Result<Integer, String>>err(inner));

    assertEquals("Cannot wrap Result in Err", exception.getMessage());
  }

  @Test
  @DisplayName("then should map Ok value")
  public void testThenMapsOk() {
    Result<Integer, String> result = Result.<String, String>ok("apple").then(String::length);

    assertEquals(new Result.Ok<>(5), result);
  }

  @Test
  @DisplayName("then should propagate Err")
  public void testThenPropagatesErr() {
    Result<Integer, String> result = Result.<String, String>err("failed").then(String::length);

    assertEquals(new Result.Err<>("failed"), result);
  }

  @Test
  @DisplayName("then should reject null mapper")
  public void testThenRejectsNullMapper() {
    var exception =
        assertThrows(
            NullPointerException.class, () -> Result.<String, String>ok("value").then(null));

    assertEquals("Mapper function cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("then should reject null mapper even for Err")
  public void testThenRejectsNullMapperForErr() {
    var exception =
        assertThrows(
            NullPointerException.class, () -> Result.<String, String>err("error").then(null));

    assertEquals("Mapper function cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("then should reject null mapped value")
  public void testThenRejectsNullMappedValue() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Result.<String, String>ok("value").then(value -> null));

    assertEquals("Ok value cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("then should reject mapper returning Result")
  public void testThenRejectsMapperReturningResult() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> Result.<String, String>ok("value").then(value -> Result.ok(value.length())));

    assertEquals("Cannot wrap Result in Ok", exception.getMessage());
  }

  @Test
  @DisplayName("thenApply should flat-map Ok value")
  public void testThenApplyMapsOk() {
    Result<Integer, String> result =
        Result.<String, String>ok("apple").thenApply(value -> Result.ok(value.length()));

    assertEquals(new Result.Ok<>(5), result);
  }

  @Test
  @DisplayName("thenApply should propagate mapper Err")
  public void testThenApplyCanReturnErr() {
    Result<Integer, String> result =
        Result.<String, String>ok("apple").thenApply(value -> Result.err("bad value"));

    assertEquals(new Result.Err<>("bad value"), result);
  }

  @Test
  @DisplayName("thenApply should propagate original Err without invoking mapper")
  public void testThenApplyPropagatesOriginalErr() {
    AtomicBoolean called = new AtomicBoolean(false);

    Result<Integer, String> result =
        Result.<String, String>err("original")
            .thenApply(
                value -> {
                  called.set(true);
                  return Result.ok(value.length());
                });

    assertFalse(called.get());
    assertEquals(new Result.Err<>("original"), result);
  }

  @Test
  @DisplayName("thenApply should support covariant success result")
  public void testThenApplySupportsCovariantSuccessResult() {
    Result<List<String>, String> result =
        Result.<String, String>ok("value")
            .thenApply(
                value -> {
                  ArrayList<String> values = new ArrayList<>();
                  values.add(value);
                  return Result.ok(values);
                });

    assertEquals(new Result.Ok<>(List.of("value")), result);
  }

  @Test
  @DisplayName("thenApply should reject null mapper")
  public void testThenApplyRejectsNullMapper() {
    var exception =
        assertThrows(
            NullPointerException.class, () -> Result.<String, String>ok("value").thenApply(null));

    assertEquals("Mapper function cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("thenApply should reject null mapper even for Err")
  public void testThenApplyRejectsNullMapperForErr() {
    var exception =
        assertThrows(
            NullPointerException.class, () -> Result.<String, String>err("error").thenApply(null));

    assertEquals("Mapper function cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("thenApply should reject null mapper result")
  public void testThenApplyRejectsNullMapperResult() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Result.<String, String>ok("value").thenApply(value -> null));

    assertEquals("Mapper result cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("thenTrying should reject mapper returning Result")
  public void testThenTryingRejectsMapperReturningResult() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                Result.<String, String>ok("123")
                    .thenTrying(
                        value -> Result.ok(Integer.parseInt(value)), Exception::getMessage));

    assertEquals("Cannot wrap Result in Ok", exception.getMessage());
  }

  @Test
  @DisplayName("thenTrying should map Ok when operation succeeds")
  public void testThenTryingMapsOk() {
    Result<Integer, String> result =
        Result.<String, String>ok("123").thenTrying(Integer::parseInt, Exception::getMessage);

    assertEquals(new Result.Ok<>(123), result);
  }

  @Test
  @DisplayName("thenTrying should map thrown exception into Err")
  public void testThenTryingMapsExceptionToErr() {
    Result<Integer, String> result =
        Result.<String, String>ok("abc")
            .thenTrying(
                Integer::parseInt, exception -> "invalid: " + exception.getClass().getSimpleName());

    assertEquals(new Result.Err<>("invalid: NumberFormatException"), result);
  }

  @Test
  @DisplayName("thenTrying should reject null successful mapper value")
  public void testThenTryingRejectsNullSuccessfulValue() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () ->
                Result.<String, String>ok("value")
                    .thenTrying(ignored -> null, Exception::getMessage));

    assertEquals("Ok value cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("thenTrying should propagate original Err without invoking mapper")
  public void testThenTryingPropagatesOriginalErr() {
    AtomicBoolean called = new AtomicBoolean(false);

    Result<Integer, String> result =
        Result.<String, String>err("original")
            .thenTrying(
                value -> {
                  called.set(true);
                  return Integer.parseInt(value);
                },
                Exception::getMessage);

    assertFalse(called.get());
    assertEquals(new Result.Err<>("original"), result);
  }

  @Test
  @DisplayName("thenTrying should reject null mapper")
  public void testThenTryingRejectsNullMapper() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Result.<String, String>ok("123").thenTrying(null, Exception::getMessage));

    assertEquals("Mapper function cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("thenTrying should reject null exception mapper")
  public void testThenTryingRejectsNullExceptionMapper() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Result.<String, String>ok("123").thenTrying(Integer::parseInt, null));

    assertEquals("Exception mapper cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("mapError should map Err value")
  public void testMapErrorMapsErr() {
    Result<String, Integer> result = Result.<String, String>err("error").mapError(String::length);

    assertEquals(new Result.Err<>(5), result);
  }

  @Test
  @DisplayName("mapError should propagate Ok")
  public void testMapErrorPropagatesOk() {
    Result<String, Integer> result = Result.<String, String>ok("value").mapError(String::length);

    assertEquals(new Result.Ok<>("value"), result);
  }

  @Test
  @DisplayName("mapError should reject null mapper")
  public void testMapErrorRejectsNullMapper() {
    var exception =
        assertThrows(
            NullPointerException.class, () -> Result.<String, String>err("error").mapError(null));

    assertEquals("Error mapper function cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("mapError should reject null mapper even for Ok")
  public void testMapErrorRejectsNullMapperForOk() {
    var exception =
        assertThrows(
            NullPointerException.class, () -> Result.<String, String>ok("value").mapError(null));

    assertEquals("Error mapper function cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("mapError should reject null mapped value")
  public void testMapErrorRejectsNullMappedValue() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Result.<String, String>err("error").mapError(error -> null));

    assertEquals("Err value cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("recover should map Err to Ok")
  public void testRecoverMapsErrToOk() {
    Result<Integer, String> result =
        Result.<Integer, String>err("not found").recover(error -> Result.ok(error.length()));

    assertEquals(new Result.Ok<>(9), result);
  }

  @Test
  @DisplayName("recover should map Err to another Err")
  public void testRecoverMapsErrToErr() {
    Result<Integer, String> result =
        Result.<Integer, String>err("not found")
            .recover(error -> Result.err("recovered: " + error));

    assertEquals(new Result.Err<>("recovered: not found"), result);
  }

  @Test
  @DisplayName("recover should propagate Ok")
  public void testRecoverPropagatesOk() {
    Result<Integer, String> result =
        Result.<Integer, String>ok(123).recover(error -> Result.ok(error.length()));

    assertEquals(new Result.Ok<>(123), result);
  }

  @Test
  @DisplayName("recover should reject null mapper")
  public void testRecoverRejectsNullMapper() {
    var exception =
        assertThrows(
            NullPointerException.class, () -> Result.<Integer, String>err("error").recover(null));

    assertEquals("Recover function cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("recover should reject null mapped result")
  public void testRecoverRejectsNullMappedResult() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Result.<Integer, String>err("error").recover(error -> null));

    assertEquals("Recover result cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("also should run consumer for Ok and return same Result")
  public void testAlsoRunsConsumerForOk() {
    AtomicInteger seen = new AtomicInteger();

    Result<Integer, String> result = Result.<Integer, String>ok(42);
    Result<Integer, String> returned = result.also(seen::set);

    assertSame(result, returned);
    assertEquals(42, seen.get());
  }

  @Test
  @DisplayName("also should not run consumer for Err and should return same Result")
  public void testAlsoDoesNotRunConsumerForErr() {
    AtomicBoolean called = new AtomicBoolean(false);

    Result<Integer, String> result = Result.err("error");
    Result<Integer, String> returned = result.also(value -> called.set(true));

    assertSame(result, returned);
    assertFalse(called.get());
  }

  @Test
  @DisplayName("also should reject null consumer")
  public void testAlsoRejectsNullConsumer() {
    var exception =
        assertThrows(NullPointerException.class, () -> Result.<Integer, String>ok(1).also(null));
    assertEquals("Consumer cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("ifOk should consume Ok value")
  public void testIfOkConsumesOk() {
    AtomicInteger seen = new AtomicInteger();

    Result.<Integer, String>ok(42).ifOk(seen::set);

    assertEquals(42, seen.get());
  }

  @Test
  @DisplayName("ifOk should do nothing for Err")
  public void testIfOkForErr() {
    AtomicBoolean called = new AtomicBoolean(false);

    Result.<Integer, String>err("error").ifOk(value -> called.set(true));

    assertFalse(called.get());
  }

  @Test
  @DisplayName("ifOk should reject null consumer")
  public void testIfOkRejectsNullConsumer() {
    var exception =
        assertThrows(
            NullPointerException.class, () -> Result.<Integer, String>err("error").ifOk(null));

    assertEquals("Consumer cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("ifErr should consume Err value")
  public void testIfErrConsumesErr() {
    AtomicInteger seen = new AtomicInteger();

    Result.<Integer, String>err("error").ifErr(error -> seen.set(error.length()));

    assertEquals(5, seen.get());
  }

  @Test
  @DisplayName("ifErr should do nothing for Ok")
  public void testIfErrForOk() {
    AtomicBoolean called = new AtomicBoolean(false);

    Result.<Integer, String>ok(42).ifErr(error -> called.set(true));

    assertFalse(called.get());
  }

  @Test
  @DisplayName("ifErr should reject null consumer")
  public void testIfErrRejectsNullConsumer() {
    var exception =
        assertThrows(NullPointerException.class, () -> Result.<Integer, String>ok(42).ifErr(null));

    assertEquals("Consumer cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("unwrap should return Ok value")
  public void testUnwrapReturnsOkValue() {
    assertEquals(42, Result.<Integer, String>ok(42).unwrap());
  }

  @Test
  @DisplayName("unwrap should throw for Err")
  public void testUnwrapThrowsForErr() {
    var exception =
        assertThrows(
            IllegalStateException.class, () -> Result.<Integer, String>err("error").unwrap());

    assertEquals("Cannot unwrap Err value: error", exception.getMessage());
  }

  @Test
  @DisplayName("unwrapOr should return Ok value")
  public void testUnwrapOrReturnsOkValue() {
    assertEquals(42, Result.<Integer, String>ok(42).unwrapOr(100));
  }

  @Test
  @DisplayName("unwrapOr should return fallback for Err")
  public void testUnwrapOrReturnsFallbackForErr() {
    assertEquals(100, Result.<Integer, String>err("error").unwrapOr(100));
  }

  @Test
  @DisplayName("unwrapOrElse should return Ok value without invoking fallback")
  public void testUnwrapOrElseReturnsOkValue() {
    AtomicBoolean called = new AtomicBoolean(false);

    int value =
        Result.<Integer, String>ok(42)
            .unwrapOrElse(
                error -> {
                  called.set(true);
                  return 100;
                });

    assertEquals(42, value);
    assertFalse(called.get());
  }

  @Test
  @DisplayName("unwrapOrElse should compute fallback for Err")
  public void testUnwrapOrElseComputesFallbackForErr() {
    int value = Result.<Integer, String>err("error").unwrapOrElse(String::length);

    assertEquals(5, value);
  }

  @Test
  @DisplayName("unwrapOrElse should reject null fallback")
  public void testUnwrapOrElseRejectsNullFallback() {
    var exception =
        assertThrows(
            NullPointerException.class, () -> Result.<Integer, String>ok(42).unwrapOrElse(null));

    assertEquals("Fallback function cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("unwrapOrElseThrow should return Ok value")
  public void testUnwrapOrElseThrowReturnsOkValue() throws Exception {
    assertEquals(42, Result.<Integer, Exception>ok(42).unwrapOrElseThrow(e -> new Exception("")));
  }

  @Test
  @DisplayName("unwrapOrElseThrow should throw mapped exception for Err")
  public void testUnwrapOrElseThrowThrowsMappedExceptionForErr() {
    var exception =
        assertThrows(
            IOException.class,
            () ->
                Result.<Integer, String>err("file not found").unwrapOrElseThrow(IOException::new));

    assertEquals("file not found", exception.getMessage());
  }

  @Test
  @DisplayName("unwrapOrElseThrow should reject null exception mapper")
  public void testUnwrapOrElseThrowRejectsNullExceptionMapper() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Result.<Integer, String>ok(42).unwrapOrElseThrow(null));

    assertEquals("Exception mapper cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("narrow should narrow wildcard Result")
  public void testNarrow() {
    Result<? extends List<String>, ? extends CharSequence> wildcard =
        Result.ok(new ArrayList<>(List.of("a", "b")));

    Result<List<String>, CharSequence> narrowed = Result.narrow(wildcard);

    assertEquals(new Result.Ok<>(List.of("a", "b")), narrowed);
  }

  @Test
  @DisplayName("trying should reject supplier returning Result")
  public void testTryingRejectsSupplierReturningResult() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                Result.<Result<Integer, String>, String>trying(
                    () -> Result.ok(42), Exception::getMessage));

    assertEquals("Cannot wrap Result in Ok", exception.getMessage());
  }

  @Test
  @DisplayName("trying should return Ok when supplier succeeds")
  public void testTryingReturnsOkWhenSupplierSucceeds() {
    Result<Integer, String> result =
        Result.trying(() -> Integer.parseInt("123"), Exception::getMessage);

    assertEquals(new Result.Ok<>(123), result);
  }

  @Test
  @DisplayName("trying should map exception into Err when supplier throws")
  public void testTryingMapsExceptionToErr() {
    Result<Integer, String> result =
        Result.trying(
            () -> {
              throw new IOException("disk failed");
            },
            exception -> "io: " + exception.getMessage());

    assertEquals(new Result.Err<>("io: disk failed"), result);
  }

  @Test
  @DisplayName("trying should reject null supplier")
  public void testTryingRejectsNullSupplier() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Result.<Integer, String>trying(null, Exception::getMessage));

    assertEquals("Supplier cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("trying should reject null exception mapper")
  public void testTryingRejectsNullExceptionMapper() {
    var exception =
        assertThrows(
            NullPointerException.class, () -> Result.<Integer, String>trying(() -> 1, null));

    assertEquals("Exception mapper cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("trying should reject null successful supplier value")
  public void testTryingRejectsNullSuccessfulValue() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () -> Result.<String, String>trying(() -> null, Exception::getMessage));

    assertEquals("Ok value cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("trying should reject null mapped error")
  public void testTryingRejectsNullMappedError() {
    var exception =
        assertThrows(
            NullPointerException.class,
            () ->
                Result.<Integer, String>trying(
                    () -> {
                      throw new IOException("disk failed");
                    },
                    ignored -> null));

    assertEquals("Err value cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("narrow should reject null Result")
  public void testNarrowRejectsNull() {
    var exception =
        assertThrows(NullPointerException.class, () -> Result.<String, String>narrow(null));

    assertEquals("Result cannot be null", exception.getMessage());
  }
}
