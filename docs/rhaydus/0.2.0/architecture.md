# Architecture

This is the canonical architecture guide for the `nl.rhaydus` family of Kotlin / Jetpack Compose
apps. The apps follow **Clean Architecture** principles with a custom presentation framework called
**TOAD**. This doc covers the layering, the module / package structure, and the dependency-direction
rules. TOAD itself is documented separately in [`toad-architecture.md`](toad-architecture.md).

The apps share one architecture but ship in two physical shapes:

- A **multi-module Gradle build** (`:app -> :orchestration -> :feature:* -> :core:*`), where the
  Clean Architecture layering is the *internal* structure of each module and the tier graph is the
  *cross-module* dependency rule.
- A **single Gradle module** (`:app`) whose **package tree** under `nl.rhaydus.<app>` mirrors the
  exact same `core/feature` structure. Here the package boundary plays the role the module graph
  would play in a larger build.

The multi-module shape is the **canonical target**: it makes every rule below mechanically
enforceable (a wrong dependency fails to compile or fails a Gradle check). A single-module app is the
same architecture expressed in packages, deliberately laid out so that a later extraction into Gradle
modules is a move-and-rename, not a re-architecture. Throughout this doc, read "module" as "module
or its mirror package" unless a section is explicitly about the Gradle-module axis.

---

## 1. The two axes

Every file is classified on two independent axes.

| Axis | Question it answers | Values |
|------|---------------------|--------|
| **Layer** | *What kind of code is this?* | `domain` / `data` / `presentation` / `di` |
| **Tier** | *Who is allowed to depend on it?* | `core` / `feature` / `orchestration` |

Layer governs the *internal* structure of a module (or mirror package). Tier governs *cross-module*
dependencies. The two are orthogonal: a `domain` use case can live in a `core` module or a `feature`
module. The layer is the same; the tier (and therefore who may import it) is not. Every type has both
a layer **and** a tier, and both are chosen deliberately, never inherited from wherever the first
caller happens to sit.

These are package-level rules first and module-level rules second. They hold whether a concept is
already its own Gradle module or still a package awaiting extraction.

---

## 2. Layered architecture

Every feature and every `core/*` operation service is organised into **data / domain / presentation**
layers. A layer holds its own sub-folders for its component types. Create only the layers and folders
a given area actually needs: a presentation-only feature has just `presentation/`; a UI-less
operation service has just `data/` + `domain/`.

```
<feature-or-core-area>/
├── domain/                 # Pure business layer - depends on nothing
│   ├── model/              # Domain models, state classifications, value types
│   ├── repository/         # Repository interfaces (contracts)
│   └── usecase/            # Business-logic use cases (added when there is real logic)
├── data/                   # Implements the domain contracts
│   ├── model/              # DTOs (*Dto / *Response), entities (*Entity)
│   ├── datasource/         # *RemoteDataSource / *LocalDataSource - interface + Impl colocated
│   ├── repository/         # Repository implementations (*RepositoryImpl)
│   └── mapper/             # Data <-> domain mappers
├── presentation/           # TOAD - see toad-architecture.md
│   └── state/ event/ action/ collector/ screenmodel/ screen/
└── di/                     # The area's Koin module
```

### Layer rules

- **Domain** depends on nothing. It owns domain models and the repository interfaces (contracts).
- **Data** implements the domain interfaces. It owns DTOs, entities, data sources, mappers, and
  `*RepositoryImpl`. Data may reference domain, never the reverse.
- **Presentation** depends on **domain** only, and reaches it through **use cases**, never a
  repository or data-source type directly. A use case may be a thin pass-through to a repository
  today and grow logic later without touching its callers.

The `domain` / `data` / `presentation` package boundary is intentionally clean: it is the natural
seam along which a module would later be split (for example into separate Gradle modules, or into
shared / platform source sets). Keep platform-only or framework-only types out of `domain` packages
so that seam stays clean.

### Contract-first repositories and data sources

Both repositories **and** data sources are contract-first: the interface is the swappable seam, the
`*Impl` is one realisation. Conventions:

- A data source's interface and its `*Impl` live in the **same file**, named after the interface
  (for example `RemoteThingDataSource.kt` holds both `RemoteThingDataSource` and
  `RemoteThingDataSourceImpl`).
- The repository **interface** lives in `domain/repository/`; its `*RepositoryImpl` lives in
  `data/repository/`. They are split across layers because the interface is the domain contract.
- DI always binds the **interface** (`single<ThingRepository> { ThingRepositoryImpl(...) }`,
  `single<ThingLocalDataSource> { ThingLocalDataSourceImpl(...) }`), so swapping a remote / local
  source is a one-line module change with no consumer edits.

### Error handling across layers

The architecture does **not** assume repositories and data sources cannot crash. The error model is
fixed by the layer a type sits in.

- **Data sources / repositories throw.** When a network, IO, or parsing operation fails, the
  exception propagates. They do not degrade a real failure into a sentinel return value, and they do
  not run failure-policy side effects (clearing bad state, retry-and-give-up). The only
  `try` / `catch` / `runCatching` allowed inside this layer encodes a *deliberate business decision*
  (for example a repository falling back to a placeholder result when a secondary fetch fails so a
  valid primary result is not dropped). Catching purely to hide an error from the caller is a smell.
- **Use cases return `Result<T>` and own failure policy.** A use case wraps its repository call in a
  cancellation-aware `runCatching` helper and returns `Result<T>`, never the bare domain type. The
  helper is `runCatching` that rethrows `CancellationException`, so structured-concurrency
  cancellation is never captured as a `Result.failure`. This is the seam where "repositories throw"
  becomes "callers get a value-typed outcome". Cross-operation reactions to a failure live here, not
  in the repository (for example a use case dropping unusable cached credentials when a dependent
  fetch fails, then still returning the original failure).
- **Presentation (TOAD actions) folds the `Result`.** Actions call the use case and unpack with
  `.onSuccess { }` / `.onFailure { }` (not `.fold()`), translating success and failure into UI state
  through the action scope. Because the use case already turned the throw into a `Result`, actions
  hold no `try` / `catch` of their own. See [`toad-architecture.md`](toad-architecture.md) for the
  worked action pattern.

### Placing a new type in the correct layer

Classify the type before choosing a package. Do not default to where the first consumer lives. Ask,
in order:

1. **Does it encode a business rule or a state classification derived from domain data?** ->
   `domain/model/`. A classification such as "loading / disconnected / connected" or
   "on track / behind / expired" is domain even though only a `@Composable` renders it today.
2. **Does it represent a persisted row, a DTO, or a remote payload?** -> `data/model/`
   (`*Entity`, `*Dto` / `*Response`).
3. **Is it display-only (colors, icons, composable argument shapes, routes, tabs)?** ->
   `presentation/`.

**Heuristic:** if a headless use case, a CLI, or a non-Compose client could consume the type without
losing meaning, it belongs in `domain`, even when today's only consumer is UI code. Attribute-bearing
enums (for example one carrying a `label`) still belong in `domain` when the category itself is a
business concept; presentation can layer additional mappings (colors, icons) on top at the call site.

**Derived state:** when a value is purely a function of other fields on a domain type, prefer a
computed property on the type itself (for example `val ThingProgress.status`) over a free-standing
extension file. It keeps the derivation co-located with the data it depends on.

**Why it matters:** filing a domain-shaped concept under `presentation/` forces downstream code to
either invert the dependency direction or duplicate the concept. Relocating it later is a cascading
import refactor.

---

## 3. Tiers and the module / package structure

Modules (or their mirror packages) are organised into tiers. **A module may depend only on modules in
a lower tier - never sideways within the feature tier, never upward.** This is the single rule that
keeps the dependency graph an acyclic DAG and the build splittable.

```
┌─────────────────────────────────────────────────────────────┐
│  T3  :app            thin application shell (Application +     │  depends on :orchestration
│                      manifest + Koin startup)                 │  (+ a few core modules it touches)
│      :orchestration  navigation host + cross-feature          │  may depend on everything below
│                      orchestration use cases + Koin aggregate │
├─────────────────────────────────────────────────────────────┤
│  T2  :feature:<aggregator>  features that compose several     │  may depend on T1, T0
│                             other features                    │
├─────────────────────────────────────────────────────────────┤
│  T1  :feature:*      leaf features (one user-facing surface)  │  may depend on T0 only
│                                                               │  NEVER on a sibling feature
├─────────────────────────────────────────────────────────────┤
│  T0  :core:*         shared kernels: domain model, operation  │  may depend only on other T0
│                      services, preferences, identity,         │
│                      design system, network, storage          │
└─────────────────────────────────────────────────────────────┘
```

In a single-module app, the same tiers exist as packages under `nl.rhaydus.<app>`:

```
nl/rhaydus/<app>/
├── core/            # Shared infrastructure + operation services consumed by features
│   ├── network/         # Client factory / API plumbing
│   ├── presentation/    # TOAD framework, theme, shared widgets, dispatchers, nav shell, nav contract
│   ├── common/          # Cross-cutting helpers (e.g. the cancellation-aware runCatching)
│   └── <operation>/     # UI-less operation services (data + domain), one folder each
└── feature/         # Leaf features, one folder each (each with its own data/domain/presentation/di)
```

Map the single-module packages onto the tiers as follows: `core/*` packages are T0; `feature/*`
packages are T1 (a T2 aggregator package appears only if a feature genuinely composes another); the
app shell and any cross-feature orchestration play the T3 role. The orchestration tier may be a thin
set of packages rather than a separate module until it earns extraction.

### T0 - `core/*`

`core` is for code consumed by **two or more features** that carries no single feature's identity.
Split it into focused units, not one `core` grab-bag. Typical members:

| Unit | Holds |
|------|-------|
| `core:domain` | shared domain models, classification enums, config value types, and cross-feature use-case **contracts** whose impls live elsewhere |
| `core:<operation>` | an **operations service**: a repository plus the use cases every feature calls (and its data sources / mappers). One per shared capability |
| `core:network` | the API client / transport, interceptors, and the shared call-wrapping helpers |
| `core:<storage>` | the local persistence layer (database, migrations, all persisted entities + DAOs, or the key-value store) |
| `core:designsystem` (or `core/presentation`) | the TOAD framework, theme, reusable Compose components, modifiers, shared presentation models, the navigation contract, and any app-scoped controllers |
| `core:common` | cross-cutting helpers with no domain identity |

`core` modules may depend on each other **downward only**. `core:domain` depends on nothing.

**The litmus test for "is this `core`?":** *Would a second, unrelated feature reasonably import this
to do its job?* If yes, it is a kernel and belongs in `core`. A type imported by two or more features
is `core` even when it is a screen model or a presentation model. A "feature" that everyone imports is
not a feature; it is a kernel wearing a feature's folder.

**The vertical-slice rule (where a shared concept's pieces live).** A `core:<concept>` operations
unit owns only the **repository + use cases** (and its data sources / mappers). Its **domain models**
live in `core:domain` (the pure, shareable tier). Its **persisted entities + DAOs** live in the
shared storage unit (a single database must see every entity, and DAOs may join across them). Do not
try to make a concept unit self-contained with its own models and entities; that breaks the shared
model tier and the single storage layer.

### T1 / T2 - `feature/*`

A `feature` owns a **user-facing surface** (a screen or a coherent flow) and the domain / data /
presentation specific to it. It keeps the standard internal layout from section 2:

```
feature/<name>/
├── domain/         repository interfaces, use cases, feature-local models - depends on core only
├── data/           repository impls, data sources, entities, mappers
├── presentation/   screens, screenmodels, actions, events, collectors, state
└── di/             the feature's Koin module
```

Module-axis rules:

- **A leaf feature (T1) imports `core` only - never a sibling feature.** If you are about to write
  `import nl.rhaydus.<app>.feature.<other>` from a leaf, stop and apply one of the three resolutions
  below.
- **Feature-local stays feature-local.** A model, enum, or component used by exactly one feature
  lives in that feature, not in `core`. Promote to `core` only on the *second* real consumer, never
  speculatively.
- **An aggregator (T2)** may depend on the leaf features it composes, downward. It still may not be
  depended on *by* a leaf (navigate to it via the navigation contract instead).

When a leaf seems to need something from a sibling, exactly one of these is true and must be applied:

1. The shared thing is actually a kernel -> push it down to `core` (most common).
2. The feature is genuinely an aggregator of the other -> it moves to T2 and may depend on it.
3. The feature only needs to *navigate* to the other's screen -> use the navigation contract
   (section 5), not a screen import.

### T3 - `:orchestration` + `:app`

The T3 tier is the only one allowed to know about many features at once.

**`:orchestration`** (the library that composes everything) holds:

- The **navigation host** (the `Navigator` / `TabNavigator` setup, root and bottom-bar screens, and
  the navigation-contract implementations). Its manifest contributes the launcher Activity.
- **Cross-feature orchestration use cases** - logic that coordinates *several* features and cannot
  honestly live in any one of them (for example wiping user data across multiple features, or a
  launch-time initialisation that spans identity plus content sync).
- The **Koin aggregate**: the list of every module's `module { }`.

**`:app`** (the lone application shell) holds only the `Application` class, the launcher resources and
manifest `<application>` element, and the Koin startup
(`modules(<appName>Modules + appModule)`). It depends on `:orchestration` plus the few core modules
its Application touches directly.

If a use case must reach into two or more *features*, it is orchestration and belongs here, not in
whichever feature you happened to be editing. (If it reaches into two or more *core* modules only, it
can live in `core` instead; orchestration is specifically about coordinating *features*.)

**When a leaf feature must trigger orchestration, invert the dependency - never let a leaf import
`:app` or `:orchestration`.** A T1 screen model that needs to kick off a cross-feature flow depends on
a **contract interface in `core:domain`**; the feature-reaching implementation (`...UseCaseImpl`)
lives in `:orchestration` and is bound to the interface in the orchestration Koin module. The leaf
depends downward on the contract; `:orchestration` depends downward on the leaf. The graph stays a
DAG.

In a single-module app, the app shell, navigation host, and any cross-feature use cases play this T3
role from within `core/presentation` and the application class; keep cross-feature coordination out of
individual feature packages just the same.

---

## 4. Dependency injection

Koin. Each feature and each `core/*` operation service owns exactly **one** `module { }` (in its
`di/` folder), binding implementations to domain interfaces
(`single<ThingRepository> { ThingRepositoryImpl(...) }`). Conventions:

- Repositories and data sources are `single`. Use cases are `factory`. Screen models are `factory`
  (injected with `koinScreenModel`).
- All modules are aggregated into one list (`<appName>Modules`). In a multi-module app the
  `:orchestration` module owns the aggregate; in a single-module app a single aggregator file does.
  The `Application` starts Koin with `modules(<appName>Modules + appModule)`.
- Shared dependencies (storage, the API client, dispatchers) come from their owning `core/*` unit.

See [`toad-architecture.md`](toad-architecture.md) for the per-feature Koin wiring of screen models
and flow collectors (`getAll()` aggregation, `bind <Feature>Initializer::class`).

---

## 5. Navigation

Voyager. `TabNavigator` drives bottom-bar tabs; `Navigator` handles push / pop stacks. The nav shell
(root screen, bottom-bar screen, bottom bar) lives at the orchestration tier in a multi-module app, or
in `core/presentation` in a single-module app, because it composes feature tabs and screens and so
must depend *down* on features.

### The cross-feature navigation contract

A feature must **never** import another feature's `Screen` or `Tab` class - that is exactly the
horizontal coupling the tier split rejects. Cross-feature navigation goes through a contract that
lives in `core` (the design-system / presentation unit):

```kotlin
interface AppNavigator {
    fun screen(destination: ScreenDestination): Screen   // resolves "what" to navigate to
    fun tab(destination: TabDestination): Tab
}
```

A feature injects the contract (`koinInject<AppNavigator>()`) and keeps control of *how* it navigates
(`navigator.push`, `navigator.parent?.push`, `tabNavigator.current = ...`); the contract only resolves
*what* to navigate to. The single implementation lives at the **orchestration tier** - the only place
allowed to depend on every feature's `Screen` / `Tab` - and is bound `single<AppNavigator>` in the
orchestration Koin module. A feature adding a new externally reachable surface adds a
`ScreenDestination` / `TabDestination` case and wires it in the implementation, never an import in the
calling feature.

Any data passed across a navigation boundary is a **shared presentation model in `core`**, not a type
owned by either feature. For non-Compose deep links (for example a notification opening a screen), an
analogous `AppEntryPoint` contract builds intents targeting the launcher Activity so a feature need
not reference it directly.

### Pitfall: per-item nested navigators need a unique `key`

When a list-detail screen hosts the detail pane in its **own nested `Navigator`** (one per selected
item, e.g. the expanded pane of a two-pane layout - see design-system-foundations.md section 5.8), every
pane will render the **first** item selected and ignore later selections. Voyager caches a `Navigator` by
the position in the composition, and a bare `Screen` defaults its identity to its class name, so two
detail screens for different ids look identical to the cache and the stale one is reused. Give the detail
`Screen` an id-derived key:

```kotlin
class BookDetailScreen(val id: String) : Screen {
    override val key = "book-detail-$id"   // without this, every pane shows the first id
}
```

The symptom is unmistakable: selecting any row shows the detail of whichever row was selected first. Reach
for the explicit `key` on any `Screen` whose identity depends on a runtime argument rather than its type.

---

## 6. Dispatchers

An `AppDispatchers` abstraction provides `Main` / `IO` / `Default` dispatchers via DI, for
testability. TOAD actions run on `Main`; use cases, data sources, and repositories switch to `IO` for
network and disk work internally.

---

## 7. Build setup and convention plugins

Module build files are kept uniform by **Gradle convention plugins** in `build-logic/`. Apply the
smallest set that fits a module:

| Plugin | Apply to |
|--------|----------|
| `rhaydus.android.library` | base for an Android-only `core` / `feature` / `orchestration` module (SDK / JDK, the shared coroutines / Koin / logging runtime, and the test stack) |
| `rhaydus.android.compose` | any Android-only module with Compose UI |
| `rhaydus.kmp.library` | base for a multiplatform module |
| `rhaydus.kmp.compose` | a multiplatform module with Compose UI |

Apply the base library plugin plus the Compose plugin only on modules that actually have UI. The lone
application module uses the Android application plugin and owns build-type-conditional wiring. Do not
re-declare what a convention plugin already provides.

Other build conventions:

- **Each module declares the `project(...)` dependencies its own code imports** - never rely on a
  transitive dependency. A module whose *public API* exposes a type from a library declares that
  library `api` so consumers get it transitively; all other dependencies are `implementation`.
- **Manifests merge upward.** Library modules contribute their own components via their
  `AndroidManifest.xml`; the application module owns the `<application>` element, permissions, and
  launcher icons. Shared resources (strings, drawables, the theme) live in the design-system unit.
- **One Koin `module { }` per module**, aggregated as in section 4.

A single-module app has no module graph to gate, but the same package boundaries and the same
"declare what you import" discipline keep it ready for extraction. The multi-module shape additionally
gates the tier rules mechanically: a Gradle check derives each module's tier from its path and fails
the build on any `project(...)` dependency pointing sideways or upward.

---

## 8. Placing a new thing - decision flow

When adding a class, enum, screen, or use case, decide its **layer** (section 2) and its **tier**
(section 3).

1. **Is it a user-facing surface or surface-specific logic?** -> a `feature` (T1, or T2 if it
   composes other features). Pick the feature whose surface it serves.
2. **Will a second, unrelated feature import it to do its job?** -> `core` (T0). Choose the focused
   `core` unit by what it is: a domain model or enum -> `core:domain`; an operation -> the matching
   `core:<operation>` service; a reusable component / theme / TOAD / nav contract ->
   `core:designsystem`.
3. **Does it coordinate two or more *features*?** -> the orchestration tier (T3).
4. **Default:** keep it feature-local. Promote to `core` only when the second real consumer appears,
   never speculatively.

**Tie-breaker (`core` vs `feature`):** if the type could be consumed by a headless use case, a CLI,
or a second feature without losing meaning, it leans `core`. If it only makes sense in the context of
one screen, it stays in that feature. This mirrors the layer heuristic in section 2, applied to the
tier axis.

---

## 9. Review checklist

A change is structurally correct when:

- [ ] No leaf feature (T1) imports another feature.
- [ ] No module depends sideways or upward - only on lower tiers.
- [ ] A type imported by two or more features lives in `core`, not in a feature.
- [ ] Cross-feature navigation goes through the `core` contract, not a `Screen` import.
- [ ] Cross-feature coordination lives in the orchestration tier, not inside a single feature.
- [ ] Repositories / data sources throw; use cases return `Result<T>`; actions fold with
      `.onSuccess` / `.onFailure`.
- [ ] The new type's **layer** and **tier** were both chosen deliberately, not inherited from where
      the first caller happened to sit.

---

## 10. Naming and folder conventions

- Module path mirrors tier and name (`:core:domain`, `:core:<operation>`, `:feature:<name>`). **One
  Gradle module per feature, not one per layer.** The `domain` / `data` / `presentation` split stays
  as **packages inside** the module. A single-module app expresses the same split as
  `core.<unit>.<layer>` / `feature.<name>.<layer>` packages.
- Suffixes: `*Repository` / `*RepositoryImpl`, `*UseCase`, `*DataSource` / `*DataSourceImpl`,
  `*Entity`, `*Dto`, `*Screen`, `*ScreenModel`, `*Action`, `*Event`, `*UiState`, `*Dependencies`,
  `*LocalVariables`.
- One declaration per file. A data source's interface and its `*Impl` are the sanctioned exception
  (colocated in one file named after the interface).
- See [`code-style.md`](code-style.md) for the full naming, file-layout, comment, and Compose
  formatting rules, and [`toad-architecture.md`](toad-architecture.md) for the presentation pattern.
