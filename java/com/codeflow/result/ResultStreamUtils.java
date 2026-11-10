package com.codeflow.result;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility transformations and resource-safe terminal operations for {@link Stream}, optimized for
 * use with {@link Result}.
 */
public final class ResultStreamUtils {

  private ResultStreamUtils() {}

  public static <T, R> Function<Stream<T>, Stream<R>> map(Function<? super T, ? extends R> fn) {
    requireNonNull(fn, "Mapper function cannot be null");
    return stream -> stream.map(fn);
  }

  /**
   * Lifts multi-mapping logic into a stream transformation, explicitly preserving input/output
   * typing.
   */
  public static <T, R> Function<Stream<T>, Stream<R>> mapMulti(
      BiConsumer<? super T, ? super Consumer<R>> out) {
    requireNonNull(out, "Mapper function cannot be null");
    return stream -> stream.mapMulti(out);
  }

  public static <T> Function<Stream<T>, Stream<T>> filter(Predicate<? super T> predicate) {
    requireNonNull(predicate, "Predicate cannot be null");
    return stream -> stream.filter(predicate);
  }

  public static <T> Consumer<Stream<T>> forEach(Consumer<? super T> consumer) {
    requireNonNull(consumer, "Consumer cannot be null");
    return stream -> {
      try (stream) {
        stream.forEach(consumer);
      }
    };
  }

  public static <T, A, R> Function<Stream<T>, R> collect(Collector<? super T, A, R> collector) {
    requireNonNull(collector, "Collector cannot be null");
    return stream -> {
      try (stream) {
        return stream.collect(collector);
      }
    };
  }

  public static <T> Function<Stream<T>, List<T>> toList() {
    return stream -> {
      try (stream) {
        return stream.toList();
      }
    };
  }

  public static <T> Function<Stream<T>, Set<T>> toSet() {
    return stream -> {
      try (stream) {
        return stream.collect(Collectors.toSet());
      }
    };
  }
}
