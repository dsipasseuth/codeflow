package com.codeflow.result;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static com.codeflow.result.ResultStreamUtils.collect;
import static com.codeflow.result.ResultStreamUtils.filter;
import static com.codeflow.result.ResultStreamUtils.forEach;
import static com.codeflow.result.ResultStreamUtils.map;
import static com.codeflow.result.ResultStreamUtils.mapMulti;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResultStreamUtils Pipeline Suite")
class ResultStreamUtilsTest {

  private static Cause fakeCause() {
    return new FakeCause("stream error");
  }

  @Test
  @DisplayName("Fluent pipeline: filter and collect to list")
  void testFluentFilterAndToList() {
    Stream<String> stream = Stream.of("apple", "banana", "cherry");

    Result<List<String>> result =
        ok(stream).then(filter(s -> s.startsWith("a"))).then(Stream::toList);

    assertEquals(ok(List.of("apple")), result);
  }

  @Test
  @DisplayName("Fluent pipeline: map, filter, and collect to set")
  void testFluentMapFilterAndToSet() {
    Stream<String> stream = Stream.of("apple", "banana", "pineapple", "apple");

    Result<Set<Integer>> result =
        ok(stream)
            .then(map(String::length))
            .then(filter(len -> len > 5))
            .then(s -> s.collect(toSet()));

    assertEquals(ok(Set.of(6, 9)), result);
  }

  @Test
  @DisplayName("Fluent pipeline: mapMulti expansion")
  void testFluentMapMulti() {
    Result<List<String>> result =
        ok(Stream.of("a", "b"))
            .then(
                mapMulti(
                    (String val, Consumer<String> consumer) -> {
                      consumer.accept(val);
                      consumer.accept(val.toUpperCase());
                    }))
            .then(Stream::toList);

    assertEquals(ok(List.of("a", "A", "b", "B")), result);
  }

  @Test
  @DisplayName("Fluent pipeline: custom collector and resource closing")
  void testFluentCustomCollectAndResourceClose() {
    AtomicBoolean closed = new AtomicBoolean(false);
    Stream<String> stream = Stream.of("foo", "bar", "baz").onClose(() -> closed.set(true));

    Result<String> result =
        ok(stream).then(map(String::toUpperCase)).then(collect(Collectors.joining("-")));

    assertEquals(ok("FOO-BAR-BAZ"), result);
    assertTrue(closed.get(), "Stream should be automatically closed via try-with-resources");
  }

  @Test
  @DisplayName("Fluent pipeline: forEach side-effect and verification")
  void testFluentForEachSideEffect() {
    List<Integer> collected = new ArrayList<>();
    AtomicBoolean closed = new AtomicBoolean(false);

    Stream<String> stream = Stream.of("a", "bb", "ccc").onClose(() -> closed.set(true));

    ok(stream).then(map(String::length)).ifOk(forEach(collected::add));

    assertEquals(List.of(1, 2, 3), collected);
    assertTrue(closed.get());
  }

  @Test
  @DisplayName("Fluent pipeline: Err propagates without invoking stream operations")
  void testErrPropagation() {
    AtomicBoolean mapped = new AtomicBoolean(false);
    AtomicBoolean multiMapped = new AtomicBoolean(false);
    AtomicBoolean filtered = new AtomicBoolean(false);
    AtomicBoolean consumed = new AtomicBoolean(false);

    Function<String, String> mapper =
        s -> {
          mapped.set(true);
          return s;
        };
    BiConsumer<String, Consumer<String>> multiMapper =
        (s, out) -> {
          multiMapped.set(true);
          out.accept(s);
        };
    Predicate<String> predicate =
        s -> {
          filtered.set(true);
          return true;
        };
    Consumer<String> consumer = s -> consumed.set(true);

    Result<List<String>> listResult =
        Result.<Stream<String>>err(fakeCause())
            .then(map(mapper))
            .then(mapMulti(multiMapper))
            .then(filter(predicate))
            .then(Stream::toList);

    Result.<Stream<String>>err(fakeCause()).ifOk(forEach(consumer));

    assertEquals(err(fakeCause()), listResult);
    assertFalse(mapped.get());
    assertFalse(multiMapped.get());
    assertFalse(filtered.get());
    assertFalse(consumed.get());
  }

  @Test
  @DisplayName("Utility guard clauses reject null parameters and null streams")
  void testNullGuards() {
    assertThrows(NullPointerException.class, () -> map(null).apply(Stream.of("a")));
    assertThrows(NullPointerException.class, () -> mapMulti(null).apply(Stream.of("a")));
    assertThrows(NullPointerException.class, () -> filter(null).apply(Stream.of("a")));
    assertThrows(NullPointerException.class, () -> forEach(null).accept(Stream.of("a")));
    assertThrows(NullPointerException.class, () -> collect(null).apply(Stream.of("a")));

    assertThrows(NullPointerException.class, () -> map(String::length).apply(null));
    assertThrows(
        NullPointerException.class,
        () -> mapMulti((String s, Consumer<String> c) -> c.accept(s)).apply(null));
    assertThrows(NullPointerException.class, () -> filter((String s) -> true).apply(null));
    assertThrows(NullPointerException.class, () -> forEach((String s) -> {}).accept(null));
    assertThrows(NullPointerException.class, () -> collect(toList()).apply(null));
  }
}
