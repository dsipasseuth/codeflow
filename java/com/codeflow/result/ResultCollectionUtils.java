package com.codeflow.result;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;

/**
 * Utility transformations for Lists, designed for fluent piping. These methods transform a List
 * into another List or perform side effects.
 */
@NullMarked
public final class ResultCollectionUtils {

  private ResultCollectionUtils() {}

  /** Lifts a mapping function to work on a List. Usage: {@code list.then(map(User::getName))} */
  public static <T, R> Function<List<? extends T>, List<R>> map(
      Function<? super T, ? extends R> fn) {
    requireNonNull(fn, "Mapper function cannot be null");
    // We cast the stream to Stream<R> or let the collector handle it
    return list -> list.stream().<R>map(fn).toList();
  }

  /**
   * Lifts a 1-to-N mapping logic to work on a List. Usage: {@code
   * list.then(multiMap(ResultCollectionUtils::onlyOk))}
   */
  public static <T, R> Function<List<T>, List<R>> multiMap(BiConsumer<? super T, Consumer<R>> fn) {
    requireNonNull(fn, "Mapper function cannot be null");
    return list -> list.stream().mapMulti(fn).toList();
  }

  /** Lifts a predicate to filter a List. */
  public static <T> Function<List<T>, List<T>> filter(Predicate<? super T> predicate) {
    requireNonNull(predicate, "Predicate cannot be null");
    return list -> list.stream().filter(predicate).toList();
  }

  /** Lifts a consumer to perform an action on every element of a List. */
  public static <T> Consumer<List<T>> forEach(Consumer<? super T> consumer) {
    requireNonNull(consumer, "Consumer cannot be null");
    return list -> list.forEach(consumer);
  }
}
