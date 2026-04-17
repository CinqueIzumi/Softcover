# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Softcover is a native Android client for [Hardcover.app](https://hardcover.app/), a book tracking platform. Built with Kotlin and Jetpack Compose, targeting SDK 26+ (Android 8.0).

## Build & Test Commands

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew test                   # Run unit tests
./gradlew :app:test              # Run unit tests for app module
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew lint                   # Run Android Lint
```

No ktlint or detekt is configured. The project uses `kotlin.code.style=official`.

## Code Style

Always follow the project's code formatting rules in [CODE_STYLE_GUIDE.md](CODE_STYLE_GUIDE.md). Read it before writing or modifying any Kotlin code in this repository — it is the source of truth for naming, layout, and whitespace conventions.

## Test Writing

ALWAYS delegate test writing to the `unit-test-writer` agent, regardless of how small or simple the task appears. Never write or modify unit tests directly in the main conversation — even for a single function, a one-line change, or a trivial assertion. This rule has no exceptions.

When the target is a whole package or directory (not a single file), the agent's brief must include: "audit existing test files in the target for coverage gaps and close them in the same pass." Do not run a separate audit round — gap-fills belong in the initial delegation.

When multiple independent files need tests, spawn unit-test-writers in parallel on disjoint file sets rather than sequentially in one agent.

The agent is required to run the tests after writing them. Prefer narrow filters (e.g. `./gradlew :app:testDebugUnitTest --tests "nl.rhaydus.softcover.feature.<name>.*"`) over the full suite. When relaying its report to the user:
- If all tests pass, mention that the suite was executed and passed.
- If any test fails, surface the failing test names and the agent's diagnosis to the user verbatim, then **stop** and wait for the user to approve any fixes. Do not delegate a fix round until the user has reviewed and authorized it.

## Architecture

The app follows **Clean Architecture** with a custom **TOAD** state management framework. Code lives under `app/src/main/java/nl/rhaydus/softcover/`.

### Core vs Feature

- `core/` — Shared infrastructure: Room database, Apollo GraphQL client, reusable Compose components, Material 3 theming, and the TOAD framework itself (`core/presentation/toad/`).
- `feature/` — Feature modules: `books`, `search`, `library`, `book_detail`, `onboarding`, `reading`, `profile`, `settings`. Each is self-contained.

### Layer Structure (per feature)

Each feature has three layers with strict dependency rules:

- **domain/** — Repository interfaces and use cases. Depends on nothing.
- **data/** — Repository implementations, data sources (local/remote), Room DAOs, entities, and mappers. Implements domain interfaces.
- **presentation/** — Screens, ScreenModels, actions, events, state. Depends on domain only (never data directly).
- **di/** — Koin module wiring up the feature's dependencies.

### TOAD State Management

Custom framework on Voyager's `ScreenModel`. Each screen has:

- **UiState** — Immutable data class exposed as `StateFlow`.
- **UiAction** — Sealed interface; each action handles a user interaction, receives dependencies and scope to read/update state.
- **UiEvent** — One-time events via `Channel` (navigation, toasts).
- **LocalVariables** — Mutable state not affecting UI (e.g. Job tracking).
- **ActionDependencies** — Container for injected use cases and dispatchers.
- **Initializers** (in `flows/`) — Flow collectors that launch on screen creation, collecting repository flows and updating state.

Data flow: `User → UiAction.execute() → use cases via Dependencies → setState() → StateFlow → Compose recomposes`.

### Key Patterns

- **Apollo GraphQL** for all API communication. Queries/mutations live in `app/src/main/graphql/`. Use `safeQuery()` / `safeMutation()` extension functions for error handling.
- **Room** for local book/user data caching with migrations in `core/data/database/`.
- **DataStore** for key-value preferences (settings, search history).
- **Koin** for DI. Each feature has its own module; top-level `di/` aggregates them.
- **Voyager** for navigation: `Navigator` for screen stacks, `TabNavigator` for bottom bar tabs.
- **AppDispatchers** abstraction provides Main/IO/Default dispatchers via DI.
- **Result\<T>** pattern with `.onSuccess()` / `.onFailure()` for error handling.
- **Timber** for logging (never `println` or `Log.*`).

### Naming Conventions

Domain models are plain nouns (`Book`, `Author`). Suffixes indicate layer/role: `*Entity` (data), `*DataSource`/`*DataSourceImpl`, `*Repository`/`*RepositoryImpl`, `*UseCase`, `*Screen`, `*ScreenModel`, `*Action`, `*Event`, `*UiState`, `*LocalVariables`, `*Dependencies`.

## Dependency Management

All versions are centralized in `gradle/libs.versions.toml`. Reference via version catalog (`libs.<alias>`) in `build.gradle.kts`.
