package com.codeflow.result;

/** Utility methods for inspecting {@link Result} instances. */
public final class ResultUtils {
  private ResultUtils() {}

  /**
   * Returns whether the supplied result is an {@link Result.Ok}.
   *
   * @param result The result to inspect.
   * @return {@code true} if the result represents success; otherwise {@code false}.
   */
  public static boolean isOk(Result<?, ?> result) {
    return result instanceof Result.Ok;
  }

  /**
   * Returns whether the supplied result is an {@link Result.Err}.
   *
   * @param result The result to inspect.
   * @return {@code true} if the result represents failure; otherwise {@code false}.
   */
  public static boolean isErr(Result<?, ?> result) {
    return result instanceof Result.Err;
  }
}
