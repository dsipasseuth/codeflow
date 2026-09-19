package com.codeflow.result.combiner;

import static com.codeflow.result.Result.err;
import static com.codeflow.result.Result.narrow;
import static com.codeflow.result.Result.ok;

import com.codeflow.result.Cause;
import com.codeflow.result.Functions;
import com.codeflow.result.Functions.Function3;
import com.codeflow.result.Functions.Function4;
import com.codeflow.result.Functions.Function5;
import com.codeflow.result.Functions.Function6;
import com.codeflow.result.Functions.ThrowingBiFunction;
import com.codeflow.result.Functions.ThrowingFunction3;
import com.codeflow.result.Functions.ThrowingFunction5;
import com.codeflow.result.Functions.ThrowingFunction6;
import com.codeflow.result.Result;
import com.codeflow.result.Result.Err;
import com.codeflow.result.Result.Ok;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CombinerUtils {

  public static <T1, T2, R> Result<R> combine(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      BiFunction<? super T1, ? super T2, ? extends R> fn) {
    if (r1 instanceof Ok(var v1) && r2 instanceof Ok(var v2)) {
      return ok(fn.apply(v1, v2));
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    return ((Err<?>) r2).into();
  }

  public static <T1, T2, R> Result<R> combine(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      ThrowingBiFunction<? super T1, ? super T2, ? extends R> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    if (r1 instanceof Ok(var v1) && r2 instanceof Ok(var v2)) {
      try {
        return ok(fn.apply(v1, v2));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Execution interrupted", e);
      } catch (Exception e) {
        return err(exceptionMapper.apply(e));
      }
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    return ((Err<?>) r2).into();
  }

  public static <T1, T2, R> Result<R> combineResult(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      BiFunction<? super T1, ? super T2, ? extends Result<? extends R>> fn) {
    if (r1 instanceof Ok(var v1) && r2 instanceof Ok(var v2)) {
      return narrow(fn.apply(v1, v2));
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    return ((Err<?>) r2).into();
  }

  public static <T1, T2, T3, R> Result<R> combine(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Function3<? super T1, ? super T2, ? super T3, ? extends R> fn) {
    if (r1 instanceof Ok(var v1) && r2 instanceof Ok(var v2) && r3 instanceof Ok(var v3)) {
      return ok(fn.apply(v1, v2, v3));
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    return ((Err<?>) r3).into();
  }

  public static <T1, T2, T3, R> Result<R> combine(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      ThrowingFunction3<? super T1, ? super T2, ? super T3, ? extends R> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    if (r1 instanceof Ok(var v1) && r2 instanceof Ok(var v2) && r3 instanceof Ok(var v3)) {
      try {
        return ok(fn.apply(v1, v2, v3));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Execution interrupted", e);
      } catch (Exception e) {
        return err(exceptionMapper.apply(e));
      }
    }

    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    return ((Err<?>) r3).into();
  }

  public static <T1, T2, T3, R> Result<R> combineResult(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Function3<? super T1, ? super T2, ? super T3, ? extends Result<? extends R>> fn) {
    if (r1 instanceof Ok(var v1) && r2 instanceof Ok(var v2) && r3 instanceof Ok(var v3)) {
      return narrow(fn.apply(v1, v2, v3));
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    return ((Err<?>) r3).into();
  }

  public static <T1, T2, T3, T4, R> Result<R> combine(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Result<? extends T4> r4,
      Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
    if (r1 instanceof Ok(var v1)
        && r2 instanceof Ok(var v2)
        && r3 instanceof Ok(var v3)
        && r4 instanceof Ok(var v4)) {
      return ok(fn.apply(v1, v2, v3, v4));
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    if (r3 instanceof Err<?> err) return err.into();
    return ((Err<?>) r4).into();
  }

  public static <T1, T2, T3, T4, R> Result<R> combine(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Result<? extends T4> r4,
      Functions.ThrowingFunction4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    if (r1 instanceof Ok(var v1)
        && r2 instanceof Ok(var v2)
        && r3 instanceof Ok(var v3)
        && r4 instanceof Ok(var v4)) {
      try {
        return ok(fn.apply(v1, v2, v3, v4));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Execution interrupted", e);
      } catch (Exception e) {
        return err(exceptionMapper.apply(e));
      }
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    if (r3 instanceof Err<?> err) return err.into();
    return ((Err<?>) r4).into();
  }

  public static <T1, T2, T3, T4, R> Result<R> combineResult(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Result<? extends T4> r4,
      Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends Result<? extends R>> fn) {
    if (r1 instanceof Ok(var v1)
        && r2 instanceof Ok(var v2)
        && r3 instanceof Ok(var v3)
        && r4 instanceof Ok(var v4)) {
      return narrow(fn.apply(v1, v2, v3, v4));
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    if (r3 instanceof Err<?> err) return err.into();
    return ((Err<?>) r4).into();
  }

  public static <T1, T2, T3, T4, T5, R> Result<R> combine(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Result<? extends T4> r4,
      Result<? extends T5> r5,
      Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> fn) {

    if (r1 instanceof Ok(var v1)
        && r2 instanceof Ok(var v2)
        && r3 instanceof Ok(var v3)
        && r4 instanceof Ok(var v4)
        && r5 instanceof Ok(var v5)) {
      return ok(fn.apply(v1, v2, v3, v4, v5));
    }

    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    if (r3 instanceof Err<?> err) return err.into();
    if (r4 instanceof Err<?> err) return err.into();
    return ((Err<?>) r5).into();
  }

  public static <T1, T2, T3, T4, T5, R> Result<R> combine(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Result<? extends T4> r4,
      Result<? extends T5> r5,
      ThrowingFunction5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    if (r1 instanceof Ok(var v1)
        && r2 instanceof Ok(var v2)
        && r3 instanceof Ok(var v3)
        && r4 instanceof Ok(var v4)
        && r5 instanceof Ok(var v5)) {
      try {
        return ok(fn.apply(v1, v2, v3, v4, v5));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Execution interrupted", e);
      } catch (Exception e) {
        return err(exceptionMapper.apply(e));
      }
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    if (r3 instanceof Err<?> err) return err.into();
    if (r4 instanceof Err<?> err) return err.into();
    return ((Err<?>) r5).into();
  }

  public static <T1, T2, T3, T4, T5, R> Result<R> combineResult(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Result<? extends T4> r4,
      Result<? extends T5> r5,
      Function5<
              ? super T1,
              ? super T2,
              ? super T3,
              ? super T4,
              ? super T5,
              ? extends Result<? extends R>>
          fn) {
    if (r1 instanceof Ok(var v1)
        && r2 instanceof Ok(var v2)
        && r3 instanceof Ok(var v3)
        && r4 instanceof Ok(var v4)
        && r5 instanceof Ok(var v5)) {
      return narrow(fn.apply(v1, v2, v3, v4, v5));
    }

    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    if (r3 instanceof Err<?> err) return err.into();
    if (r4 instanceof Err<?> err) return err.into();
    return ((Err<?>) r5).into();
  }

  public static <T1, T2, T3, T4, T5, T6, R> Result<R> combine(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Result<? extends T4> r4,
      Result<? extends T5> r5,
      Result<? extends T6> r6,
      Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R>
          fn) {
    if (r1 instanceof Ok(var v1)
        && r2 instanceof Ok(var v2)
        && r3 instanceof Ok(var v3)
        && r4 instanceof Ok(var v4)
        && r5 instanceof Ok(var v5)
        && r6 instanceof Ok(var v6)) {
      return ok(fn.apply(v1, v2, v3, v4, v5, v6));
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    if (r3 instanceof Err<?> err) return err.into();
    if (r4 instanceof Err<?> err) return err.into();
    if (r5 instanceof Err<?> err) return err.into();
    return ((Err<?>) r6).into();
  }

  public static <T1, T2, T3, T4, T5, T6, R> Result<R> combine(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Result<? extends T4> r4,
      Result<? extends T5> r5,
      Result<? extends T6> r6,
      ThrowingFunction6<
              ? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R>
          fn,
      Function<? super Exception, ? extends Cause> exceptionMapper) {
    if (r1 instanceof Ok(var v1)
        && r2 instanceof Ok(var v2)
        && r3 instanceof Ok(var v3)
        && r4 instanceof Ok(var v4)
        && r5 instanceof Ok(var v5)
        && r6 instanceof Ok(var v6)) {
      try {
        return ok(fn.apply(v1, v2, v3, v4, v5, v6));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Execution interrupted", e);
      } catch (Exception e) {
        return err(exceptionMapper.apply(e));
      }
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    if (r3 instanceof Err<?> err) return err.into();
    if (r4 instanceof Err<?> err) return err.into();
    if (r5 instanceof Err<?> err) return err.into();
    return ((Err<?>) r6).into();
  }

  public static <T1, T2, T3, T4, T5, T6, R> Result<R> combineResult(
      Result<? extends T1> r1,
      Result<? extends T2> r2,
      Result<? extends T3> r3,
      Result<? extends T4> r4,
      Result<? extends T5> r5,
      Result<? extends T6> r6,
      Function6<
              ? super T1,
              ? super T2,
              ? super T3,
              ? super T4,
              ? super T5,
              ? super T6,
              ? extends Result<? extends R>>
          fn) {
    if (r1 instanceof Ok(var v1)
        && r2 instanceof Ok(var v2)
        && r3 instanceof Ok(var v3)
        && r4 instanceof Ok(var v4)
        && r5 instanceof Ok(var v5)
        && r6 instanceof Ok(var v6)) {
      return narrow(fn.apply(v1, v2, v3, v4, v5, v6));
    }
    // 2. Short-circuit and return the first encountered Err
    if (r1 instanceof Err<?> err) return err.into();
    if (r2 instanceof Err<?> err) return err.into();
    if (r3 instanceof Err<?> err) return err.into();
    if (r4 instanceof Err<?> err) return err.into();
    if (r5 instanceof Err<?> err) return err.into();
    return ((Err<?>) r6).into();
  }
}
