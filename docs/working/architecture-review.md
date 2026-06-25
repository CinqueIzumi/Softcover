# Softcover — Architecture & Modularization Review

> **Status:** Advisory read-out, **Tier 1 (M1–M8) implemented** on `refactor/audit` (2026-06-24) —
> see the "Tier 1 implementation status" section directly below. The remaining tiers (1b, 2–7) are
> still open. Each item is tagged with severity and cost; the prioritized table near the end is the
> pick-up list for what's left.
>
> **Date:** 2026-06-17 (review) · 2026-06-24 (Tier 1 implementation) · **Branch reviewed:**
> `adopt/rhaydus-foundation` · **Implemented on:** `refactor/audit`
> **Method:** Full read-only sweep of the module graph + build files, data/domain layer,
> presentation/TOAD layer, build-logic/platform seams, and the test suite — cross-referenced against
> the Doveletter curated Android/Kotlin knowledge base. Headline build-graph findings verified directly
> against `build.gradle.kts` files.
>
> This document is appended to as further topic sweeps complete (see "Additional findings" at the end).

## Tier 1 implementation status (2026-06-24)

All of **TIER 1 — Modularization (M1–M8)** is implemented on `refactor/audit` (8 commits). Every phase
left the build green (`styleCheck` incl. JVM/iOS compile + `checkModuleGraph`, `buildHealth`, and the
relevant tests); each logic phase was run through `code-reviewer` and moved/affected tests through
`unit-test-writer`. **Tier 1b (B1–B4) is now also implemented on `refactor/audit` (see the Tier 1b
section below); Tiers 2–7 remain open.**

Keystone result: `:core:designsystem` went from `api`-exporting **7 data modules** to **`:core:book`
+ `:core:domain` only**, and a new gate (M4) prevents that regressing.

| # | Status | Commit | Notes / deviation from the review |
|---|--------|--------|----------------------------------|
| ✅ M5 | ✅ done | `5c360e9c` | Koin → `includes()` rooted at `orchestrationModule` (`softcoverModules = listOf(orchestrationModule)`). **Deviation:** the ordering was *not* actually load-bearing (lazy resolution, no eager init), so the real value added is a new Koin **`verify()` DI safety-net test** (`SoftcoverModulesVerificationTest`) — the repo had no DI verification. A follow-up commit `4b22ce78` fixed a multiplatform-build break the includes introduced in `:feature:session` (a `core:notification` androidMain-only dep was pulled into commonMain). |
| ✅ M2 | ✅ done | `3e851d70` | Deleted `:core:library`. **Deviation:** the review said move `RefreshLibraryUseCase` to `:orchestration`, but features consume it — that would break the tier rules. Instead the **contract** went to `:core:domain` and the **impl** to `:orchestration` (the existing `ResetUserDataUseCase` pattern). |
| ✅ M1 | ✅ done | `5b2b1133` (1a) · `a33f5a12` (1b) | **Deviation:** the review said move `ActiveSessionController` / `MainActivityViewModel` to `:orchestration`, but features inject them — a feature → orchestration edge is illegal. Used the repo's *contract-in-core / impl-in-orchestration* pattern: `ActiveSessionController` is now an interface in `:core:designsystem` with its impl in orchestration; `MainActivityViewModel` moved to orchestration behind a new one-method `SessionAuthenticator` core seam (only thing features needed). designsystem dropped `:core:profile/identity/personal/preferences` + `lifecycle-viewmodel`. |
| ✅ M3 | ✅ done | `064626ae` | Demoted `:core:connectivity`'s `:core:book/lists/database` (+ `coreUi`) to `implementation`. **Deviation:** not the "trivial demote" the review assumed — `buildHealth` proved the public syncers exposed those types. The demotion required lifting `start(scope)` onto the domain `*Drainer` interfaces and making the syncers `internal`. |
| ✅ M4 | ✅ done | `ee417ec4` | Extended `checkModuleGraph` with an api-visibility rule: an `api(project(":core:<data>"))` edge must be on an explicit allowlist (8 current edges allowlisted). Spot-tested to fire. Chosen over relying on `dependency-analysis` because that only checks ABI-correctness, not the architectural constraint. |
| ✅ M6 | ✅ done | `ee417ec4` | Documented the two `core` module kinds (domain-area data vs infra/contract) + the api-visibility rule in `module-structure.md` (the roster already listed them). |
| ✅ M7 | ✅ done | `ee417ec4` | Renamed `createListModule` → `listsScreenModule` (matches `profileScreenModule`); documented the DI-naming convention. `libraryServiceModule` disappeared with M2. |
| ✅ M8 | ✅ done | `ee417ec4` | Kept `:feature:session` (owns an Android foreground `Service` + manifest entry); documented why, per the review's low-confidence suggestion to keep it. |

Doc updates landed in lockstep across `docs/reference/{module-structure,architecture,design-system}.md`.
Not yet run: the full release acceptance build (rhaydus merge gate) — do before merge.

## Context

An extensive, critical pass over the current architecture, with the most weight on modularization. The
app is a KMP/CMP Hardcover client on the nl.rhaydus foundation, Clean Architecture + custom TOAD,
`:app → :orchestration → :feature:* → :core:*`.

The headline: **the layering is genuinely good** — acyclic feature graph (machine-enforced), no
feature→feature imports, domain interfaces split from impls, consistent TOAD, convention-plugin build
with near-zero per-module boilerplate. The problems are concentrated in the `:core` tier's **dependency
hygiene** (a few modules quietly re-export half the app), a couple of **risky test gaps** (Room
migrations, the Apollo cache resolver), and a handful of correctness/consistency cleanups. None of this
is a rewrite; it's surgical.

---

## TIER 1 — Modularization (highest priority)

### ✅ M1 [HIGH] `:core:designsystem` `api`-exports 7 lower modules → app-wide transitive leak
`core/designsystem/build.gradle.kts:24-30` declares as **`api`**:
`:core:domain`, `:core:book`, `:core:library`, `:core:profile`, `:core:identity`, `:core:personal`,
`:core:preferences`.

Every feature depends on `:core:designsystem` for UI primitives, so **every feature transitively
gains the data/use-case layer of books, profile, lists-orchestration, identity, reading-logs and
preferences** — whether it touches them or not. This is the single biggest structural smell: the
UI-primitives module is also the app's de-facto god-module. It defeats the point of splitting `core`
at all (compile-avoidance, clear reach, enforced boundaries), and it's *why* the graph "works" today
— everything can see everything through the back door.

Root cause: app-level state lives in the wrong module. `MainActivityViewModel` and
`ActiveSessionController` sit in `:core:designsystem` (`core/presentation/`) and pull book/profile/
identity use cases into a module that should only know Compose + design tokens + domain *models*.

What I'd change:
- Move `MainActivityViewModel`, `ActiveSessionController`, and any other cross-feature app state out
  of `:core:designsystem` into `:orchestration` (they are orchestration concerns; ../reference/architecture.md
  already says TOAD is per-screen and `MainActivityViewModel` is the one app-level exception — it
  just lives in the wrong tier).
- After the move, `:core:designsystem` should depend on **`:core:domain` only** (for the model types
  its components render), and via `implementation`, not `api`, wherever a type isn't in a public
  component signature.
- Audit each remaining `api(...)`: `api` is correct only for types that appear in this module's
  *public* API surface. Everything else → `implementation`. The Doveletter "Real Modularization"
  reference (SOLID + dependency inversion) is exactly this point: modules should expose the minimum.

Cost: medium (move 2-3 classes + their Koin bindings; then prune designsystem's deps and fix the
fallout in features that were leaning on the transitive deps). High payoff — this is the keystone.

### ✅ M2 [HIGH] `:core:library` is not a module — it's one cross-feature use case wearing a module costume
`core/library/src` has **two production files**: `RefreshLibraryUseCase.kt` + `LibraryModule.kt`. Yet
its build file `api`-depends on `:core:book`, `:core:lists`, `:core:preferences`, `:core:identity`
(`core/library/build.gradle.kts`). It is pure cross-feature orchestration (refresh books + lists +
settings together) — which by this app's own rules belongs in **`:orchestration`** (where
`InitializeUserIdAndBooksUseCaseImpl` and `ResetUserDataUseCaseImpl` already live).

Two extra harms:
- **Name collision** with `:feature:library` — `core:library` vs `feature:library` is a constant
  "which one is this?" tax, and its Koin module is even named `libraryServiceModule` (the build
  module and the DI module disagree on the name).
- `:core:designsystem` `api`-exposes `:core:library` (M1), so this aggregator leaks app-wide too.

What I'd change: move `RefreshLibraryUseCase` into `:orchestration/usecase/`, delete the
`:core:library` module, fold `libraryServiceModule` into `orchestrationModule`. If you'd rather keep
it a module, at minimum rename it (`:core:library-sync`) so it stops colliding with the feature.

Cost: low-medium. Mostly a move + settings.gradle delete + Koin re-point.

### ✅ M3 [MEDIUM] `:core:connectivity` `api`-re-exports `:core:book` and `:core:lists`
`core/connectivity/build.gradle.kts` `api`-depends on `:core:book` + `:core:lists`. A sync/offline
*facade* should expose its own façade types (sync state, pending-write counts), not republish two
feature-data modules to whoever depends on it. Same fix as M1: demote to `implementation` and expose
only the connectivity surface; if a consumer needs `Book`, it should depend on `:core:book` itself.
Cost: low.

### ✅ M4 [MEDIUM] Layering IS enforced — but the gate checks edges, not `api`-visibility granularity
A `checkModuleGraph` task already exists in the root `build.gradle.kts` (~lines 244-282) and runs at
`check` time. It enforces the tier edges: `feature:* -> core:*` only (no feature→feature),
`core:* -> core:*`, `orchestration -> feature+core`, `app/desktopApp -> orchestration+core`. That's
genuinely good and explains why the macro-layering is clean. The root build also gates iOS/JVM
compilation + detekt + Kover + the ktlint ruleset.

The gap: that task validates *that an edge is allowed*, not *whether it should be `api` vs
`implementation`*. M1/M3 (designsystem and connectivity re-exporting data modules) are **legal edges**
— so the existing gate waves them through. What I'd add on top:
- An `api`-visibility rule: a non-data module may not `api(project(":core:<data>"))`. This is the
  invariant that actually catches M1/M3 and stops them recurring. The dependency-analysis plugin
  (already configured in the root build) can flag `api` deps that aren't part of the ABI — lean on it,
  or extend `checkModuleGraph` with a per-module allowed-`api` allowlist.
Doveletter's modularization threads land on the same point: edges you allow but don't *constrain the
visibility of* are how god-modules form. Cost: low (extend an existing task).

### ✅ M5 [MEDIUM] Koin aggregation relies on implicit list ordering
`orchestration/di/SoftcoverModules.kt` builds `softcoverModules` as a flat ordered list with load-
bearing-but-undocumented ordering (`designSystemModule` first; `libraryServiceModule` after book+
lists). Koin doesn't enforce list order, so a reorder fails at runtime, not compile time. I'd switch
to Koin **`includes(...)`**: each feature/aggregate module declares its own dependencies, and
`orchestrationModule` includes the graph. Ordering becomes a real dependency edge Koin resolves,
not a comment. Cost: low.

### ✅ M6 [LOW] The `core` tier mixes two different module *kinds* with one naming scheme
Two distinct things both live under `:core:` with `*Module` Koin names:
- **Domain-area data modules** (`:core:book`, `:core:lists`, `:core:deadlines`, `:core:personal`,
  `:core:profile`) — domain interfaces + use cases + data impls for one area. Legitimate.
- **Pure infra** (`:core:network`, `:core:database`, `:core:preferences`, `:core:connectivity`,
  `:core:notification`, `:core:designsystem`).

This is fine, but worth making explicit in ../reference/module-structure.md (e.g. a `core:data:*` vs
`core:*` infra split, or just a documented convention), so the next module lands in the right bucket.
Related: `:core:domain` is a single shared-kernel holding models for *every* area — it grows
monotonically and couples all areas to one module. Worth a note on when a model is "shared kernel"
vs "belongs to its area module." Cost: doc-only now; structural later if you split it.

### ✅ M7 [LOW] Feature DI-module naming is inconsistent
`bookDetailModule` / `libraryModule` / `readingModule` but `profileScreenModule` / `createListModule`
/ `libraryServiceModule`. Pick one convention (recommend `*ScreenModule` for feature/presentation DI,
`*Module` for data) and apply it across the board. Cost: trivial (rename + re-point).

### ✅ M8 [LOW] `:feature:session` (5 files) barely earns module status
It's essentially a focus-mode overlay over existing data with notification wiring — no real data
layer. Either fold it into `:orchestration` (it's presentation glue) or accept it but document why
it's a module. Cost: low. (Low confidence — keep if there's a roadmap reason it'll grow.)

---

## TIER 1b — Build setup & platform seams (mostly clean; small cleanups)

> **Status (2026-06-25): B1–B4 implemented on `refactor/audit`.** B1 was narrower than the review
> claimed — `coreKtx` (used in `:core:connectivity`) and `material-components` (used in
> `:core:designsystem`) are *referenced*, so only the three genuinely-dead aliases were pruned
> (`androidx-junit`, `androidx-espresso-core`, `androidx-compose-material3`, plus their version refs
> `junitVersion`/`espressoCore`/`material3ComposeVersion`). B2 is a documented "flip back when KSP
> supports AGP 9 built-in Kotlin" tracker (the rationale already lives in `gradle.properties`) — no
> action beyond confirming it's recorded. B3: the documented `foundation.local` inner-loop switch was
> genuinely **absent** from `settings.gradle.kts`; it's now implemented (reads `foundation.local` from
> `local.properties`, `includeBuild("../rhaydus-foundation")` when true) to match the CAPABILITIES.md
> contract. B4: the `BarcodeScanner` expect/actual (4 files) + its CameraX/ML Kit deps moved from
> `:core:designsystem` to `:feature:scan`; the `SoftcoverIcon.BarcodeScanner` icon token, the
> `ic_barcode_scanner` drawable, and `ScreenDestination.BarcodeScanner` stay (icon catalog / nav
> contract, not hardware). `design-system.md` updated in lockstep (scanner reframed as feature-owned).

The Gradle setup is a genuine strength. Convention plugins (`build-logic/`: `softcover.kmp.library`,
`.kmp.compose`, `.android.room`, `.android.apollo`) absorb all SDK/JDK/test/target boilerplate — the
38 module build files are 3-8 lines each, no duplication. The `mobileMain` (Android+iOS) vs `jvmMain`
(desktop-alone) source-set hierarchy is well-designed; expect/actual seams are tight and consistent
(`expect val platformXModule: Module` per feature, thin iOS/desktop actuals). Room KSP runs per-target
correctly; only `:orchestration` emits the iOS framework. Nothing structural to fix here. The nits:

- **B1 [LOW] Dead version-catalog aliases.** `gradle/libs.versions.toml` carries unused entries:
  `coreKtx`, `androidx-junit`, `androidx-espresso-core` (project uses JUnit5/Kotest/MockK, not
  androidTest), and a `material3ComposeVersion`/`material-components` pin that isn't referenced. Prune
  them so the catalog reflects reality. Cost: trivial.
- **B2 [LOW] AGP-9 / KSP compatibility debt.** `gradle.properties` sets `android.builtInKotlin=false`
  (+ related flags) to work around KSP not yet supporting AGP 9's built-in Kotlin. Track it; flip back
  and drop the explicit Kotlin-android plugin once KSP catches up. Cost: trivial (later).
- **B3 [LOW] Confirm the `foundation.local` includeBuild switch exists.** CAPABILITIES.md / CLAUDE.md
  describe a `foundation.local=true` → `includeBuild("../rhaydus-foundation")` inner-loop switch, but
  the build sweep didn't find it in `settings.gradle.kts`. Either it's there and I missed it, or the
  documented inner-loop path is actually absent — worth a 2-minute check. Cost: trivial.
- **B4 [LOW] Barcode scanner (CameraX/MLKit) lives in `:core:designsystem`'s `androidMain`.** Camera
  capture is not a design-system concern; it's another symptom of designsystem-as-catch-all (see M1).
  Move it to `:feature:scan` (or a `:core:camera`) so the UI-primitives module stops owning hardware
  integration. Cost: low.

---

## TIER 2 — Data & Domain layer

### D1 [MEDIUM] Network errors are thrown, then re-wrapped in `Result` — two error models in one path
`safeQuery()`/`safeMutation()` *throw* (`OfflineException`, wrapped `RuntimeException`) **and** notify
`UserMessageNotifier`. Use cases then wrap calls in `runCatching` → `Result<T>`. So the same call
crosses two error idioms (throw at the network seam, `Result` above it) and side-effects a user
message from deep in the data layer. The Doveletter **Sandwich** deep-dive makes the case cleanly:
collapsing failures to thrown exceptions / fallback values loses the *kind* of failure (offline vs
401 vs server-500 vs empty) before the presentation layer can decide what to show — which is exactly
why error-state handling is inconsistent across screens (see P2).

What I'd change (pick the app's single error model and commit):
- Make the network seam return a typed result (`ApiResult`/sealed) instead of throwing, OR keep
  throwing but stop also wrapping in `Result` upstream — don't do both.
- Move `UserMessageNotifier.notify()` **out** of the network primitive. A data-layer helper deciding
  user-facing copy is a layer violation; surface the typed failure and let presentation choose the
  message. Doveletter's multi-module-error-handling thread (a shared error interface across modules)
  is the pattern to copy here.
Cost: medium-high (touches every use case). High payoff for consistency + testability. This is the
one I'd most want sign-off on before touching, because it ripples.

### D2 [MEDIUM] `BooksRepositoryImpl` (~250 lines) bundles cache + network + offline-queue + dedup + orphan-cleanup
It works and is tested, but it's five responsibilities in one class: fetch strategy, local store,
`UserBookWriteQueue` enqueue, `UserBookWriteDrainer` replay, inflight-mutex dedup, orphan removal.
I'd extract the offline-sync concern (queue + drainer + connectivity) into an
`OfflineUserBookSync`/decorator so the repository reads as "fetch + merge" and the sync machinery is
independently testable. Cost: medium. (Clean-architecture-correct per the project's own principle of
never preferring the smaller-diff pass-through.)

### D3 [LOW] Offline write replay is opportunistic, not guaranteed
Pending writes drain only on the next library refresh (`UserBookWriteDrainer`). If the user edits
offline and never reopens the library before the process dies, the write waits indefinitely. The
Doveletter **WorkManager internals** piece is the relevant lens: durable, constraint-gated
(`NetworkType.CONNECTED`) background replay is the standard answer on Android. KMP wrinkle: you'd need
a per-platform "schedule sync" seam (WorkManager on Android, BGTaskScheduler/equivalent on iOS, no-op/
on-launch on desktop). I'd at minimum add an app-resume drain trigger now and consider the durable
scheduler later. Cost: low (resume trigger) / medium (full per-platform scheduler).

### D4 [LOW] `PendingUserBookWriteEntity.kind` is a raw `String` round-tripped through `enum.valueOf`
A persisted discriminator parsed by `valueOf(entity.kind)` is a latent crash if a value is ever
renamed/removed. Use a Room `TypeConverter` for `PendingUserBookWriteKind` so the mapping is in one
place and compile-checked. Cost: trivial. (Tests via unit-test-writer.)

### D5 [LOW] No cache TTL / invalidation story for Room-cached entities
Room entities are cached indefinitely; freshness relies on aggressive refresh-on-entry. Acceptable
today, but worth an explicit decision (per-entity `fetchedAt` + staleness check, or documented
"refresh-on-entry is the contract"). The Doveletter caching-architecture thread (Room+DataStore+
network for instant nav) is the reference. Cost: low (doc) / medium (TTL).

---

## TIER 3 — Presentation / TOAD

### P1 [MEDIUM] TOAD boilerplate is heavy and one-action-per-file amplifies it
`:feature:book_detail` is 91 files, **49 of them single `UiAction` subclasses**. The pattern is
consistent and correct, but the file-count tax is real and discourages small features / encourages
copy-paste drift. Options (foundation-level, so flag upstream to rhaydus rather than fork locally):
group related actions into one sealed file per concern; or a light codegen/template for the
UiState/Action/Event/ScreenModel/Dependencies quintet. Cost: low locally (grouping) / foundation
change (codegen). Mostly a "name the cost" item — don't fork the foundation convention unilaterally.

### P2 [MEDIUM] Error/loading state handling is inconsistent across screens
`book_detail`/`reading`/`library` track per-item mutation failures in state (good); `explore`/
`onboarding` `AppLog.e()` and swallow — the user sees a spinner resolve to nothing on a failed
search. I'd define **one error-state contract** on the TOAD `UiState` baseline (e.g. a standard
`transientError`/`inlineError` slot + a convention for retry) so every screen handles failure the
same way. This is the presentation-side symptom of D1 — fixing D1 makes this natural. Cost: low-medium.

### P3 [LOW] A few justified-but-fragile UI state syncs
`library` shelf keeps a `mutableStateList` synced via `LaunchedEffect` (commented: avoids a blank
frame); `explore` holds file-level scroll/list state to survive movableContent moves on resize. Both
are documented and defensible, but they're the kind of thing that breaks silently on refactor. Worth a
shared helper (e.g. a `rememberSyncedList`) so the pattern is in one tested place rather than re-derived
per screen. Cost: low.

---

## TIER 4 — Testing strategy

Baseline is strong: ~243 test files, strict **MockK-only** discipline (no mock servers — matches the
project standard), `runTest` + JUnit5 + Kotest throughout, and — notably — the TOAD presentation logic
**is** tested (76 Action tests, 19 Collector tests, 74 use-case tests, 10 mapper tests). The drift is
in *what's left uncovered*, and a couple of these are genuinely risky:

### ✅ T1 [HIGH] Room migrations have ZERO tests, at schema version 40 (37 migrations)
**Done (2026-06-25, `refactor/audit`).** `:core:database` is at v42 with 39 hand-written `execSQL`
migrations and only mapper tests — none exercised a migration path. A bad migration is data loss on real
users' devices, and `fallbackToDestructiveMigration(dropAllTables = true)` is configured, so a silent
migration failure **wipes the local DB** rather than crashing visibly.

**Deviation:** Room `MigrationTestHelper` is **not usable** for the historical migrations — it needs the
exported schema JSON for each *start* version, and this project never exported schemas (and they can't be
regenerated retroactively). Instead, since the migrations use the new KMP `Migration.migrate(connection:
SQLiteConnection)` API over `BundledSQLiteDriver`, they are **host-runnable on the JVM directly**: a test
opens an in-memory SQLite connection, hand-builds the *old* schema, inserts representative rows, calls the
migration object, then asserts data survival + post-migration schema shape. Covered the three structural
migrations the review named — v5→6 denormalization split, v31→32 pending-writes rename, v35→36 review-format
rebuild (33 tests in `core/database/src/androidHostTest/.../migration/`). To reach the migration objects the
companion now exposes a single `internal val ALL_MIGRATIONS` (the migration set's source of truth, consumed
by `build()`); the host-test runner gets `androidx.sqlite:sqlite-bundled-jvm` as a `runtimeOnly` test dep
(production still uses the KMP `sqlite-bundled`). Schema export for *future* MigrationTestHelper coverage is
a worthwhile separate follow-up, not required for these tests.

### ✅ T2 [HIGH] The custom Apollo cache resolver is untested
**Done (2026-06-25, `refactor/audit`).** `SoftcoverCacheResolver` (the primary-key redirect that makes
detail-screen cache hits work) and the `safeQueryFlow` dual-emission handling had effectively no tests
(`:core:network` had 1 test, the auth interceptor). This is bespoke, subtle, offline-affecting logic. Added
20 MockK tests (`core/network/src/androidHostTest/.../cache/` + `.../helper/`): resolver redirect hit
(`_eq`/`_in`, null filtering, Int→String key) vs delegate (foreign-key filter, absent `where`,
non-redirectable field, non-root parent); and `safeQueryFlow` cache+network dual emission, CacheMiss
ignored, 401-after-cache-hit (notify, no throw), 401/403 with no data (throw), offline/server/transient
(503/429) transport mapping, and non-retryable/empty-flow generic-error paths.

### T3 [MEDIUM] Offline write queue/drainer and `core:personal` mutations under-tested
The `UserBookWriteQueue`/`UserBookWriteDrainer` replay path (the heart of offline-first, see D2/D3) and
`:core:personal` session/highlight use cases (start/pause/resume/delete, add/update/delete highlight)
are critical user-data mutations with thin coverage. These are exactly the paths where a silent bug
loses a user's reading progress. Cost: medium. (Delegate to `unit-test-writer`, MockK only.)

### T4 [MEDIUM] Flows asserted by manual `StateFlow.value` reads, not Turbine
The stack declares Turbine but tests read `state.value` directly and assert — brittle to timing/emission
ordering and unable to assert emission *sequences*. Standardizing collector/flow tests on Turbine would
make the (already good) TOAD tests robust. Cost: low-medium, incremental.

### T5 [LOW] No Compose UI / screenshot / instrumented tests anywhere
Only `androidHostTest` unit tests exist — no `runComposeUiTest`, no Paparazzi/Roborazzi, no Room
integration tests on a real in-memory DB. Given a design-system-heavy multiplatform UI, a small
screenshot-test or `runComposeUiTest` suite on 2-3 critical flows (add-a-book, record-progress) would
catch render/transition regressions CI currently can't see. Also confirm Kover coverage is actually
*gated*, not just *configured*. Cost: medium (new test type + CI wiring).

### T6 [LOW] Per-class duplicated `stub*` fixtures
Each test class re-declares its own `stubBook()`/`stubEdition()`/etc. Works, but a mapper/model change
ripples across dozens of files. Extract shared test-data builders into a `commonTest` fixtures module.
Cost: low-medium.

---

## Cross-cutting / smaller nits
- **Platform TODOs** (not architectural, just gaps): iOS barcode scanner stub
  (`// TODO(iOS): real AVCaptureSession + Vision`), iOS gallery save stub, density-aware share-capture
  limitation, onboarding snackbar-hidden-behind-modal bug. Track these as issues.
- ~~**`:core:designsystem` doubles as the barcode-scanner home** (CameraX/MLKit in its `androidMain`).~~
  **Resolved (B4, 2026-06-25):** the `BarcodeScanner` expect/actual + its CameraX/ML Kit deps moved to
  `:feature:scan`; designsystem no longer owns hardware integration.
- **Document the wide-but-correct reach**: `:feature:book_detail` depends on 9 core modules. That's
  legitimate for its size; a one-line note in ../reference/architecture.md heads off "is this a smell?" reviews.
- **`SoftcoverWorker` catches `Throwable` then re-throws `CancellationException` inside the block**;
  functionally correct but clearer as ordered catches (`catch (c: CancellationException) { throw c }`
  then `catch (t: Throwable)`). Trivial.
- **`ExpandableFlowRow`** in `:core:designsystem` may duplicate a foundation flow-row primitive — check
  CAPABILITIES.md before keeping the hand-rolled version (reuse-first rule). Trivial.
- **One `runBlocking` in composition**: desktop `WindowStatePersistence.kt` seeds window state via
  `runBlocking { getDesktopWindowState().first() }`. It's a one-time startup read, not mid-recompose,
  so acceptable — but worth a comment, or hoist the read above composition. Low.
- **Verified clean (no action needed), so you know it was checked**: Compose stability across the app
  (LazyList `key {}` present everywhere, one correct `derivedStateOf`, no `GlobalScope`, modifiers
  stable/remembered, `LaunchedEffect` keys correct), 100% `AppLog` (no `println`/`Log.*`),
  `:feature:settings` / `:feature:app_update` / `:core:notification` platform isolation, and the
  `:core:designsystem` component catalog (no foundation-duplicating components found).

---

## Priority summary (what to do first)

| # | Change | Severity | Cost | Why first |
|---|--------|----------|------|-----------|
| ✅ M1 | Move app state out of `:core:designsystem`; prune its `api` deps to `:core:domain` | HIGH | Med | Removes the app-wide transitive leak — the keystone |
| ✅ M2 | Delete `:core:library`; move `RefreshLibraryUseCase` to `:orchestration` | HIGH | Low-Med | Kills a fake module + name collision |
| ✅ T1 | Room migration tests (v42, 39 migrations; destructive fallback = silent data loss) | HIGH | Med | Highest data-loss risk in the repo |
| ✅ T2 | Test `SoftcoverCacheResolver` + `safeQueryFlow` dual-emission | HIGH | Low-Med | Bespoke offline-affecting logic, untested |
| ✅ M4 | Extend the existing `checkModuleGraph` with an `api`-visibility rule | MED | Low | The existing gate allows M1/M3; this catches them |
| ✅ M3 | Demote `:core:connectivity` `api`→`implementation` | MED | Low | Same leak class as M1 |
| ✅ C1 | Cancel `ReadingSessionService.serviceScope` in `onDestroy()` | MED-HIGH | Trivial | Already present (commit `5381f8d5`, predates review) — closed, no change |
| ✅ DC1 | Re-sync Room from server after offline write replay; scope `preserveSyncedProgress` | MED | Med | Apollo↔Room divergence on offline edits |
| ✅ M5 | Koin `includes(...)` instead of ordered list | MED | Low | Removes runtime-fragile ordering |
| D1 | Single error model at the network seam; move `UserMessageNotifier` out | MED | Med-High | Root of P2; needs buy-in (ripples) |
| D2 | Extract offline-sync out of `BooksRepositoryImpl` (pairs with DC1) | MED | Med | Testability + SRP |
| T3 | Test offline write queue/drainer + `core:personal` mutations | MED | Med | User-data-loss paths, thin coverage |
| P2 | One error-state contract on TOAD `UiState` | MED | Low-Med | Consistent failure UX |
| R1 | Enable R8 + keep rules (Koin/Apollo/Room/serialization/TOAD) + release smoke test | MED | Med | Unobfuscated, leaky release builds today |
| S1 | Verify `allowBackup` rules exclude `api_key.enc` + DBs | MED | Trivial | Possible secret exfiltration via backup |
| T4 | Standardize flow tests on Turbine | MED | Low-Med | Hardens the (good) TOAD tests |
| ✅ M6/M7/M8 | Doc the two core-module kinds; unify DI naming; reassess `:feature:session` | LOW | Low | Hygiene |
| D3/D4/D5 | Resume-drain trigger; `kind` TypeConverter; cache-TTL decision | LOW | Low | Correctness hardening |
| C2/DC2 | Wire `ApiKeyLocalDataSource` scope to `ApplicationScope`; close refresh-vs-mutation race | LOW | Low | Leak/race hardening |
| ✅ B1-B4 | Prune dead catalog aliases; AGP/KSP debt; confirm `foundation.local`; move scanner out of designsystem | LOW | Low | Build hygiene |
| N1/N2 | Type-safe deep links; back-stack restore across process death | LOW | Med | Nav reach/polish |
| A1 | Richer screen-reader semantics (headings/cards); debug-gated testTags if UI tests arrive | LOW | Low | Accessibility |
| DB1/I1 | Index `ReadingJournalEntity.userBookId`; externalize desktop strings + RTL spot-check | LOW | Low | DB perf / i18n |
| S2/B2 | (Decide on) cert pinning; baseline/startup profile for release | LOW | Low-Med | Release-readiness |
| T5/T6 | Compose UI/screenshot tests + gate Kover; shared test fixtures | LOW | Med | Coverage breadth |
| P1/P3 | Flag TOAD boilerplate upstream; shared synced-list helper | LOW | Low | Ergonomics |

## Verification (when/if any item is greenlit)
- After any module-dependency change: `./gradlew styleCheck` (module-graph + dependency-analysis
  gates) then a full build incl. the iOS-release caveat (per the rhaydus-adoption merge gate).
- Logic changes (D1/D2/D4) → `code-reviewer` agent + `unit-test-writer` for the touched repos/use
  cases, narrow `--tests` filters.
- M-series are mechanical refactors: rely on the build + module-graph assertion (M4) as the gate.

---

## TIER 5 — Concurrency & data-consistency (deep dive)

Overall the concurrency story is good: a single DI-owned `ApplicationScope`
(`CoroutineScope(Dispatchers.Default + SupervisorJob())` in `DispatcherModule`), disciplined
`AppDispatchers` usage (no hardcoded `Dispatchers.*` leaking into repos/data sources), and **correct**
`Mutex`-based inflight dedup (`BooksRepositoryImpl`, `ListsRepositoryImpl`, the syncers,
`BookDetailPrefetcher`). No race-prone lazy init, no `synchronized`/`AtomicReference` misuse, flows use
`distinctUntilChanged`/`collectLatest` correctly. Two real leaks and a data-consistency gap stand out:

### ✅ C1 [MEDIUM-HIGH] `ReadingSessionService` creates a long-lived scope that is never cancelled
**Already fixed; no change needed (verified 2026-06-25, `refactor/audit`).** The review flagged this from
excerpts (and its own confidence caveat said to verify C1 in-file). `ReadingSessionService` already
overrides `onDestroy()` → `serviceScope.cancel()` (then `super.onDestroy()`) — the fix the review
prescribed. It landed in commit `5381f8d5` (2026-06-10, the KMP conversion of `:feature:session`),
which **predates** the 2026-06-17 review. So the leak described here never existed on the reviewed
branch; the item is closed with the doc note rather than a code change.

### C2 [LOW-MEDIUM] `ApiKeyLocalDataSourceImpl` launches an untracked scope in `init`
`core/preferences/.../ApiKeyLocalDataSource.kt` (~line 42): `init { CoroutineScope(io + SupervisorJob()).launch { initialize() } }`
— the scope is never stored or cancelled. Low practical impact (it's a singleton), but architecturally
unsound; wire it through `ApplicationScope` like the syncers do. Cost: trivial.
(`ConnectivityDataSourceImpl` jvm also self-owns a polling scope — safe as an app-lifetime singleton, but
wiring it to `ApplicationScope` would be consistent.)

### ✅ DC1 [MEDIUM] Offline write replay never re-syncs Room from the server response → Room can diverge
**Done (2026-06-25, `refactor/audit`).** The headline divergence — `preserveSyncedProgress()` re-applying
the *whole* local `userBook`+`userBookRead` over freshly-fetched server data for synced IDs, clobbering any
field the server changed underneath that no pending write touched — is fixed by **scoping the preservation
to the fields each replayed write actually owns**.

**What changed:** `UserBookWriteDrainer.drainPendingUpdates()` now returns
`Map<Int, Set<PendingUserBookWriteKind>>` (per affected `userBook` id, the kinds that synced) instead of a
bare `Set<Int>`. `PendingUserBookWriteSyncer` records the kind on each `SYNCED` replay.
`BooksRepositoryImpl.preserveSyncedProgress` → `preserveSyncedWrites`: for each synced book it folds the
synced kinds over the server-fetched book, re-applying the local optimistic value for **only** the owned
fields — `UPDATE_PROGRESS` → `userBookRead.{currentPage, currentSeconds, progress, startedAt, finishedAt}`;
`MARK_AS_READ` → `userBook.status` + `userBookRead.{progress, finishedAt}`; `UPDATE_RATING` →
`userBook.rating`; `UPDATE_REVIEW` → `userBook.{reviewDocument, reviewHasSpoilers}`. Every other field flows
through from the server, so the local copy can no longer overwrite server-of-record state; owned fields
self-heal on the next refresh once the queue drains. Preserving owned fields (not re-fetching/applying the
mutation response) is deliberate: the optimistic value is exactly what was just replayed, and it rides out
the read-after-write lag that motivated `preserveSyncedProgress` in the first place — a re-fetch would
reintroduce the pre-edit flicker that hack was added to prevent.

**Deviation:** the review offered "re-fetch the affected book / apply the mutation's own server response to
Room" *and* scoping; only the scoping path was taken (see rationale above). **DC2 deliberately not done** —
it's a separate LOW item. Tests: the drainer/syncer kind-mapping (connectivity) and the per-kind scoped
preservation incl. discriminating "non-owned server field flows through" cases (`:core:book`). Pairs
naturally with D2 (extracting offline-sync) if that lands later.

### DC2 [LOW] Latent race: a local mutation during an in-flight refresh can be clobbered
`refreshUserBooks()` drains pending writes *before* fetching, and the inflight `Mutex` serialises
refreshes — good. But a local mutation enqueued *while a refresh is mid-flight* isn't covered by
`preserveSyncedProgress` (which only protects drainer-synced IDs), so the about-to-land server snapshot
can overwrite it. Usually serialises correctly via network latency; worth closing when DC1 is addressed. Cost: low.

---

## TIER 6 — Navigation & accessibility

Navigation is **exemplary** and needs no structural change: `RootScreen` → `BottomBarScreen`
(`TabNavigator`, width-adaptive chrome) → `AppNavigatorImpl` (the single orchestration-tier place that
imports every feature `Screen`; features only reference `ScreenDestination`/`TabDestination` enums + the
`AppNavigator` interface, so the graph stays acyclic). The expanded-width detail pane is keyed per book
id (disposes prior state), clears across tab switches, and carries the book over to a pushed screen when
leaving EXPANDED. No nav logic leaks into ScreenModels; no feature constructs a peer's `Screen`. Tab
selection persists via `rememberSaveableStateHolder`; desktop window geometry persists to DataStore.
Font scaling is correct (editorial typography derives from Material `Typography`, so it respects system
font-size settings). The items here are **gaps/opportunities**, not defects:

### N1 [LOW] Deep linking is Android-only and not type-safe
The only deep link is the Focus-Mode `Intent` extra (`AppEntryPointImpl`). There's no HTTP-scheme routing
(e.g. `hardcover.app/book/123` → BookDetail) and no Voyager type-safe link builder. Adding type-safe deep
links for the major destinations (book by id/ISBN, profile by username, a list) would unlock notification
taps, share-sheet targets, and web links. Doveletter strongly favours type-safe nav/deep-links. Cost: medium.

### N2 [LOW] Back stack isn't restored across process death
Tab selection and desktop window geometry survive, but pushed screens (BookDetail, sub-settings) are not
saved — on process death the user lands at the root. Acceptable for a mainstream app; if full-stack
recovery becomes a requirement, use Voyager's saveable stack / `rememberSaveable` on navigator items. Cost: medium.

### A1 [LOW] Accessibility baseline is strong; screen-reader semantics could be richer
Good: the `RhaydusIconResource` token carries `contentDescription` so descriptions travel with icons;
shared components (top bar, buttons) label their icon buttons; decorative images correctly use
`contentDescription = null`; Material components meet touch-target minimums; auto-focus is used where it
helps (create-list, scan). Opportunities: add explicit roles (`semantics { heading() }`, list/card
semantics) on the library/explore grids for better TalkBack/VoiceOver navigation; if UI tests arrive,
introduce **debug-gated** `testTag`s (currently zero, which is fine). Spot-audit a few leaf screens to
confirm no user-facing `Image`/`Icon` is missing a description. Cost: low, incremental.

---

## TIER 7 — Security, release-hardening, i18n & DB efficiency

Security fundamentals are **solid**: API-key storage is platform-correct (Android Keystore AES/GCM, iOS
Keychain, desktop KSafe → macOS Keychain / Windows DPAPI), with a one-time migration off the legacy
plaintext DataStore key and no plaintext fallback. No tokens/keys/PII are logged, the `safeQuery`/
`safeMutation` error wrapping does **not** echo the Authorization header, and the Apollo endpoint is
hardcoded HTTPS. The findings are concentrated in release-build hardening:

### R1 [MEDIUM] Release builds have minification DISABLED and empty ProGuard rules
`app/build.gradle.kts` (~line 43) sets `isMinifyEnabled = false`, and `app/proguard-rules.pro` is just
the template comments. Consequences for a shipped release: larger APK, no obfuscation, class/method names
leak in stack traces. Re-enabling R8 is the right end state but **not a one-liner** — it needs keep rules
for the reflection-touching libraries: **Koin** (DI definitions), **Apollo** (generated operations/
adapters), **Room** (DAO/entities), **kotlinx-serialization** (`@Serializable` + serializers), and the
TOAD `UiState`/`UiAction`/`UiEvent` types. Plan: enable `isMinifyEnabled = true`, add the keep rules,
then do a **full release smoke test** (DI graph resolves, Apollo queries deserialize, Room opens) before
shipping. May be intentionally off during the adoption branch — but flag it so it's a conscious decision
before a public release, not a default. Cost: medium (rules + release verification).

### S1 [MEDIUM] `allowBackup="true"` — verify backup rules exclude the encrypted key + databases
`AndroidManifest.xml` has `allowBackup="true"` with `dataExtractionRules` + `fullBackupContent` declared.
Confirm those XML files (`res/xml/data_extraction_rules.xml`, `backup_rules.xml`) actually exclude
`filesDir/api_key.enc` and the Room DB — otherwise adb/cloud backup can exfiltrate them. (Not read in this
pass.) Cost: trivial (verify; tighten if needed).

### S2 [LOW] No certificate pinning
Absent. Acceptable for a book-tracker (pinning adds key-rotation fragility and isn't Play-required for
non-financial apps) — noting it as a conscious gap, not a defect. Relies on platform default cleartext
blocking (correct on modern API levels). Cost: n/a unless you want it.

### DB1 [LOW] Missing index on `ReadingJournalEntity.userBookId`
That column is filtered and `GROUP BY`-ed in `BookDao` reading-stats queries but has no index (other hot
entities like `UserBookEntity` are indexed on `bookId`/`statusCode`). Add `@Index("userBookId")` (or a
composite `(userBookId, updatedAt)`). Otherwise DB access is healthy — SQL does the aggregation (no N+1),
and every query is `suspend`/`Flow` (no main-thread Room access). Needs a migration bump. Cost: low.

### DB2 [LOW] `BookDao` is too large — split into area-scoped DAOs
`BookDao` has grown past **100 functions** (book/edition/list/journal queries all live on one DAO) and
trips detekt's `TooManyFunctions` interface threshold. It's currently `@Suppress("TooManyFunctions")`-ed
(surfaced by the 2026-06-24 release build) — a deliberate stopgap, not a fix. It should eventually be
split into smaller, area-scoped DAOs (e.g. `BookDao` / `EditionDao` / `BookListDao` / `ReadingJournalDao`),
each owning its own queries; the single `@Database` keeps seeing all of them, and `BookDao`'s cross-entity
`@Transaction` joins move to whichever DAO best owns them. Cost: medium (mechanical move + re-point call
sites; no schema/migration change). Removes the suppression. Cost: medium.

### I1 [LOW] A couple of hardcoded desktop strings; RTL declared but unverified
Two `jvmMain` reading-screen strings ("Now reading", "Refreshing…", etc.) are hardcoded literals;
externalize for completeness (mobile is the primary target, so low impact). `supportsRtl="true"` is
declared but unverified — spot-check key screens use `padding(start=/end=)` not `left=/right=`. Cost: low.

### B2-bis [LOW] No authored baseline/startup profiles
Confirmed absent. Not a blocker; a baseline profile would cut cold-start meaningfully on first launch
(Doveletter cites ~15-30%). Consider for a release-readiness pass. Cost: low-medium.

---

## Coverage note

Topics swept and documented: module graph & dependency hygiene, build-logic/convention plugins &
platform seams, data/domain layer, presentation/TOAD, testing strategy, coroutine/concurrency hygiene,
Apollo↔Room consistency, Voyager navigation, accessibility/semantics, Compose performance/stability,
logging, **security, release/R8 hardening, i18n, DB efficiency**. Cross-referenced against the Doveletter
knowledge base (architecture/clean-architecture/state/navigation/di/testing/performance best-practices +
the Sandwich and WorkManager deep-dives).

**Not reviewed** (out of scope, not reviewed-and-clean): analytics/telemetry & crash-reporting wiring,
CI pipeline specifics, signing/release-distribution config, and the Apollo `.graphqls` schema (skipped by
policy — so `@typePolicy` correctness and schema-level concerns are unverified).

**Confidence caveats:** findings came from agents reading excerpts, not exhaustive reads, and the build/
tests were **not run**. Verify specific line numbers and behaviours in-file before acting — especially the
`ReadingSessionService` leak (C1), `preserveSyncedProgress` divergence (DC1), and the R1 minification state.
