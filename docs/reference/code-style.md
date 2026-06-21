# Code Style Guide

The shared Kotlin code style is governed by the foundation [`docs/rhaydus/0.2.0/code-style.md`](../rhaydus/0.2.0/code-style.md) (enforced by the `nl.rhaydus:ktlint-rules` ruleset + `scripts/style-check.sh`). Read it first — it is the source of truth for naming, project structure, one-declaration-per-file, enums, data classes, boolean negation, visibility, if/else, Compose, comments, code organization, argument/property layout, whitespace, import ordering, data flow, dependencies, and test class / unit test structure.

This file keeps only Softcover-specific deltas.

## Error Handling & Logging (Softcover concretizations)

The layered error model (data sources/repositories throw, use cases return `Result<T>` via a cancellation-aware `runCatching`, actions fold with `.onSuccess` / `.onFailure`) is the foundation's; the project-specific bindings are:

- **Apollo network calls use the `safeQuery()` and `safeMutation()` extension functions** — the shared HTTP-client wrapping the foundation refers to is Apollo here. `safeQuery` takes an optional `FetchPolicy`; see [architecture.md](architecture.md) for the cache/fetch-policy behavior.
- **Apollo errors are wrapped in `RuntimeException` with descriptive messages.**
- **`AppLog` (the Kermit-backed logging facade in `:core:domain`) is the logger** — never `println` or `Log.*`. It exposes `i` / `w` / `e` (message and throwable variants).
- **Blank line around an `AppLog.e(...)` log.** An `AppLog.e(...)` error log is its own paragraph: leave a blank line between it and the following code. This is the Softcover instance of the foundation's generic "blank line after an error log" rule — the foundation `nl.rhaydus:ktlint-rules` `BlankLineAfterStatementRule` keys on `AppLog.e`.
- **A terminal flow read must never be able to crash the app.** `.first()` / `.single()` throw `NoSuchElementException` on an empty flow, and *every* terminal operator (`.first`, `.firstOrNull`, `.single`, `.collect`, …) re-throws an upstream error (a failing DataStore / network / Apollo / repository flow). So a bare `.first()` / `.single()` on such a flow — e.g. reading a preference one-shot inside a `UiAction` — is a crash risk: an empty or erroring source takes the app down. The rule is **crash-safety, not "always use a `Collector`"** — pick whichever fits:
  - **Guard the one-shot read** when a snapshot is genuinely what you want: `.firstOrNull()` with a sensible default, wrapped so the throw cannot escape (a `.catch {}` on the flow, or the cancellation-aware `runCatching`). A bare `.first()` with nothing catching it is the defect.
  - **Consume it reactively** when the value should track the source over the screen's lifetime: a TOAD `Collector` that folds the flow into `UiState` (the established pattern for every preference fed into a screen, e.g. `DateStyleCollector`). This also sidesteps the empty/error throw entirely.

  `scripts/style-check.sh` flags `.first(` / `.single(` in production source for exactly this reason; treat each hit as "prove the source can't be empty/error, or guard it." (Collection `List`/`Iterable` `.first()`/`.single()` match too — review and confirm the receiver can't be empty.)
