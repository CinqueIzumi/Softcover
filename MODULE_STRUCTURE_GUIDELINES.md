# Module Structure Guidelines

This document is the source of truth for **how code is categorized, grouped, and placed** across
Softcover's modules — what belongs in `core` vs a `feature` vs the orchestration tier, the allowed
dependency directions, and where a new type or screen goes. It is the *target* structure the codebase
is migrating toward; the migration path itself is in
[MODULARIZATION_PLAN.md](MODULARIZATION_PLAN.md).

It builds on [ARCHITECTURE.md](ARCHITECTURE.md) (Clean Architecture layers + TOAD). Layer rules
(`domain → data → presentation`) are unchanged and not repeated here — this doc is about the
*module* axis that sits above the layer axis. When the two are in tension, both must hold: every type
has a layer **and** a tier.

> Authored 2026-06-01. Whether the code physically lives in one Gradle module or many, these
> categorization rules apply — they are package-level rules first, module-level rules second.

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
│  T3  :app            navigation host + cross-feature          │  may depend on everything below
│                      orchestration use cases                  │
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
| `core:domain` | shared domain models, classification enums, config value types, and cross-feature use-case **contracts** whose impls live in `:app` | `Book`, `BookEdition`, `BookStatus`, `LibrarySortMode`, `SortDirection`, `DateStyle`, `LibraryGridLayout`; account-lifecycle contracts `ResetUserDataUseCase`, `InitializeUserIdAndBooksUseCase` (in `core/domain/account/`) |
| `core:book` | the book-**operations** service: repository + use cases every feature calls | `BooksRepository`, `MarkBookAsReadUseCase`, `RecordBookProgressUseCase`, `AddBookByIsbnUseCase` |
| `core:lists` | the list-**operations** service: repository + use cases consumed by library/book_detail/settings | `ListsRepository`, `GetAllUserListsUseCase`, `AddBookToListUseCase`, `SetEditionAsOwnedUseCase` |
| `core:preferences` | preference read/write contracts + value access + the DataStore-backed impl | `SettingsRepository`, `Get*AsFlowUseCase` readers, `AppSettingsDataStore`, `ApiKeyLocalDataSource` |
| `core:identity` | user identity / auth credential use cases | `GetUserIdUseCase`, `UpdateApiKeyUseCase` (storage lives in `core:preferences/data`) |
| `core:designsystem` | TOAD framework, theme, reusable components, modifiers, shared presentation models | `core/presentation/{toad, theme, component, model}` |
| `core:network` | Apollo client, interceptors, `safeQuery`/`safeMutation` | |
| `core:database` | Room database, migrations | |

**The litmus test for "is this `core`?":** *Would a second, unrelated feature reasonably import this
to do its job?* If yes, it is a kernel and belongs in `core`. A feature that everyone imports is not
a feature — it is a kernel wearing a feature's folder (this is exactly what `books` and `settings`
became; see the plan).

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

| Tier | Features |
|------|----------|
| T2 (aggregator) | `book_detail` |
| T1 (leaf) | `library`, `reading`, `explore`, `lists`, `deadlines`, `profile`, `scan`, `session`, `onboarding`, `personal`, `connectivity`, `settings`, `app_update` |

`settings` remains a T1 feature for its **screens** only — its shared value types, preference readers,
and identity use cases belong in `core` (`core:domain` / `core:preferences` / `core:identity`), and
its cross-feature orchestration belongs in T3 (see §5).

`lists` likewise remains a T1 feature for its **CreateList surface** only (`CreateListUseCase` +
`CreateListScreen`) — its repository and the operation use cases the rest of the app calls live in
`core:lists`, and the shared list UI (`ChooseListsBottomSheet`, `ListMembership`) in
`core:designsystem`. The remaining `* → lists` edges are `CreateListScreen` *navigation* imports,
resolved by the §6 navigation contract.

---

## 5. What goes in `:app` (T3, orchestration)

`:app` is the only tier allowed to know about many features at once. It holds:

- The **navigation host** (Voyager `Navigator` / `TabNavigator` setup, root screens, tab registry).
- **Cross-feature orchestration use cases** — logic that coordinates *several* features and cannot
  honestly live in any one of them. Examples: `ResetUserDataUseCase` (wipes data across library,
  lists, profile, identity), `InitializeUserIdAndBooksUseCase` (identity + book sync on launch),
  app-launch sync coordination.
- Top-level DI aggregation (`di/` combining every module's Koin module).

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
- Each module owns exactly one Koin `module { }`; `:app` aggregates them.
- The `domain`/`data`/`presentation` package boundary inside a feature is intentionally the future
  KMP source-set boundary (`commonMain`/`androidMain`). Keep Android-only types out of `domain`
  packages so that boundary stays clean — `app_update` is the one sanctioned exception (its domain is
  inherently Android, and stays `androidMain`).

---

## 9. Review checklist

A change is structurally correct when:

- [ ] No leaf feature (T1) imports another feature.
- [ ] No module depends sideways or upward — only on lower tiers.
- [ ] A type imported by ≥2 features lives in `core`, not in a feature.
- [ ] Cross-feature navigation goes through a `core` contract, not a `Screen` import.
- [ ] Cross-feature coordination lives in `:app` orchestration, not inside a single feature.
- [ ] The new type's **layer** (domain/data/presentation) and **tier** (core/feature/app) were both
      chosen deliberately, not inherited from where the first caller happened to sit.
