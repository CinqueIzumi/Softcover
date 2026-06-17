# TOAD Architecture

TOAD is the `nl.rhaydus` apps' home-grown unidirectional (MVI-style) presentation architecture. Read
this before adding a feature so you can follow the pattern without reverse-engineering existing code.

The runtime is published as the **`nl.rhaydus:toad`** library (package `nl.rhaydus.toad`); any module
that builds UI depends on it. Every feature is built from the same five type parameters, threaded
through `ToadScreenModel<S, E, D, F, V>`:

| Param | Type | Role |
|-------|------|------|
| `S` | `UiState` | Immutable UI state rendered by the screen. Exposed as `StateFlow<S>`. |
| `E` | `UiEvent` | One-off side effects (navigation, snackbars). Delivered via a `Channel`, observed once. |
| `D` | `ActionDependencies` | Everything actions need: the coroutine scope, dispatchers, and injected repositories. |
| `F` | `Collector` | Long-lived initializers that start with the screen (e.g. subscribing to a repository flow). |
| `V` | `LocalVariables` | Transient, non-rendered state (in-flight ids, scratch values). Exposed as `localState`. |

## The building blocks

- **`UiState`**: a `data class` holding everything the screen draws. Default every field so the
  initial state is constructible with no args.
- **`LocalVariables`**: a `data class` for state the UI does *not* render but actions need to
  remember between dispatches. Keep it separate from `UiState` so recomposition isn't triggered.
- **`UiEvent`**: a `sealed interface`; each subtype is a fire-once effect. Empty until you need one.
- **`ActionDependencies`**: abstract base exposing `coroutineScope` + `mainDispatcher` and a
  `launch {}` helper. Each feature's concrete `XDependencies` adds injected collaborators
  (repositories, use-cases) as constructor `val`s.
- **`UiAction`**: `suspend fun execute(dependencies, scope)`. One action = one user intent or
  triggered effect. Implementations are usually `data object`s on a `sealed interface`.
- **`Collector`**: `suspend fun onLaunch(scope, dependencies)`. Runs once when the screen model is
  created (via `startInitializers()`), on `mainDispatcher`. Use it to `collect` a repository flow
  into state. Collectors are `object`s implementing the feature's `sealed interface XCollector`.
- **`ActionScope`**: the only way actions/collectors mutate things:
  - `setState { it.copy(...) }`: update UI state
  - `setLocalVariables { it.copy(...) }`: update transient state
  - `sendEvent(event)`: emit a one-off effect
  - `currentState` / `currentLocalVariables`: read current values
- **`ToadScreenModel`**: base `ScreenModel` (Voyager). Holds the flows, builds the `ActionScope`,
  runs `dispatch(action)` on `screenModelScope`, and runs `startInitializers()` for collectors.

## Data flow

```
Composable -- runAction(SomeAction) --> ScreenModel.dispatch --> Action.execute(deps, scope)
                                                                       |
                                         scope.setState { ... } <------|
                                         scope.sendEvent(...)   <------/
     ^                                               |
     +-------- state: StateFlow<S> (collectAsState) -+
```

Collectors push into the same `scope` from the side (e.g. a repository's `StateFlow` ->
`setState`), so external state changes flow into the UI without a user action.

## Anatomy of a feature

Each feature lives under `feature/<name>/` (or, in a single-module app, the mirror package
`feature.<name>`) with this layout:

```
feature/<name>/
├── di/<Name>Module.kt                     # Koin module: ScreenModel factory (collectors inline)
└── presentation/
    ├── state/<Name>UiState.kt             # data class : UiState
    ├── state/<Name>LocalVariables.kt      # data class : LocalVariables
    ├── event/<Name>Event.kt               # sealed interface : UiEvent
    ├── action/<Name>Action.kt             # sealed interface : UiAction<...>  (interface ONLY)
    ├── action/<Verb>Action.kt             # data object : <Name>Action  (one file PER action, *Action suffix)
    ├── collector/<Name>Collector.kt       # sealed interface : Collector<...>
    ├── collector/<Specific>Collector.kt   # object : <Name>Collector  (optional)
    ├── screenmodel/<Name>Dependencies.kt  # class : ActionDependencies()
    ├── screenmodel/<Name>ScreenModel.kt   # class : ToadScreenModel<...>
    └── screen/<Name>Screen.kt             # object : Screen  (render fn public, sub-parts private)
        screen/<Name>Tab.kt                # object : Tab  (only if it's a bottom-bar destination)
```

## Adding a new feature - checklist

1. **State**: `XUiState : UiState` and `XLocalVariables : LocalVariables`, all fields defaulted.
2. **Event**: `sealed interface XEvent : UiEvent` (leave empty until needed).
3. **Dependencies**: `class XDependencies(override val coroutineScope, override val mainDispatcher,
   /* injected repos */) : ActionDependencies()`.
4. **Action**: `sealed interface XAction : UiAction<XDependencies, XUiState, XEvent,
   XLocalVariables>` in `<Name>Action.kt`, containing **only the interface**. Each concrete action
   is a `data object` in its **own file**, named with the `Action` suffix (e.g. `RefreshAction.kt`),
   overriding `execute`, calling a use case via `dependencies.<useCase>()` and mutating via `scope`.
   Use cases return `Result<T>`; unpack it with `.onSuccess { }` / `.onFailure { }` (never `.fold()`)
   and fold both outcomes into state - actions carry no `try`/`catch` of their own. See
   [`architecture.md`](architecture.md#error-handling-across-layers). One action = one file (see
   "Conventions").
5. **Collector**: `sealed interface XCollector : Collector<XUiState, XEvent, XDependencies,
   XLocalVariables>`. Add `object`s that `collect` repository flows into `setState`.
6. **ScreenModel**: `class XScreenModel(appDispatchers, /* repos */, flows: List<XCollector>) :
   ToadScreenModel<...>(initialState, initialLocalVariables, initializers = flows)`. Build
   `dependencies` from `appDispatchers.main` + `screenModelScope`, call `startInitializers()` in
   `init`, expose `fun runAction(action) = dispatch(action)`.
7. **Screen**: `object XScreen : Screen`. In `Content()`, `koinScreenModel<XScreenModel>()`,
   `collectAsState()`, hand `state` + `screenModel::runAction` to a stateless render composable.
   **All of the screen's composables are members of the `object`.** The core render function
   `XScreen(state, runAction)` is public so `@Preview` can drive it; every sub-component composable
   is `private` (see "Conventions").
8. **DI**: `val xModule = module { factory { XScreenModel(get(), ..., flows =
   listOf(SomeCollector)) } }`. Register it in the app's aggregated `<App>Module.kt` (`<app>Modules`).
   **Pass collectors directly into the factory - do _not_ bind them as `single<List<XCollector>>`.**
   Generics are erased at runtime, so every feature's `List<...Collector>` collides under the same
   Koin key (`List`); the last one registered wins and gets handed to *every* screen model, which
   then casts the wrong feature's `Dependencies` and crashes.
9. **Navigation**: if it's a bottom-bar destination, add an `XTab : Tab` and list it in the shell's
   bottom navigation component.

## Conventions

- The screen model exposes exactly one public entry point: `runAction(action)`. UI never touches
  state directly.
- **One action per file.** `<Name>Action.kt` holds *only* the `sealed interface`; every concrete
  action is a `data object` in its own file implementing it. Adding behaviour = adding a file, never
  editing a shared one (open for extension, closed for modification). Kotlin allows a sealed type's
  implementations to live in separate files as long as they're in the same package and module.
- **All of a screen's composables are `private` members of the screen `object`.** That includes the
  stateless render function and every sub-component; only the `override fun Content()` is public.
  Keeping them private prevents composables from one screen leaking into and being reused by other
  screens (which couples screens and bypasses the screen model). If you need a preview, put the
  `@Preview` function inside the same `object`.
- Put cross-feature state (auth, session, anything more than one feature reads) in a `core`
  repository exposing a `StateFlow`, inject it through `XDependencies`, and mirror it into UI state
  with a collector - don't trap shared state inside a feature. An auth/session/account repository
  exposing a `StateFlow` is the typical example.
- `single` for things with one instance (repositories); `factory` for the `ScreenModel` so each
  screen gets a fresh one. Collectors are not registered in Koin at all - they're listed inline in
  the screen model's factory (see step 8).
