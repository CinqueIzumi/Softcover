# Code Style Guide

The shared Kotlin code style is governed by the foundation [`docs/rhaydus/0.2.0/code-style.md`](docs/rhaydus/0.2.0/code-style.md) (enforced by the `nl.rhaydus:ktlint-rules` ruleset + `scripts/style-check.sh`). Read it first — it is the source of truth for naming, project structure, one-declaration-per-file, enums, data classes, boolean negation, visibility, if/else, Compose, comments, code organization, argument/property layout, whitespace, import ordering, data flow, dependencies, and test class / unit test structure.

This file keeps only Softcover-specific deltas.

## Error Handling & Logging (Softcover concretizations)

The layered error model (data sources/repositories throw, use cases return `Result<T>` via a cancellation-aware `runCatching`, actions fold with `.onSuccess` / `.onFailure`) is the foundation's; the project-specific bindings are:

- **Apollo network calls use the `safeQuery()` and `safeMutation()` extension functions** — the shared HTTP-client wrapping the foundation refers to is Apollo here. `safeQuery` takes an optional `FetchPolicy`; see [ARCHITECTURE.md](ARCHITECTURE.md) for the cache/fetch-policy behavior.
- **Apollo errors are wrapped in `RuntimeException` with descriptive messages.**
- **`AppLog` (the Kermit-backed logging facade in `:core:domain`) is the logger** — never `println` or `Log.*`. It exposes `i` / `w` / `e` (message and throwable variants).
- **Blank line around an `AppLog.e(...)` log.** An `AppLog.e(...)` error log is its own paragraph: leave a blank line between it and the following code. This is the Softcover instance of the foundation's generic "blank line after an error log" rule — the foundation `nl.rhaydus:ktlint-rules` `BlankLineAfterStatementRule` keys on `AppLog.e`.
