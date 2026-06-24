# Architecture

Clean Architecture layering, the core/feature/orchestration tiers, the TOAD pattern, DI, navigation, and dispatchers are governed by the foundation [`docs/rhaydus/0.2.0/architecture.md`](../rhaydus/0.2.0/architecture.md) and [`docs/rhaydus/0.2.0/toad-architecture.md`](../rhaydus/0.2.0/toad-architecture.md). Read those first — they are the source of truth for the two axes, the layer rules, placing a new type, the generic TOAD framework + per-feature boilerplate, Koin wiring, the `AppNavigator` contract, and the `AppDispatchers` abstraction.

This file keeps only Softcover's concrete deltas.

Softcover is a Kotlin Multiplatform / Compose Multiplatform client for [Hardcover.app](https://hardcover.app/) (Android, iOS, desktop).

## Project Structure

The app is a **multi-module Gradle build**. Modules depend only on lower tiers
(`:app → :orchestration → :feature:* → :core:*`). The tier rules, the authoritative full module
roster, and build-setup conventions live in
[module-structure.md](module-structure.md) — consult it rather than duplicating
the roster here. Modules are Kotlin Multiplatform: shared source lives under
`<module>/src/commonMain/kotlin/nl/rhaydus/softcover/…` with platform source sets
(`androidMain` / `iosMain` / `jvmMain` / `mobileMain`) for the platform seams.

```
:app                  # Application shell: SoftCoverApp, launcher manifest + resources, Koin startup
:orchestration        # Nav host (MainActivity, RootScreen, bottom bars), AppNavigator / AppEntryPoint
                      #   impls, cross-feature orchestration use cases, the softcoverModules aggregate
:feature:*            # Leaf features: lists, profile, onboarding, explore, library, book_detail,
                      #   reading, session, scan, settings, app_update
:core:*               # domain, database, network, designsystem, notification, preferences, identity,
                      #   book, lists, deadlines, personal, profile, library, connectivity
```

## TOAD — Softcover-specific notes

The generic TOAD framework, the five generic parameters, and the per-feature boilerplate are covered
by [`toad-architecture.md`](../rhaydus/0.2.0/toad-architecture.md). Softcover deltas:

- **TOAD is per-Voyager-screen only.** `MainActivityViewModel` is a plain `ViewModel`, not a TOAD
  `ScreenModel`. It lives in `core:designsystem` (`core/presentation/`).
- The Koin aggregate is named `softcoverModules`; `:app`'s `SoftCoverApp` starts Koin with
  `modules(softcoverModules + appModule)`. Each module self-declares its DI dependencies via
  `includes(...)`; `orchestrationModule` is the composition root that includes the whole graph, so
  `softcoverModules = listOf(orchestrationModule)` (order-independent). A Koin `verify()` test
  (`SoftcoverModulesVerificationTest`) gates that every binding resolves across the aggregate.
- The TOAD runtime is the foundation library `nl.rhaydus:toad` (`nl.rhaydus.toad.*`) — it is no longer
  vendored in the app. Softcover's per-feature flow-collector interfaces are named `XxxCollector`
  (e.g. `BookDetailCollector`, in each feature's `presentation/collector/`) and implement the foundation
  `nl.rhaydus.toad.Collector` role.

## Network Layer

- **Apollo GraphQL** communicates with the Hardcover API. GraphQL operations live in
  `core/network/src/commonMain/graphql/`.
- `safeQuery()` and `safeMutation()` extension functions wrap Apollo calls with error handling.
  `safeQuery` takes an optional `FetchPolicy` (defaults to `NetworkOnly`); a `safeQueryFlow` variant
  exists for `CacheAndNetwork` rendering.
- An in-memory normalized cache is configured on `ApolloClient` (10 MiB), keyed by `@typePolicy`
  declarations on entity types in `core/network/src/commonMain/graphql/extra.graphqls`. Session-stable
  queries (book detail, editions, reviews, series lookups, books-by-ids hydration) are served
  `CacheFirst` for instant revisits. Lists that should refresh on screen entry stay on `NetworkOnly`.
  Mutations write through the cache automatically — Room remains the source of truth for user-book
  state, so Apollo cache writes on `user_books` rows are currently inert observers.
- Network interceptors handle authentication headers.
- Apollo errors are wrapped in `RuntimeException` with descriptive messages.

## Local Storage

- **Room**: relational data (books, user books, editions) with migration support. The Room database,
  migrations, and **all** persisted entities + DAOs live in `:core:database` (not in the feature whose
  data source uses them — see the vertical-slice rule in module-structure.md).
- **DataStore**: simple key-value preferences (app settings, search history).
