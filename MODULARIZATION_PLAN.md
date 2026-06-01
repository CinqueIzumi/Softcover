# Modularization Plan

This document describes the planned restructuring of the Softcover codebase ahead of a Gradle
multi-module split (and, later, Kotlin Multiplatform). It is a **planning document**: it records
*what* will change and *why*, in the order changes should land. No build wiring is created here —
every step below is a single-module refactor that keeps the app shippable, so the eventual
`include(":feature:x")` becomes mechanical and acyclic.

The companion document [MODULE_STRUCTURE_GUIDELINES.md](MODULE_STRUCTURE_GUIDELINES.md) defines the
target categorization (tiers, what is `core` vs `feature` vs orchestration, naming) that this plan
moves the code toward. Read it first; this plan is the migration path to that end state.

> Status legend: ☐ not started · ◐ in progress · ☑ done. Update inline as steps land.
> Authored 2026-06-01.

---

## 1. Why restructure before splitting

The current code lives in one Gradle module (`:app`) with a `core/` + `feature/` package split.
The layering *within* features is already clean — a dependency audit found **zero
`domain → data/presentation` violations**, and only `app_update/domain` touches Android types (the
in-app update flow legitimately needs `Activity`/`AppUpdateManager`). That clean layering is what
makes a module split tractable.

What is *not* yet ready is the **horizontal coupling between features**. A cross-feature import
audit surfaced three structural problems that Gradle would reject or that would force ugly module
boundaries. Each has a distinct fix. The rest of this document is those fixes, sequenced.

### 1.1 The dependency audit (snapshot, 2026-06-01)

Cross-feature import edges (`caller → target (import count)`), highest-signal first:

```
library      → settings   (40)      book_detail → books      (23)
library      → books      (18)      book_detail → deadlines  (13)
library      → lists      (15)      book_detail → lists      (11)
reading      → books      (13)      books       → settings   (12)
reading      → settings    (8)      reading     → deadlines   (8)
reading      → profile     (8)      onboarding  → settings    (6)
library      → deadlines   (6)      session     → personal    (6)
book_detail  → settings    (6)      scan        → books       (5)
explore      → books      (11)      ...plus navigation edges (see §4)
```

Three findings drive the plan:

1. **`settings` and `books` are shared kernels disguised as features.** Of all cross-feature
   imports into `settings`, **75 of 76 are `domain`** — pure config value types
   (`LibrarySortMode`, `SortDirection`, `DateStyle`, `LibraryGridLayout`, `LibrarySortSettings`),
   preference-read use cases, and user-identity use cases (`GetUserIdUseCase`,
   `InitializeUserIdAndBooksUseCase`, `ResetUserDataUseCase`). Of all imports into `books`, **64 of
   76 are `domain`** — `BooksRepository` plus the book-mutation use cases (`MarkBookAsReadUseCase`,
   `RecordBookProgressUseCase`, `RemoveBookFromLibraryUseCase`, `AddBookByIsbnUseCase`, …) that six
   other features call. These are services, not screens.

2. **True cycles that Gradle forbids:** `settings ↔ library`, `settings ↔ books`,
   `settings ↔ lists`, `settings ↔ profile`. They exist because `settings` owns cross-feature
   *orchestration* use cases (`ResetUserDataUseCase`, `InitializeUserIdAndBooksUseCase`, and
   coordination calling `RefreshLibraryUseCase` / `GetAllUserListsUseCase` / `ProfileRepository`)
   that sit in the wrong tier.

3. **Navigation cycles:** `reading/explore/library/scan → book_detail` is only `BookDetailScreen`
   + `BookInitialCover` (opening the screen), and the back-edge `book_detail → reading` is one
   presentation enum (`ProgressSheetTab`). Classic "feature A imports feature B's screen" coupling.

---

## 2. Target shape (summary)

The end state is **horizontal `:core:*` modules at the bottom, one vertical `:feature:*` module per
feature on top, and `:app` as the orchestration + navigation host at the very top.** One Gradle
module per feature — *not* a module per layer. The `domain`/`data`/`presentation` split stays as
**packages inside** each feature module (which become `commonMain`/`androidMain` source sets under
KMP later).

```
:app                      nav host + cross-feature ORCHESTRATION use cases (top tier)
   │
:feature:{library, reading, explore, book_detail, lists, profile, scan,
          session, onboarding, settings, app_update}   ← all leaves after Steps 7–18
   │
:core:book  :core:lists  :core:deadlines  :core:personal  :core:profile  :core:library
   │         operations services: repositories + the use cases features call
:core:domain  :core:preferences  :core:identity   shared model, config value types, user id
   │           (T3 :app also holds orchestration/ — cross-feature use case impls behind core/domain contracts)
:core:designsystem (toad, theme, components, nav contract, session controller)
:core:connectivity  :core:network  :core:database
```

> **Update (Steps 7–18 landed, 2026-06-01).** The post-Step-6 audit was *not* yet a clean DAG — its
> §4 script greps only `feature → feature`, so it missed (a) four `core → feature` cycles and (b) a
> second wave of kernels still wearing feature folders. Steps 7–18 (below) closed both. The graph is
> now **fully acyclic with zero `feature → feature` edges**: every former aggregator edge resolved by
> pushing the shared thing to `core`, so even `book_detail` and `reading` are clean **leaves** (no T2
> tier is needed in practice). `deadlines`, `personal`, and `connectivity` became `core` modules and
> are no longer features; `profile`/`explore` kept their *screens* as features while their *services*
> moved to core.

Tier rule: a module may depend only on modules **below** it. See
[MODULE_STRUCTURE_GUIDELINES.md](MODULE_STRUCTURE_GUIDELINES.md) §"Tiers" for the authoritative
definition.

---

## 3. The pre-split refactors (ordered)

Each step is independently shippable and touches only packages/imports — no Gradle module is created
until §5. Re-run the cross-feature import audit after each step to confirm the edge it targets is
gone.

### Step 1 — Lift shared value types into `core/domain` ☑

**Move** the preference/config enums and value objects that other features consume out of
`feature/settings/domain` into `core/domain/model/`:
`LibrarySortMode`, `SortDirection`, `DateStyle`, `LibraryGridLayout`, `LibrarySortSettings`, and any
remaining `BookStatus`-style classification enums still filed under a feature.

- **Why:** these are pure, headless-consumable business/config types — by the §"Placing a new type"
  rule in `ARCHITECTURE.md` they belong in domain, not a feature. `Book` already lives in
  `core/domain/model` — this follows the same pattern.
- **Blast radius:** import-only churn across `library`, `books`, `reading`, `book_detail`,
  `onboarding`, `profile`. No logic changes.
- **Done when:** no feature imports a value type from `feature/settings`.

**Landed (2026-06-01).** The five types (`LibrarySortMode`, `SortDirection`, `DateStyle`,
`LibraryGridLayout`, `LibrarySortSettings`) moved into a **flat** `core/domain/model/` (package
`…core.domain.model`), alongside `Book` and the other domain types. `SortDirectionTest` moved with
its subject. Pure restructure — no logic, serialization, or enum-ordering changes (the types are
DataStore/kotlinx-serialization values keyed by constant name, so the package move is
on-disk-compatible). This also removed the only **core → feature** violations on these types
(`core/domain/model/UserBook.kt` and `core/presentation/component/DeadlineSummaryLine.kt` previously
imported `DateStyle` from the feature). Remaining inbound edges on `settings` (`SettingsRepository`,
preference/identity use cases, `ThemeConfiguration`, `BottomBarStyle`, `SettingsTab`) are the targets
of Steps 2–6.

> **Structure note — no construct-axis subpackages.** `core/domain/model` is organized by domain
> concept, not by Kotlin construct. The pre-existing `core/domain/model/enum/` subpackage (which held
> some but not all of the module's enums — `PrivacySetting`/`TagCategory`/`UserBookStatus` already sat
> at the top level) was retired in this step and its files flattened up, so enums and data classes
> coexist in one flat package. The lone `feature/reading/presentation/enums/` directory
> (`ProgressSheetTab`) was likewise flattened into `feature/reading/presentation/model/`, matching
> `library`'s `presentation/model/LibraryTab`. Concept-level grouping is delivered by the module split
> itself (`core:preferences`, `core:book` in Steps 2–3), not by folders-by-keyword. Do not
> reintroduce an `enum/` (or similar construct-named) directory. Separately noted for a later cleanup:
> `AppDispatchers`/`ApplicationScope` are concurrency infrastructure, not domain models, and should
> eventually move out of `domain/model`.

### Step 2 — Extract preferences + identity contracts into core ☑

**Move** the `SettingsRepository` interface, its preference-**read** use cases
(`GetDateStyleAsFlowUseCase`, `GetLibrarySortSettingsAsFlowUseCase`,
`GetLibraryGridLayoutAsFlowUseCase`, `GetEnabledStatusCodesAsFlowUseCase`, etc.), and the
user-identity use cases (`GetUserIdUseCase`, and the read side of user-id storage) into new
`core/preferences/` and `core/identity/` packages.

- The `settings` **feature** keeps only the settings *screens* and the write/mutation paths that are
  genuinely settings-screen concerns.
- **Why:** preference reads and user identity are cross-cutting infrastructure (like DataStore
  itself), consumed by nearly every feature. Keeping them in a "feature" is what creates most of the
  inbound pressure on `settings`.
- **Done when:** the only inbound edges left on `feature/settings` are write-path calls from screens
  that genuinely belong to settings.

**Landed (2026-06-01).** The **full vertical** moved into core (not just the domain contracts), and
each new kernel got its **own Koin module**:

- **`core/preferences/`** — `SettingsRepository` (kept whole, per plan) under
  `domain/repository/`; the cross-feature preference use cases under `domain/usecase/`
  (`GetDateStyleAsFlowUseCase`, `GetLibraryGridLayoutAsFlowUseCase`,
  `GetLibrarySortSettingsAsFlowUseCase`, `GetEnabledStatusCodesAsFlowUseCase`,
  `GetEnabledListIdsAsFlowUseCase`, `GetLibraryTabOrderAsFlowUseCase`,
  `ObservePlanTodayDismissalsUseCase`, `GetThemeConfigurationUseCase`, plus the cross-feature *writes*
  `SetLibraryGridLayoutUseCase`/`SetLibrarySortUseCase`/`DismissPlanTodayUseCase` — these are
  library/reading concerns, not settings-screen concerns); and the entire data layer under `data/`
  (`SettingsRepositoryImpl`, `Settings{Local,Remote}DataSource(+Impl)`, `ApiKeyLocalDataSource(+Impl)`,
  `AppSettingsDataStore` + the `Context.appSettings` delegate, `AppSettingsSerializer`,
  `AppSettingsEntity`, `ThemeConfigurationEntity`). New `core/preferences/di/preferencesModule`.
- **`core/identity/`** — `GetUserIdUseCase`, `GetUserIdAsFlowUseCase`, `UpdateApiKeyUseCase` (the api
  key is the auth credential, so it sits with user-id). These read through the `core:preferences`
  `SettingsRepository`, i.e. `core:identity → core:preferences` (a legal downward core→core edge). New
  `core/identity/di/identityModule`.
- **`core/domain/model/`** — the value types `ThemeConfiguration`, `BottomBarStyle` joined
  `DateStyle` et al. from Step 1. `SettingsRepository` references them, and they were already imported
  by `core/presentation`, so promoting them also cleared a **core → feature** violation.
- This step also fixed the second **core → feature** violation: `core/data/network/AuthInterceptor`
  now imports `ApiKeyLocalDataSource` from `core/preferences/data/datasource`.
- Storage is on-disk-compatible: `BottomBarStyle` serializes by kotlinx-serialization constant name,
  so the package move doesn't touch persisted `app_settings.json` (same reasoning as Step 1).
- `feature/settings` now holds only its screens/screenmodels/flows + the genuinely settings-screen
  write use cases (`SetDateStyle`, `SetBottomBarStyle`, `SetDynamicColor`, `SetEnabledStatusCodes`,
  `SetEnabledListIds`, `SetLibraryTabOrder`) and — still — the two orchestration use cases.

> **Deliberately left for later steps.** The remaining inbound edges on `feature/settings` are
> `onboarding/profile → {InitializeUserIdAndBooksUseCase, ResetUserDataUseCase}` (cross-feature
> **orchestration**, relocated to `:app` in **Step 4**) and `library → LibraryVisibilitySettingsScreen`
> + `core/presentation/BottomNavigationBar → SettingsTab` (cross-feature **navigation**, addressed by
> the routing contract in **Step 5**). No preference, identity, or value-type edge into `settings`
> remains.

### Step 3 — Extract book-operations into `core/book` ☑

**Move** `BooksRepository` and the cross-feature book use cases (mark-as-read/reading/want-to-read,
record/update progress, update rating/review/edition, add-by-ISBN, resolve-by-ISBN, reorder shelf,
the `Get*UserBooks*` queries) out of `feature/books` into `core/book/`.

- Any book-**screen** UI that is genuinely its own surface stays in a thin `feature` (or folds into
  `library`). The cross-cutting prefetch helpers (`rememberBookDetailPrefetcher`,
  `prefetchBookDetailOnPress`, `LocalBookDetailPrefetcher`) move to `core/presentation`.
- **Why:** `books` is doing double duty as a feature *and* the book-operations service the rest of
  the app is built on. Splitting the service out lets every feature depend on `core/book` without
  depending on a sibling feature.
- **Done when:** no feature imports `feature/books`; book operations come from `core/book`.

**Landed (2026-06-01).** The **full vertical** moved into core (matching Step 2), and the new kernel
got its **own Koin module** (`val bookModule`, renamed from `booksModule`):

- **`core/book/`** — `BooksRepository` under `domain/repository/`; all the book use cases under
  `domain/usecase/` (the mutation set `MarkBookAs{Read,Reading,WantToRead}UseCase`,
  `RecordBookProgressUseCase`/`UpdateBookProgressUseCase`,
  `UpdateBook{Rating,Review,Edition}UseCase`, `RemoveBookFromLibraryUseCase`,
  `AddBookByIsbnUseCase`/`ResolveBookByIsbnUseCase`, `ReorderShelfBooksUseCase`,
  `PersistEditionImageUseCase`, plus the `Get*UserBooks*` queries and `FetchBookByIdUseCase`); the
  `CreatedBook`/`IsbnEditionMatch` domain models; and the entire data layer under `data/`
  (`BooksRepositoryImpl`, `Books{Local,Remote}DataSource(+Impl)`, `BookNotFoundException`, the
  `SortSql` ORDER-BY builder). New `core/book/di/BookModule.kt`. `feature/books` no longer exists.
- **Prefetch helpers** (`BookDetailPrefetcher`, `LocalBookDetailPrefetcher`,
  `rememberBookDetailPrefetcher`, `Modifier.prefetchBookDetailOnPress`) moved to
  `core/presentation/prefetch/`. `core/presentation` already depended on a book use case
  (`EditionImage` → `PersistEditionImageUseCase`), so this introduces no new edge.
- **`BookMapper` was split along its natural seam** to keep the graph acyclic. The mapper mixed
  Apollo-fragment→domain mappers with domain↔Room-entity mappers, and `BookDao` (in
  `core/data/database`) consumed the entity ones — i.e. there was a pre-existing
  `core/data/database ↔ feature/books` **cycle**. Because `core:book → core:database` (its local
  data source uses `BookDao`), moving the whole mapper into `core/book` would have recreated that as
  a `core:database ↔ core:book` cycle. Instead: the Apollo→domain half stays with the remote data
  source at `core/book/data/mapper/BookMapper.kt`; the domain↔entity half — which maps
  `core/domain` ↔ `core/data/database/model`, neither of them book-feature-specific — moved **into
  the database layer** at `core/data/database/mapper/BookEntityMapper.kt`. `BookDao` now uses those
  mappers intra-module; the local data source and `lists`' `ListMapper` reach them via the legal
  downward `→ core:database` edge; the remote data source and `explore` use the Apollo half via
  `→ core:book`. This **resolves** the long-standing `database↔books` cycle rather than relocating
  it. (`BookMapperTest` split to mirror, into `BookMapperTest` + `BookEntityMapperTest`.)
- Audit result: **`books` has zero inbound cross-feature edges** and `core/book` imports no feature.
  The only remaining `core → feature` edges left on `BookDao` (`feature/lists` `toEntity`,
  `feature/deadlines` `BookDeadlineEntity`) are unrelated to books and belong to later steps.

### Step 4 — Move orchestration use cases up to `:app` (+ extract `core/lists`) ☑

**Move** the cross-feature orchestration use cases — `ResetUserDataUseCase`,
`InitializeUserIdAndBooksUseCase`, and any refresh/sync coordination that reaches into
`RefreshLibraryUseCase` / `GetAllUserListsUseCase` / `ProfileRepository` — out of `feature/settings`
into the top tier (an `app/`-level `orchestration/` package, or a `core/sync` module if it grows).

- **Why:** these are the edges that create the `settings ↔ library/lists/profile` cycles. They are
  *allowed* to know about multiple features — but only from the top tier, which depends downward on
  all of them. Relocating them **breaks all four `settings ↔ *` cycles** at once.
- **Done when:** the audit shows no `settings → {library, profile, books}` orchestration edges and no
  feature → `core/lists` *kernel* edges from `library` / `book_detail` / `settings` / `connectivity`.

**Landed (2026-06-01).** Investigation showed the targeted cycles were carried by **two distinct kinds
of coupling**, and both were resolved in this step:

- **Orchestration (dependency-inverted, not a plain move).** `ResetUserDataUseCase` and
  `InitializeUserIdAndBooksUseCase` are injected as constructor params into **leaf-feature** screen
  models (`profile`, `onboarding`). Moving the concrete classes straight to T3 would have made a T1
  leaf depend on T3 — an upward edge the import-audit script (which only greps `feature → feature`)
  does **not** catch, but the Gradle split would. So each became a **pure contract interface** in
  `core/domain/account/` (`suspend operator fun invoke(): Result<Unit>`, imports nothing
  feature-specific), with the feature-reaching implementation as `…UseCaseImpl` in a new top-level T3
  `nl.rhaydus.softcover.orchestration.usecase` package + `orchestrationModule` Koin module. Leaf
  features depend on the `core` interface (downward, legal); `:app` binds interface→impl. Call sites
  changed only their import line (the `Repository`/`RepositoryImpl` convention applied to a use case).
  `core/domain/account` was chosen over `core/identity` to keep identity pure (a first-wave KMP
  `commonMain` candidate) — the contracts touch books/profile/library, which is broader than identity.
- **A disguised kernel — `core/lists` extracted.** The `settings → lists` edge was **not**
  orchestration: `settings`, `library`, and `book_detail` all consumed `feature/lists` as a service.
  `lists` was a kernel wearing a feature's folder, exactly like `books` (Step 3). The **full operations
  vertical** moved into **`core/lists/`** — `ListsRepository`, `ListNameTakenException`, the six
  cross-feature use cases (`GetAllUserListsUseCase`, `AddBookToListUseCase`,
  `RemoveBookFromListUseCase`, `SetEditionAsOwnedUseCase`, `SetListRankedUseCase`,
  `ReorderListBooksUseCase`), `ListsRepositoryImpl`, both data sources, and the Apollo→domain
  `ListMapper` — with its own `listsModule` Koin module. `ListsRepositoryImpl`'s deps are all
  downward: `core:book`, `core:domain`, and the `ListWriteQueue`/`ListWriteDrainer` contracts already
  in `core/domain/connectivity`, so **no `core:lists ↔ connectivity` cycle**. The shared list UI
  (`ChooseListsBottomSheet`, `ListMembership`) moved to `core/presentation/component/` (recorded in
  `DESIGN_SYSTEM.md`). `feature/lists` survives as a thin T1 feature owning only the **CreateList**
  surface (`CreateListUseCase` + `CreateListScreen` and its TOAD pieces, `createListModule`).
- **`ListMapper` split along the Step-3 seam.** `BookDao` (in `core/data/database`) consumed the
  domain↔entity half of `ListMapper`, a pre-existing `core/data/database → feature/lists` cycle.
  The Apollo→domain half stays at `core/lists/data/mapper/ListMapper.kt`; the domain↔entity half
  (`BookList.toEntity`, `ListBook.toEntity`, `ListBookFull.toModel`, `BookListWithBooks.toModel`)
  moved **into the database layer** at `core/data/database/mapper/ListEntityMapper.kt`. `BookDao` now
  uses those intra-module (the `toListEntity` alias is gone). This **resolves** the `database↔lists`
  cycle (same outcome as `BookEntityMapper`). `ListMapperTest` split to mirror, into `ListMapperTest`
  (Apollo) + `ListEntityMapperTest` (entity).
- **This extends the written plan.** `core:lists` was not in the original §1.1 findings (which named
  only `settings` and `books` as kernels); it was recognised during Step 4 and added to §2 and to
  `MODULE_STRUCTURE_GUIDELINES.md` §3.

> **Audit blind spot, recorded.** The §4 script only greps `feature → feature` edges, so it cannot
> catch a future leaf → `:app` orchestration import. Step 4 verification therefore **also** runs
> `grep -rn "import nl.rhaydus.softcover.orchestration" feature/` (must be empty) alongside the script.

> **Deliberately left for later steps.** The only `* → lists` edges that remain are the
> `CreateListScreen` **navigation** imports from `library`, `book_detail`, and `settings` (Step 5's
> routing contract). Separately, `settings → library` survives via `LibraryVisibilitySettingsScreen`
> (it injects `RefreshLibraryUseCase` and imports `LibraryTab`) and `settings → profile` via the
> `ProfileScreen` nav import — these are a library-config screen and a screen-nav edge, both
> pre-existing and **out of Step 4's scope**; they belong with the Step 5 navigation work or a
> follow-up that relocates `LibraryVisibilitySettingsScreen` to `library`.

### Step 5 — Break navigation cycles with a routing contract ☑

**Introduce** a navigation contract in `core/presentation` (a route key, or a `BookDetailNavigator`
interface) so `reading`, `explore`, `library`, and `scan` open the book-detail surface **without
importing `BookDetailScreen`**. **Move** the shared presentation types they pass across the boundary
— `BookInitialCover`, and `book_detail`'s `ProgressSheetTab` — into `core/presentation/model/`.

- **Why:** removes the `* → book_detail` screen imports and the `book_detail → reading` back-edge,
  leaving `book_detail` as a clean top-of-graph aggregator with no inbound feature edges.
- **Done when:** no feature imports `feature/book_detail`; cross-feature navigation goes through the
  contract.

**Landed (2026-06-01).** Generalised beyond `book_detail` to a **single unified navigation contract
covering every cross-feature destination**, and the **entire app shell relocated to the orchestration
tier** — so the graph is acyclic for *all* navigation, not just the book-detail cycle.

- **Type moves.** `BookInitialCover` → `core/presentation/model/`; `ProgressSheetTab` (which actually
  lived in `feature/reading/presentation/model`, not `book_detail`) → `core/presentation/model/`. The
  latter also cleared a pre-existing **core → feature** violation (`core/presentation/component/
  UpdateProgressBottomSheet` imported `ProgressSheetTab` from `reading`).
- **`core/presentation/navigation/` contract.** `AppNavigator` (`fun screen(ScreenDestination): Screen`,
  `fun tab(TabDestination): Tab`) with a sealed `ScreenDestination` (`BookDetail(id, initialCover,
  transitionSurface)`, `CreateList`, `BarcodeScanner`, `LibraryVisibilitySettings`, `FocusMode`,
  `Profile`) and a `TabDestination` enum (`READING`/`LIBRARY`/`EXPLORE`/`SETTINGS`). Features inject it
  via `koinInject` and keep control of *how* they navigate (`push` / `parent?.push` /
  `tabNavigator.current = …`); the contract only resolves *what*. Implemented once as
  `orchestration/navigation/AppNavigatorImpl` (the single place that imports every feature's
  `Screen`/`Tab`), bound `single<AppNavigator>` in `orchestrationModule`. This follows Step 4's
  pattern — pure contract in `core`, impl in the top tier, Koin-bound — rather than Voyager's global
  `ScreenRegistry` (used nowhere else). **All 11 cross-feature screen pushes + the one
  `reading → ExploreTab` tab switch now route through it; zero `feature → feature` navigation imports
  remain.**
- **App-shell relocation.** The nav host was mis-filed in `core/presentation` while reaching into
  features. `MainActivity`, `RootScreen`, `BottomBarScreen`, and the two bottom bars
  (`DockedBottomNavigationBar`/`BottomFloatingBar`) moved to `orchestration/presentation/`, legalising
  their tab / `SessionPeekBar` / `ConnectivityBanner` / `ActiveSessionController` / `FocusModeScreen` /
  `OnboardingScreen` / `AppUpdateState` references as downward edges. `AndroidManifest.xml` now points
  at `.orchestration.presentation.MainActivity`. The composition locals the shell *defines* but core
  utilities + features *consume* were extracted **down** to core first: `LocalThemeConfiguration` →
  `core/presentation/theme/`, `LocalBottomBarPadding` + `LocalAppUpdate(State/StartAppUpdate)` →
  `core/presentation/util/`. `core/presentation/screen/` was emptied and removed.
- **A surfaced edge — `AppEntryPoint`.** Relocating `MainActivity` upward exposed
  `feature/session`'s `ReadingSessionService`, which built a notification `Intent` targeting the
  launcher Activity (was a legal `feature → core` edge only because the Activity was mis-filed in
  `core`). Resolved with the same contract pattern: `AppEntryPoint` (`fun focusModeIntent(Context):
  Intent`) in `core/presentation/navigation/`, `AppEntryPointImpl` in `orchestration/navigation/`,
  Koin-bound; the service injects it instead of referencing `MainActivity`.

> **Deliberately left for later steps.** `core/presentation/util/LocalAppUpdate.kt` still types its
> locals with `feature/app_update`'s `AppUpdateState` (a **non-navigation** core → feature edge,
> consumed by `settings` + the bottom bar) — it belongs with the `app_update` work in Step 6 / a
> follow-up that promotes `AppUpdateState` to `core/domain`. The remaining `settings → library`
> (`LibraryVisibilitySettingsScreen` injecting `RefreshLibraryUseCase` + `LibraryTab`) and other
> non-screen cross-feature use-case edges are out of Step 5's navigation scope.

### Step 6 — Acknowledge `app_update`'s Android domain ☑

**No code change.** `feature/app_update/domain` is the only domain layer that imports Android types
(`AppUpdateRepository`, `StartAppUpdateFlowUseCase` need `Activity`/`AppUpdateManager`). This is
correct. Recorded here so it is a deliberate decision, not a surprise: under KMP this module's domain
stays in `androidMain`, and it must not be a target for `commonMain` extraction.

**Acknowledged (2026-06-01).** Re-ran the audit
(`grep -rlnE "^import (android\.|androidx\.|com\.google\.android\.play)" $(find . -type d -name domain)`):
exactly **two** domain files import Android types, both in `app_update`:

- `feature/app_update/domain/repository/AppUpdateRepository.kt`
- `feature/app_update/domain/usecase/StartAppUpdateFlowUseCase.kt`

The concrete imports are `androidx.activity.result.ActivityResultLauncher` and
`androidx.activity.result.IntentSenderRequest` — the in-app-update flow must hand the Play
`AppUpdateManager` an `ActivityResultLauncher` to surface the system update dialog, which is
irreducibly Android UI plumbing. **Decision: leave it.** This is the deliberate `androidMain` boundary
for this module; do not "purify" it into `commonMain`, and do not treat it as a layering violation in
future audits. Every *other* feature's `domain/` is Android-free and remains a `commonMain` candidate.

> **Not part of Step 6 — a separate follow-up.** The `core → feature` edge surfaced in Step 5
> (`core/presentation/util/LocalAppUpdate.kt` typing its composition locals with `app_update`'s
> `AppUpdateState`, consumed by `settings` + the bottom bar) is a distinct concern: it is resolved by
> **promoting the pure `AppUpdateState` enum to `core/domain/model`**, not by anything in this step.
> Tracked for a later cleanup; it does not affect the `androidMain` decision above.

---

## 3b. The second-wave refactors (Steps 7–18, landed 2026-06-01)

A fresh audit before the mechanical split found the post-Step-6 graph was **not** the clean DAG §3
promised: the §4 script greps only `feature → feature`, so it missed four `core → feature` cycles and
a whole second wave of kernels disguised as features. Steps 7–18 closed all of it. Each was
independently shippable, build-green, and package-only (no behavioural change except the trending
relocation and the session-launcher inversion noted below).

### Step 7 — Room schema ownership → `core:database` ☑
**Moved** the DAOs+entities for the shared/cross-joined tables (`deadlines`, `connectivity`,
`explore`, `personal`) into `core/data/database/{dao,model}`. `SoftcoverDatabase` and `BookDao` (which
SQL-joins the deadline table) now reference them intra-`core`. Each feature's *data source* stays put
and injects the now-`core` DAO downward — the `BooksLocalDataSource → BookDao` precedent. **No Room
migration** (package-only; same tables/columns, `books.db`, `@Database(version = 40)`). Cleared the two
hardest `core → feature` blockers.

### Step 8 — Promote shared models → `core/domain/model` ☑
`BookDeadline`, `DeadlineProgress`, `DeadlineStatus`, `DeadlineUnit`, `ReadingSession`,
`ReadingDayActivity` → `core/domain/model` (flat). Re-pointed the three core `Deadline*` components.
Cleared the `core/presentation → feature.deadlines` blocker.

### Step 9 — Extract `core:deadlines` ☑
Deadline repository + `Observe*/Set/Clear` use cases + data source + mapper + Koin module → `core/deadlines`.
`feature/deadlines` ceased to exist. Removed `library/reading/book_detail → deadlines`.

### Step 10 — Extract `core:personal` ☑
The reading-activity service (highlight/log/session repos + use cases + data; no presentation) →
`core/personal`. `feature/personal` ceased to exist. Removed `session → personal`.

### Step 11 — Split `profile`: service → `core:profile`, screen stays a feature ☑
`ProfileRepository`(+impl), `Observe/RefreshUserProfileData`, `ObserveRecentReadingActivity`,
`UserProfileData`/`Snapshot` → `core/profile` (own `profileModule`). `feature/profile` keeps
`presentation/**` (own `profileScreenModule`) and depends down. Removed `reading/book_detail → profile`.

### Step 12 — Fold explore trending into `core:book` ☑
`GetTrendingBooksUseCase` + a `fetchTrendingBooks()` method on `BooksRepository`/`BooksRemoteDataSource`
(reusing the existing `toBook` mapper and id-batch query) → `core/book`. The Explore *screen* +
search/continue-series stay in `feature/explore`. Removed `reading → explore`.

### Step 13 — Move `connectivity` infra → core ☑
Repo/queue impls + sync → `core/connectivity` (contracts already in `core/domain/connectivity`);
`ConnectivityBanner` + `OfflineGuard` (`OfflineScreenContent`/`rememberIsOnline`) →
`core/presentation/component`. `feature/connectivity` ceased to exist. Removed `book_detail/explore →
connectivity`.

### Step 14 — Promote `ActiveSessionController` → `core/presentation/session` ☑
The app-scoped controller (+ `ActiveSession`, `formatSessionElapsed`) → core. Its one feature coupling
— starting `ReadingSessionService` — was **inverted** with a `ReadingSessionLauncher` contract in core
(impl `ReadingSessionLauncherImpl` in `feature/session`, Koin-bound), which also removed Android
`Context` from the controller. `SessionPeekBar`/`FocusModeScreen`/`ReadingSessionService` stay in
`feature/session`. Removed `reading → session`.

### Step 15 — `MainActivityViewModel` stays in `core` (deviation from the original plan) ☑
The plan said relocate it to orchestration. Investigation showed it is consumed by **two features**
(`profile`, `onboarding`, signal-only `setUserAuthenticated`) and the shell — so by this repo's own
core litmus (a type imported by ≥2 features is `core`) it belongs in `core`. Its *only* feature
dependency was `RefreshLibraryUseCase`, which Step 17 moves to `core:library`; once that landed the VM
coordinates **only core services** and is a legitimate `core/presentation` type. No relocation, no
orchestration `UserAuthenticationNotifier` indirection needed. (Cleared `core → feature` blocker #2.)

### Step 16 — Promote `AppUpdateState` + `AppUpdateSimulator` → core ☑
`AppUpdateState` (pure sealed state) → `core/domain/model`; the `AppUpdateSimulator` **contract** →
`core/domain/appupdate` (debug/release impls stay in `feature/app_update`). Re-typed `LocalAppUpdate`;
`settings` imports from core. The Play-`AppUpdateManager`-bound repository + flow stay in
`feature/app_update` (the sanctioned androidMain domain). Cleared `core → feature` blocker #4 and
removed `settings → app_update`.

### Step 17 — Extract `core:library`; promote `LibraryTab` ☑
`RefreshLibraryUseCase` (deps all core) → `core/library` (own `libraryServiceModule`), covering both
`reading → library` and the `settings → library` use-case edge. The shared sealed `LibraryTab`
(consumed by `library` + the settings library-visibility screen) → `core/presentation/model`. The
library-visibility screen stays in `settings` and reaches `library` only through the Step-5
`AppNavigator` contract, so `settings → library` is fully gone.

### Step 18 — Final audit ☑
All three greps clean: `feature → feature` **empty** (not just `:app`/`book_detail` downward — *zero*
sibling edges, so `book_detail`/`reading` are clean leaves and no T2 tier is needed), `core → feature`
**empty**, `feature → orchestration` **empty**. Full unit suite green. Docs updated
(`MODULE_STRUCTURE_GUIDELINES.md`, `DESIGN_SYSTEM.md`).

> **BSD-`sed` foot-gun, recorded.** macOS `sed` does not support `\b`; word-boundary remaps silently
> no-op. Two class-specific remaps (`ReadingSession`, `ReadingDayActivity`, `ActiveSession`) hit this
> and were caught + corrected by per-step compilation. Use exact-string FQN remaps, and after any move
> verify both the `package` declaration *and* the file's physical directory match.

---

## 4. Verification between steps

After each step, re-run the cross-feature import audit and confirm the targeted edge is gone and no
new cycle appeared:

```bash
cd app/src/main/java/nl/rhaydus/softcover
for f in feature/*/; do
  name=$(basename "$f")
  grep -rhoE "import nl\.rhaydus\.softcover\.feature\.[a-z_]+" "$f" 2>/dev/null \
    | sed -E 's/.*feature\.//' | grep -v "^${name}$" | sort | uniq -c \
    | while read c t; do echo "  $name -> $t ($c)"; done
done
```

The exit criterion for the whole of §3 is: **the only cross-feature edges that remain point from
`:app` and `:feature:book_detail` downward** — i.e. the graph is a DAG that matches the tiers in §2.

---

## 5. The mechanical split (after §3 is green)

Only once the audit is acyclic and core-pointing:

1. Create `:core:*` and `:feature:*` modules; add a `build.gradle.kts` per module (a convention
   plugin in `build-logic/` keeps these uniform).
2. Move each package folder into its module unchanged; fix `package`/`import` paths.
3. Wire `include(...)` in `settings.gradle.kts` and declare module dependencies following the tier
   order in §2 — never upward.
4. Split each feature's Koin module into a per-module `module { }`, aggregated at `:app`.

No business logic changes in §5 — it is folder moves plus build files. The hard thinking is all in
§3.

---

## 6. What this sets up (KMP, later — out of scope here)

This plan deliberately stops at an Android multi-module structure. It is shaped so the later KMP
conversion is a per-module `commonMain`/`androidMain` source-set split rather than a re-architecture:
`core/domain`, `core/preferences`, `core/identity`, and `core/book` are the natural first
`commonMain` candidates; `app_update`, `scan`, notification/WorkManager code, and Play in-app updates
stay `androidMain`. That migration is tracked separately and is not part of this document.
