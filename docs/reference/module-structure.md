# Module Structure Guidelines

The two-axes / tier model, what goes in `core` vs `feature` vs `orchestration`, cross-feature
navigation, the placing-a-new-thing decision flow, the review checklist, and the naming / folder
conventions are governed by the foundation
[`docs/rhaydus/0.2.0/architecture.md`](../rhaydus/0.2.0/architecture.md) (§1–3, §5, §8–10). Read it
first — it is the source of truth for how code is categorized, grouped, and placed across the Gradle
modules, and for the allowed dependency directions (`:app → :orchestration → :feature:* → :core:*`,
gated so the graph stays an acyclic DAG).

This file keeps only Softcover's concrete module roster and build setup.

## Current feature roster and tier

**Every feature is a leaf** — there are no sibling `feature → feature` edges, so `book_detail` and
`reading`, though they compose many concepts, import only `core` and need no separate T2 tier in
practice. T2 remains the *sanctioned position* for a future aggregator that genuinely must import
another feature's screen; `book_detail` is the canonical candidate for that slot.

| Tier | Features |
|------|----------|
| T1 (leaf) | `library`, `reading`, `explore`, `book_detail`, `lists`, `profile`, `scan`, `session`, `onboarding`, `settings`, `app_update` |

Notes on features that are leaves only for their *screens*:

- `settings` is a T1 feature for its **screens** only — its shared value types, preference readers,
  and identity use cases belong in `core`, and its cross-feature orchestration belongs in T3.
- `profile` and `explore` keep their **screens** as features; their cross-feature **services** moved
  to `core:profile` / `core:book` (trending).
- `lists` is a T1 feature for its **CreateList surface** only (`CreateListUseCase` +
  `CreateListScreen`) — its repository and the operation use cases the rest of the app calls live in
  `core:lists`, and the shared list UI (`ChooseListsBottomSheet`, `ListMembership`) in
  `core:designsystem`. The remaining `* → lists` edges are `CreateListScreen` *navigation* imports,
  resolved by the foundation's navigation contract.
- `app_update` stays a feature (its Play-`AppUpdateManager` domain is the sanctioned `androidMain`
  exception), but its pure `AppUpdateState` and the `AppUpdateSimulator` contract live in
  `core:domain`.

`deadlines`, `personal`, and `connectivity` were kernels in disguise and are now `core` modules —
they are **not** features.

`session` is a small feature (a Focus-Mode overlay over the shared `ActiveSessionController`) but
**stays its own module**: it contributes an Android foreground `ReadingSessionService` and its manifest
entry, which is a genuine module boundary (manifests merge upward — see below), not presentation glue
that belongs in `:orchestration`.

## The module roster and build setup

The build is split into the following Gradle modules (see `settings.gradle.kts`). A module may depend
only on modules **below** its tier.

| Tier | Modules |
|------|---------|
| T3 app shell | `:app` |
| T3 orchestration | `:orchestration` |
| T1 features | `:feature:{lists, profile, onboarding, explore, library, book_detail, reading, session, scan, settings, app_update}` |
| T0 core | `:core:{domain, database, network, notification, preferences, identity, book, lists, deadlines, personal, profile, connectivity, designsystem}` |

### What each `core:*` module holds (Softcover roster)

| Module | Holds |
|--------|-------|
| `core:domain` | shared domain models, classification enums, config value types, cross-feature use-case **contracts** (`ResetUserDataUseCase`, `InitializeUserIdAndBooksUseCase`, `ReAuthenticateUseCase`, `RefreshLibraryUseCase`, `AppUpdateSimulator`) whose impls live in orchestration |
| `core:book` | the book-operations service: `BooksRepository` + use cases every feature calls (incl. `GetTrendingBooksUseCase`) |
| `core:lists` | the list-operations service: `ListsRepository`, `GetAllUserListsUseCase`, `AddBookToListUseCase`, `SetEditionAsOwnedUseCase` |
| `core:deadlines` | the deadline-operations service: `BookDeadlineRepository`, `ObserveBookDeadlineUseCase`, `SetBookDeadlineUseCase` |
| `core:personal` | the reading-activity service (sessions, highlights, reading log) — no screen |
| `core:profile` | the profile-data service consumed by reading/book_detail (the `ProfileScreen` stays a feature) |
| `core:preferences` | `SettingsRepository`, `Get*AsFlowUseCase` readers, `AppSettingsDataStore`, `ApiKeyLocalDataSource` |
| `core:identity` | `GetUserIdUseCase`, `UpdateApiKeyUseCase` (storage lives in `core:preferences/data`) |
| `core:connectivity` | offline write-queue / sync infra (contracts in `core:domain/connectivity`) |
| `core:designsystem` | TOAD framework, Material 3 theme, reusable components, modifiers, shared presentation models, and the cross-tier presentation contracts whose impls live in orchestration: nav (`AppNavigator`), `ActiveSessionController`, `SessionAuthenticator` |
| `core:network` | Apollo client, interceptors, `safeQuery` / `safeMutation` |
| `core:database` | Room database, migrations, **all** persisted entities + DAOs (incl. those a feature's data source uses) |

The `core` tier holds two **kinds** of module, and a new one should land deliberately in one bucket:

- **Domain-area data modules** — `core:{book, lists, deadlines, personal, profile, identity,
  preferences}`: a repository + use cases (+ data sources/mappers) for one area. These carry a feature's
  data/use-case layer, so depending on one is normal but **re-exporting one is not**: a module must
  `implementation`-depend on them, never `api`, unless the edge is on the allowlist in the
  `checkModuleGraph` task. That api-visibility rule is what stops a UI/infra module from quietly becoming
  a god-module (the way `:core:designsystem` once `api`-exported half the app).
- **Infra / contract modules** — `core:{domain, database, network, notification, connectivity,
  designsystem}`: cross-cutting plumbing (Apollo, Room, DI/UI primitives, sync) and the shared kernel
  (`core:domain`). They may `api`-expose their own surface (and `core:domain` types) as needed.

### The vertical-slice rule (Softcover concretization)

A `core:<concept>` operations module owns only the **repository + use cases** (and its data
sources/mappers). Its **domain models** live in `core:domain`; its **Room entities + DAOs** live in
`core:database` (the single `@Database` must see every entity, and `BookDao` SQL-joins across them);
any remote payloads go through `core:network` (Apollo). So `core:deadlines` =
`BookDeadlineRepository` + use cases; `BookDeadline` is in `core:domain`;
`BookDeadlineEntity` / `BookDeadlineDao` are in `core:database`. Do **not** try to make a concept
module self-contained with its own models/entities — that breaks the shared model tier and the single
Room DB.

### Build wiring conventions

- **Convention plugins** in `build-logic/` keep module build files uniform. Apply the smallest set:
  - `softcover.android.library` — base for every Android-only `:core:*` / `:feature:*` /
    `:orchestration` module (SDK/JDK, the shared coroutines/Koin runtime (logging is Kermit via `AppLog`, transitive from `:core:domain`) + JUnit5/Kotest/MockK/
    Turbine test stack).
  - `softcover.android.compose` — any Android-only module with Compose UI.
  - `softcover.android.room` — `:core:database` only.
  - `softcover.android.apollo` — `:core:network` only.
  - `softcover.kmp.library` / `softcover.kmp.compose` — the multiplatform siblings (KMP base, and KMP
    Compose UI wiring AndroidX Compose into `androidMain`) used by modules that have migrated to
    Kotlin Multiplatform.
  - `:app` is the lone `com.android.application` and stays Android-only (it owns build-type-conditional
    wiring).

  Do **not** re-declare what a convention plugin already provides, and do **not** enable
  `buildConfig` / `room` / `ksp` in a feature (Room/Apollo are core-only).
- **Each module declares the `project(":core:x")` deps its own code imports** — never rely on a
  transitive dep. A module whose *public API* exposes a type from a library (e.g. `:core:designsystem`
  returning a coil `ImageRequest`) declares that library `api`; all other deps are `implementation`.
  Gated by the `dependency-analysis` plugin (`./gradlew buildHealth`); the convention-plugin bundle is
  excluded from the check.
- **Manifests merge upward.** Library modules contribute components via their own
  `src/main/AndroidManifest.xml` (`:orchestration` the launcher `MainActivity`, `:feature:session` the
  `ReadingSessionService`); `:app` owns the `<application>` element, permissions, FileProvider, and
  launcher icons. Shared resources (strings, drawables, `Theme.Softcover`) live in `:core:designsystem`.
- **Koin:** each module owns one `module { }` that declares its own dependencies via `includes(...)`
  (the sibling modules whose bindings it `get()`s, plus its platform `expect`/`actual` module). The
  composition root `orchestrationModule` `includes(...)` the whole graph, so `softcoverModules` is just
  `listOf(orchestrationModule)` — order-independent, not a hand-sequenced list. `:app` starts Koin with
  `modules(softcoverModules + appModule)`. The whole-graph wiring is guarded by a Koin `verify()`
  test (`orchestration` `SoftcoverModulesVerificationTest`): every binding's constructor deps must be
  resolvable across the aggregate (externally-supplied types are listed in its `extraTypes`).

  **Naming:** a module's `module { }` val is `<moduleName>Module` (e.g. `bookModule`, `readingModule`);
  a feature whose name collides with a `core` module of the same name uses `<feature>ScreenModule` for
  its presentation DI (`profileScreenModule`, `listsScreenModule`) so the two are distinguishable. The
  two infra modules named for their tech rather than their path — `dispatcherModule` (`core:domain`) and
  `apolloModule` (`core:network`) — are deliberate.
- The tier rules and the data-module **api-visibility** rule are **enforced automatically** by the
  `checkModuleGraph` Gradle task (wired into `check`): it derives each module's tier from its path and
  fails the build on any `project(...)` dependency pointing sideways or upward, and on any
  `api(project(":core:<data-module>"))` edge that is not on the task's explicit allowlist (§10).

### Kotlin Multiplatform

The KMP migration is in place: the `domain` / `data` / `presentation` package boundary inside each
module is the KMP source-set boundary (`commonMain` / `androidMain`), so Android-only types stay out
of `domain` packages. `:feature:app_update` (Play `AppUpdateManager`) is the one sanctioned exception
— its domain is inherently Android and stays `androidMain`. `:feature:scan` (CameraX),
`:core:notification` (WorkManager), and `:core:database` (Room) are likewise `androidMain`.
