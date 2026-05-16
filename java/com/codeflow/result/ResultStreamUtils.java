package com.codeflow.result;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

/**
 * Utility transformations for Streams, optimized for use with {@link Result}. These methods are
 * designed to be used in fluent chains to improve scannability.
 */
@NullMarked
public final class ResultStreamUtils {

  private ResultStreamUtils() {}

  /**
   * Lifts a mapping function into a {@link Stream} transformation.
   *
   * <p>Usage: {@code result.then(map(String::length))}
   *
   * @param fn The mapping function to apply to each stream element.
   * @param <T> The input element type.
   * @param <R> The output element type.
   * @return A function that maps a stream of {@code T} values to a stream of {@code R} values.
   */
  public static <T, R> Function<Stream<T>, Stream<R>> map(Function<? super T, ? extends R> fn) {
    requireNonNull(fn, "Mapper function cannot be null");
    return stream -> stream.map(fn);
  }

  /**
   * Lifts multi-mapping, or 1-to-N mapping, logic into a {@link Stream} transformation.
   *
   * <p>Usage: {@code result.then(multiMap((value, out) -> out.accept(value.length())))}
   *
   * @param out The multi-mapping function that emits zero or more output values per input value.
   * @param <T> The input element type.
   * @param <R> The output element type.
   * @return A function that transforms a stream using {@link Stream#mapMulti(BiConsumer)}.
   */
  public static <T, R> Function<Stream<T>, Stream<R>> multiMap(
      BiConsumer<? super T, Consumer<R>> out) {
    requireNonNull(out, "Mapper function cannot be null");
    return stream -> stream.mapMulti(out);
  }

  /**
   * Lifts a predicate into a {@link Stream} transformation.
   *
   * <p>Usage: {@code result.then(filter("apple"::equals))}
   *
   * @param predicate The predicate used to retain stream elements.
   * @param <T> The stream element type.
   * @return A function that filters a stream using the supplied predicate.
   */
  public static <T> Function<Stream<T>, Stream<T>> filter(Predicate<? super T> predicate) {
    requireNonNull(predicate, "Predicate cannot be null");
    return stream -> stream.filter(predicate);
  }

  /**
   * Lifts a consumer to perform an action on every element of a stream.
   *
   * @param consumer The action to perform on each stream element.
   * @param <T> The stream element type.
   * @return A consumer that applies the supplied action to every stream element.
   */
  public static <T> Consumer<Stream<T>> forEach(Consumer<? super T> consumer) {
    requireNonNull(consumer, "Consumer cannot be null");
    return stream -> stream.forEach(consumer);
  }
}
