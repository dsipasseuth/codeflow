package com.codeflow.result;

import static com.codeflow.result.Result.ok;
import static com.codeflow.result.ResultStreamUtils.collect;
import static com.codeflow.result.ResultStreamUtils.filter;
import static com.codeflow.result.ResultStreamUtils.forEach;
import static com.codeflow.result.ResultStreamUtils.map;
import static com.codeflow.result.ResultStreamUtils.mapMulti;
import static com.codeflow.result.ResultStreamUtils.toList;
import static com.codeflow.result.ResultStreamUtils.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResultStreamUtils Pipeline Suite")
class ResultStreamUtilsTest {

  @Test
  @DisplayName("Fluent pipeline: filter and collect to list")
  void testFluentFilterAndToList() {
    Result<List<String>> result =
        ok(Stream.of("apple", "banana", "cherry"))
            .then(filter(s -> s.startsWith("a")))
            .then(toList());

    assertEquals(ok(List.of("apple")), result);
  }

  @Test
  @DisplayName("Fluent pipeline: map, filter, and collect to set")
  void testFluentMapFilterAndToSet() {
    Result<Set<Integer>> result =
        ok(Stream.of("apple", "banana", "pineapple", "apple"))
            .then(map(String::length))
            .then(filter(len -> len > 5))
            .then(toSet());

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
            .then(toList());

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
  @DisplayName("Utility guard clauses reject null parameters")
  void testNullGuards() {
    assertThrows(NullPointerException.class, () -> map(null));
    assertThrows(NullPointerException.class, () -> mapMulti(null));
    assertThrows(NullPointerException.class, () -> filter(null));
    assertThrows(NullPointerException.class, () -> forEach(null));
    assertThrows(NullPointerException.class, () -> collect(null));
  }
}
