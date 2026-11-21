package com.codeflow.result;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.ok;
import static com.codeflow.result.ResultCollectionUtils.filter;
import static com.codeflow.result.ResultCollectionUtils.forEach;
import static com.codeflow.result.ResultCollectionUtils.map;
import static com.codeflow.result.ResultCollectionUtils.multiMap;
import static com.codeflow.result.ResultCollectionUtils.sorted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResultCollectionUtils Fluent Pipeline Suite")
class ResultCollectionUtilsTest {

  private static Cause fakeCause() {
    return new FakeCause("collection error");
  }

  @Test
  @DisplayName("Fluent pipeline: map a list of strings to lengths")
  void testMapWithResultOk() {
    Result<List<Integer>> result =
        ok(List.of("apple", "banana", "pineapple")).then(map(String::length));

    assertEquals(ok(List.of(5, 6, 9)), result);
  }

  @Test
  @DisplayName("Fluent pipeline: map propagates Err without invoking mapper")
  void testMapWithResultErr() {
    AtomicBoolean called = new AtomicBoolean(false);
    Function<String, Integer> mapper =
        value -> {
          called.set(true);
          return value.length();
        };

    Result<List<Integer>> result = Result.<List<String>>err(fakeCause()).then(map(mapper));

    assertEquals(err(fakeCause()), result);
    assertTrue(!called.get());
  }

  @Test
  @DisplayName("Fluent pipeline: 1-to-N multiMap expansion")
  void testMultiMapWithResultOk() {
    Result<List<String>> result =
        ok(List.of("a", "b"))
            .then(
                multiMap(
                    (String val, Consumer<String> consumer) -> {
                      consumer.accept(val);
                      consumer.accept(val.toUpperCase());
                    }));

    assertEquals(ok(List.of("a", "A", "b", "B")), result);
  }

  @Test
  @DisplayName("Fluent pipeline: multiMap propagates Err without invoking handler")
  void testMultiMapWithResultErr() {
    AtomicBoolean called = new AtomicBoolean(false);
    BiConsumer<String, Consumer<String>> handler =
        (val, consumer) -> {
          called.set(true);
          consumer.accept(val);
        };

    Result<List<String>> result = Result.<List<String>>err(fakeCause()).then(multiMap(handler));

    assertEquals(err(fakeCause()), result);
    assertTrue(!called.get());
  }

  @Test
  @DisplayName("Fluent pipeline: filter a list")
  void testFilterWithResultOk() {
    Result<List<String>> result =
        ok(List.of("apple", "banana", "cherry")).then(filter(s -> s.startsWith("a")));

    assertEquals(ok(List.of("apple")), result);
  }

  @Test
  @DisplayName("Fluent pipeline: filter supports covariant list elements")
  void testFilterCovariantList() {
    List<ArrayList<String>> lists =
        List.of(new ArrayList<>(List.of("x")), new ArrayList<>(), new ArrayList<>(List.of("y")));
    Predicate<List<String>> nonEmpty = list -> !list.isEmpty();

    Result<List<List<String>>> result = ok(lists).then(filter(nonEmpty));

    assertEquals(ok(List.of(List.of("x"), List.of("y"))), result);
  }

  @Test
  @DisplayName("Fluent pipeline: sort with custom comparator")
  void testSortedCustomComparator() {
    Result<List<String>> result =
        ok(List.of("apple", "banana", "kiwi"))
            .then(sorted(Comparator.comparingInt(String::length)));

    assertEquals(ok(List.of("kiwi", "apple", "banana")), result);
  }

  @Test
  @DisplayName("Fluent pipeline: sorted supports covariant list elements")
  void testSortedCovariantList() {
    List<ArrayList<String>> lists =
        List.of(new ArrayList<>(List.of("a", "b", "c")), new ArrayList<>(List.of("a")));
    Comparator<List<String>> bySize = Comparator.comparingInt(List::size);

    Result<List<List<String>>> result = ok(lists).then(sorted(bySize));

    assertEquals(ok(List.of(List.of("a"), List.of("a", "b", "c"))), result);
  }

  @Test
  @DisplayName("Fluent pipeline: transform list into set")
  void testToSetWithResultOk() {
    Result<Set<String>> result = ok(List.of("apple", "banana", "apple")).then(Set::copyOf);
    assertEquals(ok(Set.of("apple", "banana")), result);
  }

  @Test
  @DisplayName("Fluent pipeline: consume collection elements via forEach")
  void testForEachWithResultOk() {
    var result = new ArrayList<Integer>();

    ok(List.of("apple", "banana", "pineapple"))
        .then(map(String::length))
        .ifOk(forEach(result::add));

    assertEquals(List.of(5, 6, 9), result);
  }

  @Test
  @DisplayName("Fluent pipeline: forEach skips execution on Err")
  void testForEachWithResultErr() {
    var seen = new ArrayList<String>();

    Result.<List<String>>err(fakeCause()).ifOk(forEach(seen::add));

    assertTrue(seen.isEmpty());
  }

  @Test
  @DisplayName("Guard clauses enforce non-null arguments")
  void testNullGuards() {
    assertThrows(NullPointerException.class, () -> map(null).apply(List.of("a")));
    assertThrows(NullPointerException.class, () -> multiMap(null).apply(List.of("a")));
    assertThrows(NullPointerException.class, () -> filter(null).apply(List.of("a")));
    assertThrows(NullPointerException.class, () -> sorted(null).apply(List.of("a")));
    assertThrows(NullPointerException.class, () -> forEach(null).accept(List.of("a")));

    assertThrows(NullPointerException.class, () -> map(String::length).apply(null));
    assertThrows(
        NullPointerException.class,
        () -> multiMap((String s, Consumer<String> c) -> c.accept(s)).apply(null));
    assertThrows(NullPointerException.class, () -> filter((String s) -> true).apply(null));
    assertThrows(
        NullPointerException.class, () -> sorted(Comparator.<String>naturalOrder()).apply(null));
    assertThrows(NullPointerException.class, () -> forEach((String s) -> {}).accept(null));
  }
}
