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
   * Lifts a mapping function into a Stream transformation. Use with: {@code
   * .then(map(User::getName))}
   */
  public static <T, R> Function<Stream<T>, Stream<R>> map(Function<? super T, ? extends R> fn) {
    requireNonNull(fn, "Mapper function cannot be null");
    return stream -> stream.map(fn);
  }

  /**
   * Lifts a multi-mapping (1-to-N) logic into a Stream transformation. Use with: {@code
   * .then(multiMap(ResultStreamUtils::onlyOk))}
   */
  public static <T, R> Function<Stream<T>, Stream<R>> multiMap(
      BiConsumer<? super T, Consumer<R>> out) {
    requireNonNull(out, "Mapper function cannot be null");
    return stream -> stream.mapMulti(out);
  }

  /**
   * Lifts a predicate into a Stream transformation. Use with: {@code
   * .then(filter(String::isBlank))}
   */
  public static <T> Function<Stream<T>, Stream<T>> filter(Predicate<? super T> predicate) {
    requireNonNull(predicate, "Predicate cannot be null");
    return stream -> stream.filter(predicate);
  }

  /** Lifts a consumer to perform an action on every element of a stream. */
  public static <T> Consumer<Stream<T>> forEach(Consumer<? super T> consumer) {
    requireNonNull(consumer, "Consumer cannot be null");
    return stream -> stream.forEach(consumer);
  }
}
