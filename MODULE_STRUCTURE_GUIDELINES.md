# Module Structure Guidelines

This document is the source of truth for **how code is categorized, grouped, and placed** across
Softcover's Gradle modules — what belongs in `core` vs a `feature` vs the orchestration tier, the
allowed dependency directions, and where a new type or screen goes. The app is a fully split
multi-module build (`:app → :orchestration → :feature:* → :core:*`); these rules keep that graph an
acyclic DAG.

It builds on [ARCHITECTURE.md](ARCHITECTURE.md) (Clean Architecture layers + TOAD). Layer rules
(`domain → data → presentation`) are unchanged and not repeated here — this doc is about the
*module* axis that sits above the layer axis. When the two are in tension, both must hold: every type
has a layer **and** a tier.

> These categorization rules are package-level rules first, module-level rules second: they hold
> whether a concept is its own Gradle module yet or still a package awaiting extraction.

---

## 1. The two axes

Every file is classified on two independent axes:

| Axis | Question it answers | Values |
|------|---------------------|--------|
| **Layer** (from `ARCHITECTURE.md`) | *What kind of code is this?* | `domain` · `data` · `presentation` · `di` |
| **Tier** (this doc) | *Who is allowed to depend on it?* | `core` · `feature` · `orchestration` |

Layer governs *internal* structure; tier governs *cross-module* dependencies. A `domain` use case
can live in a `core` module or a `feature` module — the layer is the same, the tier (and therefore
who may import it) is not.

---

## 2. Tiers

Modules are organized into tiers. **A module may depend only on modules in a lower tier — never
sideways within the feature tier, never upward.** This is the single rule that keeps the graph a DAG
and the build splittable.

```
┌─────────────────────────────────────────────────────────────┐
│  T3  :app            thin application shell (Application +     │  depends on :orchestration (+ a few
│                      manifest + Koin startup)                 │  core modules it touches directly)
│      :orchestration  navigation host + cross-feature          │  may depend on everything below
│                      orchestration use cases + Koin aggregate │
├─────────────────────────────────────────────────────────────┤
│  T2  :feature:book_detail   aggregator features that compose  │  may depend on T1, T0
│                             several other features            │
├─────────────────────────────────────────────────────────────┤
│  T1  :feature:*      leaf features (library, reading,         │  may depend on T0 only
│                      lists, deadlines, profile, scan, …)      │  NEVER on a sibling feature
├─────────────────────────────────────────────────────────────┤
│  T0  :core:*         shared kernels: domain model, book       │  may depend only on other T0
│                      operations, preferences, identity,       │
│                      design system, network, database         │
└─────────────────────────────────────────────────────────────┘
```

### Why two feature tiers (T1 vs T2)

Most features are **leaves**: they own one user-facing surface and pull only from `core`. A few are
**aggregators** — `book_detail` is the canonical one — that legitimately compose several leaf
features into one screen. Aggregators sit *above* the leaves they compose so the dependency points
downward. The defining test: **a leaf feature must never import another feature.** If feature A needs
something from feature B, one of these is true and must be resolved:

1. The shared thing is actually a kernel → push it down to `core` (most common — see §4).
2. A is genuinely an aggregator of B → A moves to T2 and may depend on B.
3. A needs to *navigate* to B's screen → use a navigation contract, not a screen import (§6).

---

## 3. What goes in `core` (T0)

`core` is for code that is consumed by **two or more features** and carries no single feature's
identity. Split into focused modules, not one `:core` grab-bag:

| Module | Holds | Examples |
|--------|-------|----------|
| `core:domain` | shared domain models, classification enums, config value types, and cross-feature use-case **contracts** whose impls live elsewhere | `Book`, `BookEdition`, `BookStatus`, `LibrarySortMode`, `DateStyle`, `BookDeadline`, `ReadingSession`, `ReadingDayActivity`, `AppUpdateState`; contracts `ResetUserDataUseCase`/`InitializeUserIdAndBooksUseCase` (`core/domain/account/`), `AppUpdateSimulator` (`core/domain/appupdate/`) |
| `core:book` | the book-**operations** service: repository + use cases every feature calls (incl. trending discovery) | `BooksRepository`, `MarkBookAsReadUseCase`, `RecordBookProgressUseCase`, `AddBookByIsbnUseCase`, `GetTrendingBooksUseCase` |
| `core:lists` | the list-**operations** service: repository + use cases consumed by library/book_detail/settings | `ListsRepository`, `GetAllUserListsUseCase`, `AddBookToListUseCase`, `SetEditionAsOwnedUseCase` |
| `core:deadlines` | the deadline-**operations** service consumed by library/reading/book_detail | `BookDeadlineRepository`, `ObserveBookDeadlineUseCase`, `SetBookDeadlineUseCase` |
| `core:personal` | the reading-**activity** service (sessions, highlights, reading log) — no screen | `ReadingSessionRepository`, `Start/Stop/Pause/ResumeReadingSessionUseCase`, `HighlightRepository` |
| `core:profile` | the profile-**data** service consumed by reading/book_detail (the `ProfileScreen` stays a feature) | `ProfileRepository`, `ObserveUserProfileDataUseCase`, `RefreshUserProfileDataUseCase` |
| `core:library` | library-refresh operation consumed by reading + the settings library-visibility screen | `RefreshLibraryUseCase` |
| `core:preferences` | preference read/write contracts + value access + the DataStore-backed impl | `SettingsRepository`, `Get*AsFlowUseCase` readers, `AppSettingsDataStore`, `ApiKeyLocalDataSource` |
| `core:identity` | user identity / auth credential use cases | `GetUserIdUseCase`, `UpdateApiKeyUseCase` (storage lives in `core:preferences/data`) |
| `core:connectivity` | offline write-queue/sync infra (contracts in `core:domain/connectivity`) | `ListWriteQueueImpl`, `UserBookWriteQueueImpl`, the pending-write syncers |
| `core:designsystem` | TOAD framework, theme, reusable components, modifiers, shared presentation models, nav contract, app-scoped session controller | `core/presentation/{toad, theme, component, model, navigation, session}`, `ActiveSessionController`, `MainActivityViewModel`, `ReadingSessionLauncher` (contract) |
| `core:network` | Apollo client, interceptors, `safeQuery`/`safeMutation` | |
| `core:database` | Room database, migrations, **all persisted entities + DAOs** (incl. those a feature's data source uses) | `SoftcoverDatabase`, `BookDao`, `BookDeadlineEntity`, `ReadingSessionEntity`, … |

**The litmus test for "is this `core`?":** *Would a second, unrelated feature reasonably import this
to do its job?* If yes, it is a kernel and belongs in `core`. A feature that everyone imports is not
a feature — it is a kernel wearing a feature's folder (this is exactly what `books`, `settings`,
`deadlines`, `personal`, and `connectivity` turned out to be — all now `core`). **Corollary:** a type
imported by ≥2 features is `core` even when it is a ViewModel or presentation model — e.g.
`MainActivityViewModel` (consumed by the shell + `profile` + `onboarding`) and `LibraryTab` (consumed
by `library` + `settings`) are `core:designsystem`, not feature-local.

**The vertical-slice rule (where a shared concept's pieces live).** A `core:<concept>` operations
module owns only the **repository + use cases** (and its data sources/mappers). Its **domain models**
live in `core:domain` (pure, KMP `commonMain` candidates); its **Room entities + DAOs** live in
`core:database` (the single `@Database` must see every entity, and `BookDao` SQL-joins across them).
So `core:deadlines` = `BookDeadlineRepository` + use cases; `BookDeadline` is in `core:domain`;
`BookDeadlineEntity`/`BookDeadlineDao` are in `core:database`. Do **not** try to make a concept module
self-contained with its own models/entities — that breaks the shared model tier and the single Room DB.

`core` modules may depend on each other **downward only** — e.g. `core:book → core:domain` is fine;
`core:domain` depends on nothing.

---

## 4. What stays a `feature` (T1 / T2)

A `feature` owns a **user-facing surface** (a screen or a coherent flow) and the domain/data/
presentation specific to it. It keeps the standard internal layout from `ARCHITECTURE.md`:

```
feature/<name>/
├── domain/         repository interfaces, use cases, feature-local models — depends on core only
├── data/           repository impls, data sources, entities, mappers
├── presentation/   screens, screenmodels, actions, events, flows, state
└── di/             the feature's Koin module
```

Rules specific to the module axis:

- **A leaf feature imports `core` only.** Not a sibling. If you are about to write
  `import nl.rhaydus.softcover.feature.<other>` from a leaf, stop and apply §2's three resolutions.
- **Feature-local stays feature-local.** A model, enum, or component used by exactly one feature
  lives in that feature, not in `core`. Promote to `core` only on the *second* real consumer — not
  speculatively.
- **An aggregator (T2) may depend on the leaf features it composes**, downward. It still may not be
  depended on *by* a leaf (navigate to it via contract instead).

### Current feature roster and tier

**Every feature is a leaf** — there are no sibling `feature → feature` edges, so `book_detail` and
`reading`, though they compose many concepts, import only `core` and need no separate T2 tier in
practice. (T2 remains a *sanctioned position* for a future aggregator that genuinely must import
another feature's screen.)

| Tier | Features |
|------|----------|
| T1 (leaf) | `library`, `reading`, `explore`, `book_detail`, `lists`, `profile`, `scan`, `session`, `onboarding`, `settings`, `app_update` |

`settings` remains a T1 feature for its **screens** only — its shared value types, preference readers,
and identity use cases belong in `core`, and its cross-feature orchestration belongs in T3 (see §5).

`profile` and `explore` keep their **screens** as features; their cross-feature **services** moved to
`core:profile` / `core:book` (trending). `deadlines`, `personal`, and `connectivity` were kernels in
disguise and are now `core` modules — they are **not** features. `app_update` stays a feature (its
Play-`AppUpdateManager` domain is the sanctioned `androidMain` exception), but its pure `AppUpdateState`
and the `AppUpdateSimulator` contract live in `core:domain`.

`lists` likewise remains a T1 feature for its **CreateList surface** only (`CreateListUseCase` +
`CreateListScreen`) — its repository and the operation use cases the rest of the app calls live in
`core:lists`, and the shared list UI (`ChooseListsBottomSheet`, `ListMembership`) in
`core:designsystem`. The remaining `* → lists` edges are `CreateListScreen` *navigation* imports,
resolved by the §6 navigation contract.

---

## 5. What goes in the T3 tier (`:orchestration` + `:app`)

The T3 tier is the only one allowed to know about many features at once. It is split across two
modules:

**`:orchestration`** (the library that composes everything) holds:

- The **navigation host** (Voyager `Navigator` / `TabNavigator` setup, root screens, the bottom bars,
  the `AppNavigator`/`AppEntryPoint` impls). Its manifest contributes the launcher `MainActivity`.
- **Cross-feature orchestration use cases** — logic that coordinates *several* features and cannot
  honestly live in any one of them. Examples: `ResetUserDataUseCase` (wipes data across library,
  lists, profile, identity), `InitializeUserIdAndBooksUseCase` (identity + book sync on launch),
  app-launch sync coordination.
- The **Koin aggregate** `softcoverModules` (the list of every feature + core module's `module { }`).

**`:app`** (the `com.android.application` shell) holds only the `SoftCoverApp` Application class, the
launcher resources/manifest `<application>`, and the Koin startup (`modules(softcoverModules + appModule)`).
It depends on `:orchestration` plus the few core modules its Application/version-provider touch
directly, and binds the `AppVersionProvider` (which reads its `BuildConfig`).

If a use case needs to reach into two or more *features*, it is orchestration and belongs here — not
in whichever feature you happened to be editing. (If it reaches into two or more *core* modules only,
it can live in `core` instead; orchestration is specifically about coordinating *features*.)

**When a leaf feature must trigger orchestration, invert the dependency — never let a leaf import
`:app`.** A T1 screen model that needs to kick off a cross-feature flow (e.g. `onboarding` completing
sign-in, `profile` logging out) depends on a **contract interface in `core/domain`**; the
feature-reaching implementation (`…UseCaseImpl`) lives here in `:app/orchestration` and is bound to the
interface in the orchestration Koin module. The leaf depends downward on the contract; `:app` depends
downward on the leaf. This keeps the graph a DAG — the import audit's `feature → feature` grep will not
flag a leaf → `:app` import, so verify it separately
(`grep -rn "import …orchestration" feature/` must be empty). Pattern landed for `ResetUserDataUseCase`
and `InitializeUserIdAndBooksUseCase` (contracts in `core/domain/account/`, impls in
`orchestration/usecase/`).

---

## 6. Cross-feature navigation

Features navigate to each other's surfaces **through a contract in `core:designsystem`**, never by
importing the destination's `Screen`:

- Define a navigation key/route or a small navigator interface (e.g. `BookDetailNavigator`) in
  `core/presentation`.
- The destination feature (T2) provides the concrete screen; the source feature (T1) triggers
  navigation via the contract.
- Any data passed across the boundary (e.g. `BookInitialCover`, `ProgressSheetTab`) is a **shared
  presentation model in `core/presentation/model`**, not a type owned by either feature.

This is what keeps "open the book detail screen from the library" from creating a
`library → book_detail` compile dependency (and the cycles that follow).

---

## 7. Placing a new thing — decision flow

When adding a class, enum, screen, or use case, decide **layer** (per `ARCHITECTURE.md`) and **tier**
(per this doc):

1. **Is it a user-facing surface or surface-specific logic?** → a `feature` module (T1, or T2 if it
   composes other features). Pick the feature whose surface it serves.
2. **Will a second, unrelated feature import it to do its job?** → `core` (T0). Choose the focused
   `core:*` module by what it is: a domain model/enum → `core:domain`; a book operation →
   `core:book`; a preference reader → `core:preferences`; identity → `core:identity`; a reusable
   component/theme/TOAD/nav contract → `core:designsystem`.
3. **Does it coordinate two or more *features*?** → `:app` orchestration (T3).
4. **Default:** keep it feature-local. Promote to `core` only when the second real consumer appears
   — never speculatively, never to "share just in case."

**Tie-breaker (`core` vs `feature`):** if the type could be consumed by a headless use case, a CLI,
or a second feature without losing meaning, it leans `core`. If it only makes sense in the context of
one screen, it stays in that feature. This mirrors the layer heuristic in `ARCHITECTURE.md` — applied
to the tier axis.

---

## 8. Naming and folder conventions

- Module path mirrors tier and name: `:core:book`, `:core:domain`, `:feature:library`,
  `:feature:book_detail`. One Gradle module per feature — **not** one per layer. The
  `domain`/`data`/`presentation` split stays as **packages inside** the module.
- Suffixes are unchanged from `ARCHITECTURE.md`: `*Repository`/`*RepositoryImpl`, `*UseCase`,
  `*DataSource`/`*DataSourceImpl`, `*Entity`, `*Dto`, `*Screen`, `*ScreenModel`, `*Action`,
  `*Event`, `*UiState`, `*Dependencies`, `*LocalVariables`.
- Each module owns exactly one Koin `module { }`; `:orchestration` aggregates them into
  `softcoverModules` and `:app` starts Koin with it.
- The `domain`/`data`/`presentation` package boundary inside a feature is intentionally the future
  KMP source-set boundary (`commonMain`/`androidMain`). Keep Android-only types out of `domain`
  packages so that boundary stays clean — `app_update` is the one sanctioned exception (its domain is
  inherently Android, and stays `androidMain`).

---

## 9. Review checklist

The first two items are **enforced automatically** by the `checkModuleGraph` Gradle task (registered in
the root build, wired into `check`, so CI gates on it). It derives each module's tier from its path and
fails the build on any `project(...)` dependency that points sideways or upward — replacing the old manual
`grep` import audits for tier violations. The remaining items still rely on review.

A change is structurally correct when:

- [ ] No leaf feature (T1) imports another feature. *(gated by `checkModuleGraph`)*
- [ ] No module depends sideways or upward — only on lower tiers. *(gated by `checkModuleGraph`)*
- [ ] A type imported by ≥2 features lives in `core`, not in a feature.
- [ ] Cross-feature navigation goes through a `core` contract, not a `Screen` import.
- [ ] Cross-feature coordination lives in `:orchestration`, not inside a single feature.
- [ ] The new type's **layer** (domain/data/presentation) and **tier** (core/feature/orchestration)
      were both chosen deliberately, not inherited from where the first caller happened to sit.

---

## 10. The module roster and build setup

The build is split into the following Gradle modules (see `settings.gradle.kts`). A module may depend
only on modules **below** its tier (§2).

| Tier | Modules |
|------|---------|
| T3 app shell | `:app` |
| T3 orchestration | `:orchestration` |
| T1 features | `:feature:{lists, profile, onboarding, explore, library, book_detail, reading, session, scan, settings, app_update}` |
| T0 core | `:core:{domain, database, network, platform, preferences, identity, book, lists, deadlines, personal, profile, library, connectivity, designsystem}` |

Build wiring conventions:

- **Convention plugins** in `build-logic/` keep module build files uniform. Apply the smallest set:
  `softcover.android.library` (base for every `:core:*`/`:feature:*`/`:orchestration` module — sets
  SDK/JDK, and the shared coroutines/Koin/Timber runtime + JUnit5/Kotest/MockK/Turbine test stack),
  plus `softcover.android.compose` (any module with Compose UI), `softcover.android.room`
  (`:core:database` only), `softcover.android.apollo` (`:core:network` only). `:app` is the lone
  `com.android.application`. Do **not** re-declare what a convention plugin already provides, and do
  **not** enable `buildConfig`/`room`/`ksp` in a feature (Room/Apollo are core-only).
- **Each module declares the `project(":core:x")` deps its own code imports** — never rely on a
  transitive dep. A module whose *public API* exposes a type from a library (e.g. `:core:designsystem`
  returning a coil `ImageRequest`) declares that library `api`, so consumers get it transitively; all
  other deps are `implementation`. This is **gated** by the `dependency-analysis` plugin: run
  `./gradlew buildHealth` (it fails on a genuinely unused dependency or a wrong `api`/`implementation`).
  The convention-plugin-provided bundle (coroutines/Koin/Timber/test stack, Compose, Room) is excluded
  from the check in the root `dependencyAnalysis {}` config, as are a few type-resolution false
  positives; the "declare transitive deps directly" advice is treated as informational, not a gate.
- **Manifests merge upward.** Library modules contribute components via their own
  `src/main/AndroidManifest.xml` (`:orchestration` the launcher `MainActivity`, `:feature:session` the
  `ReadingSessionService`); `:app` owns the `<application>` element, permissions, FileProvider, and
  launcher icons. Shared resources (strings, drawables, `Theme.Softcover`) live in `:core:designsystem`.
- **Koin:** each module owns one `module { }`; `:orchestration` aggregates them all into
  `softcoverModules`, and `:app` starts Koin with `modules(softcoverModules + appModule)`.

## 11. Future: Kotlin Multiplatform

The module boundaries are shaped so a later KMP conversion is a per-module `commonMain`/`androidMain`
source-set split rather than a re-architecture. `:core:domain`, `:core:preferences`, `:core:identity`,
and `:core:book` are the natural first `commonMain` candidates (their `domain` packages are Android-free
by the layer rule). `:feature:app_update` (Play `AppUpdateManager`), `:feature:scan` (CameraX),
notification/WorkManager code in `:core:platform`, and Room in `:core:database` stay `androidMain`. Keep
Android-only types out of `domain` packages so that boundary stays clean — `app_update` is the one
sanctioned exception (its domain is inherently Android).
