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

The open candidates are organised below into **implementation batches** — each batch clusters items that
share a target module and a kind of work, so it can be designed and landed in one focused pass. F-numbers
are stable identifiers (referenced from commits and other docs) and are **never reused or renumbered**, so
they are not sequential within a batch. Batches are ordered to respect the dependencies noted on each.

## Batch index

| Batch | Theme | Home | Items |
|---|---|---|---|
| — | **Implemented & adopted** | — | _(none yet)_ |
| — | **Implemented, not adopted** | `core-common` / `core-platform` / `designsystem-core` / `ktlint-rules` / `detekt-rules` | F1, F2, F3, F4, F5, F6, F7, F9, F10, F12, F13, F14, F15, F19 |
| F | Error-slot + inline error UX | `nl.rhaydus:designsystem-core` + `nl.rhaydus:toad` | F16, F17 |
| G | Image export | `nl.rhaydus:designsystem-image` | F11 |
| H | Offline mutation queue | new connectivity/offline seam | F8 |
| I | Shared build & gate tooling | `build-logic` / `detekt-rules` / `style-check` skill | F18, F20, F21, F22, F23 |

---

# Implemented

An item is **implemented** once it lands in the foundation — but that splits into two distinct states, and
each implemented item belongs under exactly one of them:

- **Implemented & adopted** — landed in the foundation **and** live in Softcover: the app-local copy is
  deleted and imports re-point to the foundation symbol. Done end to end; nothing left to do.
- **Implemented, not adopted** — landed in the foundation, but Softcover still ships its app-local copy and
  has **not** switched to the foundation symbol. It arrives in the app on the next `rhaydus-adopt` pass (or
  once `foundation.local=true` is flipped). When that happens, **move the item up to *Implemented &
  adopted*** and delete the "delete on adopt" notes.

## Implemented & adopted

_(none yet)_

## Implemented, not adopted

F4, F5, and F6 landed together in `nl.rhaydus:core-common` on the foundation `release/0.3.0` branch (the result
helpers built on the logging facade). Softcover still ships the app-local copies and has not re-pointed its
imports.

### F4 — `runCatchingCancellable` helper that rethrows `CancellationException`

- **Type:** enhancement (shared util)
- **Home:** `nl.rhaydus:core-common` (the non-visual seam that already ships `AppDispatchers`)
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:core-common` (`common/RunCatchingCancellable.kt`, foundation `release/0.3.0`) as the pure primitive below; Softcover still ships its app-local `:core:domain` `result/RunCatchingCancellable.kt`. Adoption (a separate, later step) deletes the local copy and re-points the one import in `result/RunCatchingLogged.kt` to `nl.rhaydus.common`.

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
`core-common` rather than being re-derived per app. It is also the "cancellation-aware `runCatching`" that
**F1** names as the acceptable guarded form of a one-shot flow read, and it is the clean replacement for
the hand-rolled `catch (Throwable) { … }` + manual `CancellationException` rethrow pattern (e.g. the
`SoftcoverWorker` note in the architecture review).

**App-local implementation (delete on adopt).** Softcover now ships this in
`:core:domain` `result/RunCatchingCancellable.kt`, kept as a **pure primitive** exactly matching the
signature above (no logging, no app coupling) so it is a drop-in delete once the upstream version
lands — at which point `:core:domain` `result/RunCatchingLogged.kt` re-points its single import to the
foundation. `runCatchingLogged` is the Softcover-only wrapper (`runCatchingCancellable(block).onFailure { AppLog.e(…) }`)
that binds the app's logger once; it is the enforced use-case-body form (every `*UseCase*.kt` uses it,
flagged advisory by `scripts/style-check.sh`), and it stays in the app because logging is an app concern, not a
foundation one. The logger is **not** a parameter on the primitive: `kotlin.Result` already exposes the
failure-injection point (`.onFailure { }`), so a parameter would only duplicate stdlib, and keeping the
53 call sites on the app wrapper (rather than calling the primitive directly with an inline lambda)
means the upstream move touches one import, not every use case.

---

### F5 — `runCatchingLogged` (log-at-source use-case wrapper)

- **Type:** enhancement (shared util)
- **Home:** `nl.rhaydus:core-common` (next to the F4 primitive)
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:core-common` (`common/RunCatchingLogged.kt`, foundation `release/0.3.0`), on top of the now-upstream `AppLog` (F6); Softcover still ships its app-local `:core:domain` `result/RunCatchingLogged.kt`. Adoption deletes the local copy and re-points the 53 use-case sites' import to `nl.rhaydus.common` (the wrapper insulates them — a one-line import move, not 53 edits).

`runCatchingLogged` = `runCatchingCancellable` + a single `AppLog.e` on failure (optional `context`
label). It is the enforced use-case-body form: a failure is logged once, at the boundary, so it is never
silently dropped even when the caller discards the `Result` or omits the presentation fold. Every app on
the foundation that has a use-case layer wants this same guarantee, so the wrapper itself is a candidate —
**but it binds a logger**, which is why it waited on the foundation logging facade (see F6). With `AppLog`
now upstream, the upstream `runCatchingLogged` binds the foundation logger; Softcover deletes its local
`result/RunCatchingLogged.kt` and re-points one import on adopt.

**Should anything change in the app when this happens? No — and that's the point.** The 53 `*UseCase*.kt`
sites call `runCatchingLogged`, so the wrapper insulates them from the relocation exactly as it does for
the F4 primitive: the move is a one-line import change in the wrapper file, not 53 edits. Keep the logger
bound *inside* the wrapper (not exposed as a per-call lambda) — `kotlin.Result` already provides the
failure-injection point via `.onFailure { }`, so a parameter would duplicate stdlib and re-expose every
call site to the upstream move. The advisory "bare `runCatching` in a `*UseCase*.kt`" recipe (F7) can
likewise become a real foundation ktlint rule once `runCatchingLogged` is a foundation symbol.

---

### F6 — Logging facade (`AppLog`) belongs in the foundation

- **Type:** enhancement (shared util)
- **Home:** `nl.rhaydus:core-common` (alongside `AppDispatchers`, the existing non-visual seam)
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:core-common` (`common/AppLog.kt`, foundation `release/0.3.0`) as a brand-agnostic, Kermit-backed facade: the `tag` and line `prefix` are `install(...)` parameters (no `"Softcover"` constant), output stays debug-gated, and Kermit is an `implementation` dependency so no Kermit type leaks into the public surface. Earns its place per F2's bar (prefix formatter + debug-gated install + cross-target consistency, not a bare re-export) and unblocked F5. Softcover still ships its app-local `:core:domain` `logging/AppLog.kt`.

`AppLog` (`:core:domain` `logging/`) is a Kermit-backed, multiplatform logging facade: `i` / `w` / `e`
with message-and-throwable variants, a debug-gated `install(...)`, and a prefix formatter — call sites
stay platform-agnostic (Logcat / os_log / stdout). Every KMP app needs exactly this, and the foundation
already carries an **implicit dependency** on it: the `nl.rhaydus:ktlint-rules` `BlankLineAfterStatementRule`
keys on `AppLog.e` (per [`../reference/code-style.md`](../reference/code-style.md) §Error Handling). A
shared rule that hard-codes an *app-level* symbol name is a smell — hoisting a brand-agnostic logging
facade into `core-common` (the `"Softcover"` tag becomes config, not a constant) makes that rule's assumption
real and unblocks F5's `runCatchingLogged`.

Mind F2's caution: keep it earning its place. The value is the cross-target consistency, the custom prefix
formatter, and the debug-gated install — not a bare Kermit re-export (which a consumer would just as easily
hand-roll). If on inspection it is only the latter, demote this entry rather than ship a thin wrapper.

---

### F19 — Shared detekt config belongs in the foundation

- **Type:** enhancement (shared gate config)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Implemented, not adopted.** The foundation now ships a shared detekt baseline (`config/detekt.yml`, bundled in `nl.rhaydus:detekt-rules`) carrying the foundation-worthy calibrations only - Compose/TOAD `ignoreAnnotated` across the complexity/naming rules, `MagicNumber` off, `LargeClass`/`LongParameterList(constructor=30)`, guard-clause `ReturnCount`, snake_case `PackageNaming`; detekt's `formatting` ruleset stays off (ktlint owns layout). A `detektCheck` task runs it (plus the custom `rhaydus` ruleset) over the foundation's own source. App-specific thresholds (Room/DAO counts, Apollo exception policy, `Typos`) stay in each app's override file. On adopt, Softcover points its detekt at the shared config and drops the duplicated calibrations.

(Surfaced as one of the F18-F23 audit findings, but built now alongside F1 - standing up detekt for the type-resolved flow rule was the natural moment to centralize the config too.)

---

F9 and F10 landed together in the new `nl.rhaydus:core-platform` module on the foundation `release/0.3.0`
branch (the two non-visual platform-capability seams of Batch B). During this work `core-ui` was **split**:
the base non-visual primitives (`AppDispatchers`, `AppLog`, the `runCatching*` helpers, the formatters) were
renamed into `nl.rhaydus:core-common` (package `nl.rhaydus.common`), and the platform-capability seams live
one module out in `core-platform` (which `api`-depends on `core-common`). Both seams ship as an interface
plus public per-platform implementation classes with **no Koin module** — matching the foundation precedent
that each app wires its own DI. Softcover still ships its app-local copies and has not re-pointed its
imports.

### F9 — Secure cross-platform secret storage seam

- **Type:** enhancement (shared util / platform seam)
- **Home:** `nl.rhaydus:core-platform`
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:core-platform` as `SecureStorage`
  (`commonMain`) — the app's single-secret `SecureApiKeyStorage` generalized to a **keyed** read/write/delete
  store (`read(key)` / `write(key, value)` / `delete(key)`). Public per-platform impls: `AndroidSecureStorage`
  (AES/GCM in the Keystore, one ciphertext file per key under `filesDir`), `IosSecureStorage` (Keychain
  generic-password keyed by account), `JvmSecureStorage` (KSafe over the desktop OS secret store; `ksafe`
  added to the foundation catalog + `core-platform` jvmMain). Impls use `core-common`'s `AppLog`
  + `AppDispatchers`. Softcover still ships its app-local `:core:preferences`
  `data/security/SecureApiKeyStorage.kt` (+ the three actuals). Adoption re-points the preferences DI/data
  code onto `SecureStorage` (keyed by the app's `"api_key"`) and deletes the app-local copies.

### F10 — `NetworkAvailabilityProvider` (reactive + instant connectivity seam)

- **Type:** enhancement (shared util / platform seam)
- **Home:** `nl.rhaydus:core-platform`
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:core-platform`: the
  `NetworkAvailabilityProvider` interface (`isOnline: StateFlow<Boolean>`, `awaitOnline()`), the
  `NetworkAvailability` instant-check singleton, and a `BaseNetworkAvailabilityProvider` that implements the
  shared `awaitOnline()`. Softcover's `ConnectivityDataSource` + `ConnectivityRepositoryImpl` two-layer split
  is **collapsed** into one provider per platform: `AndroidNetworkAvailabilityProvider`
  (`ConnectivityManager`), `IosNetworkAvailabilityProvider` (`nw_path_monitor`),
  `JvmNetworkAvailabilityProvider` (reachability polling, `AutoCloseable`). Softcover still ships the
  app-local interface (`:core:domain` `connectivity/`) + data layer (`:core:connectivity`). Adoption
  re-points `NetworkAvailability.install(...)` and the DI onto the foundation types and deletes the app-local
  copies. (Unblocks Batch H's drain-on-network-return trigger.)

---

F2 and F3 landed together in `nl.rhaydus:designsystem-core` on the foundation `release/0.3.0` branch — the
bottom-bar batch (Batch C). Diagnosing the broken padding primitive (F2) and shipping the reusable host (F3)
converged on one deliverable: the write-side host the read primitive was always missing. Softcover still
ships its app-local forks and has not re-pointed its imports.

### F2 — `rememberBottomBarPadding()` does not work due to the way it's implemented

- **Type:** bug
- **Home:** `nl.rhaydus:designsystem-core` (layout)
- **Status:** **Implemented, not adopted.** Root cause confirmed: `LocalBottomBarPadding` (default `0.dp`) was
  read by `rememberBottomBarPadding()` but **never provided anywhere in the foundation** — no
  `CompositionLocalProvider(LocalBottomBarPadding provides …)` existed — so the helper always resolved to
  `0.dp` and scrolling content was occluded. `design-system-foundations.md` §5.2 delegated the write side to
  "the bottom-bar host screen," which the foundation never shipped. Fixed by shipping that host as
  `BottomBarScaffold` (see F3) and simplifying `rememberBottomBarPadding()` to a direct
  `LocalBottomBarPadding.current` read (the old `remember(current){current}` wrapper was a no-op). The host is
  double-inset-safe: it measures the laid-out bar (`onSizeChanged` outside `windowInsetsPadding`) so the
  nav-bar inset is counted once, not recomputed and re-added the way Softcover's `BottomBarScreen` did.
  **Surface audit (F2's wider ask):** scanned the designsystem-core public surface for the same
  "published-but-unusable / near-zero-value" class — the padding pair was *the* finding; no demotions or
  removals (`BottomNavigationSpacer`, `pointerHandCursor`, `conditional`, and the one-line `model/*` enums all
  earn their place). Softcover still ships its app-local forked `rememberBottomBarPadding()`
  (`core/designsystem/.../util/BottomBarPadding.kt`, the DOCKED→`16.dp` / FLOATING→local branch); adoption
  re-points the floating path onto `BottomBarScaffold` and deletes the fork.

### F3 — Make bottom bars reusable

- **Type:** enhancement (shared component)
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Implemented, not adopted.** Evaluated as **plumbing only**: the reusable, brand-agnostic value
  is (a) `BottomBarScaffold` — the measure-and-provide overlay host (also the F2 fix) that takes the
  brand-styled bar as a slot — and (b) the cross-tab pulse, generalized from Softcover's `BottomBarPulseManager`
  (a global object with a hardcoded `libraryPulseKey`) into `NavPulse` (an instance-owned, keyed signal:
  `pulse(key)` / `countFor(key)`) + `rememberPulseScale(pulse, key)` (the animated icon scale, gated by
  `playDecorativeMotion()`), both in a new `designsystem-core` `nav/` package. Only the concrete key stays
  app-supplied. **Deliberately not hoisted** (would violate the design-agnostic contract): the Voyager
  `TabNavigator` coupling, the four concrete renderers (docked `NavigationBar`, floating toolbar, rail,
  `EditorialSidebar`), `SessionPeekBar`, the app tab set, the `BottomBarStyle` preference, and the whole
  `BottomBarScreen` shell — the app composes those from the foundation primitives (`rememberWindowSizeClass`,
  `TwoPaneScaffold`, `BottomBarScaffold`, `NavPulse`). Softcover still ships its app-local
  `BottomBarPulseManager` + `libraryPulseKey` and the shell; adoption re-points `pulseLibrary()` onto a
  `NavPulse` instance keyed by the app's `LibraryTab` and passes the app bar into `BottomBarScaffold`.

---

F13 and F14 landed together in `nl.rhaydus:designsystem-core` on the foundation `release/0.3.0` branch — the
shared Compose components batch (Batch D). Two independent, brand-agnostic widgets lifted with the "expose
the brand bits as params" shape: `StarRatingInput` in the `component/` catalog, `ExpandableFlowRow` in the
`layout/` primitives. Softcover still ships its app-local forks and has not re-pointed its imports.

### F13 — `StarRatingInput` (half-star, drag-scrub, haptics)

- **Type:** enhancement (shared component)
- **Home:** `nl.rhaydus:designsystem-core` (shared component catalog)
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:designsystem-core`
  (`component/StarRatingInput.kt`) as the interactive N-star, half-star control — tap + drag-scrub, haptics
  on each half-step crossing, live preview committing on release. Brand-decoupled on the way in: the fill
  tint is a `filledColor` param (defaulting to `MaterialTheme.colorScheme.primary`) alongside `emptyColor`,
  and the star glyph is a `starIcon: RhaydusIconResource` param (was Softcover's `SoftcoverIcon.StarFilled`)
  — the foundation haptics (`rememberHaptics()`) it already used stays. The half-star math is extracted to
  the pure `ratingForOffsetX(...)` (unit-tested under `androidHostTest`). Softcover still ships its app-local
  `core/designsystem/.../presentation/component/StarRatingInput.kt`. Adoption re-points the two call sites
  (`ShareCard.kt`, `BookDetailShelf.kt`) onto the foundation symbol, passing `filledColor = RatingGold` and
  `starIcon = SoftcoverIcon.StarFilled`, and deletes the fork.

---

### F14 — `ExpandableFlowRow` (collapsible flow row with progressive reveal)

- **Type:** enhancement (layout primitive)
- **Home:** `nl.rhaydus:designsystem-core` (layout primitives)
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:designsystem-core`
  (`layout/ExpandableFlowRow.kt`) — a `FlowRow` that collapses to `collapsedLines` behind a trailing "show
  more" affordance and reveals `linesPerExpand` more lines per tap. Lifted almost verbatim (it had zero
  app-local imports); the header comment was genericized (the app-repo `now.md` / `PillChip` references
  dropped) and the show-more label became a `showMoreLabel` param on top of the existing `showMoreIndicator`
  slot. Softcover still ships its app-local
  `core/designsystem/.../presentation/component/ExpandableFlowRow.kt`. Adoption is a pure import swap at the
  one call site (`LibraryFilterSheet.kt`, all defaults) and deletes the fork.

---

F12 and F15 landed together in `nl.rhaydus:designsystem-core` on the foundation `release/0.3.0` branch — the
desktop (jvm) affordances batch (Batch E). Two self-contained, brand-agnostic desktop interaction affordances
lifted next to the existing `dismissOnEscape` / `DesktopContextMenu` helpers: `DesktopVerticalScrollbar` in the
`component/` catalog (jvm-only) and `platformModifierClick` as a `commonMain` expect/actual modifier (jvm real
gesture, mobile pass-through). Softcover still ships its app-local forks and has not re-pointed its imports.

### F12 — `DesktopVerticalScrollbar` (themed, dark-surface-visible)

- **Type:** enhancement (desktop affordance)
- **Home:** `nl.rhaydus:designsystem-core` (jvm affordances)
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:designsystem-core`
  (`component/DesktopScrollbar.kt`, jvmMain) as the themed vertical scrollbar with `LazyGridState` /
  `LazyListState` / `ScrollState` overloads, colouring the thumb to `MaterialTheme.colorScheme.onSurface` so it
  is visible on dark surfaces (Compose Desktop's default near-black thumb disappears there). Lifted verbatim;
  the only brand-named symbol — the private `softcoverScrollbarStyle()` — was renamed to
  `rhaydusScrollbarStyle()`, and the KDoc genericised (the "editorial surfaces" / "desktop Reading list"
  phrasing dropped). No params — the sole choice is a standard Material colour role, so it stays a pure
  skeleton. Softcover still ships its app-local
  `core/designsystem/.../presentation/component/DesktopScrollbar.kt`. Adoption re-points the 13 call sites
  across 9 feature modules (reading, settings, library, explore, profile, book_detail, onboarding, session)
  onto the foundation symbol and deletes the fork.

---

### F15 — `platformModifierClick` (desktop modifier-aware selection)

- **Type:** enhancement (desktop affordance / modifier)
- **Home:** `nl.rhaydus:designsystem-core` (modifier catalog)
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:designsystem-core` as a `commonMain`
  expect/actual `Modifier` extension (`modifier/PlatformModifierClick.kt` + `.jvm.kt` real gesture +
  `.mobile.kt` pass-through, mirroring `DesktopContextMenu`): Ctrl/Cmd toggles selection, Shift range-selects,
  intercepting in the pointer Initial phase and consuming only when a modifier is held (a plain click still
  reaches the inner `combinedClickable`), inert on touch. Lifted near-verbatim (zero app-local imports): the
  dangling `[jvmMain]` / `[mobileMain]` KDoc links were flattened to plain text, and the desktop actual's
  event loop was restructured to drop its two `continue` guards (behaviour-identical) for the foundation's
  from-zero detekt (`LoopWithTooManyJumpStatements`). Softcover still ships its
  app-local fork triple `core/designsystem/.../presentation/modifier/PlatformModifierClick{,.jvm,.mobile}.kt`.
  Adoption is an import swap at the one call site (`LibraryShelf.kt`) and deletes the fork.

---

# Open work — batched for implementation

## Batch A (implemented, not adopted) — Style gates → blocking rules

*Home: `nl.rhaydus:ktlint-rules` + `nl.rhaydus:detekt-rules`. Covers **F1, F7** - both landed on the
foundation `release/0.3.0` branch (see their statuses below); kept here for the rationale. Five recipes
became blocking ktlint rules; the flow-terminal one (F1) needed type resolution and became a detekt rule.
Not yet adopted: Softcover still runs its local `scripts/style-check.sh` until it consumes the new release.*

### F1 — Crash-safety gate for terminal flow reads should be a blocking ktlint rule

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Implemented, not adopted.** Shipped as the type-resolved detekt rule `UnguardedFlowTerminalRead` in `nl.rhaydus:detekt-rules` (foundation `release/0.3.0`). ktlint cannot tell `Flow.first()` from `Collection.first()` without type resolution, so the blocking rule lives in detekt (resolving the receiver fqName); the guarded `firstOrNull()`/`singleOrNull()` forms and the collection operators are never flagged. Softcover still ships the advisory `check_unguarded_flow_terminal` recipe; dropped on adopt.

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

### F7 — Promote the `style-check.sh` recipes to blocking ktlint rules (generalize F1)

- **Type:** gate (lint rules) + tooling ownership
- **Home:** `nl.rhaydus:ktlint-rules` (+ `nl.rhaydus:detekt-rules` for the type-resolved one)
- **Status:** **Implemented, not adopted (sub-move 1).** Five recipes are now blocking ktlint rules in `nl.rhaydus:ktlint-rules` — `one-type-per-file`, `no-fully-qualified-reference`, `project-import-order`, `inline-mockk-stub`, and `use-case-run-catching` (which names the upstream `runCatchingLogged`); the sixth (flow-terminal) is F1's detekt rule. **Sub-move 2** (own the `style-check.sh` harness + on-touch hook in the `style-check` skill) is tracked as **F22** and lands at adopt, when Softcover retires the now-redundant recipes.

`scripts/style-check.sh` is an app-local bash port of the greppable code-style rules: inline
fully-qualified references, one-type-per-file, project-import ordering, **inline mockk stubs**, unguarded
terminal flow reads (F1), and bare `runCatching` in a use case (F5). Most of these are *foundation* style
rules, not Softcover's — e.g. the inline-mockk-stub rule ("open the `coEvery`/`every` block onto its own
line") is explicitly a foundation `code-style.md` rule. So they belong in `nl.rhaydus:ktlint-rules` as
real auto-fixing / gating rules, where every consuming project gets them with zero setup and a hard CI
gate — not re-derived as advisory greps in each app's `scripts/`. Two sub-moves:

1. **Promote each greppable rule to a ktlint rule** (blocking, like the rest of the ruleset). F1 already
   files the flow-terminal one; the same applies to inline mockk stubs, inline FQ refs, one-type-per-file,
   and import ordering. The bare-`runCatching`-in-a-use-case rule is gated on F5 (it names the upstream
   `runCatchingLogged`).
2. **Own the script upstream, not per app.** The `rhaydus-kotlin` plugin already ships a `style-check`
   skill; until a rule is promoted, its recipe (and the script harness) should live with that skill so
   apps don't each copy and maintain `style-check.sh`. The app keeps only genuinely app-specific recipes.

---

## Batch F — Error-slot + inline error UX (`designsystem-core` + `toad`)

*Home: `nl.rhaydus:designsystem-core` (component) + `nl.rhaydus:toad` (`toad-architecture.md`). Covers
**F16, F17**. These are one feature split across two modules: F16 is the inline error/retry component and
F17 is the TOAD `UiState` convention that renders through it. Design them together so the component's API
and the state-slot contract match; F17 leans on the now-implemented `runCatchingLogged` (F5) for the
cancellation guarantee.*

### F16 — `InlineErrorState` (inline load/submit-failure + retry surface)

- **Type:** enhancement (shared component)
- **Home:** `nl.rhaydus:designsystem-core` (shared component catalog)
- **Status:** Open — **app-local now** (added in Phase 2b); delete on adopt

`InlineErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier)`
(`core/designsystem/.../presentation/component/InlineErrorState.kt`) renders a failure message in the
error colour role plus a retry affordance — the standard in-content treatment for a failed load/submit
(as opposed to the full-screen `OfflineGuard`-style placeholder). The Phase 2b survey confirmed the
foundation ships **no** inline error/empty+retry component, yet every app needs one. The skeleton is
brand-agnostic; only the button/typography/error-role bindings are app theme, layered as usual. Pairs
with the TOAD error-slot convention (F17).

---

### F17 — A TOAD `UiState` error-slot + retry convention

- **Type:** enhancement (framework convention / shared contract)
- **Home:** `nl.rhaydus:toad` / `toad-architecture.md`
- **Status:** Open — app-local convention now; bless it in the TOAD baseline upstream

TOAD's `UiState` ships no standard error affordance, so each app re-invents how a screen surfaces a
load/submit failure. Softcover's Phase 2b convention: a screen that can fail exposes a nullable
`String?` error slot on its `UiState` (e.g. `ExploreScreenUiState.searchError`,
`OnboardingUiState.submissionError`), set by the action — copy authored in *presentation* via
`toUserMessage()` plus a screen-specific fallback — cleared on edit/retry, and rendered by
`InlineErrorState` (F16) whose retry re-dispatches the screen's own action. Cancellation is **not**
re-handled in the fold: `runCatchingLogged` (F5) guarantees it at the use-case boundary, so the slot
only ever holds a real failure. This is mostly a *documented convention* (the slot is per-screen, so
there is little framework code to own) — the upstream move is to bless it in `toad-architecture.md`, and
optionally offer an opt-in `UiError` type / base interface for screens that want a richer shape. Kept
deliberately light locally (a provisional note in `docs/reference/architecture.md`) pending the
foundation owning it — the foundation, not the app, should define the canonical TOAD error contract.

---

## Batch G — Image export (`designsystem-image`)

*Home: `nl.rhaydus:designsystem-image` (existing) or `designsystem-core`. Covers **F11**. Standalone — a
self-contained capture-to-image platform seam with no shared work with the other batches.*

### F11 — `ShareCardCapture` (capture a composable to an image, save/share)

- **Type:** enhancement (shared util / platform seam)
- **Home:** `nl.rhaydus:designsystem-image` (existing) or `designsystem-core`
- **Status:** Open — already well-shaped

`ShareCardCapture` (`core/designsystem/.../presentation/share/ShareCardCapture.kt`, with `.android`,
`.jvm`, `.ios` implementations) renders a composable to a bitmap via a `GraphicsLayer` and then
encodes / saves / shares it, with result types for the save and share outcomes. The capture-encode-save-share
mechanism — including the genuinely fiddly platform seams (scoped-storage save on Android, clipboard/file
on desktop, the iOS share path) — is 100% brand-agnostic skeleton; only the *card design* fed into it is
app-specific. Every Compose-Multiplatform app that ever exports an image re-derives this, so it is a
high-value hoist.

---

## Batch H — Offline mutation queue (new connectivity/offline seam)

*Home: a foundation offline/connectivity seam (likely a new module built on the skeleton). Covers **F8**.
Standalone and the largest piece — **requires a generic-skeleton extraction first** and depends on Batch B's
`NetworkAvailabilityProvider` (F10) for the drain-on-network-return trigger. Land it last.*

### F8 — Offline mutation queue + drain-and-reconcile pattern

- **Type:** enhancement (shared infra) — **requires a generic-skeleton extraction first**
- **Home:** a foundation offline/connectivity seam (likely a new module built on the skeleton)
- **Status:** Open — pattern is sound but currently entangled with Softcover's domain models

Softcover's offline write path (`core/connectivity/.../data/sync/PendingUserBookWriteSyncer.kt`,
`PendingListWriteSyncer.kt`; domain contracts in `core/domain/connectivity/`; the
`OfflineUserBookSync` collaborator) is a full optimistic-write-replay engine: enqueue a write locally,
drain it when the network returns and on startup, replay with kind-specific dispatch, halt-and-retry on
transient failure vs. discard on terminal failure (with bounded exponential backoff), and return
per-entity *reconciliation hints* so the merge step knows which local fields a successful replay owns.
Every offline-capable app re-solves exactly this.

What's reusable is the **skeleton**: a `Queue<T>` enqueue facade, a `Drainer<T, K>` contract that takes
replay lambdas (not hard-coded mutation dispatch) and returns `Map<EntityId, Set<Kind>>` hints, plus the
backoff/halt policy. What's app-specific and must be lifted out before it can upstream: the
`PendingUserBookWrite` / `PendingListWrite` payload shapes, the `PendingUserBookWriteKind` enum, and the
hard-wired dispatch to `BooksRemoteDataSource` / `ListsRemoteDataSource`. This is medium structural work
— **file it, but don't lift the current Softcover-specific shapes directly**; extract the generic
contracts first.

---

## Batch I — Shared build & gate tooling

*Home: `build-logic` convention plugins / `nl.rhaydus:detekt-rules` / the `rhaydus-kotlin` `style-check`
skill. Covers **F18, F20, F21, F22, F23** - reusable build gates and policies surfaced during the Batch A
work, none yet extracted. Most ship with the convention plugins so a consuming app inherits the gate with
zero setup; only the concrete allowlists/thresholds stay per-app.*

### F18 — `checkModuleGraph` tier-DAG + api-visibility enforcement

- **Type:** gate (custom Gradle task)
- **Home:** `build-logic` convention plugins
- **Status:** Open - strongest new candidate

A custom verification task that fails on any module edge violating the tier DAG (the `core` / `feature` /
orchestration allowed-direction model) and on an `api` edge to a data-area module unless allowlisted. The
tier model is already a foundation architecture concept (`architecture.md` / module-structure), so the
enforcement *mechanism* is foundation-worthy; only the concrete `dataAreaModules` / `allowedApiDataEdges` /
per-module edge lists are app data. Softcover has it inline in `build.gradle.kts`.

### F20 — `dependencyAnalysis` (buildHealth) gating policy

- **Type:** gate (policy)
- **Home:** `build-logic` convention plugins (ships with the convention bundle)
- **Status:** Open

Gate `onUnusedDependencies` + `onIncorrectConfiguration` to fail; set `onUsedTransitive` / `onRuntimeOnly` /
`onRedundantPlugins` to ignore. The policy plus the exclusion list of the centrally-provided convention
bundle (Koin, coroutines, JUnit5/Kotest/MockK/Turbine, Compose MP artifacts) is foundation-worthy - the
exclusions mirror exactly what the convention plugins provide. Only app-library false positives stay local.

### F21 — Shared `lint.xml` + `warningsAsErrors` policy

- **Type:** gate (policy + config)
- **Home:** `build-logic` convention plugins + a shared `lint.xml`
- **Status:** Open

Every module references one shared lint config with `warningsAsErrors` / `abortOnError`. The version-freshness
checks (`NewerVersionAvailable` / `GradleDependency` / `AndroidGradlePluginVersion`) set to `informational`
because every nl.rhaydus app pins to the foundation catalog. The convention plugins already wire
`lintConfig = rootProject.file("lint.xml")`; the shared `lint.xml` file and the freshness policy are the
missing pieces.

### F22 — On-touch style hook + script ownership in the `style-check` skill

- **Type:** tooling ownership
- **Home:** the `rhaydus-kotlin` `style-check` skill
- **Status:** Open - this is F7's sub-move 2

The PostToolUse adapter (`scripts/kt-style-hook.sh`) that runs the style script on the just-edited file and
feeds findings back for on-touch fixing is reusable infra. It - and any residual greppable recipes not yet
promoted to rules - should live with the `style-check` skill so every app gets on-touch enforcement without
copying the adapter. Done at adopt, alongside retiring the now-promoted recipes from Softcover's
`scripts/style-check.sh`.

### F23 — "No raw `println` / `Log.*` - use the logging facade" rule

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:detekt-rules` (a `ForbiddenMethodCall`-style rule) or `nl.rhaydus:ktlint-rules`
- **Status:** Open

A mechanizable ban on raw `println` / `android.util.Log.*` in favour of the `AppLog` facade (now upstream,
F6). Currently review-only in Softcover's `code-style.md`. Pairs with F6 and is a natural addition to the new
detekt ruleset built for F1.
