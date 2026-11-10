package com.codeflow.result;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility transformations for Collections and Lists, optimized for fluent piping with {@link
 * Result}. These methods transform lists into lists, sets, reductions, or perform side effects.
 */
public final class ResultCollectionUtils {

  private ResultCollectionUtils() {}

  /**
   * Lifts a mapping function to work on a {@link List}, supporting covariant inputs (PECS).
   *
   * @param fn The mapping function to apply to each list element.
   * @param <T> The input element type.
   * @param <R> The output element type.
   * @return A function that maps a list of {@code T} values to a list of {@code R} values.
   */
  public static <T, R> Function<List<? extends T>, List<R>> map(
      Function<? super T, ? extends R> fn) {
    requireNonNull(fn, "Mapper function cannot be null");
    return list -> list.stream().<R>map(fn).toList();
  }

  /**
   * Lifts multi-mapping (1-to-N) logic to work on a {@link List}.
   *
   * @param fn The multi-mapping function that emits zero or more output values per input value.
   * @param <T> The input element type.
   * @param <R> The output element type.
   * @return A function that transforms a list using multi-mapping.
   */
  public static <T, R> Function<List<? extends T>, List<R>> multiMap(
      BiConsumer<? super T, ? super Consumer<R>> fn) {
    requireNonNull(fn, "Mapper function cannot be null");
    return list -> list.stream().mapMulti(fn).toList();
  }

  /**
   * Lifts a predicate to filter a {@link List}, supporting covariant inputs.
   *
   * @param predicate The predicate used to retain list elements.
   * @param <T> The list element type.
   * @return A function that filters a list using the supplied predicate.
   */
  public static <T> Function<List<? extends T>, List<T>> filter(Predicate<? super T> predicate) {
    requireNonNull(predicate, "Predicate cannot be null");
    return list -> {
      @SuppressWarnings("unchecked")
      List<T> casted = (List<T>) list;
      return casted.stream().filter(predicate).toList();
    };
  }

  /**
   * Sorts the elements of a list using the specified comparator.
   *
   * @param comparator The comparator used for sorting.
   * @param <T> The list element type.
   * @return A function that sorts the list.
   */
  public static <T> Function<List<? extends T>, List<T>> sorted(Comparator<? super T> comparator) {
    requireNonNull(comparator, "Comparator cannot be null");
    return list -> {
      @SuppressWarnings("unchecked")
      List<T> casted = (List<T>) list;
      return casted.stream().sorted(comparator).toList();
    };
  }

  /**
   * Lifts a consumer to perform an action on every element of a {@link List}.
   *
   * @param consumer The action to perform on each list element.
   * @param <T> The list element type.
   * @return A consumer that applies the supplied action to every list element.
   */
  public static <T> Consumer<List<? extends T>> forEach(Consumer<? super T> consumer) {
    requireNonNull(consumer, "Consumer cannot be null");
    return list -> list.forEach(consumer);
  }
}
