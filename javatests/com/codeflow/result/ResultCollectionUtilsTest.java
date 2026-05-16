package com.codeflow.result;

import static com.codeflow.result.ResultCollectionUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultCollectionUtilsTest {

  @Test
  @DisplayName("Filter a collection")
  void testFilterWithResultOk() {
    assertEquals(
        new Result.Ok<>(List.of("apple", "banana", "cherry")).then(filter("apple"::equals)),
        new Result.Ok<>(List.of("apple")));
  }

  @Test
  @DisplayName("Map a collection")
  void testMapWithResultOk() {
    assertEquals(
        new Result.Ok<>(List.of("apple", "banana", "pineapple")).then(map(String::length)),
        new Result.Ok<>(List.of(5, 6, 9)));
  }

  @Test
  @DisplayName("Consume a collection")
  void testForEachWithResultOk() {
    var result = new ArrayList<Integer>();
    new Result.Ok<>(List.of("apple", "banana", "pineapple"))
        .then(map(String::length))
        .ifOk(forEach(result::add));
    assertEquals(result, List.of(5, 6, 9));
  }
}
