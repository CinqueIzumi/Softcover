# Architecture

Softcover is a native Android client for [Hardcover.app](https://hardcover.app/), built with Kotlin and Jetpack Compose. It follows **Clean Architecture** principles with a custom state management framework called **TOAD**.

## Project Structure

The app is a **multi-module Gradle build**. Modules depend only on lower tiers
(`:app → :orchestration → :feature:* → :core:*`); the tier rules, full module roster, and build-setup
conventions live in [MODULE_STRUCTURE_GUIDELINES.md](MODULE_STRUCTURE_GUIDELINES.md). Each module's
source lives under `<module>/src/main/java/nl/rhaydus/softcover/…` with the package matching its
namespace.

```
:app                  # Application shell: SoftCoverApp, launcher manifest + resources, Koin startup
:orchestration        # Nav host (MainActivity, RootScreen, bottom bars), AppNavigator / AppEntryPoint
                      #   impls, cross-feature orchestration use cases, the softcoverModules aggregate
:feature:*            # Leaf features: lists, profile, onboarding, explore, library, book_detail,
                      #   reading, session, scan, settings, app_update
:core:domain          # Shared domain models, classification enums, config value types, use-case contracts
:core:database        # Room database, migrations, all persisted entities + DAOs
:core:network         # Apollo GraphQL client, interceptors, safeQuery / safeMutation
:core:designsystem    # TOAD framework, Material 3 theme, reusable Compose components, nav contract,
                      #   shared presentation models, app-scoped session controller, MainActivityViewModel
:core:platform        # Logging, notifications, permission infrastructure
:core:preferences     # SettingsRepository, preference readers, DataStore-backed impl
:core:identity        # User identity / auth-credential use cases
:core:{book,lists,deadlines,personal,profile,library}   # Operation services (repository + use cases)
:core:connectivity    # Offline write-queue / sync infrastructure
```

## Layered Architecture

Each `:feature:*` module is organized into three layers (the same `domain`/`data`/`presentation`
split also structures the `:core:*` operation modules):

```
feature/<name>/src/main/java/nl/rhaydus/softcover/feature/<name>/
├── data/                              # Data layer
│   ├── dao/                           # Room DAO interfaces
│   ├── datasource/                    # Local and Remote data sources
│   ├── datastore/                     # Proto DataStore for key-value storage
│   ├── model/                         # Data layer entities
│   ├── mapper/                        # Entity <-> Domain model mappers
│   └── repository/                    # Repository implementations
├── domain/                            # Domain layer
│   ├── repository/                    # Repository interfaces
│   ├── usecase/                       # Business logic use cases
│   └── model/                         # Domain models
├── presentation/                      # Presentation layer
│   ├── action/                        # UiAction implementations
│   ├── event/                         # UiEvent definitions
│   ├── flows/                         # Initializers (data collectors)
│   ├── screen/                        # Composable screens
│   ├── screenmodel/                   # ScreenModel with dependencies
│   └── state/                         # UiState and LocalVariables
└── di/                                # Koin module for feature DI
```

### Layer Rules

- **Domain** depends on nothing. It defines repository interfaces and use cases.
- **Data** implements domain interfaces. It owns entities, mappers, and data sources.
- **Presentation** depends on domain (via use cases). It never accesses data layer directly.

### Placing a new type in the correct layer

When introducing a new class, enum, interface, or value object, classify it deliberately before choosing a package — don't default to the layer where the first consumer happens to live.

Ask, in order:

1. **Does it encode a business rule or a state classification derived from domain data?** → `domain/model/` (either feature-local or `core/domain/model/`). Examples: status enums like `BookStatus`, `DeadlineStatus`; value objects computed from domain state; policy results. A classification like "on track / behind / expired" is domain even if only a `@Composable` renders it today.
2. **Does it represent a persisted row, DTO, or remote payload?** → `data/model/` (entities with `*Entity`, network DTOs with `*Dto`).
3. **Does it represent display-only concerns — colors, icons, composable argument shapes, routes, tabs?** → `presentation/component/` (shared) or `feature/<x>/presentation/...` (feature-local).

**Heuristic:** if the type could be consumed by a headless use case, a CLI, or a non-Compose client without losing meaning, it belongs in `domain` — even when today's only consumer is UI code. Attribute-bearing enums (e.g. one carrying a `label`) still belong in `domain` when the category itself is a business concept; presentation can layer additional mappings (colors, icons) on top at the call site.

**Derived state:** when a value is purely a function of other fields on a domain type, prefer a computed property on the type itself (e.g. `val DeadlineProgress.status`) rather than a free-standing extension file — it keeps the derivation co-located with the data it depends on.

**Why it matters:** filing a domain-shaped concept under `presentation/` forces downstream code to either invert the dependency direction or duplicate the concept. Once a concept leaks into the wrong layer, relocating it later is a cascading import refactor.

## TOAD State Management

TOAD is a custom state management framework built on Voyager's `ScreenModel`. Each screen has these components:

| Component | Purpose |
|-----------|---------|
| **UiState** | Immutable data class representing UI state, exposed as `StateFlow` |
| **UiEvent** | One-time events sent via `Channel` (e.g. navigation, toasts) |
| **UiAction** | Sealed interface; each action encapsulates a user interaction handler |
| **LocalVariables** | Mutable state that doesn't affect the UI (e.g. coroutine `Job` tracking) |
| **ActionDependencies** | Container for injected use cases and dispatchers |
| **Initializers** | Flow collectors that launch on screen creation and update state reactively |

### Data Flow

```
User Interaction
       │
       ▼
   UiAction.execute()
       │
       ├── reads current state
       ├── calls use cases via Dependencies
       │
       ▼
   ActionScope.setState()  ──►  StateFlow emits  ──►  Compose recomposes
       │
       └── ActionScope.sendEvent()  ──►  One-time event consumed by UI
```

### Reactive Data Collection

Initializers launch when the ScreenModel is created. They collect `Flow`s from repositories and update the UI state reactively:

```
Repository Flow  ──►  Initializer.collect()  ──►  setState()  ──►  UI updates
```

### Implementation Reference

TOAD lives in `core/presentation/toad/`, one type per file (`UiState.kt`, `UiEvent.kt`, `LocalVariables.kt`, `ActionDependencies.kt`, `UiAction.kt`, `Initializer.kt`, `ActionScope.kt`, `ToadScreenModel.kt`). `ToadScreenModel` has five generic parameters: `S : UiState`, `E : UiEvent`, `D : ActionDependencies`, `F : Initializer<S, E, D, V>`, `V : LocalVariables`.

#### Core contracts

- `interface UiState` / `interface UiEvent` / `interface LocalVariables` — marker interfaces.
- `abstract class ActionDependencies` — holds `coroutineScope: CoroutineScope` and `mainDispatcher: CoroutineDispatcher`; `fun launch(block)` launches on `coroutineScope + mainDispatcher`.
- `interface UiAction<D, S, E, V> { suspend fun execute(dependencies: D, scope: ActionScope<S, E, V>) }`.
- `interface Initializer<S, E, D, V> { suspend fun onLaunch(scope: ActionScope<S, E, V>, dependencies: D) }`.
- `class ActionScope<S, E, V>(stateFlow, localVariablesFlow, eventChannel)` exposes `currentState`, `currentLocalVariables`, `setState { reducer }`, `setLocalVariables { reducer }`, `sendEvent(event)` (uses `Channel.trySend`).

#### Base screen model (`ToadScreenModel.kt`)

```kotlin
abstract class ToadScreenModel<S, E, D, F : Initializer<S, E, D, V>, V>(
    initialState: S,
    initialLocalVariables: V,
    private val initializers: List<F>,
) : ScreenModel {
    protected abstract val dependencies: D
    val state: StateFlow<S>
    val localState: StateFlow<V>
    val events: Flow<E>               // Channel.BUFFERED → receiveAsFlow
    val scope: ActionScope<S, E, V>   // new instance per getter call — never cache
    protected fun dispatch(action: UiAction<D, S, E, V>)  // launches in screenModelScope
    protected fun startInitializers()                      // each collector runs via dependencies.launch
}
```

#### Per-feature boilerplate

For a feature `foo`, create these files under `feature/foo/presentation/`:

1. **`state/FooUiState.kt`** — `data class FooUiState(...) : UiState`.
2. **`state/FooLocalVariables.kt`** — `data class FooLocalVariables(...) : LocalVariables`.
3. **`event/FooEvent.kt`** — `sealed interface FooEvent : UiEvent`.
4. **`screenmodel/FooDependencies.kt`**:
   ```kotlin
   data class FooDependencies(
       val someUseCase: SomeUseCase,
       override val coroutineScope: CoroutineScope,
       override val mainDispatcher: CoroutineDispatcher,
   ) : ActionDependencies()
   ```
5. **`action/FooAction.kt`** — `sealed interface FooAction : UiAction<FooDependencies, FooUiState, FooEvent, FooLocalVariables>`. **One type per file.** This file contains *only* the sealed interface — no concrete action implementations.
6. **`action/OnXxxAction.kt`** — concrete actions implementing `execute(dependencies, scope)`; call use cases via `dependencies.<useCase>(...)`, update state via `scope.setState { it.copy(...) }`, emit events via `scope.sendEvent(...)`. **Each concrete action lives in its own file** named after the action class (`OnRefreshAction.kt`, `OnSaveAction.kt`, etc.). Never group multiple actions — or the sealed interface and its implementations — into a shared file.
7. **`flows/FooInitializer.kt`** — `sealed interface FooInitializer : Initializer<FooUiState, FooEvent, FooDependencies, FooLocalVariables>`.
8. **`flows/XxxCollector.kt`** — concrete initializers collecting repository flows (e.g. `dependencies.useCase().collectLatest { scope.setState { ... } }`).
9. **`screenmodel/FooScreenModel.kt`**:
   ```kotlin
   class FooScreenModel(
       private val someUseCase: SomeUseCase,
       appDispatchers: AppDispatchers,
       flows: List<FooInitializer>,
   ) : ToadScreenModel<FooUiState, FooEvent, FooDependencies, FooInitializer, FooLocalVariables>(
       initialState = FooUiState(),
       initialLocalVariables = FooLocalVariables(),
       initializers = flows,
   ) {
       override val dependencies = FooDependencies(
           someUseCase = someUseCase,
           coroutineScope = screenModelScope,
           mainDispatcher = appDispatchers.main,
       )

       init {
           startInitializers()
       }

       fun runAction(action: FooAction) = dispatch(action)
   }
   ```

#### Koin wiring per feature

- `factory { FooScreenModel(..., flows = getAll()) }` — `getAll()` aggregates every binding declared as `bind FooInitializer::class`.
- `factory { XxxCollector() } bind FooInitializer::class` — one line per initializer.
- Use cases: `factory { ... }`. Repositories / data sources: `single { ... }`.

#### Invariants

- Actions execute on the `Main` dispatcher (via `ActionDependencies.launch`); use cases switch to IO internally when needed.
- `scope` on `ToadScreenModel` is recreated per access — always read fresh, never cache.
- TOAD is per-Voyager-screen only. `MainActivityViewModel` is a plain `ViewModel`, not TOAD.

## Dependency Injection

Koin is used for DI. Each feature defines its own Koin module that provides:

- Data sources (local and remote)
- Repositories (bound to domain interfaces)
- Use cases
- ScreenModels (injected via `koinScreenModel`)

Each Gradle module owns one Koin `module { }`. `:orchestration` aggregates them all into the `softcoverModules` list, and `:app`'s `SoftCoverApp` starts Koin with `modules(softcoverModules + appModule)`. Shared dependencies (database, Apollo client, dispatchers) come from their owning `:core:*` modules.

## Navigation

Voyager handles navigation with two patterns:

- **Navigator**: Standard push/pop screen stack for flows like onboarding vs. main content.
- **TabNavigator**: Bottom bar navigation for main feature tabs (Reading, Library, Explore, Settings).

Authentication state determines the root screen:

```kotlin
Navigator(screen = if (authenticated) RootScreen else OnboardingScreen)
```

### Cross-feature navigation: the `AppNavigator` contract

A feature must **never** import another feature's `Screen` or `Tab` class — that is a horizontal
coupling the module split rejects. Instead, cross-feature navigation goes through the
`AppNavigator` contract in `core/presentation/navigation/`:

```kotlin
interface AppNavigator {
    fun screen(destination: ScreenDestination): Screen   // BookDetail(id, …), CreateList, Profile, …
    fun tab(destination: TabDestination): Tab             // READING, LIBRARY, EXPLORE, SETTINGS
}
```

A feature injects it with `koinInject<AppNavigator>()` and keeps control of *how* it navigates
(`navigator.push`, `navigator.parent?.push`, `tabNavigator.current = …`); the contract only resolves
*what* to navigate to. The single implementation, `AppNavigatorImpl`, lives in the **orchestration
tier** (`orchestration/navigation/`) — the only place allowed to depend on every feature's
`Screen`/`Tab` — and is bound `single<AppNavigator>` in `orchestrationModule`. A feature adding a
new externally-reachable surface adds a `ScreenDestination`/`TabDestination` case and wires it in
`AppNavigatorImpl`, never an import in the calling feature.

`AppEntryPoint` (same package) is the analogous contract for non-Compose deep links: it builds
`Intent`s targeting the launcher Activity (e.g. a notification opening Focus Mode) so a feature need
not reference `MainActivity`.

### The app shell lives at the orchestration tier

The navigation host — `MainActivity`, `RootScreen`, `BottomBarScreen`, and the bottom bars — lives in
the **`:orchestration`** module, **not** `core`. It composes feature tabs and screens, so it depends
*down* on features (legal); its manifest contributes the launcher `MainActivity`, merged into `:app`.
`core` only owns the reusable pieces the shell and features both consume (theme, components, and the
composition locals `LocalThemeConfiguration` / `LocalBottomBarPadding` / `LocalAppUpdateState`). Do not
move host/shell code back into `core`.

## Network Layer

- **Apollo GraphQL** communicates with the Hardcover API.
- `safeQuery()` and `safeMutation()` extension functions wrap Apollo calls with error handling. `safeQuery` takes an optional `FetchPolicy` (defaults to `NetworkOnly`); a `safeQueryFlow` variant exists for `CacheAndNetwork` rendering.
- An in-memory normalized cache is configured on `ApolloClient` (10 MiB), keyed by `@typePolicy` declarations on entity types in `core/network/src/main/graphql/extra.graphqls`. Session-stable queries (book detail, editions, reviews, series lookups, books-by-ids hydration) are served `CacheFirst` for instant revisits. Lists that should refresh on screen entry stay on `NetworkOnly`. Mutations write through the cache automatically — Room remains the source of truth for user-book state, so Apollo cache writes on `user_books` rows are currently inert observers.
- Network interceptors handle authentication headers.
- Apollo errors are wrapped in `RuntimeException` with descriptive messages.

## Local Storage

- **Room**: Relational data (books, user books, editions) with migration support.
- **DataStore**: Simple key-value preferences (app settings, search history).

## Dispatchers

An `AppDispatchers` abstraction provides `Main`, `IO`, and `Default` dispatchers via DI, enabling testability.
