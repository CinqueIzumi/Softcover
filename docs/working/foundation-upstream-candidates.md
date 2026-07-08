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
entries refer to is indexed in [`../rhaydus/0.3.0/CAPABILITIES.md`](../rhaydus/0.3.0/CAPABILITIES.md).

Each entry: **type** (bug / enhancement / gate), **home** (target foundation module), **status**, and
enough context for whoever picks it up.

The open candidates are organised below into **implementation batches** — each batch clusters items that
share a target module and a kind of work, so it can be designed and landed in one focused pass. F-numbers
are stable identifiers (referenced from commits and other docs) and are **never reused or renumbered**, so
they are not sequential within a batch. Batches are ordered to respect the dependencies noted on each.

## ✅ Adoption progress — LANDED & committed (updated 2026-07-10)

Softcover runs on the **local 0.3.0** foundation (`foundation.local=true` in `local.properties` — gitignored, so it
stays local — includeBuild `../rhaydus-foundation` @ branch `release/0.3.0`). Everything below is **committed and
green** (`ktlintCheck`, `styleCheck`, `buildHealth`, `checkModuleGraph`, Android+JVM compile, affected host tests).

**Adopted so far (18 F-items):** F4/F5/F6 (core-common), F13/F14, F16/F17, F12/F15, F11 (designsystem-core), F17
(toad convention), F7/F23 (ktlint gates), F3 (NavPulse + `BottomBarScaffold`), F2 (designsystem-core), F1/F19/F22
(type-resolved detekt + shared baseline; `style-check.sh` retired). Commits on Softcover `hotfix/3.0.3`: `39df3489`
(switch + F4/F5/F6 + Batch A) → `9b29f108` (F13/F14) → `8cb8c976` (F16/F17) → `5530c5e5` (F12/F15) → `cbfecab4` (F11)
→ `9a79d874` (F7/F23) → `5e7467d5` (F3 NavPulse) → `b2662405` (F2/F3 `BottomBarScaffold`) → F1/F19/F22, plus doc
records. Foundation `release/0.3.0`: `40b23bf` (mockk-stub autocorrect + two ktlint carve-outs) → `1e0a159`
(`BottomBarPlacement`) → the detekt baseline's type-resolution calibration + the rule's hot-flow carve-out.

**Remaining (not adopted) — each needs a call/verification I can't do solo:**
- **F9/F10 → F8** (core-platform SecureStorage/connectivity, then offline-sync) — iOS actuals the build hook won't
  compile here, and F10↔F8 are entangled; best when iOS can be built/tested.
- **F18/F20/F21** (build-logic convention plugins) — likely need the foundation `build-logic` **published as Gradle
  plugins** to apply in the app build; awkward under `foundation.local`.

⏱ **`styleCheck` is now ~24s warm (was ~2s).** It runs the type-resolved detekt tasks, which need the compile
classpath and therefore compile Android + JVM. That is the accepted price of F1: the rule is `@RequiresTypeResolution`
and is silently inert on the source-only `detekt` task, so a fast gate here would be a gate that never fires.

⚠️ Because `foundation.local` stays out of git, the committed Softcover state resolves catalog `0.3.0` from the
**published** `nl.rhaydus:*` artifacts — so **foundation 0.3.0 must be published** before CI / a fresh clone (without
`foundation.local=true`) can build.

### Done this pass
- **Wiring → local 0.3.0** (via `rhaydus-adopt`): catalog `0.2.0`→`0.3.0`; retired `core-ui` → `core-common`
  (added `core-platform` / `offline-sync` / `detekt-rules` catalog entries for later batches); 18 build files
  `libs.rhaydus.coreUi`→`coreCommon`; package rename `nl.rhaydus.ui.common`→`nl.rhaydus.common` (AppDispatchers +
  date/number formatters); docs re-vendored `docs/rhaydus/0.2.0/`→`docs/rhaydus/0.3.0/`; managed CLAUDE.md block refreshed.
- **F4/F5/F6 adopted** — see *Implemented & adopted* below. Deleted app-local `:core:domain`
  `result/RunCatchingCancellable.kt`, `result/RunCatchingLogged.kt`, `logging/AppLog.kt` (+ their 2 tests);
  re-pointed the imports to `nl.rhaydus.common`; install sites now `AppLog.install(tag = "Softcover", …)`; added
  `implementation(libs.rhaydus.coreCommon)` to app, desktopApp (replacing its facade-only `:core:domain` dep),
  orchestration, core/{deadlines,identity,notification,profile}, feature:app_update **androidMain**; removed the
  now-unused `libs.kermit` from `:core:domain`. `code-style.md` updated (helpers now live in `nl.rhaydus:core-common`).
- **Batch A (F1/F7) ktlint tail cleared to green** — consuming the 0.3.0 ktlint ruleset activated the F7 rules as
  hard gates (~2076 pre-existing violations). Resolved via three moves (all done):
  1. **`inline-mockk-stub` made autocorrecting** (foundation change): `InlineMockkStubRule` was detect-only; added a
     whitespace-only reflow autocorrect + updated its test to assert formatted output. `ktlintFormat` cleared
     **1988 → 0**. Also sorted 110 `project-import-order` files (detect-only rule).
  2. **Two foundation rule carve-outs** (foundation change, + unit tests): `OneTypePerFileRule` now exempts a single
     interface + its single implementing class (the 19 co-located `interface Foo`+`class FooImpl` datasources);
     `InlineFullyQualifiedReferenceRule` now exempts generated `nl.rhaydus.*.fragment.*` types (the 13 `BookMapperTest`
     Apollo refs). Cleared 32 sites with no app churn.
  3. **56 safe FQ hoists** in Softcover — 44 uniform `testScope: kotlinx.coroutines.test.TestScope` params + 11 misc
     (test files via two `unit-test-writer` agents; `SoftcoverNotifierImpl.kt` iosMain `@Volatile` directly).
- **`buildHealth` under `foundation.local`** — dependency-analysis can't resolve includeBuild-substituted artifacts, so
  it false-flags every `nl.rhaydus:*` dep as unused. Added a `foundationLocal`-guarded `exclude(nl.rhaydus:*)` to the
  root `dependencyAnalysis { onUnusedDependencies }` (local mode only; published/CI mode resolves them and the gate
  stays fully effective).
- **code-reviewer** run over the whole diff: 3 minor findings, all fixed (iosMain import order, `build.gradle.kts`
  `Properties` import, 2 added `OneTypePerFileRule` edge-case tests) and re-verified green.

### Next up
1. **Publish foundation `0.3.0`** (or keep everyone on `foundation.local=true`) so non-local builds resolve the
   catalog `0.3.0` coordinates — see the ⚠️ above.
2. Continue with the next foundation batch: **F9/F10 → F8** (`core-platform` + `offline-sync`, wants an iOS build) or
   **F18/F20/F21** (`build-logic` convention plugins, wants the foundation `build-logic` published as Gradle plugins).
3. **Wire the foundation's own `detektCheck` into its `check`.** It is registered but attached to nothing, so the
   foundation does not gate on the config it ships. Also tracked in `now.md`.

---

## Batch index

| Batch | Theme | Home | Items |
|---|---|---|---|
| — | **Implemented & adopted** | `core-common` / `designsystem-core` / `toad` / `ktlint-rules` / `detekt-rules` | F1, F2, F3, F4, F5, F6, F7, F11, F12, F13, F14, F15, F16, F17, F19, F22, F23 |
| — | **Implemented, not adopted** | `build-logic` / `core-platform` / `offline-sync` | F8, F9, F10, F18, F20, F21 |

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

F4, F5, and F6 landed together in `nl.rhaydus:core-common` (foundation `release/0.3.0`, package
`nl.rhaydus.common`) — the result helpers built on the logging facade — and are now **live in Softcover**:
the app-local `:core:domain` copies are deleted and every import re-points to the foundation symbol.

### F4 — `runCatchingCancellable` helper that rethrows `CancellationException`

- **Type:** enhancement (shared util)
- **Home:** `nl.rhaydus:core-common`
- **Status:** **Implemented & adopted.** The cancellation-aware `runCatching` (rethrows
  `CancellationException` before treating a throwable as failure) ships as `nl.rhaydus.common.runCatchingCancellable`.
  Softcover's app-local `:core:domain` `result/RunCatchingCancellable.kt` (and its test) are deleted; it had no
  direct call sites of its own (only `runCatchingLogged` composed it), so adoption was purely the delete.

### F5 — `runCatchingLogged` (log-at-source use-case wrapper)

- **Type:** enhancement (shared util)
- **Home:** `nl.rhaydus:core-common`
- **Status:** **Implemented & adopted.** `runCatchingLogged` (= `runCatchingCancellable` + a single
  `AppLog.e` on failure, optional `context`) ships as `nl.rhaydus.common.runCatchingLogged`, binding the
  now-upstream `AppLog` (F6). Softcover's app-local `:core:domain` `result/RunCatchingLogged.kt` (and its
  test) are deleted; the 54 `*UseCase*.kt` sites had their import re-pointed to `nl.rhaydus.common`. The
  bare-`runCatching`-in-a-use-case gate is now the foundation `nl.rhaydus:ktlint-rules` `use-case-run-catching`
  rule.

### F6 — Logging facade (`AppLog`) belongs in the foundation

- **Type:** enhancement (shared util)
- **Home:** `nl.rhaydus:core-common`
- **Status:** **Implemented & adopted.** The brand-agnostic, Kermit-backed `AppLog` facade ships as
  `nl.rhaydus.common.AppLog` — the `tag` and line `prefix` are `install(...)` parameters (no `"Softcover"`
  constant), output stays debug-gated, Kermit is an `implementation` dep so no Kermit type leaks. Softcover's
  app-local `:core:domain` `logging/AppLog.kt` is deleted; the ~77 `AppLog` call sites re-point to
  `nl.rhaydus.common`, and the two install sites (`SoftCoverApp`, desktop `Main`) now pass
  `AppLog.install(tag = "Softcover", debug = …)`. The foundation `BlankLineAfterStatementRule` still keys on
  `AppLog.e`, now against the upstream symbol. Modules that reached `AppLog`/`runCatchingLogged` transitively
  via `:core:domain` now declare `nl.rhaydus:core-common` directly (app, desktopApp, orchestration, core/{deadlines,
  identity, notification, profile}, feature/app_update androidMain); desktopApp's `:core:domain` dep — which
  existed only for the facade — is replaced by `core-common`.

F13 and F14 landed together in `nl.rhaydus:designsystem-core` (Batch D — shared Compose components) and are now
**live in Softcover**: the app-local forks are deleted and the call sites re-point to the foundation symbols.

### F13 — `StarRatingInput` (half-star, drag-scrub, haptics)

- **Type:** enhancement (shared component)
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Implemented & adopted.** The interactive half-star control ships as
  `nl.rhaydus.designsystem.component.StarRatingInput`, brand-decoupled via a `starIcon: RhaydusIconResource`
  param and a `filledColor` (default `MaterialTheme.colorScheme.primary`) alongside `emptyColor`. Softcover's
  app-local `core/designsystem/.../component/StarRatingInput.kt` is deleted; the two call sites (`ShareCard.kt`,
  `BookDetailShelf.kt`) pass `starIcon = drawableIconResource(icon = SoftcoverIcon.StarFilled, …)` and
  `filledColor = RatingGold` (the `emptyColor` low-alpha `onSurfaceVariant` and the a11y label are foundation
  defaults, so the brand look is unchanged). The pure `ratingForOffsetX(...)` math is unit-tested in the
  foundation, so no app-local test remains. `design-system.md` §Star rating input updated.

### F14 — `ExpandableFlowRow` (collapsible flow row with progressive reveal)

- **Type:** enhancement (layout primitive)
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Implemented & adopted.** The collapsible `FlowRow` ships as
  `nl.rhaydus.designsystem.layout.ExpandableFlowRow` (a `showMoreLabel` param on top of the `showMoreIndicator`
  slot). Softcover's app-local `core/designsystem/.../component/ExpandableFlowRow.kt` (and its private
  `ShowMoreChip`) is deleted; the one call site (`LibraryFilterSheet.kt`) is a pure import swap (all defaults).
  `design-system.md` §Expandable flow row updated.

F16 and F17 landed together (Batch F — error-slot + inline error UX) and are now **live in Softcover**: the
`InlineErrorState` component (in `designsystem-core`) and the TOAD error-slot convention (documented in
`toad-architecture.md`) were designed together, and the app now consumes both.

### F16 — `InlineErrorState` (inline load/submit-failure + retry surface)

- **Type:** enhancement (shared component)
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Implemented & adopted.** The in-content failure surface ships as
  `nl.rhaydus.designsystem.component.InlineErrorState`, brand-decoupled via `retryLabel: String = "Retry"`
  and `textStyle: TextStyle = MaterialTheme.typography.bodySmall` params (error tint stays the Material
  `error` role; retry is the foundation `RhaydusButton`). Softcover's app-local
  `core/designsystem/.../component/InlineErrorState.kt` is deleted; the two call sites
  (`ExploreScreenLayout.mobile.kt`, `OnboardingShelf.kt`) pass `textStyle = MaterialTheme.editorialTypography.bodySmall`
  (the `retryLabel` default matches), so the editorial look is preserved. `design-system.md` §Inline error state updated.

### F17 — A TOAD `UiState` error-slot + retry convention

- **Type:** enhancement (framework convention / shared contract)
- **Home:** `nl.rhaydus:toad` / `toad-architecture.md`
- **Status:** **Implemented & adopted.** The convention (nullable `String?` slot on the screen's own
  `UiState`, folded in `.onFailure` with presentation-authored copy, cleared on retry / any invalidating edit,
  rendered with `InlineErrorState`, no `CancellationException` re-handling) is blessed in the foundation
  `toad-architecture.md` §Conventions. Softcover's provisional note in `docs/reference/architecture.md` is
  re-pointed at that canonical convention; the app-specific `toUserMessage()` copy-authoring stays app-side.

F12 and F15 landed together in `nl.rhaydus:designsystem-core` (Batch E — desktop jvm affordances) and are now
**live in Softcover**: the app-local forks are deleted and the call sites re-point to the foundation symbols.

### F12 — `DesktopVerticalScrollbar` (themed, dark-surface-visible)

- **Type:** enhancement (desktop affordance)
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Implemented & adopted.** The themed jvm scrollbar (`LazyGridState` / `LazyListState` /
  `ScrollState` overloads, thumb tinted to `onSurface` for dark surfaces) ships as
  `nl.rhaydus.designsystem.component.DesktopVerticalScrollbar` (no params — pure skeleton). Softcover's app-local
  `core/designsystem/.../component/DesktopScrollbar.kt` (jvmMain, with its private `softcoverScrollbarStyle()`) is
  deleted; the call sites (15 invocations across 10 jvm layout files in 8 feature modules — reading, settings,
  library, explore, profile, book_detail, onboarding, session) are pure import swaps. `design-system.md`
  §Desktop scrollbar updated.

### F15 — `platformModifierClick` (desktop modifier-aware selection)

- **Type:** enhancement (desktop affordance / modifier)
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Implemented & adopted.** The `commonMain` expect/actual `Modifier` extension (Ctrl/Cmd toggle +
  Shift range-select on jvm, pass-through on mobile) ships as `nl.rhaydus.designsystem.modifier.platformModifierClick`.
  Softcover's app-local fork triple `core/designsystem/.../modifier/PlatformModifierClick{,.jvm,.mobile}.kt` is
  deleted; the one call site (`LibraryShelf.kt`) is a pure import swap. `design-system.md` §Desktop selection updated.

### F11 — `ShareCardCapture` (capture a composable to an image, save/share)

- **Type:** enhancement (shared util / platform seam)
- **Home:** `nl.rhaydus:designsystem-core` (`share/` package)
- **Status:** **Implemented & adopted.** The capture/save/share seam ships in `nl.rhaydus.designsystem.share`
  (`ShareCardCapture` + `rememberShareCardCapture(config)`, `CapturableShareCard` with a generic `@Composable`
  slot, `ShareCardCaptureConfig`, `SaveOutcome` / `ShareOutcome` — now with a `Cancelled` case — and
  `GalleryWritePermissionRequester`, with android/jvm/ios actuals). Softcover **deleted its 11 app-local seam
  files** (the interface + `CapturableShareCard` + outcomes + requester + the android/jvm/ios actuals) and kept
  its **card design** app-side (`ShareCard` — now `public` — `ShareContent` and the `*ShareContent` subtypes,
  `ShareCardDimensions`). Adoption: a new app-side `softcoverShareCardCaptureConfig` (album `Softcover`, prefix
  `softcover`, authority `nl.rhaydus.softcover.shareprovider`) is passed to `rememberShareCardCapture`; the two
  call sites (`ShareBookBottomSheet.kt`, `ShareCardDebugScreen.kt`) re-point the seam imports, wrap the app's
  `ShareCard` in `CapturableShareCard`'s slot, and `ShareBookBottomSheet` adds a no-op `ShareOutcome.Cancelled`
  branch. The (from Kotlin) now-unused `com.google.android.material:material` — still needed for the Android
  `Theme.Material3.*` XML themes — was added to the root buildHealth false-positive exclusions. `design-system.md`
  §Share card updated.

### F3 — Make bottom bars reusable (`NavPulse` + `BottomBarScaffold`)

- **Type:** enhancement (shared component)
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Implemented & adopted (both halves).**

**The `BottomBarScaffold` half** (adopted 2026-07-10, reversing the 2026-07-06 skip — see F2). To host Softcover's
*two* bar styles the foundation host was generalized: `BottomBarScaffold` gained a
`placement: BottomBarPlacement` parameter (new `DOCKED` / `OVERLAY` enum, defaulted to `OVERLAY` so existing callers
are untouched). `OVERLAY` is the old body verbatim (measure the laid-out bar, provide `footprint + barSpacing`);
`DOCKED` hosts the bar in a Material `Scaffold` and provides `barSpacing` alone, since `innerPadding` already
reserves the bar. Both routes share the pure `bottomBarContentPadding(barFootprint, barSpacing)`. This makes the
`LocalBottomBarPadding` contract **uniform** — it is always "the trailing padding scrolling content reserves" — which
is what let Softcover delete its read-side fork (F2). Softcover's `CompactNavShell` is now one `BottomBarScaffold`
whose `placement` is mapped from the persisted `BottomBarStyle`, so flipping the preference re-places the bar without
relocating the `movableContentOf` tab body; `WideNavShell` hosts its flush `SessionPeekBar` in the same scaffold
(`OVERLAY`, `barSpacing = 0.dp`), retiring the last hand-rolled `onSizeChanged` measurement and a vestigial inner
`Scaffold`. The `bottomPadding: Dp` parameter threaded through the shell is gone. `design-system.md` §3.1 + §4
updated; the foundation `design-system-foundations.md` §5.2 rewritten around `placement`.

**The `NavPulse` half.** The cross-tab pulse now uses the foundation
  `NavPulse` (an instance-owned, keyed signal) + `rememberPulseScale`. Softcover deleted its global
  `BottomBarPulseManager` + `libraryPulseKey`; a single `NavPulse` Koin singleton lives in `designSystemModule`,
  keyed by a shared `data object LibraryNavPulseKey` (in `core:designsystem`, so `feature:reading`'s trigger and
  `:orchestration`'s bottom bar name the same signal without either reaching the other's `LibraryTab`).
  `MarkAsReadController` injects the pulse and calls `navPulse.pulse(LibraryNavPulseKey)`; `NavDestinations` reads
  `rememberPulseScale(navPulse, LibraryNavPulseKey)` and the hand-rolled pulse animation is gone. `design-system.md`
  §Mark-as-read choreography updated.

### F2 — `rememberBottomBarPadding()` does not work due to the way it's implemented

- **Type:** bug
- **Home:** `nl.rhaydus:designsystem-core` (layout)
- **Status:** **Implemented & adopted** (adopted 2026-07-10; the 2026-07-06 "deliberate skip" is reversed — the
  reasoning was that Softcover's shell already worked around the missing write side, which is an argument for
  keeping a fork, not a reason to have one).

  **Upstream root cause.** `LocalBottomBarPadding` (default `0.dp`) was read by `rememberBottomBarPadding()` but
  **never provided anywhere in the foundation** — no `CompositionLocalProvider(LocalBottomBarPadding provides …)`
  existed — so the helper always resolved to `0.dp` and scrolling content was occluded.
  `design-system-foundations.md` §5.2 delegated the write side to "the bottom-bar host screen," which the foundation
  never shipped. Fixed by shipping that host as `BottomBarScaffold` (F3) and simplifying
  `rememberBottomBarPadding()` to a direct `LocalBottomBarPadding.current` read (the old `remember(current){current}`
  wrapper was a no-op). The host is double-inset-safe: it measures the laid-out bar (`onSizeChanged` outside
  `windowInsetsPadding`) so the nav-bar inset is counted once, not recomputed and re-added.

  **Adoption also fixed a live Softcover bug.** The app-local fork
  (`core/designsystem/.../util/BottomBarPadding.kt`) branched the *read* side on `BottomBarStyle`
  (`FLOATING` → the local, `DOCKED` → a hardcoded `16.dp`). But that preference only governs the **compact** bar. On
  a medium/expanded window — rail or sidebar, no bottom bar at all — a `DOCKED`-preference user got `16.dp`,
  discarding the `sessionPeekHeight + navBarsInset` that `WideNavShell` had correctly measured and provided, so with
  a live reading session on desktop/tablet the last row of `LibraryShelf` (a `commonMain` surface) scrolled under
  the session peek bar. Deleting the fork and re-pointing the four read sites (reading / settings / explore
  `.mobile` layouts + `LibraryShelf`) at the foundation `rememberBottomBarPadding()` fixes it at a stroke: the write
  side alone decides, per shell, what the padding means.

  **Surface audit (F2's wider ask):** scanned the designsystem-core public surface for the same
  "published-but-unusable / near-zero-value" class — the padding pair was *the* finding; no demotions or removals
  (`BottomNavigationSpacer`, `pointerHandCursor`, `conditional`, and the one-line `model/*` enums all earn their
  place).

### F19 — Shared detekt config belongs in the foundation

- **Type:** enhancement (shared gate config)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Implemented & adopted.** The foundation ships a shared detekt baseline (`config/detekt.yml`, bundled
  in the `nl.rhaydus:detekt-rules` jar) carrying the foundation-worthy calibrations only — Compose/TOAD
  `ignoreAnnotated` across the complexity/naming rules, `MagicNumber` off, `LargeClass` /
  `LongParameterList(constructor=30)`, guard-clause `ReturnCount`, snake_case `PackageNaming`, `MaxLineLength: 140`.
  detekt's `formatting` ruleset stays off (ktlint owns layout). Softcover now layers it: a root
  `extractRhaydusDetektConfig` `Sync` task unpacks `config/detekt.yml` from the jar and
  `config.setFrom(<baseline>, config/detekt/detekt.yml)` puts the app's deltas on top. The app's config shrank from
  83 lines to the genuine Softcover-only calibration (Room/DAO `TooManyFunctions`, `LongMethod: 140`,
  `ThrowsCount`, the Apollo `TooGenericExceptionThrown` policy).

  **The baseline gained a type-resolution section during adoption.** On a type-resolved run detekt activates rules
  that are inert on a syntactic one, and detekt 1.23 embeds a **Kotlin 1.9** frontend while these apps are on
  Kotlin 2.x. Three rules produce only false positives and are now off, with cause, in the shared baseline:
  `UnreachableCode` (types an elvis whose RHS is `throw`/`return` as `Nothing`, so it flags the whole body of any
  guard-clause function — 133 findings, zero real), `IgnoredReturnValue` (does not see a call in a lambda's
  last-expression position as its return value — 22 findings, zero real), and `RedundantSuspendModifier` (cannot see
  that a callee from a dependency jar is itself `suspend` — verified against the compiler: two of three findings did
  not compile once the modifier was removed). `InjectDispatcher` is excluded on `**/di/**`, where naming the
  dispatchers is the module's whole job. **Revisit all four when detekt 2.x (K2 frontend) is adopted.**

  **A wiring subtlety worth keeping:** the extract task derives its file tree from `configuration.elements`, not from
  `.singleFile` inside a bare `provider { }`. The latter resolves eagerly, severing the task dependency that rebuilds
  the jar — the gate then silently runs against a stale config, which is worse than no gate.

### F1 — Crash-safety gate for terminal flow reads

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Implemented & adopted.** The type-resolved detekt rule `rhaydus:UnguardedFlowTerminalRead` now gates
  Softcover. ktlint cannot tell `Flow.first()` from `Collection.first()` without type resolution, so the blocking rule
  lives in detekt (resolving the receiver's fqName); the guarded `firstOrNull()` / `singleOrNull()` forms are
  different functions and never match.

  **Adoption required switching detekt from source-only to type-resolved**, which is the whole point: the rule is
  `@RequiresTypeResolution` and is **silently inert** on the plain `detekt` task, so wiring the ruleset without
  switching tasks would have gated nothing. `styleCheck` and `check` now run `detektAndroidMain` / `detektJvmMain` /
  `detektMain`; the source-only `detekt` task is disabled. Two traps, both load-bearing and both commented in the
  root build: detekt seeds each KMP task with only that target's **own** source set (so a `commonMain` module reports
  `NO-SOURCE` and analyses nothing), and the compilation's source set also carries **generated** code (KSP/Room,
  Apollo, Compose resources — ~2700 findings we neither own nor can fix). Both are fixed by an `afterEvaluate`
  `setSource(...)` naming the hand-written dirs, which detekt would otherwise overwrite from its own `afterEvaluate`.
  `iosMain` stays uncovered: detekt offers no type resolution for native targets, and no real `Flow` terminal reads
  live there.

  **The rule gained a hot-flow carve-out** (foundation change, + 5 unit tests). It was flagging
  `isOnline.first { it }` on a `StateFlow<Boolean>` — which is *the foundation's own*
  `BaseNetworkAvailabilityProvider.awaitOnline()`. A `SharedFlow` / `StateFlow` never completes and never fails, so
  neither hazard the rule guards against can occur; `first` on a hot flow is now exempt. `single()` stays flagged even
  there (on a flow that never completes it either suspends forever or throws on the second emission). Note the rule
  must stay on a conservative stdlib slice: it compiles against Kotlin 2.x but *runs* inside detekt's embedded 1.9
  runtime, so `sequenceOf(element)` linked fine and then died with `NoSuchMethodError` mid-analysis.

  **13 real findings fixed** — a `firstOrNull()` plus a *sensible default*, per the two hazards the rule names
  (emptiness → `NoSuchElementException`; upstream error → re-thrown by every terminal, `firstOrNull()` included).
  `GetUserIdUseCase`, `ReAuthenticateUseCaseImpl` (both now `?: NO_USER_ID`), `RefreshLibraryUseCaseImpl`
  (`?: true` — never re-seed, and so never clobber the user's enabled lists, on a preference we failed to read),
  `ApiKeyLocalDataSource`, `OfflineUserBookSyncImpl` (whose comment asserted the Room flow "always emits" — true for
  emptiness, but it can still throw), `ReadingSessionRepositoryImpl` ×3, `OnRefreshAction`, `InitKoin` (which also had
  a bare `runCatching` swallowing `CancellationException`), and the two desktop-startup `runBlocking { …first() }`
  reads. An enclosing `runCatchingLogged` does **not** exempt a site: it addresses the error hazard only, turning an
  empty flow into a logged failure and an error snackbar where the rule wants a default.

  Type resolution also surfaced 44 further real findings, invisible before because the old `setSource` never looked at
  `androidMain` / `jvmMain` / `mobileMain` and because several rules are themselves `@RequiresTypeResolution`: 29
  `?: ""` → `orEmpty()`, two `!!` in `LibrarySort` (rewritten to drop both the `!!` and two unchecked casts), a
  redundant `suspend`, a generic `catch (Throwable)` → `runCatchingCancellable`, a shadowed `it`, three long lines.

### F7 — Promote the `style-check.sh` recipes to blocking ktlint rules (generalize F1)

- **Type:** gate (lint rules) + tooling ownership
- **Home:** `nl.rhaydus:ktlint-rules` (+ `nl.rhaydus:detekt-rules` for the type-resolved one)
- **Status:** **Implemented & adopted (both sub-moves).** The five greppable recipes are blocking ktlint rules in
  `nl.rhaydus:ktlint-rules` — `one-type-per-file`, `no-fully-qualified-reference`, `project-import-order`,
  `inline-mockk-stub`, and `use-case-run-catching` (which names the upstream `runCatchingLogged`); the sixth
  (flow-terminal) is F1's detekt rule. Softcover consumes the 0.3.0 ruleset and is clean against all five (Batch A),
  and those recipes were retired from `scripts/style-check.sh`. **Sub-move 2** — own the residual harness rather than
  copying it per app — is tracked as **F22** and completed once F1's detekt rule landed: the script retires entirely
  rather than moving, since every recipe now has a blocking rule.

### F22 — Retire the app-local style script

- **Type:** tooling ownership
- **Home:** the app (deletion); the `rhaydus-kotlin` `style-check` skill (wording)
- **Status:** **Implemented & adopted.** F7's sub-move 2, and smaller than the entry assumed. Two discoveries:
  `scripts/kt-style-hook.sh` claimed in its own header to be wired as a PostToolUse hook but **was never registered**
  anywhere (`.claude/settings.json` carries only a PreToolUse Bash hook), so there was no on-touch hook to migrate;
  and `scripts/style-check.sh` could no longer fail — `error_hits` was initialised to `0` and never incremented, so
  the ERROR tier its header documented was dead code and the script always exited `0`.

  Audited against the script's full git history before deleting: it carried exactly **six** recipes ever (three at
  birth in `515c3427`, six at its fullest, one after `9a79d874`). All six have live, blocking replacements — five
  ktlint rules (`InlineFullyQualifiedReferenceRule`, `OneTypePerFileRule`, `ProjectImportOrderRule`,
  `InlineMockkStubRule`, `UseCaseRunCatchingRule`, all registered in `ktlint-rules/.../Main.kt` and gated by
  `ktlintCheck`) and the detekt rule above. Each replacement is *stronger* than the recipe it replaced: blocking
  rather than advisory, AST-accurate rather than grep-approximate. Both scripts are deleted; `styleCheck` keeps its
  name and becomes the type-resolved detekt aggregator; the foundation `style-check` skill no longer calls it
  advisory. (The script's header also advertised a boolean-`!` ERROR rule that never existed in any revision — that
  has always been ktlint's `BooleanNotationRule`.)


## Implemented, not adopted

(F19's entry moved up to *Implemented & adopted*.)

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

F8 landed on the foundation `release/0.3.0` branch as the **new `nl.rhaydus:offline-sync` module** — the
offline mutation queue batch (Batch H). Per the standing caution, the Softcover-specific shapes were **not**
lifted: only the generic skeleton was extracted, with the persistence and remote-replay as injected seams.
Softcover still ships its app-local engine and has not re-pointed anything.

### F8 — Offline mutation queue + drain-and-reconcile pattern

- **Type:** enhancement (shared infra) — generic-skeleton extraction
- **Home:** `nl.rhaydus:offline-sync` (new module, `commonMain`-only, depends on `core-platform` + `core-common`)
- **Status:** **Implemented, not adopted.** Landed as the new `nl.rhaydus:offline-sync` module (package
  `nl.rhaydus.offlinesync`), extracting only the generic engine from Softcover's two entangled syncers:
  `WriteQueue<P>` (enqueue facade) + `PendingWriteStore<P>` (the pluggable persistence seam — `enqueue` /
  `getPending(maxAttempts)` / `delete` / `incrementAttempts`, so **Room stays app-side**), `PendingWrite<P>`
  (localId + attempts + payload row), `ReplayOutcome` (SYNCED / DISCARDED), `DrainPolicy` (poison cap +
  in-drain exponential backoff), `OfflineWriteDrainer<I, K>` + `DefaultOfflineWriteDrainer<P, I, K>` (the
  drain loop: online-triggered + startup drain under a mutex; per ordered row → backoff replay → SYNCED:delete
  +hint / DISCARDED:delete / transient:incrementAttempts+halt / terminal:discard; `drain()` returns the
  `Map<I, Set<K>>` reconciliation hints). The engine takes the app's `replay` dispatch, `hintKey`,
  `isTransient` classifier, and `NetworkAvailabilityProvider` (F10) as injected inputs; it uses
  `AppDispatchers` + `runCatchingCancellable` / `runCatchingLogged` from `core-common`. The two Softcover
  asymmetries collapse into config: `isTransient` (user-book `{ it is RetryableSyncException }` vs list
  `{ true }`) and `DrainPolicy.inDrainRetries` (1 vs 3). **Drainer only** — the app owns the
  drain→fetch→reconcile composition (`OfflineUserBookSync.preserveOwnedFields` is inherently app-specific).
  Unit-tested (`DefaultOfflineWriteDrainerTest`, `commonTest`, fakes + coroutines-test). Softcover still ships
  its app-local `core/connectivity/.../data/sync/PendingUserBookWriteSyncer.kt` + `PendingListWriteSyncer.kt`,
  the `core/domain/connectivity/` queue+drainer contracts, the Room DAOs/entities, and `OfflineUserBookSync`.
  Adoption re-points the two syncers onto `DefaultOfflineWriteDrainer` (payloads/kinds/replay/reconcile stay),
  makes the Room DAOs back a `PendingWriteStore<P>` impl, and deletes the duplicated drain-loop/backoff code.

---

F18, F20, F21, and F23 landed on the foundation `release/0.3.0` branch — the shared build & gate tooling batch
(Batch I), in `build-logic` (the convention plugins + root) and `nl.rhaydus:ktlint-rules`. Each extracts a
reusable gate mechanism from Softcover's inline build config, with the concrete app data left configurable.
The fifth item, **F22**, is not a foundation-library change (plugin/skill ownership, done at adopt) and stays
open. Softcover still ships its inline gates and has not re-pointed anything.

### F18 — `checkModuleGraph` tier-DAG + api-visibility enforcement

- **Type:** gate (custom Gradle task)
- **Home:** `build-logic` convention plugins
- **Status:** **Implemented, not adopted.** Landed as the root-applied `rhaydus.module-graph` convention plugin
  (`build-logic` `ModuleGraphConventionPlugin` + `ModuleGraphExtension`): the `checkModuleGraph` task derives
  each module's tier from its path and fails the build on any `project(...)` edge breaking the tier DAG, plus
  the api-visibility allowlist (an `api` edge to a data-area module must be allowlisted). The mechanism is
  generic — the concrete `tierOf` mapping, `allowedTargetTiers`, `dataAreaModules`, and `allowedApiDataEdges`
  are supplied per build via the `moduleGraph { }` extension; the foundation configures its own graph
  (core→{core}, designsystem→{designsystem, core}, toad→{}, tooling excluded), and it wires into every module's
  `check` like Softcover's inline version. Verified to fail on an injected illegal edge. Softcover still has
  the equivalent inline in its root `build.gradle.kts`; adoption applies `rhaydus.module-graph` and moves the
  Softcover `dataAreaModules` / `allowedApiDataEdges` / tier mapping into the `moduleGraph { }` block.

### F20 — `dependencyAnalysis` (buildHealth) gating policy

- **Type:** gate (policy)
- **Home:** `build-logic` (root)
- **Status:** **Implemented, not adopted.** Landed as the root `dependencyAnalysis { }` policy (plugin
  `com.autonomousapps.dependency-analysis` 3.14.1, applied at root + every subproject): `onUnusedDependencies`
  and `onIncorrectConfiguration` gate to `fail`; `onUsedTransitiveDependencies` / `onRuntimeOnly` /
  `onRedundantPlugins` are `ignore`. The exclusion list mirrors the foundation's own convention bundle (the
  junit/kotest/turbine/mockk test stack, koin + coroutines, the Compose Multiplatform + androidx-compose +
  desktop-host artifacts, `androidx.core:core-ktx`) plus `eu.anifantakis:ksafe` kept `implementation` (no
  KSafe type leaks into SecureStorage's public surface). Triaged to a green `buildHealth`; verified to fail
  when an exclusion is removed. Softcover keeps its own inline `dependencyAnalysis { }` (its exclusions cover
  its app libraries — voyager-koin, work-runtime-ktx, camera, mlkit, coil); adoption keeps only the
  app-specific exclusions and inherits the shared policy shape.

### F21 — Shared `lint.xml` + `warningsAsErrors` policy

- **Type:** gate (policy + config)
- **Home:** `build-logic` convention plugins + the shared `lint.xml`
- **Status:** **Implemented, not adopted.** The duplicated `lint { warningsAsErrors; abortOnError; lintConfig }`
  block in the two library convention plugins was consolidated into one `build-logic` helper
  (`Lint.applyRhaydusLintPolicy(project)`), and the root `lint.xml` gained the version-freshness policy
  (`NewerVersionAvailable` / `GradleDependency` / `AndroidGradlePluginVersion` = `informational`) so a newer
  upstream release never breaks a build pinned to the foundation catalog. Softcover already had the freshness
  policy in its own `lint.xml`; adoption is a no-op on the app side beyond inheriting the shared helper.

### F23 — "No raw `println` / `Log.*` - use the logging facade" rule

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:ktlint-rules`
- **Status:** **Implemented & adopted.** The pure-AST ktlint rule `rhaydus:no-raw-logging` (flags unqualified
  `println(...)`, `System.out`/`System.err` `println`, and `Log.*` / `android.util.Log.*`, steering to the
  `AppLog` facade — F6) ships in the `nl.rhaydus:ktlint-rules` 0.3.0 ruleset Softcover now consumes (adopted in
  Batch A). Zero violations (pure ratchet), so no code changed; it now gates on `ktlintCheck`. The rule was
  review-only in Softcover's `code-style.md` (no `style-check.sh` recipe existed), so nothing further to retire.

## Batch A (adopted) — Style gates → blocking rules

*Home: `nl.rhaydus:ktlint-rules` + `nl.rhaydus:detekt-rules`. Covered **F1, F7**, and the residual **F22**. Six
greppable `style-check.sh` recipes became blocking rules: five ktlint rules (gated by `ktlintCheck`), and the
flow-terminal one — which needs type resolution — a detekt rule (gated by `styleCheck` / `check`). With F1 adopted,
`scripts/style-check.sh` retired entirely (F22). All three entries are now in *Implemented & adopted* above; this
section is kept only for the rationale.*

`scripts/style-check.sh` was an app-local bash port of the greppable code-style rules. Most were *foundation* style
rules, not Softcover's — the inline-mockk-stub rule ("open the `coEvery`/`every` block onto its own line") is
explicitly a foundation `code-style.md` rule. So they belonged in the foundation rulesets, where every consuming
project gets them with zero setup and a hard CI gate, rather than re-derived as advisory greps in each app's
`scripts/`. A bare `.first()` / `.single()` on a cold flow is the crash risk that motivated the detekt half: it
throws `NoSuchElementException` on an empty flow, and any terminal operator re-throws an upstream error (DataStore /
network / Apollo / repository). The rule is **crash-safety, not "always use a Collector"** — a guarded one-shot read
is acceptable; an unguarded throwing terminal is the defect.
