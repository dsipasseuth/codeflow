# Result

A small, opinionated Java `Result` type for composing operations that can fail using Railway-Oriented Programming (ROP).

Inspired by **Rust's `Result`** and **JavaScript Promise chaining**, this library keeps the happy path linear and readable while allowing failures to travel through a pipeline without repetitive branching or `Optional` clutter.

## Core Mental Model

- **Opinionated toward success:** Compose operations along the happy path; once an `Err` occurs, subsequent transformations are skipped and the failure propagates automatically.
- **`.then` for sequence, `.map` for loop:** Parting ways with Vavr and `Either`-style APIs—use `then` / `thenApply` to chain sequential workflow steps, and reserve `map` (via collection and stream helpers) for iterating over elements.
- **Compile-time and runtime null-safety:** Packages are `@NullMarked` via JSpecify so unannotated types are non-null by default (`Result<@Nullable T>` is disallowed at the type level, and `Optional` wrappers are avoided), backed by `Objects.requireNonNull` checks at runtime across both `Ok` and `Err` paths.
- **Sealed records and minimal dependencies:** `Result<T>` is a `sealed interface` implemented by `Ok<T>` and `Err<T>` records for exhaustive `switch` pattern matching, with a tiny dependency footprint.

## Getting Started

A `Result<T>` is either:

- `Result.Ok<T>` — a successful non-null value (cannot wrap another `Result`), or
- `Result.Err<T>` — a failure carrying a non-null `Cause`.

```java
Result<String> greeting = Result.ok("hello");
Result<Integer> length = greeting.then(String::length);

String message =
        switch (length) {
            case Result.Ok<Integer>(var value) -> "Length: " + value;
            case Result.Err<Integer>(var cause) -> "Failed: " + cause;
        };
```

`Cause` is the library's error representation; `ExceptionCause` wraps exception-derived failures, while applications can define custom domain `Cause` implementations:

```java
Result<String> missing = Result.err(new ExceptionCause(new IOException("Not found")));
```

## Chaining

### Transform a successful value with `then`

Use `then` to map a success value. If the result is an `Err`, that error propagates unchanged.

```java
Result<User> user = findUser(userId)
    .then(User::normalize)
    .then(User::validate);
```

### Continue with another `Result` using `thenApply`

Use `thenApply` when the next step already returns a `Result`. It flattens the returned result rather than nesting it.

```java
Result<Account> account = findUser(userId)
    .thenApply(user -> loadAccount(user.id()));
```

### Capture exceptions with `thenTrying`

Ordinary `then` leaves exceptions visible. Use `thenTrying` to capture thrown `Exception`s into an `Err`, using either the default `ExceptionCause` mapping or a custom mapper.

```java
Result<Config> config = readConfig(path)
    .thenTrying(Config::parse);

Result<Config> customConfig = readConfig(path)
    .thenTrying(Config::parse, exception -> new ExceptionCause(exception));
```

### Start with a potentially-throwing operation using `trying`

```java
Result<String> contents = Result.trying(() -> Files.readString(path));

Result<String> customContents = Result.trying(
    () -> Files.readString(path),
    exception -> new ExceptionCause(exception));
```

## Recovering and Inspecting

### Recover with `onError` or extract at boundaries with `unwrapOr*`

Use `onError` to recover mid-pipeline by returning a replacement `Result`, or use `unwrapOr...` at system boundaries when you need a plain value.

```java
Result<String> recovered = loadValue()
    .onError(error -> Result.ok("fallback"));

String value = loadValue().unwrapOr("default");
String computedFallback = loadValue().unwrapOrElse(error -> "fallback for: " + error);
String suppliedFallback = loadValue().unwrapOrElse(() -> "default");
String strictValue = loadValue().unwrapOrElseThrow(MyApplicationException::new);
```

`unwrap()` returns the success value and throws `IllegalStateException` on `Err`. Treat it as a boundary escape hatch rather than standard control flow.

### Observe without transforming (`peekValue`, `peekCause`) and terminal inspection (`ifOk`, `ifErr`)

`peekValue` runs a side effect on `Ok` and returns the original `Result` for continued chaining; `peekCause` runs a side effect on `Err` and returns the original `Result` so you can log or inspect a `Cause` mid-pipeline. `ifOk` and `ifErr` are terminal operations that inspect the outcome without returning a `Result` to chain further.

```java
Result<Order> order = createOrder(request)
    .peekValue(saved -> logger.info("Created order {}", saved.id()))
    .peekCause(cause -> logger.warn("Failed to create order: {}", cause))
    .then(Order::toDto);

order.ifErr(error -> logger.warn("Order creation failed: {}", error));
```

## Collections

Collection helpers live in `ResultCollectionUtils` and operate directly on standard Java `List` values without introducing custom collection wrappers.

```java
import static com.codeflow.result.ResultCollectionUtils.filter;
import static com.codeflow.result.ResultCollectionUtils.map;

Result<List<String>> names = Result.ok(List.of("Ada", "Grace", "Linus"))
    .then(filter(name -> name.length() > 3))
    .then(map(String::toUpperCase));
```

Available list helpers:

- `map(fn)` — transform each element.
- `multiMap(fn)` — emit zero or more output elements per input (`mapMulti`).
- `filter(predicate)` — retain matching elements.
- `sorted(comparator)` — sort elements.
- `forEach(consumer)` — perform an action for each element.

## Streams

Stream helpers in `ResultStreamUtils` compose inside a `Result` pipeline. Intermediate operations stay lazy until a terminal operation consumes the stream.

```java
import static com.codeflow.result.ResultStreamUtils.map;

Result<List<String>> names = Result.ok(Stream.of("Ada", "Grace", "Linus"))
    .then(map(String::toUpperCase))
    .then(Stream::toList);
```

Available stream helpers:

- `map(fn)` — transform stream elements.
- `mapMulti(fn)` — emit zero or more elements per input.
- `filter(predicate)` — retain matching elements.
- `collect(collector)` — terminal operation; collects with a `Collector` and closes the stream.
- `forEach(consumer)` — terminal operation; consumes elements and closes the stream.

Terminal stream helpers (`collect`, `forEach`) close their input `Stream` via `try-with-resources`. Do not reuse a stream after passing it to one of these terminal helpers.

## Combining Independent Results

The `Combiner` family (`Combiner` through `Combiner6`) lets you accumulate multiple `Result` values progressively without manual branching. Start a chain with `Combiner.begin(...)`, add stages with `andThen`, `andThenApply`, or `andThenTrying`, inspect intermediate values with `peekValue`, and finish with `then`, `thenApply`, or `thenTrying`.

If any step produces an `Err`, dependent stages are skipped and the first failure propagates.

```java
import com.codeflow.result.combiner.Combiner;

Result<OrderSummary> summary = Combiner.begin(loadUser(userId))
        .andThenApply(() -> loadCart(userId))
        .andThen((user, cart) -> subtotal(user, cart))
        .peekValue((user, _, total) -> logCheckout(user.id(), total))
        .then((user, cart, total) -> new OrderSummary(user.id(), cart.size(), total));
```

The family intentionally stops at **six inputs** (`Combiner6`). If a workflow requires more than six independent results, group related values into a domain record or split the workflow into stages rather than chaining arbitrarily wide tuples.

## API at a Glance

| Method | Meaning |
|---|---|
| `ok(value)` | Create a successful `Result` |
| `err(cause)` | Create a failed `Result` |
| `trying(supplier)` | Run a potentially-throwing supplier and capture exceptions |
| `then(fn)` | Transform a successful value in sequence |
| `thenApply(fn)` | Chain another `Result`-producing operation |
| `thenTrying(fn)` | Transform a successful value with exception capture |
| `onError(fn)` | Recover from an error by returning another `Result` |
| `peekValue(consumer)` | Observe a success value without changing the `Result` |
| `peekCause(consumer)` | Observe an error `Cause` without changing the `Result` |
| `ifOk(consumer)` | Terminal inspection of success without transforming |
| `ifErr(consumer)` | Terminal inspection of failure without transforming |
| `unwrap()` | Return success value or throw `IllegalStateException` |
| `unwrapOr(value)` | Return success value or a fixed fallback |
| `unwrapOrElse(fn)` | Return success value or a computed/lazy fallback |
| `unwrapOrElseThrow(fn)` | Return success value or throw a mapped exception |

## Boundaries and Philosophy

- `Result.ok(value)` rejects `null` and nested `Result` instances; `Result.err(cause)` rejects `null`.
- `then` does not catch exceptions; only `trying` and `thenTrying` convert `Exception`s into `Err` via a `Cause` mapper (defaulting to `ExceptionCause`).
- `InterruptedException` caught inside `trying` or `thenTrying` restores the thread's interrupt flag and is rethrown as a `RuntimeException`.
- `onError` is for recovery (returning a replacement `Result`), whereas `peekCause` taps a `Cause` mid-chain and `ifErr` is a terminal inspection.

This library favors practical, readable application code over maximal abstraction. It is not trying to turn Java into a mathematical exercise or force every value into a wrapper—use `Result` where failure is part of the workflow, and keep ordinary Java types everywhere else.