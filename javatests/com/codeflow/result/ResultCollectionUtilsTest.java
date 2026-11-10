package com.codeflow.result;

import static com.codeflow.result.Result.ok;
import static com.codeflow.result.ResultCollectionUtils.filter;
import static com.codeflow.result.ResultCollectionUtils.forEach;
import static com.codeflow.result.ResultCollectionUtils.map;
import static com.codeflow.result.ResultCollectionUtils.multiMap;
import static com.codeflow.result.ResultCollectionUtils.sorted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResultCollectionUtils Fluent Pipeline Suite")
class ResultCollectionUtilsTest {

  @Test
  @DisplayName("Fluent pipeline: map a list of strings to lengths")
  void testMapWithResultOk() {
    Result<List<Integer>> result =
        ok(List.of("apple", "banana", "pineapple")).then(map(String::length));

    assertEquals(ok(List.of(5, 6, 9)), result);
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
  @DisplayName("Fluent pipeline: filter a list")
  void testFilterWithResultOk() {
    Result<List<String>> result =
        ok(List.of("apple", "banana", "cherry")).then(filter(s -> s.startsWith("a")));

    assertEquals(ok(List.of("apple")), result);
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
  @DisplayName("Guard clauses enforce non-null arguments")
  void testNullGuards() {
    assertThrows(NullPointerException.class, () -> map(null));
    assertThrows(NullPointerException.class, () -> multiMap(null));
    assertThrows(NullPointerException.class, () -> filter(null));
    assertThrows(NullPointerException.class, () -> sorted(null));
    assertThrows(NullPointerException.class, () -> forEach(null));
  }
}
