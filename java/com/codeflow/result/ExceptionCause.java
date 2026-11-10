package com.codeflow.result;

/**
 * A standard {@link Cause} implementation that encapsulates an unexpected {@link Throwable} or
 * exception thrown during execution.
 *
 * @param throwable the underlying exception or error that caused the failure
 */
public record ExceptionCause(Throwable throwable) implements Cause {}
