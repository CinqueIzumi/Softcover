# Foundation upstream candidates

Findings discovered while building Softcover that belong in the **nl.rhaydus foundation** rather than
in this app — upstream bugs to fix, app-local mechanisms that should become shared foundation
capabilities, and gates that should move from advisory (here) to blocking (in the foundation).

This is an **internal** working doc. It is the queue for the next foundation revision; nothing here is
acted on automatically. Before reaching for a workaround in the app, check whether the right fix is
upstream and record it here.

**Process.** With `foundation.local=true` (includeBuild against `../rhaydus-foundation`), fix it at the
source and bump the foundation. Otherwise file it against the foundation and track it here until a
released version carries the fix, then re-run the `rhaydus-adopt` agent. The capability surface these
entries refer to is indexed in [`../rhaydus/0.2.0/CAPABILITIES.md`](../rhaydus/0.2.0/CAPABILITIES.md).

Each entry: **type** (bug / enhancement / gate), **home** (target foundation module), **status**, and
enough context for whoever picks it up.

---

## F1 — Crash-safety gate for terminal flow reads should be a blocking ktlint rule

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:ktlint-rules`
- **Status:** Open — currently advisory-only in this app

A bare `.first()` / `.single()` on a cold flow is a crash risk: it throws `NoSuchElementException` on
an empty flow, and any terminal operator re-throws an upstream error (DataStore / network / Apollo /
repository). We added app-local enforcement — a `scripts/style-check.sh` recipe
(`check_unguarded_flow_terminal`) plus the crash-safety rule in
[`../reference/code-style.md`](../reference/code-style.md) (Error Handling) — but it is **advisory**:
it surfaces for review and on every touched file via the PostToolUse hook, yet it does not gate CI.

The foundation `nl.rhaydus:ktlint-rules` ruleset is the only mechanical layer that hard-gates the
build (via `ktlintCheck` / the `check` lifecycle) for every consuming project. A custom rule there —
flag unguarded terminal flow reads (`.first()` / `.single()`) in production source, ignoring guarded
forms and test sources — would promote this from "advisory in Softcover" to "blocking everywhere."
The rule is **crash-safety, not "always use a Collector"**: a guarded one-shot read (`.firstOrNull()`
+ default + `.catch` / cancellation-aware `runCatching`) is acceptable; an unguarded throwing terminal
is the defect.

---

## F2 — `rememberBottomBarPadding()` does not work due to the way it's implemented

- **Type:** bug
- **Home:** `nl.rhaydus:designsystem-core` (layout)
- **Status:** Open — root cause to be diagnosed before an upstream fix

`rememberBottomBarPadding()` (and the `LocalBottomBarPadding` it reads) is part of the
designsystem-core layout surface, but it does not work as intended — the issue is in how it is
implemented, not in how the app calls it. Needs a root-cause diagnosis (what the helper resolves to
vs. what callers expect) before proposing the upstream fix.

While in there, **audit the rest of the foundation's current surface for the same class of problem** —
items that are published as shared API but either (a) can't actually be reused across consuming apps
(too coupled to one app's assumptions, like the bottom-bar padding helper appears to be), or (b)
carry very little value (thin wrappers, near-empty primitives, things a consumer would just as easily
hand-roll). For each, decide: fix so it's genuinely reusable, demote it back into the app that needs
it, or remove it. The goal is that everything the foundation exposes earns its place in the
[CAPABILITIES.md](../rhaydus/0.2.0/CAPABILITIES.md) surface; dead or unusable API there is worse than
no API, because the reuse-first rule sends people to reach for it.

---

## F3 — Make bottom bars reusable

- **Type:** enhancement (shared component)
- **Home:** `nl.rhaydus:designsystem-core` (component)
- **Status:** Open — evaluate

The bottom bar is an often-recurring UI component that tends to get re-implemented per app. Evaluate
hoisting a reusable bottom-bar component into the foundation design system, alongside the existing
bottom-bar primitives it already ships (`LocalBottomBarPadding`, `rememberBottomBarPadding()`,
`BottomNavigationSpacer`). Keep the shared piece brand-agnostic (skeleton in designsystem-core; brand
styling layered by the app) so it fits the foundation's design-agnostic contract.

---

## F4 — `runCatchingCancellable` helper that rethrows `CancellationException`

- **Type:** enhancement (shared util)
- **Home:** `nl.rhaydus:core-ui` (the non-visual seam that already ships `AppDispatchers`)
- **Status:** Open — evaluate

Kotlin's stdlib `runCatching` catches *every* `Throwable`, including `CancellationException`. Inside a
coroutine that silently swallows structured-concurrency cancellation: a cancelled child runs its
fallback path instead of unwinding, so the coroutine never exits gracefully and the parent's
cancellation signal is lost. The standard fix is a cancellation-aware variant that rethrows
`CancellationException` before treating the throwable as a failure:

```kotlin
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }
```

Every app on the foundation that uses `runCatching` in coroutine code needs this, so it belongs in
`core-ui` rather than being re-derived per app. It is also the "cancellation-aware `runCatching`" that
**F1** names as the acceptable guarded form of a one-shot flow read, and it is the clean replacement for
the hand-rolled `catch (Throwable) { … }` + manual `CancellationException` rethrow pattern (e.g. the
`SoftcoverWorker` note in the architecture review). Until it lands upstream, app code should keep the
explicit ordered-catch (`catch (c: CancellationException) { throw c }` first) rather than a bare
`runCatching`.
