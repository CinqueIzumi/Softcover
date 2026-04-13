# Architecture

Softcover is a native Android client for [Hardcover.app](https://hardcover.app/), built with Kotlin and Jetpack Compose. It follows **Clean Architecture** principles with a custom state management framework called **TOAD**.

## Project Structure

```
app/src/main/java/nl/rhaydus/softcover/
├── core/                              # Shared modules used across features
│   ├── data/
│   │   ├── database/                  # Room database setup and migrations
│   │   └── network/                   # Apollo GraphQL client and interceptors
│   ├── domain/
│   │   ├── exception/                 # Custom exceptions
│   │   ├── model/                     # Shared domain models and enums
│   │   └── util/                      # Domain utilities
│   └── presentation/
│       ├── component/                 # Reusable Compose components
│       ├── modifier/                  # Custom Compose modifiers
│       ├── screen/                    # Root screens (MainActivity, RootScreen)
│       ├── state/                     # Shared UI states
│       ├── theme/                     # Material 3 theming
│       ├── toad/                      # TOAD framework implementation
│       ├── viewmodel/                 # Activity-level ViewModels
│       └── util/                      # Presentation utilities
├── di/                                # Top-level Koin DI modules
├── feature/                           # Feature modules
│   ├── books/                         # Book management (core feature)
│   ├── search/                        # Search functionality
│   ├── library/                       # User's library view
│   ├── book_detail/                   # Book detail screen
│   ├── onboarding/                    # Authentication flow
│   ├── reading/                       # Reading progress tracking
│   ├── profile/                       # User profile
│   └── settings/                      # App settings and theming
└── SoftCoverApp.kt                    # Application entry point
```

## Layered Architecture

Each feature is organized into three layers:

```
feature/<name>/
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

## Dependency Injection

Koin is used for DI. Each feature defines its own Koin module that provides:

- Data sources (local and remote)
- Repositories (bound to domain interfaces)
- Use cases
- ScreenModels (injected via `koinScreenModel`)

Top-level modules in `di/` aggregate feature modules and provide shared dependencies (database, Apollo client, dispatchers).

## Navigation

Voyager handles navigation with two patterns:

- **Navigator**: Standard push/pop screen stack for flows like onboarding vs. main content.
- **TabNavigator**: Bottom bar navigation for main feature tabs (Library, Search, Profile, Settings).

Authentication state determines the root screen:

```kotlin
Navigator(screen = if (authenticated) RootScreen else OnboardingScreen)
```

## Network Layer

- **Apollo GraphQL** communicates with the Hardcover API.
- `safeQuery()` and `safeMutation()` extension functions wrap Apollo calls with error handling.
- Network interceptors handle authentication headers.
- Apollo errors are wrapped in `RuntimeException` with descriptive messages.

## Local Storage

- **Room**: Relational data (books, user books, editions) with migration support.
- **DataStore**: Simple key-value preferences (app settings, search history).

## Dispatchers

An `AppDispatchers` abstraction provides `Main`, `IO`, and `Default` dispatchers via DI, enabling testability.
