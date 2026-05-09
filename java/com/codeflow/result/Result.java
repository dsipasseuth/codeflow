package com.codeflow.result;

import static java.util.Objects.requireNonNull;

/**
 * A container object which may contain either a success value (Ok) or an error value (Err).
 * Inspired by Rust's {@code Result} type, this sealed interface provides a type-safe way to handle
 * operations that can fail without throwing exceptions.
 *
 * @param <T> The type of the success value.
 * @param <E> The type of the error value.
 */
public sealed interface Result<T, E> permits Result.Ok, Result.Err {

  /**
   * Represents a successful outcome containing a value.
   *
   * @param value The success value, must not be null.
   */
  record Ok<T, E>(T value) implements Result<T, E> {
    public Ok {
      requireNonNull(value, "Ok value cannot be null");
    }
  }

  /**
   * Represents a failed outcome containing an error.
   *
   * @param error The error value, must not be null.
   */
  record Err<T, E>(E error) implements Result<T, E> {
    public Err {
      requireNonNull(error, "Err value cannot be null");
    }
  }
}
