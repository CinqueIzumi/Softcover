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

**Include the cross-tab pulse signal.** Softcover ships `BottomBarPulseManager`
(`core/designsystem/src/commonMain/.../presentation/util/BottomBarPulseManager.kt`): a process-wide,
non-persistent one-shot signal that makes a bottom-bar tab icon briefly pulse when an event lands on a
*different* tab (e.g. a book added from the Reading tab landing on the Library shelf). The mechanism — a
keyed broadcast that nav components observe to play a scale pulse — is part of the reusable bottom-bar
contract and should ship with it; only the concrete key name (`libraryPulseKey`) is app-specific and
becomes caller-supplied. Without this, every app re-invents the "notify the nav of a cross-feature
event" plumbing around the shared bottom bar.

---

## F4 — `runCatchingCancellable` helper that rethrows `CancellationException`

- **Type:** enhancement (shared util)
- **Home:** `nl.rhaydus:core-ui` (the non-visual seam that already ships `AppDispatchers`)
- **Status:** Open — **app-local implementation now in Softcover**; replace with the upstream version when it lands

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

## F5 — `runCatchingLogged` (log-at-source use-case wrapper)

- **Type:** enhancement (shared util)
- **Home:** `nl.rhaydus:core-ui` (next to the F4 primitive) — **gated on F6**
- **Status:** Open — app-local in Softcover; upstream only once a foundation logging facade (F6) exists

`runCatchingLogged` = `runCatchingCancellable` + a single `AppLog.e` on failure (optional `context`
label). It is the enforced use-case-body form: a failure is logged once, at the boundary, so it is never
silently dropped even when the caller discards the `Result` or omits the presentation fold. Every app on
the foundation that has a use-case layer wants this same guarantee, so the wrapper itself is a candidate —
**but it binds the app's logger**, and the foundation has no logging facade yet (see F6). It therefore
cannot upstream until F6 lands; at that point an upstream `runCatchingLogged` binds the foundation logger
and Softcover deletes its local `result/RunCatchingLogged.kt`, re-pointing one import.

**Should anything change in the app when this happens? No — and that's the point.** The 53 `*UseCase*.kt`
sites call `runCatchingLogged`, so the wrapper insulates them from the relocation exactly as it does for
the F4 primitive: the move is a one-line import change in the wrapper file, not 53 edits. Keep the logger
bound *inside* the wrapper (not exposed as a per-call lambda) — `kotlin.Result` already provides the
failure-injection point via `.onFailure { }`, so a parameter would duplicate stdlib and re-expose every
call site to the upstream move. The advisory "bare `runCatching` in a `*UseCase*.kt`" recipe (F7) can
likewise become a real foundation ktlint rule once `runCatchingLogged` is a foundation symbol.

---

## F6 — Logging facade (`AppLog`) belongs in the foundation

- **Type:** enhancement (shared util)
- **Home:** `nl.rhaydus:core-ui` (alongside `AppDispatchers`, the existing non-visual seam)
- **Status:** Open — evaluate (unblocks F5)

`AppLog` (`:core:domain` `logging/`) is a Kermit-backed, multiplatform logging facade: `i` / `w` / `e`
with message-and-throwable variants, a debug-gated `install(...)`, and a prefix formatter — call sites
stay platform-agnostic (Logcat / os_log / stdout). Every KMP app needs exactly this, and the foundation
already carries an **implicit dependency** on it: the `nl.rhaydus:ktlint-rules` `BlankLineAfterStatementRule`
keys on `AppLog.e` (per [`../reference/code-style.md`](../reference/code-style.md) §Error Handling). A
shared rule that hard-codes an *app-level* symbol name is a smell — hoisting a brand-agnostic logging
facade into `core-ui` (the `"Softcover"` tag becomes config, not a constant) makes that rule's assumption
real and unblocks F5's `runCatchingLogged`.

Mind F2's caution: keep it earning its place. The value is the cross-target consistency, the custom prefix
formatter, and the debug-gated install — not a bare Kermit re-export (which a consumer would just as easily
hand-roll). If on inspection it is only the latter, demote this entry rather than ship a thin wrapper.

---

## F7 — Promote the `style-check.sh` recipes to blocking ktlint rules (generalize F1)

- **Type:** gate (lint rules) + tooling ownership
- **Home:** `nl.rhaydus:ktlint-rules` (the rules) + the `rhaydus-kotlin` `style-check` skill (the script)
- **Status:** Open — **F1 is the first instance**; this generalizes it to the rest

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

## F8 — Offline mutation queue + drain-and-reconcile pattern

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

## F9 — Secure cross-platform secret storage seam

- **Type:** enhancement (shared util / platform seam)
- **Home:** `nl.rhaydus:core-ui`, or a dedicated security seam
- **Status:** Open — already cleanly abstracted; no refactor needed

`SecureApiKeyStorage` (`core/preferences/src/commonMain/.../data/security/SecureApiKeyStorage.kt`) is a
minimal read/write/delete interface over hardware-backed secret storage, with platform implementations
for Android (Keystore), iOS (Keychain), and desktop. Cross-platform secret storage is universal KMP
infrastructure — not app-specific — and the interface is already clean and correct. The only app-coupled
thing is the name (it stores *an API key*); generalize it to `SecureStorage` / a keyed secret store and
it drops into the foundation as-is. Mind F2's bar: the value here is the three real platform-backed
implementations, not the interface alone.

---

## F10 — `NetworkAvailabilityProvider` (reactive + instant connectivity seam)

- **Type:** enhancement (shared util / platform seam)
- **Home:** `nl.rhaydus:core-ui` (alongside `AppDispatchers`)
- **Status:** Open — straightforward seam

`NetworkAvailabilityProvider` (`core/domain/connectivity/NetworkAvailabilityProvider.kt`) exposes
connectivity both reactively (`isOnline: StateFlow<Boolean>`, `awaitOnline()`) and synchronously (the
`NetworkAvailability` instant-check singleton, for guard clauses like "throw offline if not connected").
Every KMP app needs both shapes, and the interface is fully domain-agnostic — only the per-platform
implementations (Android `ConnectivityManager`, iOS Network framework, desktop) bind underneath. Hoisting
this also unblocks a foundation connectivity-banner component (Softcover's `ConnectivityBanner` /
`OfflineGuard` are generic except for their dependency on this provider type and their copy).

---

## F11 — `ShareCardCapture` (capture a composable to an image, save/share)

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

## F12 — `DesktopVerticalScrollbar` (themed, dark-surface-visible)

- **Type:** enhancement (desktop affordance)
- **Home:** `nl.rhaydus:designsystem-core` (jvm affordances)
- **Status:** Open

`DesktopScrollbar.kt` (`core/designsystem/src/jvmMain/.../presentation/component/`) is a themed vertical
scrollbar with overloads for `LazyGridState`, `LazyListState`, and `ScrollState`, colouring the thumb to
`onSurface` so it is actually visible — Compose Desktop's default near-black thumb disappears on dark
editorial surfaces. Pure skeleton (the only choice is a standard Material colour role), and it belongs in
the foundation's jvm affordances section next to the existing desktop helpers.

---

## F13 — `StarRatingInput` (half-star, drag-scrub, haptics)

- **Type:** enhancement (shared component)
- **Home:** `nl.rhaydus:designsystem-core` (shared component catalog)
- **Status:** Open — parameterize the fill colour

`StarRatingInput` (`core/designsystem/.../presentation/component/StarRatingInput.kt`) is an interactive
N-star rating control with half-star precision: tap and drag-to-scrub gestures, haptics fired on each
half-step crossing, and a live drag preview that commits on release. The half-star math, the drag
handling, and the haptics hookup are non-trivial and re-derived by every app with a rating surface. The
only brand coupling is the filled-star tint (`RatingGold`); parameterize it (`filledColor` / `emptyColor`)
and it is fully generic.

---

## F14 — `ExpandableFlowRow` (collapsible flow row with progressive reveal)

- **Type:** enhancement (layout primitive)
- **Home:** `nl.rhaydus:designsystem-core` (layout primitives)
- **Status:** Open

`ExpandableFlowRow` (`core/designsystem/.../presentation/component/ExpandableFlowRow.kt`) is a `FlowRow`
that collapses to a maximum number of lines with a trailing "show more" affordance, revealing
`linesPerExpand` further lines per tap (gradual reveal rather than all-at-once). It solves a recurring
layout problem — unbounded tag/chip rows burying content — and sits naturally alongside the foundation's
existing layout primitives (e.g. `TwoPaneScaffold`). Generic skeleton; parameterize the show-more
affordance as a composable slot (defaulting to the chip) and the label text.

---

## F15 — `PlatformModifierClick` (desktop modifier-aware selection)

- **Type:** enhancement (desktop affordance / modifier)
- **Home:** `nl.rhaydus:designsystem-core` (jvm affordances / modifier catalog)
- **Status:** Open

`PlatformModifierClick` (`core/designsystem/.../presentation/modifier/PlatformModifierClick.kt`, with
`.jvm` and `.mobile` actuals) adds desktop modifier-click selection — Ctrl/Cmd to toggle, Shift to
range-select — intercepting in the Initial pointer phase so a plain click still fires, and passing
through untouched on touch platforms. A standard desktop multi-select gesture, fully brand-agnostic; it
belongs with the foundation's other jvm affordances (`dismissOnEscape`, desktop context menu) and pairs
with F12.
