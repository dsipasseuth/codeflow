package com.codeflow.result;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static com.codeflow.result.Result.trying;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Result Type Specification")
@NullMarked
public class ResultTest {

  static Cause fakeCause() {
    return new FakeCause("Fake");
  }

  @Test
  @DisplayName("Ok should store and allow extraction of a value")
  public void testOkExtraction() {
    Result<Integer> ok = ok(100);

    int value =
        switch (ok) {
          case Result.Ok(var v) -> v;
          case Result.Err(var reason) -> fail("Expected Ok, got Err: " + reason);
        };

    assertEquals(100, value);
  }

  @Test
  @DisplayName("Err should store and allow extraction of an error")
  public void testErrExtraction() {
    Result<Integer> err = err(new FakeCause("test"));

    if (err instanceof Result.Err(var msg)) {
      assertEquals(new FakeCause("test"), msg);
    } else {
      fail("Expected Err type");
    }
  }

  @Test
  @DisplayName("Ok should throw NullPointerException if value is null")
  public void testOkNullCheck() {
    var exception = assertThrows(NullPointerException.class, () -> ok(null));

    assertNull(exception.getMessage());
  }

  @Test
  @DisplayName("Ok should reject wrapping another Result")
  public void testOkRejectsWrappingResult() {
    Result<Integer> inner = ok(42);

    var exception = assertThrows(IllegalArgumentException.class, () -> ok(inner));

    assertEquals("Cannot wrap Result in Ok", exception.getMessage());
  }

  @Test
  @DisplayName("Err should throw NullPointerException if error is null")
  public void testErrNullCheck() {
    var exception = assertThrows(NullPointerException.class, () -> err(null));

    assertNull(exception.getMessage());
  }

  @Test
  @DisplayName("Sealed interface should support exhaustive switch")
  public void testExhaustiveness() {
    Result<Boolean> result = ok(true);

    boolean check =
        switch (result) {
          case Result.Ok(var v) -> v;
          case Result.Err(var _) -> fail("switch branch expected to be unreachable");
        };

    assertTrue(check);
  }

  @Test
  @DisplayName("ok factory should create an Ok result")
  public void testOkFactory() {
    assertEquals(ok("value"), ok("value"));
  }

  @Test
  @DisplayName("err factory should create an Err result")
  public void testErrFactory() {
    assertEquals(err(fakeCause()), err(fakeCause()));
  }

  @Test
  @DisplayName("ok factory should reject null values")
  public void testOkFactoryRejectsNull() {
    var exception = assertThrows(NullPointerException.class, () -> ok(null));

    assertNull(exception.getMessage());
  }

  @Test
  @DisplayName("ok factory should reject wrapping another Result")
  public void testOkFactoryRejectsWrappingResult() {
    Result<Integer> inner = ok(42);

    var exception = assertThrows(IllegalArgumentException.class, () -> ok(inner));
    assertEquals("Cannot wrap Result in Ok", exception.getMessage());
  }

  @Test
  @DisplayName("err factory should reject null errors")
  public void testErrFactoryRejectsNull() {
    var exception = assertThrows(NullPointerException.class, () -> err(null));

    assertNull(exception.getMessage());
  }

  @Test
  @DisplayName("then should map Ok value")
  public void testThenMapsOk() {
    Result<Integer> result = ok("apple").then(String::length);

    assertEquals(ok(5), result);
  }

  @Test
  @DisplayName("then should propagate Err")
  public void testThenPropagatesErr() {
    Result<Integer> result = Result.<String>err(fakeCause()).then(String::length);

    assertEquals(err(fakeCause()), result);
  }

  @Test
  @DisplayName("then should reject null mapper consistently on Ok and Err")
  public void testThenRejectsNullMapperOnOkAndErr() {
    assertThrows(NullPointerException.class, () -> ok("value").then(null));
    assertThrows(NullPointerException.class, () -> Result.<String>err(fakeCause()).then(null));
  }

  @Test
  @DisplayName("then should reject mapper returning Result")
  public void testThenRejectsMapperReturningResult() {
    var exception =
        assertThrows(
            IllegalArgumentException.class, () -> ok("value").then(value -> ok(value.length())));

    assertEquals("Cannot wrap Result in Ok", exception.getMessage());
  }

  @Test
  @DisplayName("thenApply should flat-map Ok value")
  public void testThenApplyMapsOk() {
    Result<Integer> result = ok("apple").thenApply(value -> ok(value.length()));

    assertEquals(ok(5), result);
  }

  @Test
  @DisplayName("thenApply should propagate mapper Err")
  public void testThenApplyCanReturnErr() {
    Result<Integer> result = ok("apple").thenApply(_ -> err(fakeCause()));

    assertEquals(err(fakeCause()), result);
  }

  @Test
  @DisplayName("thenApply should propagate original Err without invoking mapper")
  public void testThenApplyPropagatesOriginalErr() {
    AtomicBoolean called = new AtomicBoolean(false);

    Result<Integer> result =
        Result.<String>err(fakeCause())
            .thenApply(
                value -> {
                  called.set(true);
                  return ok(value.length());
                });

    assertFalse(called.get());
    assertEquals(err(fakeCause()), result);
  }

  @Test
  @DisplayName("thenApply should reject null mapper consistently on Ok and Err")
  public void testThenApplyRejectsNullMapperOnOkAndErr() {
    assertThrows(NullPointerException.class, () -> ok("value").thenApply(null));
    assertThrows(NullPointerException.class, () -> Result.<String>err(fakeCause()).thenApply(null));
  }

  @Test
  @DisplayName("thenApply should support covariant success result")
  public void testThenApplySupportsCovariantSuccessResult() {
    Result<List<String>> result =
        ok("value")
            .thenApply(
                value -> {
                  ArrayList<String> values = new ArrayList<>();
                  values.add(value);
                  return ok(values);
                });

    assertEquals(ok(List.of("value")), result);
  }

  @Test
  @DisplayName("thenTrying should reject mapper returning Result")
  public void testThenTryingRejectsMapperReturningResult() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> ok("123").thenTrying(value -> ok(Integer.parseInt(value))));

    assertEquals("Cannot wrap Result in Ok", exception.getMessage());
  }

  @Test
  @DisplayName("thenTrying should map Ok when operation succeeds")
  public void testThenTryingMapsOk() {
    Result<Integer> result = ok("123").thenTrying(Integer::parseInt);

    assertEquals(ok(123), result);
  }

  @Test
  @DisplayName("thenTrying should map thrown exception into Err")
  public void testThenTryingMapsExceptionToErr() {
    Result<Integer> result = ok("abc").thenTrying(Integer::parseInt, _ -> new FakeCause("error"));
    assertEquals(err(new FakeCause("error")), result);
  }

  @Test
  @DisplayName("thenTrying should use default exception handler when mapper is omitted")
  public void testThenTryingUsesDefaultExceptionHandler() {
    Result<Integer> result =
        ok("abc")
            .thenTrying(
                _ -> {
                  throw new IOException("boom");
                });

    assertTrue(
        result instanceof Result.Err<Integer>(Cause cause) && cause instanceof ExceptionCause);
  }

  @Test
  @DisplayName("thenTrying should propagate original Err without invoking mapper")
  public void testThenTryingPropagatesOriginalErr() {
    AtomicBoolean called = new AtomicBoolean(false);

    Result<Integer> result =
        Result.<String>err(fakeCause())
            .thenTrying(
                value -> {
                  called.set(true);
                  return Integer.parseInt(value);
                });

    assertFalse(called.get());
    assertEquals(err(fakeCause()), result);
  }

  @Test
  @DisplayName("thenTrying should reject null arguments consistently on Ok and Err")
  public void testThenTryingRejectsNullArgumentsOnOkAndErr() {
    assertThrows(NullPointerException.class, () -> ok("123").thenTrying(null));
    assertThrows(
        NullPointerException.class, () -> Result.<String>err(fakeCause()).thenTrying(null));
    assertThrows(NullPointerException.class, () -> ok("123").thenTrying(Integer::parseInt, null));
    assertThrows(
        NullPointerException.class,
        () -> Result.<String>err(fakeCause()).thenTrying(Integer::parseInt, null));
  }

  @Test
  @DisplayName(
      "thenTrying should re-interrupt thread and throw RuntimeException on InterruptedException")
  public void testThenTryingHandlesInterruptedException() {
    try {
      var exception =
          assertThrows(
              RuntimeException.class,
              () ->
                  ok("value")
                      .thenTrying(
                          _ -> {
                            throw new InterruptedException("stop");
                          }));

      assertEquals("Execution interrupted", exception.getMessage());
      assertTrue(Thread.currentThread().isInterrupted());
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  @DisplayName("onError should map Err value")
  public void testOnErrorMapsErr() {
    Result<String> result = Result.<String>err(fakeCause()).onError(_ -> ok("value"));

    assertEquals(ok("value"), result);
  }

  @Test
  @DisplayName("onError should propagate Ok")
  public void testOnErrorPropagatesOk() {
    Result<String> result = ok("value").onError(_ -> ok("broken"));

    assertEquals(ok("value"), result);
  }

  @Test
  @DisplayName("onError should reject null mapper consistently on Ok and Err")
  public void testOnErrorRejectsNullMapperOnOkAndErr() {
    assertThrows(NullPointerException.class, () -> ok("value").onError(null));
    assertThrows(NullPointerException.class, () -> Result.<String>err(fakeCause()).onError(null));
  }

  @Test
  @DisplayName("peek should run consumer for Ok and return same Result")
  public void testPeekRunsConsumerForOk() {
    AtomicInteger seen = new AtomicInteger();

    Result<Integer> result = ok(42);
    Result<Integer> returned = result.peekOk(seen::set);

    assertSame(result, returned);
    assertEquals(42, seen.get());
  }

  @Test
  @DisplayName("peek should not run consumer for Err and should return same Result")
  public void testPeekDoesNotRunConsumerForErr() {
    AtomicBoolean called = new AtomicBoolean(false);

    Result<Integer> result = err(fakeCause());
    Result<Integer> returned = result.peekOk(_ -> called.set(true));

    assertSame(result, returned);
    assertFalse(called.get());
  }

  @Test
  @DisplayName("peek should reject null consumer consistently on Ok and Err")
  public void testPeekRejectsNullConsumerOnOkAndErr() {
    assertThrows(NullPointerException.class, () -> ok(42).peekOk(null));
    assertThrows(NullPointerException.class, () -> Result.<Integer>err(fakeCause()).peekOk(null));
  }

  @Test
  void shouldInvokeConsumerAndReturnSameErrInstanceWhenErr() {
    Cause cause = new FakeCause("boom");
    Result<String> result = err(cause);
    List<Cause> observed = new ArrayList<>();

    Result<String> returned = result.peekErr(observed::add);

    assertThat(observed).containsExactly(cause);
    assertThat(returned).isSameAs(result);
  }

  @Test
  void shouldSkipConsumerAndReturnSameOkInstanceWhenOk() {
    Result<String> result = ok("value");
    AtomicBoolean called = new AtomicBoolean(false);

    Result<String> returned = result.peekErr(cause -> called.set(true));

    assertThat(called).isFalse();
    assertThat(returned).isSameAs(result);
  }

  @Test
  void shouldRejectNullConsumerConsistentlyOnOkAndErr() {
    Result<String> okResult = ok("value");
    Result<String> errResult = err(new FakeCause("boom"));

    assertThatThrownBy(() -> okResult.peekErr(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> errResult.peekErr(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldPropagateExceptionThrownByConsumer() {
    Result<String> errResult = err(new FakeCause("boom"));
    RuntimeException boom = new IllegalStateException("consumer failed");

    assertThatThrownBy(
            () ->
                errResult.peekErr(
                    cause -> {
                      throw boom;
                    }))
        .isSameAs(boom);
  }

  @Test
  @DisplayName("ifOk should run consumer only on Ok")
  public void testIfOk() {
    AtomicInteger seen = new AtomicInteger();

    ok(7).ifOk(seen::set);
    assertEquals(7, seen.get());

    Result.<Integer>err(fakeCause()).ifOk(_ -> fail("Should not run for Err"));
  }

  @Test
  @DisplayName("ifOk should reject null consumer consistently on Ok and Err")
  public void testIfOkRejectsNullConsumerOnOkAndErr() {
    assertThrows(NullPointerException.class, () -> ok(7).ifOk(null));
    assertThrows(NullPointerException.class, () -> Result.<Integer>err(fakeCause()).ifOk(null));
  }

  @Test
  @DisplayName("ifErr should run consumer only on Err")
  public void testIfErr() {
    AtomicBoolean called = new AtomicBoolean(false);

    ok(7).ifErr(_ -> fail("Should not run for Ok"));
    Result.<Integer>err(fakeCause()).ifErr(_ -> called.set(true));

    assertTrue(called.get());
  }

  @Test
  @DisplayName("ifErr should reject null consumer consistently on Ok and Err")
  public void testIfErrRejectsNullConsumerOnOkAndErr() {
    assertThrows(NullPointerException.class, () -> ok(7).ifErr(null));
    assertThrows(NullPointerException.class, () -> Result.<Integer>err(fakeCause()).ifErr(null));
  }

  @Test
  @DisplayName("unwrap should return Ok value")
  public void testUnwrapReturnsOkValue() {
    assertEquals(42, ok(42).unwrap());
  }

  @Test
  @DisplayName("unwrap should throw for Err")
  public void testUnwrapThrowsForErr() {
    var exception =
        assertThrows(IllegalStateException.class, () -> Result.<Integer>err(fakeCause()).unwrap());

    assertEquals("Cannot unwrap Err value: FakeCause[message=Fake]", exception.getMessage());
  }

  @Test
  @DisplayName("unwrapOr should return Ok value")
  public void testUnwrapOrReturnsOkValue() {
    assertEquals(42, ok(42).unwrapOr(100));
  }

  @Test
  @DisplayName("unwrapOr should return fallback for Err")
  public void testUnwrapOrReturnsFallbackForErr() {
    assertEquals(100, Result.<Integer>err(fakeCause()).unwrapOr(100));
  }

  @Test
  @DisplayName("unwrapOr should reject null fallback consistently on Ok and Err")
  public void testUnwrapOrRejectsNullFallbackOnOkAndErr() {
    assertThrows(NullPointerException.class, () -> ok(42).unwrapOr(null));
    assertThrows(NullPointerException.class, () -> Result.<Integer>err(fakeCause()).unwrapOr(null));
  }

  @Test
  @DisplayName("unwrapOrElse(Function) should return Ok value without invoking fallback")
  public void testUnwrapOrElseFunctionReturnsOkValue() {
    AtomicBoolean called = new AtomicBoolean(false);

    int value =
        ok(42)
            .unwrapOrElse(
                _ -> {
                  called.set(true);
                  return 100;
                });

    assertEquals(42, value);
    assertFalse(called.get());
  }

  @Test
  @DisplayName("unwrapOrElse(Function) should compute fallback from error on Err")
  public void testUnwrapOrElseFunctionComputesFallbackForErr() {
    int value =
        Result.<Integer>err(new FakeCause("404"))
            .unwrapOrElse(
                reason -> reason instanceof FakeCause(String msg) ? Integer.parseInt(msg) : -1);

    assertEquals(404, value);
  }

  @Test
  @DisplayName("unwrapOrElse(Function) should reject null fallback consistently on Ok and Err")
  public void testUnwrapOrElseFunctionRejectsNullOnOkAndErr() {
    assertThrows(
        NullPointerException.class,
        () -> ok(42).unwrapOrElse((java.util.function.Function<Cause, Integer>) null));
    assertThrows(
        NullPointerException.class,
        () ->
            Result.<Integer>err(fakeCause())
                .unwrapOrElse((java.util.function.Function<Cause, Integer>) null));
  }

  @Test
  @DisplayName("unwrapOrElse(Supplier) should return Ok value or fallback for Err")
  public void testUnwrapOrElseSupplier() {
    AtomicBoolean called = new AtomicBoolean(false);
    assertEquals(
        42,
        ok(42)
            .unwrapOrElse(
                () -> {
                  called.set(true);
                  return 100;
                }));
    assertFalse(called.get());

    assertEquals(100, Result.<Integer>err(fakeCause()).unwrapOrElse(() -> 100));
  }

  @Test
  @DisplayName("unwrapOrElse(Supplier) should reject null supplier consistently on Ok and Err")
  public void testUnwrapOrElseSupplierRejectsNullOnOkAndErr() {
    assertThrows(
        NullPointerException.class,
        () -> ok(42).unwrapOrElse((java.util.function.Supplier<Integer>) null));
    assertThrows(
        NullPointerException.class,
        () ->
            Result.<Integer>err(fakeCause())
                .unwrapOrElse((java.util.function.Supplier<Integer>) null));
  }

  @Test
  @DisplayName("unwrapOrElseThrow should return Ok value")
  public void testUnwrapOrElseThrowReturnsOkValue() throws Exception {
    assertEquals(42, ok(42).unwrapOrElseThrow(_ -> new Exception("")));
  }

  @Test
  @DisplayName("unwrapOrElseThrow should throw mapped exception for Err")
  public void testUnwrapOrElseThrowThrowsMappedExceptionForErr() {
    var exception =
        assertThrows(
            RuntimeException.class,
            () ->
                Result.<Integer>err(new FakeCause("file not found"))
                    .unwrapOrElseThrow(
                        reason -> {
                          if (reason instanceof FakeCause(String message)) {
                            return new RuntimeException(message);
                          }
                          fail();
                          return null;
                        }));

    assertEquals("file not found", exception.getMessage());
  }

  @Test
  @DisplayName("unwrapOrElseThrow should reject null function consistently on Ok and Err")
  public void testUnwrapOrElseThrowRejectsNullOnOkAndErr() {
    assertThrows(NullPointerException.class, () -> ok(42).unwrapOrElseThrow(null));
    assertThrows(
        NullPointerException.class, () -> Result.<Integer>err(fakeCause()).unwrapOrElseThrow(null));
  }

  @Test
  @DisplayName("narrow should narrow wildcard Result and reject null")
  public void testNarrow() {
    Result<? extends List<String>> wildcard = ok(new ArrayList<>(List.of("a", "b")));

    Result<List<String>> narrowed = Result.narrow(wildcard);

    assertEquals(ok(List.of("a", "b")), narrowed);
    assertThrows(NullPointerException.class, () -> Result.narrow(null));
  }

  @Test
  @DisplayName("trying should reject supplier returning Result")
  public void testTryingRejectsSupplierReturningResult() {
    var exception = assertThrows(IllegalArgumentException.class, () -> trying(() -> ok(42)));

    assertEquals("Cannot wrap Result in Ok", exception.getMessage());
  }

  @Test
  @DisplayName("trying should return Ok when supplier succeeds")
  public void testTryingReturnsOkWhenSupplierSucceeds() {
    Result<Integer> result = trying(() -> Integer.parseInt("123"));

    assertEquals(ok(123), result);
  }

  @Test
  @DisplayName("trying should map exception into Err when supplier throws")
  public void testTryingMapsExceptionToErr() {
    Result<Integer> result =
        trying(
            () -> {
              throw new IOException("disk failed");
            },
            exception -> new FakeCause("io: " + exception.getMessage()));

    assertEquals(err(new FakeCause("io: disk failed")), result);
  }

  @Test
  @DisplayName("trying should reject null supplier and null exceptionMapper")
  public void testTryingRejectsNulls() {
    assertThrows(NullPointerException.class, () -> trying(null));
    assertThrows(NullPointerException.class, () -> trying(() -> 1, null));
    assertThrows(NullPointerException.class, () -> trying(null, ExceptionCause::new));
  }
}
