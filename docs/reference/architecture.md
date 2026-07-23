# Architecture

Clean Architecture layering, the core/feature/orchestration tiers, the TOAD pattern, DI, navigation, and dispatchers are governed by the foundation [`docs/rhaydus/0.3.1/architecture.md`](../rhaydus/0.3.1/architecture.md) and [`docs/rhaydus/0.3.1/toad-architecture.md`](../rhaydus/0.3.1/toad-architecture.md). Read those first — they are the source of truth for the two axes, the layer rules, placing a new type, the generic TOAD framework + per-feature boilerplate, Koin wiring, the `AppNavigator` contract, and the `AppDispatchers` abstraction.

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
                      #   book, lists, deadlines, personal, profile, connectivity
```

## TOAD — Softcover-specific notes

The generic TOAD framework, the five generic parameters, and the per-feature boilerplate are covered
by [`toad-architecture.md`](../rhaydus/0.3.1/toad-architecture.md). Softcover deltas:

- **TOAD is per-Voyager-screen only.** `MainActivityViewModel` is a plain `ViewModel`, not a TOAD
  `ScreenModel`. It is the one app-level view model and lives in `:orchestration`
  (`presentation/viewmodel/`); features that only need to flip the authenticated flag depend on the
  `SessionAuthenticator` contract in `:core:designsystem`, which the view model implements (mirroring
  the `AppNavigator` / `ActiveSessionController` seams).
- The Koin aggregate is named `softcoverModules`; `:app`'s `SoftCoverApp` starts Koin with
  `modules(softcoverModules + appModule)`. Each module self-declares its DI dependencies via
  `includes(...)`; `orchestrationModule` is the composition root that includes the whole graph, so
  `softcoverModules = listOf(orchestrationModule)` (order-independent). A Koin `verify()` test
  (`SoftcoverModulesVerificationTest`) gates that every binding resolves across the aggregate.
- The TOAD runtime is the foundation library `nl.rhaydus:toad` (`nl.rhaydus.toad.*`) — it is no longer
  vendored in the app. Softcover's per-feature flow-collector interfaces are named `XxxCollector`
  (e.g. `BookDetailCollector`, in each feature's `presentation/collector/`) and implement the foundation
  `nl.rhaydus.toad.Collector` role.
- **Error-slot convention.** A screen that can fail a load/submit follows the foundation TOAD error-slot
  convention ([`../rhaydus/0.3.1/toad-architecture.md`](../rhaydus/0.3.1/toad-architecture.md) §Conventions):
  a nullable `String?` error slot on its `UiState` (e.g. `ExploreScreenUiState.searchError`,
  `OnboardingUiState.submissionError`), set by the action, cleared on any invalidating edit and on retry,
  and rendered with `InlineErrorState` whose retry re-dispatches the screen's own action. Do **not**
  re-handle `CancellationException` in the fold: `runCatchingLogged` guarantees it at the use-case boundary,
  so the slot only holds a real failure. The Softcover-specific bindings: the copy is authored in
  presentation via `Throwable.toUserMessage()` plus a screen-specific fallback, and the renderer is the
  foundation `nl.rhaydus.designsystem.component.InlineErrorState` (call sites pass the editorial `bodySmall`
  as `textStyle`).

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
- Apollo errors are thrown as typed, sealed `ApiException` subtypes (`RetryableSyncException` ⊃
  `OfflineException` / `ServerUnavailableException`, `InvalidTokenException`, `UnexpectedApiException`)
  in `:core:domain/exception`. The seam does **not** author user-facing copy; presentation maps the
  kind via `Throwable.toUserMessage()` + the `Result.onApiFailure()` fold helper in `:core:designsystem`
  (see [code-style.md](code-style.md)).

### Unresolvable API enums

Some Hardcover columns are integer foreign keys with **no corresponding lookup type in the GraphQL
schema**, so they cannot be resolved through the API and must be mapped in our own code. Treat each as
an external contract that can drift without notice: define the mapping in exactly one place, never
inline the integers at call sites, and re-confirm with the Hardcover team before relying on a new value.

- **`gender_id`** (on `authors` as `Int`, on `characters` as `bigint`) — **`1` = male, `2` = female,
  `3` = other.** Confirmed by the Hardcover team; there is no `genders` type anywhere in the schema to
  derive it from. Three rules apply wherever this surfaces to users:
  - Render `3` as **"other"**, Hardcover's own word. Do not translate it into a specific identity —
    the bucket contains both non-binary authors and at least one trans woman, so a narrower label
    misgenders real people.
  - **`null` is the unknown case and is distinct from `3`.** A statistic must never merge them or
    relabel unknown as "other". Unknown may be surfaced as its own clearly-distinct segment — rendered
    muted, in a neutral role, and labelled "Unknown" (as the profile author-representation section
    does, where it is a share of *all* distinct authors) — but it is never counted as, blended into, or
    renamed to the "other" category, which is a real gender bucket in its own right.
  - The bucket also contains **non-person entities** (publishers, design studios, collective
    pseudonyms). Filter them out before charting, or the "other" share reads inflated.

## Platform seams from the foundation

Three non-visual seams are **not** app-local — they come from `nl.rhaydus:core-platform` and
`nl.rhaydus:offline-sync`. Reach for these rather than re-deriving them:

- **Connectivity** — `nl.rhaydus.platform.NetworkAvailabilityProvider` (`isOnline: StateFlow<Boolean>`,
  `awaitOnline()`) plus the `NetworkAvailability` instant-check singleton, which `startAppServices` installs.
  `:core:connectivity`'s `platformModule` binds the foundation provider per platform; on jvm it is also bound as
  `AutoCloseable` so desktop shutdown stops the reachability poll loop. There is no app-local connectivity data
  source or repository.
- **Secure storage** — `nl.rhaydus.platform.SecureStorage`, a **keyed** `read`/`write`/`delete` store. Softcover
  keeps exactly one secret in it, under `"api_key"`. On desktop the app constructs the namespaced `KSafe` and
  injects it: the OS secret store is user-scoped, so cross-app isolation is the caller's responsibility there
  (unlike Android's per-UID Keystore and iOS's per-bundle Keychain access group).
- **Offline write queue** — `nl.rhaydus.offlinesync.DefaultOfflineWriteDrainer` owns the drain loop, the
  online-triggered restart, the mutex, the poison cap and the in-drain backoff. The app supplies only what is
  genuinely its own: a `PendingWriteStore<P>` backed by Room (which is also the `WriteQueue<P>` — enqueue and
  drain-ops live on one type), a `replay` dispatch (`UserBookWriteReplay` / `ListWriteReplay`), a `hintKey`, and an
  `isTransient` classifier. `:core:domain`'s `UserBookWriteDrainer` / `ListWriteDrainer` are **named subinterfaces**
  of the foundation's generic `OfflineWriteDrainer`, because its two instantiations erase to one class and Koin
  could not otherwise tell them apart. The drain→fetch→reconcile composition and `preserveOwnedFields` stay
  app-side, in `OfflineUserBookSyncImpl`.

## Local Storage

- **Room**: relational data (books, user books, editions) with migration support. The Room database,
  migrations, and **all** persisted entities + DAOs live in `:core:database` (not in the feature whose
  data source uses them — see the vertical-slice rule in module-structure.md).
- **DataStore**: simple key-value preferences (app settings, search history).
