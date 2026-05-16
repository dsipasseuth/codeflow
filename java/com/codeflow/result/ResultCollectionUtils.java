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

  /**
   * Lifts a mapping function to work on a {@link List}.
   *
   * <p>Usage: {@code result.then(map(String::length))}
   *
   * @param fn The mapping function to apply to each list element.
   * @param <T> The input element type.
   * @param <R> The output element type.
   * @return A function that maps a list of {@code T} values to a list of {@code R} values.
   */
  public static <T, R> Function<List<? extends T>, List<R>> map(
      Function<? super T, ? extends R> fn) {
    requireNonNull(fn, "Mapper function cannot be null");
    // We cast the stream to Stream<R> or let the collector handle it
    return list -> list.stream().<R>map(fn).toList();
  }

  /**
   * Lifts multi-mapping, or 1-to-N mapping, logic to work on a {@link List}.
   *
   * <p>Usage: {@code result.then(multiMap((value, out) -> out.accept(value.length())))}
   *
   * @param fn The multi-mapping function that emits zero or more output values per input value.
   * @param <T> The input element type.
   * @param <R> The output element type.
   * @return A function that transforms a list using {@link
   *     java.util.stream.Stream#mapMulti(BiConsumer)}.
   */
  public static <T, R> Function<List<T>, List<R>> multiMap(BiConsumer<? super T, Consumer<R>> fn) {
    requireNonNull(fn, "Mapper function cannot be null");
    return list -> list.stream().mapMulti(fn).toList();
  }

  /**
   * Lifts a predicate to filter a {@link List}.
   *
   * <p>Usage: {@code result.then(filter("apple"::equals))}
   *
   * @param predicate The predicate used to retain list elements.
   * @param <T> The list element type.
   * @return A function that filters a list using the supplied predicate.
   */
  public static <T> Function<List<T>, List<T>> filter(Predicate<? super T> predicate) {
    requireNonNull(predicate, "Predicate cannot be null");
    return list -> list.stream().filter(predicate).toList();
  }

  /**
   * Lifts a consumer to perform an action on every element of a {@link List}.
   *
   * @param consumer The action to perform on each list element.
   * @param <T> The list element type.
   * @return A consumer that applies the supplied action to every list element.
   */
  public static <T> Consumer<List<T>> forEach(Consumer<? super T> consumer) {
    requireNonNull(consumer, "Consumer cannot be null");
    return list -> list.forEach(consumer);
  }
}
