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

## ✅ Adoption progress — LANDED & committed (updated 2026-07-06)

Softcover runs on the **local 0.3.0** foundation (`foundation.local=true` in `local.properties` — gitignored, so it
stays local — includeBuild `../rhaydus-foundation` @ branch `release/0.3.0`). Everything below is **committed and
green** (`ktlintCheck`, `styleCheck`, `buildHealth`, `checkModuleGraph`, Android+JVM compile, affected host tests).

**Adopted so far (13 F-items):** F4/F5/F6 (core-common), F13/F14, F16/F17, F12/F15, F11 (designsystem-core), F17
(toad convention), F7/F23 (ktlint gates), F3 NavPulse half (designsystem-core). Commits on Softcover `hotfix/3.0.3`:
`39df3489` (switch + F4/F5/F6 + Batch A) → `9b29f108` (F13/F14) → `8cb8c976` (F16/F17) → `5530c5e5` (F12/F15) →
`cbfecab4` (F11) → `9a79d874` (F7/F23) → F3 NavPulse (next), plus doc records. Foundation `release/0.3.0` `40b23bf` —
the mockk-stub autocorrect + two ktlint carve-outs (+ tests).

**Remaining (not adopted) — each needs a call/verification I can't do solo:**
- **F2 + F3's `BottomBarScaffold` half** (bottom bar) — **deliberate skip** (2026-07-06): the app's `BottomBarScreen`
  already provides `LocalBottomBarPadding`, so the foundation host is redundant; adopting it would be a risky
  refactor of working nav chrome for marginal benefit. F3's `NavPulse` half **is** adopted (above).
- **F9/F10 → F8** (core-platform SecureStorage/connectivity, then offline-sync) — iOS actuals the build hook won't
  compile here, and F10↔F8 are entangled; best when iOS can be built/tested.
- **F19/F1** (detekt) — requires switching the app's detekt from source-only to **type-resolved** (perf change) and
  may surface a violation tail. F1's `check_unguarded_flow_terminal` recipe stays in `style-check.sh` until then.
- **F18/F20/F21** (build-logic convention plugins) — likely need the foundation `build-logic` **published as Gradle
  plugins** to apply in the app build; awkward under `foundation.local`.
- **F22** — residual: move the `style-check.sh` harness + on-touch hook into the `style-check` skill (the script can't
  retire entirely until F1/detekt lands).

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
| — | **Implemented & adopted** | `core-common` / `designsystem-core` / `toad` / `ktlint-rules` | F3 (NavPulse half), F4, F5, F6, F7, F11, F12, F13, F14, F15, F16, F17, F23 |
| — | **Implemented, not adopted** | `build-logic` / `core-platform` / `offline-sync` / `designsystem-core` / `detekt-rules` | F1, F8, F9, F10, F18, F19, F20, F21 · **F2 = deliberate skip** (redundant; F3's `BottomBarScaffold` half likewise) |
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

### F3 — Make bottom bars reusable — `NavPulse` half adopted (`BottomBarScaffold` skipped)

- **Type:** enhancement (shared component)
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Implemented & adopted (the `NavPulse` half).** The cross-tab pulse now uses the foundation
  `NavPulse` (an instance-owned, keyed signal) + `rememberPulseScale`. Softcover deleted its global
  `BottomBarPulseManager` + `libraryPulseKey`; a single `NavPulse` Koin singleton lives in `designSystemModule`,
  keyed by a shared `data object LibraryNavPulseKey` (in `core:designsystem`, so `feature:reading`'s trigger and
  `:orchestration`'s bottom bar name the same signal without either reaching the other's `LibraryTab`).
  `MarkAsReadController` injects the pulse and calls `navPulse.pulse(LibraryNavPulseKey)`; `NavDestinations` reads
  `rememberPulseScale(navPulse, LibraryNavPulseKey)` and the hand-rolled pulse animation is gone. `design-system.md`
  §Mark-as-read choreography updated. **The `BottomBarScaffold` half of F3 is not adopted** — see F2: Softcover's
  shell already provides `LocalBottomBarPadding`, so the reusable host is redundant here.

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

### F2 — `rememberBottomBarPadding()` does not work due to the way it's implemented

- **Type:** bug
- **Home:** `nl.rhaydus:designsystem-core` (layout)
- **Status:** **Implemented; deliberately NOT adopted in Softcover** (decided 2026-07-06). Root cause confirmed: `LocalBottomBarPadding` (default `0.dp`) was
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
  earn their place). **Softcover does not need this fix:** its `BottomBarScreen` shell long ago worked around the
  missing write side by measuring the bar itself (`onSizeChanged`) and providing `LocalBottomBarPadding` across
  its docked / floating / wide layouts, so the app's bottom padding already works. Adopting `BottomBarScaffold`
  would be a **risky refactor of that working central nav chrome for only marginal (consistency) benefit**, so it
  is a deliberate skip. The app keeps its shell provision + the `rememberBottomBarPadding()` fork
  (`core/designsystem/.../util/BottomBarPadding.kt`, DOCKED→`16.dp` / FLOATING→local). Revisit only if the shell
  is rewritten for another reason. (The foundation `BottomBarScaffold` fix remains valuable for apps that lack a
  working provision.)

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

## Batch A (implemented, not adopted) — Style gates → blocking rules

*Home: `nl.rhaydus:ktlint-rules` + `nl.rhaydus:detekt-rules`. Covers **F1, F7** - both landed on the
foundation `release/0.3.0` branch (see their statuses below); kept here for the rationale. Five recipes
became blocking ktlint rules; the flow-terminal one (F1) needed type resolution and became a detekt rule.
**F7 (sub-move 1) is adopted** — Softcover consumes the 0.3.0 ktlint ruleset, is clean against all five, and
retired those recipes from `scripts/style-check.sh`. **F1 is not adopted** — the foundation `detekt-rules` is
not wired, so the one `check_unguarded_flow_terminal` recipe stays; retiring `style-check.sh` entirely (F22)
waits on it.*

### F1 — Crash-safety gate for terminal flow reads should be a blocking ktlint rule

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Implemented, not adopted.** Shipped as the type-resolved detekt rule `UnguardedFlowTerminalRead` in `nl.rhaydus:detekt-rules` (foundation `release/0.3.0`). ktlint cannot tell `Flow.first()` from `Collection.first()` without type resolution, so the blocking rule lives in detekt (resolving the receiver fqName); the guarded `firstOrNull()`/`singleOrNull()` forms and the collection operators are never flagged. **Not adopted** (Softcover has not wired the foundation `detekt-rules`), so it still ships the advisory `check_unguarded_flow_terminal` recipe — now the **sole remaining recipe** in `scripts/style-check.sh` after the five ktlint-promoted recipes were retired with F7; dropped on adopt.

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
- **Status:** **Implemented & adopted (sub-move 1); sub-move 2 partial.** The five recipes are blocking ktlint
  rules in `nl.rhaydus:ktlint-rules` — `one-type-per-file`, `no-fully-qualified-reference`, `project-import-order`,
  `inline-mockk-stub`, and `use-case-run-catching` (names the upstream `runCatchingLogged`); the sixth
  (flow-terminal) is F1's detekt rule. Softcover consumes the 0.3.0 ruleset and is clean against all five (Batch A),
  so the five now-redundant recipes were **retired from `scripts/style-check.sh`** and the `CLAUDE.md` Code Style
  section updated to point at the ktlint gate. **Sub-move 2** (own the residual `style-check.sh` harness + on-touch hook in the `style-check` skill) is
  tracked as **F22** and can't fully land until F1's detekt rule is adopted — the script still carries the one
  `check_unguarded_flow_terminal` recipe until then, so it can't retire entirely yet.

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
