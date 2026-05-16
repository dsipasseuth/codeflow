package com.codeflow.result;

import static com.codeflow.result.ResultStreamUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResultStreamUtils Technical Suite")
class ResultStreamUtilsTest {

  @Test
  @DisplayName("Filter a stream")
  void testFilterWithResultOk() {
    assertEquals(
        new Result.Ok<>(Stream.of("apple", "banana", "cherry"))
            .then(filter("apple"::equals))
            .then(Stream::toList),
        new Result.Ok<>(Stream.of("apple")).then(Stream::toList));
  }

  @Test
  @DisplayName("Map a stream")
  void testMapWithResultOk() {
    assertEquals(
        new Result.Ok<>(Stream.of("apple", "banana", "pineapple"))
            .then(map(String::length))
            .then(Stream::toList),
        new Result.Ok<>(Stream.of(5, 6, 9)).then(Stream::toList));
  }

  @Test
  @DisplayName("Consume a stream")
  void testForEachWithResultOk() {
    var result = new ArrayList<Integer>();
    new Result.Ok<>(Stream.of("apple", "banana", "pineapple"))
        .then(map(String::length))
        .ifOk(forEach(result::add));
    assertEquals(result, List.of(5, 6, 9));
  }
}
