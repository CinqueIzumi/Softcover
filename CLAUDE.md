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

## Design System

Always consult [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) before designing or modifying any UI surface — it is the source of truth for the app's visual and interaction language (color roles, editorial typography, layout primitives, components, patterns, decision rules).

**Maintenance rule (enforced by review).** Any change that introduces, retires, or alters a foundation, component, or pattern in the design system MUST update `DESIGN_SYSTEM.md` in the same change. The `code-reviewer` agent treats a design-system change without a corresponding doc update as a blocker. Examples that require a doc update: a new shared component under `core/presentation/component/`, a new editorial typography role, a new color role usage, a new layout pattern that other screens should adopt, retirement or renaming of any of the above. Localized tweaks to a single screen that don't change the system itself do not require an update.

## Code Style

Always follow the project's code formatting rules in [CODE_STYLE_GUIDE.md](CODE_STYLE_GUIDE.md). Read it before writing or modifying any Kotlin code in this repository — it is the source of truth for naming, layout, and whitespace conventions.

The repo has no ktlint/detekt configured, so style is enforced by review, not by tooling. Two-step compliance discipline:

**Before declaring small Kotlin edits done, run the self-check below.** "Small" = a localized edit to an existing file (a few lines, a new helper, a rename, a state-flag addition). The rules below are the ones that are easy to miss because they are not idiomatic Kotlin and are not flagged by the compiler:

- Boolean negation uses `.not()`, never `!` (e.g. `isLoading.not()`, not `!isLoading`).
- Every sibling composable inside a layout scope (`Column`, `Row`, `Box`, `LazyRow` content, etc.) is separated by a blank line — including `Spacer`.
- Multi-line constructs (multi-arg calls, multi-line `if`/`when`, mockk stubs, `coEvery { }`) are paragraphs: blank line before and after.
- A `Timber.e(...)` log is its own paragraph: blank line before the next statement.
- Imports are grouped androidx → third-party → project (`nl.rhaydus.*`) → kotlin/java, alphabetical within each group, no fully-qualified inline references.
- Multi-argument calls, declarations, and data-class instantiations break one-per-line with a trailing comma as soon as they have ≥2 arguments.
- Optional UI rows (rating, badge, etc.) inside fixed-width carousel/list cards must reserve their space (e.g. fixed `Modifier.height(...)`) so cards do not jump as content scrolls in.

**For substantial Kotlin changes, delegate to the `code-reviewer` agent before reporting work done.** "Substantial" = a new file, a new feature module, a change spanning multiple files, or any change touching layout/state/data flow. The reviewer audits against the full current `CODE_STYLE_GUIDE.md` and catches both new violations and pre-existing ones in the touched files (per the on-touch compliance policy). Run it after the build succeeds and before the wrap-up message.

## Test Writing

ALWAYS delegate test writing to the `unit-test-writer` agent, regardless of how small or simple the task appears. Never write or modify unit tests directly in the main conversation — even for a single function, a one-line change, or a trivial assertion. This rule has no exceptions.

When the target is a whole package or directory (not a single file), the agent's brief must include: "audit existing test files in the target for coverage gaps and close them in the same pass." Do not run a separate audit round — gap-fills belong in the initial delegation.

When multiple independent files need tests, spawn unit-test-writers in parallel on disjoint file sets rather than sequentially in one agent.

**Scope the prompt tightly to keep token/tool usage down.** A loose brief on a large test file (e.g. `BookMapperTest` is 3000+ lines) can burn 100K+ tokens on rediscovery and re-reads. For small mechanical changes (adding one field, renaming a symbol, fixing compile breaks):
- Hand the agent the exact file paths and line numbers of the construction sites you want fixed. Do the `grep` yourself first and paste the results — don't make the agent rediscover them.
- Skip the package-wide audit ask. List the specific 1-2 round-trip tests you want added and stop there. The audit rule above is for genuinely package-wide work, not single-field additions.
- Specify ONE narrow gradle `--tests` filter in the prompt; don't let the agent pick.
- Tell the agent explicitly NOT to re-audit, NOT to run the broader suite, and to keep its report concise (e.g. "under 150 words").

The agent is required to run the tests after writing them. Prefer narrow filters (e.g. `./gradlew :app:testDebugUnitTest --tests "nl.rhaydus.softcover.feature.<name>.*"`) over the full suite. When relaying its report to the user:
- If all tests pass, mention that the suite was executed and passed.
- If any test fails, surface the failing test names and the agent's diagnosis to the user verbatim, then **stop** and wait for the user to approve any fixes. Do not delegate a fix round until the user has reviewed and authorized it.

## Architecture

Always consult [ARCHITECTURE.md](ARCHITECTURE.md) before writing or reviewing code that touches layering, DI, navigation, or the TOAD state-management framework. Read it before adding a new feature module, modifying a ScreenModel / Action / Initializer, or changing data flow between layers — it is the source of truth for Clean Architecture boundaries and TOAD implementation details (generic signatures, per-feature boilerplate, Koin wiring). The summary below is a quick reference only; resolve any ambiguity against `ARCHITECTURE.md`.

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
