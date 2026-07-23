# Code Style Guide

The shared Kotlin code style is governed by the foundation [`docs/rhaydus/0.3.1/code-style.md`](../rhaydus/0.3.1/code-style.md) (enforced by the `nl.rhaydus:ktlint-rules` ruleset + the type-resolved `nl.rhaydus:detekt-rules` ruleset). Read it first — it is the source of truth for naming, project structure, one-declaration-per-file, enums, data classes, boolean negation, visibility, if/else, Compose, comments, code organization, argument/property layout, whitespace, import ordering, data flow, dependencies, and test class / unit test structure.

This file keeps only Softcover-specific deltas.

## Formatting (Softcover deltas)

### Trailing lambda over a multi-line body — no brace glomming

The foundation §Argument and Property Layout exempts a trailing lambda from the one-per-line rule
and its examples show the inline glommed form (e.g. `scope.setState { it.copy(link = …) }`,
`.onFailure { AppLog.e(…) }`). **Softcover tightens this:** when a trailing lambda's body is a
**multi-line** construct — a wrapped multi-argument call, a multi-line `it.copy(...)`,
`AppLog.e(...)`, `async { call(...) }`, etc. — the lambda's opening `{` and closing `}` each go on
their own line. Never glom the closer as `) }`.

```kotlin
// Bad — closing paren + brace glommed onto one line.
scope.setState { it.copy(
    hiddenBooks = books,
    initialized = true,
) }

.onFailure { AppLog.e(
    it,
    "Failed to unblock book $bookId",
) }

// Good — braces on their own lines.
scope.setState {
    it.copy(
        hiddenBooks = books,
        initialized = true,
    )
}

.onFailure {
    AppLog.e(
        it,
        "Failed to unblock book $bookId",
    )
}
```

A trailing lambda whose body **fits on one line** stays inline (`repository.observe { it.first() }`),
and a single-argument call inside a trailing lambda is not forced to wrap. Only the multi-line body
case is affected.

**Not yet tool-enforced.** `.editorconfig` disables ktlint's standard ruleset, and the foundation
`nl.rhaydus:ktlint-rules` multi-arg wrapping rule currently *exempts* trailing-lambda calls (and even
produces the glommed `) }` when it wraps a call inside one), so `ktlintCheck` does **not** catch this
today — it is enforced by the guide + review. The un-glommed form is stable under `ktlintFormat`
(verified: the formatter never collapses it back). This rule is filed as a foundation ktlint-rule
candidate — see [`../working/foundation-upstream-candidates.md`](../working/foundation-upstream-candidates.md);
once that rule lands it will auto-fix and gate the whole codebase.

## Error Handling & Logging (Softcover concretizations)

The layered error model (data sources/repositories throw, use cases return `Result<T>` via a cancellation-aware `runCatching`, actions fold with `.onSuccess` / `.onFailure`) is the foundation's — including the `runCatchingCancellable` / `runCatchingLogged` helpers and the `AppLog` facade, which now live in `nl.rhaydus:core-common` (package `nl.rhaydus.common`). The project-specific bindings (the typed `ApiException` model, the presentation-authored copy, the `"Softcover"` tag passed to `AppLog.install`) are:

- **Apollo network calls use the `safeQuery()` and `safeMutation()` extension functions** — the shared HTTP-client wrapping the foundation refers to is Apollo here. `safeQuery` takes an optional `FetchPolicy`; see [architecture.md](architecture.md) for the cache/fetch-policy behavior.
- **Apollo errors are thrown as typed `ApiException` subtypes** (sealed root in `:core:domain/exception`), *not* `RuntimeException`: retryable transports as `RetryableSyncException` (`OfflineException` / `ServerUnavailableException`), auth rejections as `InvalidTokenException`, and every other Apollo failure as `UnexpectedApiException`. The seam never decides user-facing copy — that is a presentation concern (below).
- **Use cases wrap their body in `runCatchingLogged { … }`, never bare `runCatching`.** Both helpers are provided by the foundation `nl.rhaydus:core-common` (package `nl.rhaydus.common`). `runCatchingCancellable` is the cancellation-aware `runCatching` — it re-throws `CancellationException` instead of capturing it (bare `runCatching` swallows it and breaks structured concurrency). `runCatchingLogged` composes that primitive with a single `AppLog.e` on failure, so **every use-case failure is logged once, at the source** — never silently dropped, even when the caller discards the `Result` (fire-and-forget) or omits the presentation fold. Optional `context` labels the log line. A bare `runCatching` in a `*UseCase*.kt` is flagged by the foundation `nl.rhaydus:ktlint-rules` `use-case-run-catching` rule. `runCatchingCancellable` is a pure primitive (no logging, no app coupling); it and `runCatchingLogged` are the adopted F4/F5 foundation candidates — see [`../working/foundation-upstream-candidates.md`](../working/foundation-upstream-candidates.md).
- **User-facing error copy is authored in presentation, never the data layer.** `Throwable.toUserMessage()` (in `:core:designsystem` `presentation/error/`) maps the failure *kind* to a message (null for `InvalidTokenException`, handled by the re-auth dialog, and for non-API throwables); the `Result<T>.onApiFailure()` fold helper there shows the snackbar. It is **surface-only**: the failure was already logged at the use-case boundary by `runCatchingLogged`, so it does not log again — which is what makes a forgotten fold safe (a missing `.onApiFailure()` costs at most a toast, never a swallowed or unlogged failure). A presentation fold reports an API failure with `.onApiFailure()`, not a bare `.onFailure { AppLog.e(...) }`. Data sources / repositories / use cases must not call it (it drives `SnackBarManager`).
- **`AppLog` (the Kermit-backed logging facade in `nl.rhaydus:core-common`) is the logger** — never `println` or `Log.*`. It exposes `i` / `w` / `e` (message and throwable variants). The app enables it once at startup via `AppLog.install(tag = "Softcover", debug = …)` (in `SoftCoverApp` / desktop `Main`).
- **Blank line around an `AppLog.e(...)` log.** An `AppLog.e(...)` error log is its own paragraph: leave a blank line between it and the following code. This is the Softcover instance of the foundation's generic "blank line after an error log" rule — the foundation `nl.rhaydus:ktlint-rules` `BlankLineAfterStatementRule` keys on `AppLog.e`.
- **A terminal flow read must never be able to crash the app.** `.first()` / `.single()` throw `NoSuchElementException` on an empty flow, and *every* terminal operator (`.first`, `.firstOrNull`, `.single`, `.collect`, …) re-throws an upstream error (a failing DataStore / network / Apollo / repository flow). So a bare `.first()` / `.single()` on such a flow — e.g. reading a preference one-shot inside a `UiAction` — is a crash risk: an empty or erroring source takes the app down. The rule is **crash-safety, not "always use a `Collector`"** — pick whichever fits:
  - **Guard the one-shot read** when a snapshot is genuinely what you want: `.firstOrNull()` with a sensible default, wrapped so the throw cannot escape (a `.catch {}` on the flow, or `runCatchingCancellable`). A bare `.first()` with nothing catching it is the defect.
  - **Consume it reactively** when the value should track the source over the screen's lifetime: a TOAD `Collector` that folds the flow into `UiState` (the established pattern for every preference fed into a screen, e.g. `DateStyleCollector`). This also sidesteps the empty/error throw entirely.

  This is **gated**, not advisory: the foundation's type-resolved detekt rule `rhaydus:UnguardedFlowTerminalRead` fails the build on a bare `Flow.first()` / `Flow.single()` in production source. Because it resolves the receiver, the identically named `Collection` / `Iterable` operators are never flagged — there is no false-positive tail to triage. Two exemptions, both principled: `firstOrNull()` / `singleOrNull()` are different functions and never match, and `first` on a **hot** flow (`SharedFlow` / `StateFlow`) is exempt because such a flow never completes and never fails, so neither hazard exists — that is what makes `isOnline.first { it }` a legal `awaitOnline()`. `single()` stays flagged even on a hot flow: it cannot complete there.
