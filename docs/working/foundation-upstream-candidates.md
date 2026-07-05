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

## ✅ First adoption pass — LANDED & committed (2026-07-06)

The first foundation-adoption pass is **complete, green, and committed** across both repos. Softcover runs on the
**local 0.3.0** foundation (`foundation.local=true` in `local.properties` — gitignored, so it stays local — includeBuild
`../rhaydus-foundation` @ branch `release/0.3.0`). The full gate set — `ktlintCheck`, `styleCheck`, `buildHealth`,
`checkModuleGraph`, Android+JVM compile, and the affected host tests — is **green**.

- **Softcover** `hotfix/3.0.3` → `39df3489` — the 0.3.0 switch + F4/F5/F6 + Batch A ktlint cleanup.
- **Foundation** `../rhaydus-foundation` `release/0.3.0` → `40b23bf` — the mockk-stub autocorrect + two carve-outs (+ tests).

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
2. **Batch-index bookkeeping:** the F7 ktlint rules are now consumed and the app is clean against them, and the
   `inline-mockk-stub` autocorrect + the two carve-outs are new foundation enhancements. F1's detekt rule + F19 detekt
   config remain *not adopted* (the app hasn't wired the foundation `detekt-rules` yet). Reconcile F1/F7/F22 status on
   the next pass rather than piecemeal here.
3. Continue with the next foundation batch (e.g. F19 detekt config, F9/F10 `core-platform`, F2/F3 bottom bar).

---

## Batch index

| Batch | Theme | Home | Items |
|---|---|---|---|
| — | **Implemented & adopted** | `core-common` | F4, F5, F6 |
| — | **Implemented, not adopted** | `build-logic` / `core-platform` / `offline-sync` / `designsystem-core` / `toad` / `ktlint-rules` / `detekt-rules` | F1, F2, F3, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16, F17, F18, F19, F20, F21, F23 |
| I | Shared build & gate tooling (residual) | `style-check` skill | F22 |

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

## Implemented, not adopted

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

F16 and F17 landed together on the foundation `release/0.3.0` branch — the error-slot + inline error UX batch
(Batch F), one feature split across two modules: `InlineErrorState` (the component) in `nl.rhaydus:designsystem-core`
and the TOAD error-slot + retry convention (documented) in `nl.rhaydus:toad`'s `toad-architecture.md`. The
component's API and the state-slot contract were designed together so they match. Softcover still ships its
app-local fork + local convention and has not re-pointed its imports.

### F16 — `InlineErrorState` (inline load/submit-failure + retry surface)

- **Type:** enhancement (shared component)
- **Home:** `nl.rhaydus:designsystem-core` (shared component catalog)
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:designsystem-core`
  (`component/InlineErrorState.kt`) as the standard in-content failure surface: a centred [message] in the
  Material `error` role above a `RhaydusButton` retry that calls `onRetry`, with the same wrap-or-fill sizing
  contract (vertical centring only when the caller's `modifier` gives it a height). Brand-decoupled on the way
  in: the two app couplings — Softcover's hardcoded `"Retry"` label and its `MaterialTheme.editorialTypography.bodySmall`
  text style — became the `retryLabel: String = "Retry"` and `textStyle: TextStyle = MaterialTheme.typography.bodySmall`
  params (the error tint stays the pure-Material `error` role; the retry already used the foundation
  `RhaydusButton`). Softcover still ships its app-local
  `core/designsystem/.../presentation/component/InlineErrorState.kt`. Adoption re-points the two call sites
  (`ExploreScreenLayout.mobile.kt`, `OnboardingShelf.kt`) onto the foundation symbol, passing the app's
  editorial `bodySmall` as `textStyle`, and deletes the fork.

---

### F17 — A TOAD `UiState` error-slot + retry convention

- **Type:** enhancement (framework convention / shared contract)
- **Home:** `nl.rhaydus:toad` / `toad-architecture.md`
- **Status:** **Implemented, not adopted.** Blessed in `nl.rhaydus:toad`'s `toad-architecture.md` (a new
  `## Conventions` bullet): a screen that can fail surfaces the failure as a nullable `String?` slot on its own
  `data class : UiState` (defaulted null), the action folds it in `.onFailure` (copy authored in presentation,
  with a screen-specific fallback), clears it on retry and on any invalidating edit, and renders it with
  `InlineErrorState` (F16) whose retry re-dispatches the screen's **own** action. Cancellation is not re-handled
  in the fold (the use-case boundary already rethrows it). Kept deliberately as a **documented convention with no
  `toad` code** — `UiState` stays a bare marker; a screen wanting a richer shape than `String?` declares its own
  error type, so no speculative `UiError` type was added. Softcover still keeps the provisional note in
  `docs/reference/architecture.md` and its app-local `toUserMessage()` copy-authoring (app-specific, stays in the
  app). Adoption re-points that provisional note at the now-canonical `toad-architecture.md` convention.

---

F11 landed on the foundation `release/0.3.0` branch — the image-export batch (Batch G), a standalone
capture-to-image platform seam. Softcover still ships its app-local fork and has not re-pointed its imports.

### F11 — `ShareCardCapture` (capture a composable to an image, save/share)

- **Type:** enhancement (shared util / platform seam)
- **Home:** `nl.rhaydus:designsystem-core` (new `share/` package)
- **Status:** **Implemented, not adopted.** Landed in `nl.rhaydus:designsystem-core` under a new `share/`
  package (commonMain `ShareCardCapture` interface + `rememberShareCardCapture(config)` expect + `recordAndDraw`;
  `CapturableShareCard`; `ShareCardCaptureConfig`; `SaveOutcome` / `ShareOutcome`; `GalleryWritePermissionRequester`;
  android/jvm/ios actual impl classes). **Placed in `designsystem-core`, not `designsystem-image`** — the image
  module is pure Coil async-loading (single commonMain set, no platform code); a capture/save/share seam there
  would force Coil onto capture-only consumers and break its opt-in intent, whereas `designsystem-core` is
  Coil-free, every consumer already depends on it, and it hosts the 3-actual seam precedent (`ClipboardReader`).
  Generalized on the way in (it was **not** a clean lift): (1) **self-contained** — the Koin-injected
  `AppDispatchers` and the desktop `AppLog` were dropped for kotlinx `Dispatchers.IO` / `.Main` (on K/N `IO` is
  an extension property, reached via `import kotlinx.coroutines.IO`), and every save/share path returns a
  `SaveOutcome.Failure` / `ShareOutcome.Failure` on error rather than throwing, so failures surface only through
  those return types; (2) the brand bits (gallery album `"Softcover"`, filename prefix `"softcover-"`, the
  Android `FileProvider` authority) became a `ShareCardCaptureConfig`; (3) `CapturableShareCard` took a generic
  `@Composable` slot instead of the app-coupled `ShareContent` + `ShareCard`; (4) the desktop cache dir moved from
  `desktopAppDataDirectory()` to `java.io.tmpdir`. designsystem-core's androidMain gained `androidx.core` +
  `activity-compose` (catalog: `androidx-core-ktx` at 1.17.0). Review-hardened over the app original: the
  iOS share now honours the share-sheet `completed` flag via a new `ShareOutcome.Cancelled` case (a user
  dismissal is no longer reported as `Shared`) and dismisses a still-presented sheet on the main queue if the
  coroutine is cancelled mid-share. Softcover still ships its app-local
  `core/designsystem/.../presentation/share/*` (the seam + the app card design `ShareContent`/`ShareCard`/`ShareCardDimensions`).
  Adoption re-points the seam import at the two call sites (`ShareBookBottomSheet.kt`, `ShareCardDebugScreen.kt`),
  passes the app's `ShareCardCaptureConfig`, wraps the app's `ShareCard` in `CapturableShareCard`'s slot, adds a
  `ShareOutcome.Cancelled` branch to the `share()` `when` in `ShareBookBottomSheet.kt` (treat as a no-op), and
  deletes the forked seam (the card design stays app-side).

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
- **Status:** **Implemented, not adopted.** Landed as the pure-AST ktlint rule `rhaydus:no-raw-logging` in
  `nl.rhaydus:ktlint-rules` (not detekt — the foundation's `detektCheck` is syntactic, so a type-resolved
  `ForbiddenMethodCall` would be inert; a ktlint rule gates on the foundation's own `ktlintCheck` immediately).
  It flags unqualified `println(...)`, `System.out`/`System.err` `println`, and `Log.*` / `android.util.Log.*`
  calls, steering to the `AppLog` facade (F6). Zero existing violations (pure ratchet); the ktlint CLI's own
  `Main.kt` carries a documented `@file:Suppress` since its stdout is its report channel. Unit-tested
  (`NoRawLoggingRuleTest`, 11 cases). Softcover's rule is review-only in `code-style.md`; adoption drops the
  advisory wording once it consumes the new ktlint ruleset.

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

## Batch I — Shared build & gate tooling (residual: F22)

*Home: the `rhaydus-kotlin` `style-check` skill. The rest of the batch (**F18, F20, F21, F23**) landed in the
foundation (see *Implemented, not adopted*); **F22** is the one residual item, and it is **not** a
foundation-library change — it is plugin/skill ownership work, done at adopt.*

### F22 — On-touch style hook + script ownership in the `style-check` skill

- **Type:** tooling ownership
- **Home:** the `rhaydus-kotlin` `style-check` skill
- **Status:** Open - this is F7's sub-move 2; no foundation-library deliverable, done at adopt

The PostToolUse adapter (`scripts/kt-style-hook.sh`) that runs the style script on the just-edited file and
feeds findings back for on-touch fixing is reusable infra. It - and any residual greppable recipes not yet
promoted to rules - should live with the `style-check` skill so every app gets on-touch enforcement without
copying the adapter. Done at adopt, alongside retiring the now-promoted recipes from Softcover's
`scripts/style-check.sh`. (All six of those recipes are already promoted to foundation ktlint/detekt rules
per F1/F7, so at adopt `scripts/style-check.sh` retires entirely and only the harness/hook move to the skill.)
