# KMP / CMP Migration Plan

This document is the source of truth for converting Softcover from an Android-only multi-module
build into a **Kotlin Multiplatform** project with a **Compose Multiplatform** UI, **by hand, one
module at a time**, keeping the build green at every step.

It builds on [MODULE_STRUCTURE_GUIDELINES.md](MODULE_STRUCTURE_GUIDELINES.md) §11 ("Future: Kotlin
Multiplatform") and [ARCHITECTURE.md](ARCHITECTURE.md). Where this doc and §11 disagree on detail,
this doc wins (it is the worked-out version); §11 stays the one-paragraph pointer.

> **Status:** Phase 0 (foundation prep) **complete** — see §0. **P1 is complete:** `core:domain`,
> `core:network`, `core:preferences`, and `core:identity` are all converted to KMP source sets, with
> all declared iOS targets compiling. `core:preferences` is the first module with a real `iosMain`
> (`commonMain` + `androidMain` + `iosMain` + `androidHostTest`) — see §4 for the platform-Koin-module +
> `SecureApiKeyStorage` template. `core:identity` was a pure `commonMain` move (no platform seam —
> its deps `core:domain`/`core:preferences` are already KMP), tests to `androidHostTest`. **P2 is
> complete:** `core:database` is converted (Room KMP — `@ConstructedBy` constructor, all 37 migrations
> on `SQLiteConnection`, `BundledSQLiteDriver` + a platform-Koin-module builder, `RoomRawQuery`), and
> `core:book` is converted —
> `EditionImageStorage` re-expressed on okio (`FileSystem`/`Path`) behind a platform Koin module
> (`platformBookModule`, Android `filesDir` / iOS `NSDocumentDirectory`), and the two `Dispatchers.IO`
> leaks swapped to an injected `AppDispatchers.io`; all iOS targets compile and `check` is green.
> **P3 is underway:** `core:deadlines` and `core:personal` are converted — both pure `commonMain` moves
> (no platform seam, like `core:identity`): all of `domain`/`data`/`di` to `commonMain`, MockK tests to
> `androidHostTest`, no JVM/Android imports remained; all iOS targets compile and `check` is green.
> `core:lists` is converted too — also an all-`commonMain` move, but it had the same `Dispatchers.IO`
> leak `core:book` hit: `ListsRemoteDataSource` now takes an injected `AppDispatchers` and uses
> `appDispatchers.io` (wired with `get()` in `listsModule`), so no JVM-only dispatcher survives into
> `commonMain`. `core:profile` is converted following the `core:preferences` okio-DataStore template:
> `ProfileCacheSerializer` is now an `OkioSerializer` (`BufferedSource`/`BufferedSink`),
> `createProfileCacheDataStore(producePath: () -> okio.Path)` builds an `OkioStorage`, and the
> `ProfileCacheDataStore` binding moved out of `profileModule` into a new `expect val
> platformProfileModule` (pulled in via `includes(...)`) — Android supplies
> `filesDir/datastore/profile_cache.json` (store continuity preserved) and koin-android stays confined to
> `androidMain`, iOS supplies `NSDocumentDirectory`. `core:library` is converted too — a pure
> `commonMain` move (no platform seam, like `core:identity`): `LibraryModule` + `RefreshLibraryUseCase`
> to `commonMain`, MockK test to `androidHostTest`; its deps (`core:domain`/`book`/`lists`/`preferences`/
> `identity`) are already KMP so nothing else moved; all iOS targets compile and `check` is green.
> `core:connectivity` is converted too — the one P3 module with a real platform seam: its
> `ConnectivityDataSource` (a `StateFlow<Boolean>` online signal) is a `commonMain` interface whose impl
> moved behind a new `expect val platformModule` (pulled in via `includes(...)`). The Android `actual`
> wraps `ConnectivityManager.registerDefaultNetworkCallback` (the reified `getSystemService` needs
> `androidx.core.content` → `core-ktx` added to `androidMain.dependencies`); the iOS `actual` uses
> `NWPathMonitor` from `platform.Network` (online = `nw_path_status_satisfied`, on a global dispatch
> queue). The rest of `data`/`di` (write-queue/syncers/mapper/repository) was a plain `commonMain` move,
> MockK tests to `androidHostTest`. The iOS gate also surfaced a pre-existing DI bug — `connectivityModule`
> constructed `PendingListWriteSyncer` without its `appDispatchers` argument — fixed in the same change.
> All iOS targets compile and `check` is green. **P3 is complete.** Next up: P4 — `core:designsystem`
> (Compose Multiplatform, Coil 2→3). Update the per-module checklist (§7) as each module lands.
>
> **Toolchain (raised for `core:network`'s Apollo codegen):** Apollo's Gradle plugin only runs
> alongside the modern `com.android.kotlin.multiplatform.library` plugin under **AGP ≥ 9**, which
> cascaded a toolchain bump: **AGP 9.0.0, Gradle 9.1.0, Kotlin 2.3.21, KSP 2.3.9, JDK 17** (Kotlin
> 2.2.x caps at AGP 8.11.1; Kotlin 2.4.0 has no KSP yet, so 2.3.21 is the ceiling). Two AGP-9 opt-outs
> are set in `gradle.properties` until KSP supports AGP's built-in Kotlin: `android.builtInKotlin=false`
> (KSP/Room is incompatible with built-in Kotlin) and `android.newDsl=false` (the external
> `org.jetbrains.kotlin.android` plugin can't apply against AGP 9's new DSL). Both go away once KSP
> ships built-in-Kotlin support. The Gradle-9 test runtime also needs an explicit
> `junit-platform-launcher` (no longer auto-provided) — added to both convention plugins' test sets.

---

## 0. Decisions & Phase 0 status

**Decisions taken** (drive the rest of this plan):

- **Scope:** full **Compose Multiplatform** (share UI, not just logic).
- **Logging:** **Kermit** (`2.0.6`) behind an app-owned `AppLog` facade — Timber **dropped entirely**
  (not bridged), since it only did debug-gated prefixed Logcat that Kermit reproduces.
- **Android KMP target:** the modern **`com.android.kotlin.multiplatform.library`** plugin (authored at P1).
- **Date/time library:** **`kotlinx-datetime` 0.6.2** + `kotlin.time.Duration`.

**Phase 0 — done** (committed on `refactor/clean-up`, build green throughout):

| Phase-0 task | Status | Notes |
|---|---|---|
| Logging facade (Kermit `AppLog`) | ✅ done | `core/domain/logging/AppLog.kt`; debug-gated; `-=-` prefix preserved; all 102 call sites migrated; Timber removed from catalog + convention plugin + buildHealth excludes; custom `BlankLineAfterStatementRule` retargeted to `AppLog.e` |
| `java.time` → `kotlinx-datetime` | ✅ done | 76 files incl. 28 tests; `DateStyle.formatter` is now `DateTimeFormat<LocalDate>`, formatted via `formatter.format(value)`; code-reviewed |
| Platform seams (§6.3) | ✅ done | `EditionImageStorage`/persist chain now takes `ByteArray` (File confined to the Android impl); `createAppSettingsDataStore`/`createProfileCacheDataStore` factories (on-disk paths preserved); `ApiKeyLocalDataSource` Keystore was already fully behind `AuthTokenProvider` — no change needed |
| KMP convention plugins (§2.3) | ✅ `softcover.kmp.library` done (validated against `core:domain`) | Applies `com.android.kotlin.multiplatform.library` + `org.jetbrains.kotlin.multiplatform`; declares `androidTarget` + `iosArm64`/`iosSimulatorArm64`/`iosX64`; SDK 36 / minSdk 26 / JVM 11; commonMain wires coroutines-core + koin-core (Kermit comes transitively from `core:domain`); test stack split — Kotest/Turbine/coroutines-test in `commonTest`, JUnit5/MockK in `androidHostTest`. `softcover.kmp.compose` still pending (P4). |
| KMP catalog entries (§2.4) | ◑ partial (advanced) | Added: `org.jetbrains.kotlin.multiplatform` + `com.android.kotlin.multiplatform.library` plugin aliases, `kotlinx-coroutines-core` (KMP variant). Still pending: Compose-MP / Coil 3 / koin-compose-mp / datastore-mp (land at their owning module's phase). |

**Known follow-up:** `UnreleasedBadge` + `StreakStrip` month/day names are now **English-only**
(kotlinx-datetime has no locale support; was `Locale.getDefault()`). Accepted as a deliberate choice;
if locale-aware display is wanted, add a platform formatter when those files move to CMP (P4/P5).

---

## 1. Goal & scope

- **End goal:** full **Compose Multiplatform** — share domain, data, *and* UI across `androidTarget`,
  `iosX64/iosArm64/iosSimulatorArm64`, and (optionally) `jvm` desktop.
- **Method:** incremental, by hand, **one module per change**, lowest tier first. The build must
  pass `./gradlew check` (incl. `checkModuleGraph`, `buildHealth`, `ktlintCheck`, `detekt`, `lint`)
  after every module. No "big bang."
- **Non-goal:** re-architecting the module graph. The existing T0→T1→T2→T3 split (see
  `MODULE_STRUCTURE_GUIDELINES.md`) is already KMP-shaped and is **kept as-is**. This migration is a
  per-module `commonMain` / `androidMain` source-set split, plus a handful of `expect`/`actual`
  seams — not a redesign.

### Why the current split holds up

The `domain`/`data`/`presentation` package boundary is already the source-set boundary §8 promised.
Verified Android-freeness of `src/main` (imports of `android`/`androidx`):

| Module | `android*` imports in `src/main` | Verdict |
|--------|----------------------------------|---------|
| `core:domain` | 0 | clean (but see **java.time**, §2.1) |
| `core:identity` | 0 | clean |
| `core:deadlines`, `core:personal`, `core:lists`, `core:library` | 0 | clean |
| `core:book` | 2 (`SimpleSQLiteQuery`, `Context`) | small `actual` seams |
| `core:preferences` | 3 (`Context`, Keystore, DataStore) | `actual` seams |
| `core:profile` | 2 (DataStore) | `actual` seams |
| `core:connectivity` | 0 in `src/main` | a real seam was *added* at conversion — `ConnectivityManager` / `NWPathMonitor` behind `platformModule` |
| `core:notification` | many (by design) | real platform seam — `commonMain` contract + Android `NotificationManagerCompat`/WorkManager & iOS `UNUserNotificationCenter` `actual`s (§4) |
| `core:designsystem` | many (by design) | move to CMP |

No module needs to be re-split. The only *structural* changes recommended up front are small
seam-tightening refactors (§6) so each split is mechanical rather than invasive.

---

## 2. The three foundation blockers (Phase 0 — before any module converts)

These are cross-cutting; resolve all of them before converting a single module, because every later
phase depends on them.

### 2.1 `java.time` → `kotlinx-datetime` (the biggest hidden refactor) — ✅ done in Phase 0 (§0)

`core:domain` — the natural first module — uses `java.time` **pervasively** in its models:
`LocalDate`, `LocalDateTime`, `Instant`, `Duration`, `DateTimeFormatter`, `ChronoUnit`
(`Book`, `UserBook`, `BookEdition`, `BookDeadline`, `ReadingSession`, `ReadingDayActivity`,
`DeadlineProgress`, `DateStyle`). `core:book` mappers parse dates with `DateTimeFormatter` /
`DateTimeParseException`.

`java.time` is **JVM-only — it does not exist in Kotlin/Native (iOS) `commonMain`.** For a full-CMP
target this is unavoidable:

- Migrate domain date/time types to **`kotlinx-datetime`** (`kotlinx.datetime.LocalDate`,
  `Instant`, `LocalDateTime`, `DateTimePeriod`/`Duration` from `kotlin.time`).
- Date **formatting** (`DateTimeFormatter`, the `DateStyle` enum) is the sharp edge — `kotlinx-datetime`
  formatting is more limited than `java.time`. Two options, decide per case: use `kotlinx-datetime`
  `LocalDate.Format { }` builders in `commonMain`, or keep a thin `expect fun formatDate(...)` with
  the JVM `actual` delegating to `DateTimeFormatter` and an iOS `actual` using `NSDateFormatter`.
- This refactor touches domain models *and* their mappers/formatters, so it ripples into `core:book`
  and any presentation that formats dates. Do it as its own dedicated change **before** starting the
  per-module source-set splits — converting `core:domain`'s build file is trivial *after* this; the
  date refactor is the real work.

> If the target had been "structural readiness, Android-only target" this blocker would not exist
> (java.time is fine on a JVM-only KMP target). It is purely the iOS/Native target that forces it.

### 2.2 Logging: Timber → Kermit — ✅ done in Phase 0 (§0)

The `AndroidLibraryConventionPlugin` injects `timber` into **every** module, and Timber has no KMP
artifact. Decision: adopt **Kermit** (Touchlab) behind a thin app-owned logging facade.

- Define a small `Logger` facade (or use Kermit's `co.touchlab.kermit.Logger` directly) reachable
  from `commonMain` — put the facade in `core:domain` (no extra module needed unless it grows).
- On Android, route Kermit to a Timber-backed `LogWriter` (or Kermit's `platformLogWriter()`), so
  existing logcat behavior is preserved.
- Remove `timber` from the **KMP** convention plugin's base bundle (§3). Migrate `Timber.*` call
  sites to the facade **as each module converts** — not all at once. Known call sites in early
  modules: `core:domain` (1 file), `core:book` (3), `core:preferences` (2).

### 2.3 New KMP convention plugins — ✅ `softcover.kmp.library` done (§0); `softcover.kmp.compose` pending (P4)

The convention plugins in `build-logic/` hardcode `com.android.library` + `kotlin.android` and inject
the `-android` variants of coroutines/Koin plus Timber. Add KMP-aware siblings; keep the Android-only
ones for the `androidMain` portions that remain (the `scan` / `app_update` Android domains and the
Android `actual`s of seam modules like `core:notification`).

- **`softcover.kmp.library`** — applies **`com.android.kotlin.multiplatform.library`** (the modern
  single Android KMP target plugin) + `org.jetbrains.kotlin.multiplatform`. Declares `androidTarget`,
  the three iOS targets, optional `jvm()`. Wires the shared `commonMain` deps as the **KMP variants**:
  `kotlinx-coroutines-core` (not `-android`), `koin-core` (not `koin-android`), Kermit. Test stack
  goes in `commonTest` (Kotest/Turbine/coroutines-test are multiplatform; **MockK is JVM-only** — see
  §5). JDK/SDK levels match the existing plugin (compileSdk 36, minSdk 26, JVM 11).
- **`softcover.kmp.compose`** — layers **Compose Multiplatform** (`org.jetbrains.compose` +
  `org.jetbrains.kotlin.plugin.compose`) on top, for `core:designsystem` and feature UI. Replaces the
  `androidx.compose.*` deps with the `org.jetbrains.compose.*` equivalents (`compose.runtime`,
  `compose.foundation`, `compose.material3`, `compose.components.resources`).
- Keep `softcover.android.library` / `.compose` for modules staying Android-only, and reuse
  `softcover.android.room` / `.apollo` (those plugins gain KMP config when their module converts).
- **Do not** re-declare in module build files what a convention plugin already provides (existing
  rule, unchanged).

### 2.4 Version-catalog prep — ◑ partial (Kermit + kotlinx-datetime + KMP/AGP-KMP plugins + coroutines-core added; Compose-MP/Coil3/koin-mp/datastore-mp pending)

Add/adjust in `gradle/libs.versions.toml` (do this with the convention-plugin change):

- ✅ already added in Phase 0: **Kermit** (`co.touchlab:kermit` `2.0.6`) and **`kotlinx-datetime` `0.6.2`**.
- `org.jetbrains.kotlin.multiplatform` plugin alias; `com.android.kotlin.multiplatform.library` alias.
- `org.jetbrains.compose` (Compose Multiplatform) plugin + its BOM-less artifacts.
- ~~**Kermit** (`co.touchlab:kermit`)~~ — done (Phase 0).
- ~~**`kotlinx-datetime`**~~ — done (Phase 0, `0.6.2`).
- **Coil 2.7 → Coil 3.x** (`io.coil-kt.coil3:coil-compose` + `coil-network-*`). Coil 2 is Android-only;
  CMP needs Coil 3. This is a behavioral bump — audit `ImageRequest`/`AsyncImage` call sites.
- **Koin**: add `koin-compose-multiplatform` (replaces `koin-androidx-compose` for shared UI); keep
  `koin-android` for the Android shell.
- **Voyager**: 1.1.0-beta02 is already the KMP/CMP-capable line — confirm the `-koin` integration
  artifact has a multiplatform variant; `voyager-navigator`/`-tab-navigator`/`-transitions` are KMP.
- **DataStore** 1.2.0 already has a multiplatform artifact (`androidx.datastore:datastore` /
  `datastore-preferences-core`) — confirm the multiplatform coordinates when `core:preferences` converts.
- **Apollo** is fully KMP (`apollo-runtime` is multiplatform), **but** its Gradle plugin only supports
  the modern `com.android.kotlin.multiplatform.library` plugin under **AGP ≥ 9** — this forced the
  toolchain bump recorded in the status block (AGP 9.0.0 / Gradle 9.1.0 / Kotlin 2.3.21 / KSP 2.3.9 /
  JDK 17) and a bump to **Apollo 5.0.0**. The OkHttp-based client/interceptor moved to Apollo's
  multiplatform engine + an `ApolloInterceptor` (no `okhttp3`/`java.*` in `commonMain`).
- **Room** 2.7.2 supports KMP — needs the KMP Gradle config + `sqlite-bundled` driver (§4, Phase 2).

### 2.5 Decide the target list now

Even if iOS/desktop source sets start empty, declare the targets in the convention plugin from day
one so each converted module is *actually* compiled for Native and the compiler catches JVM-only
leaks (like java.time) immediately instead of at the end. Recommended initial set: `androidTarget`,
`iosArm64`, `iosSimulatorArm64`, `iosX64`. Add `jvm()` desktop only when desktop is a real target.

---

## 3. Migration order (phased, dependency-driven)

Convert strictly **lowest tier / fewest dependencies first**, so a module is only converted after
everything it depends on is already KMP. Within a phase, the order is the dependency order.

| # | Module | Why now (deps already KMP) | Moves to `commonMain` | Stays `androidMain` (`expect`/`actual`) | Risk |
|---|--------|----------------------------|------------------------|------------------------------------------|------|
| **P0** | foundation | — | logging facade, kotlinx-datetime, convention plugins, catalog | — | **High** (cross-cutting) |
| **P1** | `core:domain` | nothing below it | all models + use-case contracts + `Result` helpers | date *formatting* if not done in common | Med (after P0 java.time work) |
| P1 | `core:network` | domain | Apollo client, `safeQuery`/`safeMutation`, interceptors | engine/auth if platform-specific | Med |
| P1 | `core:preferences` | domain, network | `SettingsRepository`, value readers, serializers | DataStore factory (`Context`), **Keystore-encrypted API key** | **High** |
| P1 | `core:identity` | preferences | `GetUserIdUseCase`, `UpdateApiKeyUseCase` | — (clean) | Low |
| **P2** | `core:database` | domain | DAO/entity declarations, queries | DB builder + driver per platform | **Highest** |
| P2 | `core:book` | domain, database, network | repository, use cases, mappers | `EditionImageStorage` (file I/O), raw `SimpleSQLiteQuery` | **High** |
| **P3** | `core:deadlines` | domain, database | all | — | Low |
| P3 | `core:personal` | domain, database | all | — | Low |
| P3 | `core:lists` | domain, database, network, book | all | — | Low |
| P3 | `core:profile` | domain, identity, network | repo, use cases | DataStore (`Context`) | Med |
| P3 | `core:library` | book, lists, preferences, identity | all | — | Low |
| P3 | `core:connectivity` | domain, database, book, lists | write-queue / syncers | platform connectivity check if any | Med |
| **P4** | `core:designsystem` | all core above | TOAD framework, theme, components, nav contract, models | barcode scanner (CameraX/MLKit), Android-only resources | **Highest (UI)** |
| P4 | `core:notification` | domain; Compose-MP convention | `SoftcoverNotifier` contract, the (Compose) permission requester, content/appearance models | Android `NotificationManagerCompat` + channels + WorkManager; iOS `UNUserNotificationCenter` + categories — behind `platformNotificationModule` | Med |
| **P5** | `feature:library`, `feature:lists`, `feature:onboarding`, `feature:profile`, `feature:reading` | their core deps (all P4-done) | screens, ScreenModels, actions, events, flows, state | — (pure leaves) | Med (per feature) |
| P5 | `feature:explore` | core deps | screens + logic | one `android.content.Context` use — verify / `expect`-wrap | Med |
| P5 | `feature:settings` | core deps | screens + logic | one `android.os.Build` SDK check → `expect`/actual | Med |
| P5 | `feature:book_detail` | core deps | screen + logic | external-link `Intent` + `Toast` → common open-URL/notify contract | Med |
| P5 | `feature:scan` | core deps | non-camera logic | **CameraX/MLKit scanner + camera-permission requester stay androidMain** | Med |
| P5 | `feature:session` | core deps, `core:notification` | session UI + logic | **`ReadingSessionService` (foreground service) + media-style notification stay androidMain**; iOS = background modes / Live Activity | High |
| P5 | `feature:app_update` | — | `AppUpdateState` (already in core:domain) | **Play `AppUpdateManager` + update flow stay androidMain** (sanctioned) | Low |
| **P6** | `:orchestration` | everything | nav host, cross-feature use cases, Koin aggregate | launcher `MainActivity` androidMain | Med |
| P6 | `:app` | orchestration | — | **stays `com.android.application`** (Android entry point) | Low |

The iOS/desktop **app entry points** (a CMP `App()` composable consumed by an iOS `MainViewController`
and a desktop `main()`) are *added* in P6 as new thin shells — `:app` stays the Android shell.

---

## 4. The heavy modules — concrete guidance

### `core:database` (Room KMP) — Phase 2 — ✅ done (Android + all 3 iOS targets compile; `check` green)

Room 2.7 supports KMP but the setup differs from Android-only. How each point landed:

- The `@Database` class carries **`@ConstructedBy(SoftcoverDatabaseConstructor::class)`** with an
  `expect object SoftcoverDatabaseConstructor : RoomDatabaseConstructor<SoftcoverDatabase>` (Room's
  KSP generates each `actual`) — replacing the reflection-based `Room.databaseBuilder(klass)`. The
  builder is created per platform inside `platformDatabaseModule` (the `core:preferences` platform-
  Koin-module template): Android via `Room.databaseBuilder(context, name)`, iOS via the
  `NSDocumentDirectory` path. **The Android `name` is the absolute `getDatabasePath("books.db")` path,
  not a bare name** — the bundled driver opens the file directly and does not resolve names through the
  `Context`, so the absolute path preserves the existing store.
- The shared `SoftcoverDatabase.build(builder, queryContext)` adds the 37 migrations, sets
  **`BundledSQLiteDriver()`** + the query coroutine context (`AppDispatchers.io`, injected), and the
  destructive-migration fallback. The `androidx.sqlite:sqlite-bundled` dependency is provided centrally
  by the (now KMP-aware) `softcover.android.room` convention plugin.
- All 37 **migrations** moved from `Migration.migrate(db: SupportSQLiteDatabase)` to
  `migrate(connection: SQLiteConnection)` + `connection.execSQL` — mechanical, since none read a
  cursor (all are `execSQL` DDL/DML).
- KSP runs for **each** target via the per-target `ksp<Target>` configurations the convention plugin
  wires (`kspAndroid`, `kspIosArm64`, …); verified codegen on all targets.
- **Raw SQL:** `BookDao.observeBooksRaw` now takes `androidx.room.RoomRawQuery` (was
  `SupportSQLiteQuery`); the one caller in `core:book` (`BooksLocalDataSource`) builds `RoomRawQuery`
  and binds its int args via the `onBindStatement` lambda (`SimpleSQLiteQuery` is retired). This was
  the cross-module ripple — done in the same change even though `core:book` is not yet KMP.
- One incidental JVM-only leak the lift-and-shift surfaced: `ListEntityMapper` used
  `java.util.Comparator.thenComparing`, swapped to the Kotlin-stdlib common `Comparator.then`.
- All persisted **entities + DAOs** stay here (single `@Database`), per the vertical-slice rule —
  unchanged.

### `core:preferences` — Phase 1, high risk — ✅ done (Android + all 3 iOS targets compile; host tests green)

This was the first module with a real `iosMain`. The pattern below is the **template for later
`actual`-seam modules** (`core:profile`, `core:book`, `core:database`):

- **Platform Koin module, not a bare `expect fun`.** `commonMain` holds `expect val
  platformPreferencesModule: Module`; `preferencesModule` pulls it in via `includes(...)`, so
  `orchestration` keeps referencing `preferencesModule` by name with no change. The two
  non-shareable bindings live in the per-target `actual val`: the `AppSettingsDataStore` file
  location and the `SecureApiKeyStorage` impl. This keeps `androidContext()` (koin-android) confined
  to `androidMain` — add `implementation(libs.koin.android)` to the module's `androidMain.dependencies`.
- **DataStore went all-in on okio (both platforms).** Catalog adds `datastore-core`,
  `datastore-core-okio`, `okio`; `AppSettingsSerializer` is an `OkioSerializer` (`BufferedSource`/
  `BufferedSink`); `createAppSettingsDataStore(producePath: () -> okio.Path)` builds an `OkioStorage`
  over `FileSystem.SYSTEM`. Only the base dir differs — Android `filesDir/datastore/app_settings.json`
  (matches the old `dataStoreFile(...)` path for store continuity), iOS `NSDocumentDirectory`.
- **API key: real secure storage on both platforms (no stub).** `SecureApiKeyStorage` (`read`/`write`/
  `delete`) is the `commonMain` seam; `ApiKeyLocalDataSourceImpl` (common) keeps the flow/mutex/legacy
  migration and delegates raw I/O to it. Android `actual` = the verbatim Keystore + AES/GCM + file
  logic (alias/transformation/file name preserved → old ciphertext still decrypts). iOS `actual` =
  Keychain (`platform.Security`, `kSecClassGenericPassword`, `SecItemAdd/CopyMatching/Update/Delete`).
  okio is I/O only — it cannot back the key, so this boundary is genuinely `expect`/`actual`.

### `core:book` — Phase 2, high risk — ✅ done (Android + all 3 iOS targets compile; host tests + `check` green)

Followed the `core:preferences` platform-Koin-module template. How each point landed:

- **`EditionImageStorage`** was a `java.io.File` store using `Context.filesDir`. `EditionImageStorageImpl`
  now lives in `commonMain` on **okio** (`FileSystem` + `Path`), constructed `(fileSystem, rootDir)`,
  persisting under `<rootDir>/edition_images/<editionId>` — the on-disk layout is unchanged, so existing
  images load. The `EditionImageStorage` binding moved out of `bookModule` into a new
  `expect val platformBookModule` (pulled in via `includes(...)`); the `actual`s supply only the root
  dir + `FileSystem.SYSTEM` — Android via `androidContext().filesDir` (so koin-android stays confined to
  `androidMain.dependencies`), iOS via `NSDocumentDirectory`.
- **`Dispatchers.IO` was the one JVM-only leak the iOS gate caught** (the §2.1 SimpleSQLiteQuery seam
  was already retired during `core:database` via `RoomRawQuery`, so `Context`/`Dispatchers.IO` were all
  that remained). `BooksRemoteDataSourceImpl` and `BooksRepositoryImpl` now take an injected
  `AppDispatchers` and use `appDispatchers.io` (the project convention), wired with `get()` in `bookModule`.
- The `BookMapper` date parsing already moved to kotlinx-datetime in P0 (§2.1).
- **Tests** stayed in `androidHostTest` (MockK, JVM-only): the storage test drives okio via
  `FileSystem.SYSTEM` + a JUnit `@TempDir`; the data-source/repository tests build an `AppDispatchers`
  from a `UnconfinedTestDispatcher`.

### `core:designsystem` (Compose Multiplatform) — Phase 4, highest UI risk

- Swap `androidx.compose.*` → `org.jetbrains.compose.*`; swap `koin-androidx-compose` →
  `koin-compose-multiplatform`.
- **Coil 2 → Coil 3**: `AsyncImage`, `ImageRequest`, and the `api`-exposed coil types (this module
  returns coil types as part of its public API — see §3 of the module guidelines) all change package.
  Audit every consumer because the type leaks transitively.
- **Fonts & resources:** Google-fonts (`androidx.compose.ui.text.google.fonts`) and shared
  drawables/strings move to **Compose Multiplatform resources** (`compose.components.resources`,
  `Res.*`). This is a real chunk of work — the editorial typography in `DESIGN_SYSTEM.md` leans on the
  font provider.
- **Barcode scanner** (CameraX + MLKit) is Android-only → stays `androidMain`; expose it via an
  `expect`/contract so common UI can place it.
- **Voyager** `ScreenModel`/`Navigator`/`TabNavigator` are KMP — the **TOAD** framework moves to
  `commonMain` cleanly (it is built on `ScreenModel` + coroutines + `StateFlow`/`Channel`, all common).
- **`MainActivityViewModel`** / Android-lifecycle-bound pieces stay `androidMain`.
- Per the CLAUDE.md maintenance rule: any design-system change here must update `DESIGN_SYSTEM.md` in
  the same change.

### `core:notification` (platform seam) — Phase 4

Originally scoped as "stays Android-only," but that was wrong for a CMP app: its consumers
(`feature:reading`'s permission gate, `feature:session`'s channel) move to `commonMain` at P5, so the
surface they touch must be common — and iOS must post notifications too. It converts like
`core:connectivity`: a `commonMain` contract with Android + iOS `actual`s behind a
`platformNotificationModule` (the `core:preferences` platform-Koin-module template), pulled in via
`includes(...)` so `orchestration` keeps referencing `notificationModule` by name. It also applies the
Compose-MP convention (P4) because the permission requester is a `@Composable`.

- **`commonMain`:** the `SoftcoverNotifier` interface, the (Compose) `rememberNotificationPermissionRequester`
  contract, and the `SoftcoverNotificationContent` / `NotificationAppearance` models (once re-expressed
  platform-neutrally — see below).
- **`androidMain`:** `SoftcoverNotifierImpl` (`NotificationManagerCompat`), `NotificationChannelInitializer`
  + `SoftcoverNotificationChannel`, `SoftcoverWorker` (WorkManager), and the `ActivityResultContracts`
  permission launch — the verbatim current code.
- **`iosMain`:** a notifier over `UNUserNotificationCenter` (`add(UNNotificationRequest)`), permission via
  `requestAuthorizationWithOptions`, and — for the scheduled-notification base — `UNTimeIntervalNotificationTrigger`
  / `BGTaskScheduler` in place of WorkManager.
- **Two models hold Android types and need platform-neutral re-expression first:**
  `SoftcoverNotificationContent.pendingIntent: PendingIntent` → a common deep-link/target abstraction the
  Android `actual` turns into a `PendingIntent` and iOS into the notification's `userInfo`/launch route;
  `NotificationAppearance`'s `@DrawableRes`/`@ColorRes` `Int`s → icon/color tokens resolved per platform
  (CMP `Res.*` on the common side, or `expect`-typed handles).
- **Notification channels are an Android-only concept.** `SoftcoverNotificationChannel` + the initializer
  stay `androidMain`; iOS maps the same logical categories to `UNNotificationCategory`. The common
  contract should speak in logical categories, not channels.

---

## 5. Cross-cutting learnings & caveats predicted

- **java.time is the foundation tax (§2.1).** Easy to under-estimate because `core:domain` "looks
  clean" — it has no `android` imports, but `java.time` fails on Native just as hard. Do this first.
- **MockK is JVM-only.** The convention plugin puts MockK in the shared test stack. In KMP, MockK
  works in `androidUnitTest`/`jvmTest` but **not** `commonTest` for Native. Per the project's testing
  rule ([MEMORY] — MockK only, mock the used surface), keep MockK-based tests in the **JVM/Android**
  test source set; write pure `commonTest` only where no mocking is needed (Kotest + Turbine +
  coroutines-test are all multiplatform). Don't introduce a different mocking library — split the
  source set instead. Test writing still goes through the `unit-test-writer` agent (CLAUDE.md).
- **Koin Compose:** `koin-androidx-compose` → `koin-compose-multiplatform`; `getViewModel`/Android
  scope helpers don't exist in common — use the multiplatform Koin Compose APIs. Voyager-Koin handles
  `ScreenModel` injection in common.
- **Apollo `responseBased` codegen** (current setting) is fully supported on KMP — no codegen mode
  change needed. The scalar mappings (`date`→`String`, etc.) stay; just wire the Apollo source set.
- **Manifest merge no longer applies off-Android.** The Android source sets of `core:notification`
  (`POST_NOTIFICATIONS`), `feature:session` (`ReadingSessionService`), and `:orchestration`
  (`MainActivity`) contribute manifest entries. Non-Android targets get no manifest; plan
  platform-equivalent entry points (iOS notification authorization, service/background modes) separately.
- **`BuildConfig`** is Android-only and lives in `:app` (the `AppVersionProvider` reads it). Keep
  version/config access behind the existing `AppVersionProvider` contract (already in core) so common
  code never touches `BuildConfig`.
- **The gates must stay green.** `checkModuleGraph`, `buildHealth` (dependency-analysis), `ktlintCheck`,
  `detekt`, and `lint` all run on `check`. Two friction points: (a) `buildHealth`'s `api`/`implementation`
  advice must be re-checked per source set after the split; (b) `checkModuleGraph` derives tiers from
  paths — it keeps working since paths don't change, but confirm it tolerates the multiplatform plugin.
  The custom `:ktlint-rules` apply to all Kotlin source sets — no change expected.
- **iOS compilation is now a `check` gate.** The root `build.gradle.kts` wires every KMP module's
  `check` (via `plugins.withId("org.jetbrains.kotlin.multiplatform")`) to compile all three iOS targets
  (`iosArm64`/`iosSimulatorArm64`/`iosX64`). This is what stops JVM-only leaks — `kotlin.jvm.*` default
  imports, `Dispatchers.IO`, `java.time` — from passing on the Android variant (which compiles them fine
  for the JVM) and silently surviving until iOS bring-up. It runs only on **macOS** (Kotlin/Native iOS
  compilation is unavailable elsewhere), so an iOS CI must use a macOS runner. `check` runs in release
  prep (via the `style-check` skill), so a broken iOS compile blocks the version bump.
- **`internal` visibility** spans all source sets of a module (good — the existing `internal` seams in
  `core:book`/`core:preferences` keep working across `commonMain`/`androidMain`).
- **`Result<T>` + coroutines + `Flow`** (the whole TOAD data-flow spine) is multiplatform with zero
  change — the architecture's core abstractions are already common-safe.
- **Build times & CI:** compiling three iOS targets + Android multiplies task count. Expect slower
  local builds; consider enabling only `iosSimulatorArm64` locally and the full set on CI.

---

## 6. Recommended structural changes *before* starting — ✅ all done in Phase 0 (§0)

Small, low-risk refactors that make each later split mechanical instead of invasive. Done on the
Android-only build (improvements regardless of KMP):

1. ✅ **Logging facade (§2.2)** — Kermit-backed `AppLog`; all `Timber` call sites migrated; Timber dropped.
2. ✅ **kotlinx-datetime in domain (§2.1)** — the dedicated date refactor, done while still Android-only.
3. ✅ **Tighten platform seams behind interfaces** so the source-set split is a move, not a rewrite:
   - `core:preferences` — `ApiKeyLocalDataSource` already implemented `AuthTokenProvider` with the
     Keystore/`Cipher` code fully behind it; verified, **no change needed**.
   - `core:book` — `EditionImageStorage` + the whole persist chain (incl. domain `BooksRepository` /
     `PersistEditionImageUseCase`) now take `ByteArray`; `java.io.File` confined to the Android impl.
   - DataStore creation — extracted `createAppSettingsDataStore` / `createProfileCacheDataStore`
     factories (on-disk paths preserved); only the path is platform-bound for a later `actual`.
4. ✅ **`core:platform` dissolved into `core:notification`.** Its camera-permission requester moved
   into `feature:scan` (its only consumer); the remaining notification/WorkManager code became
   `core:notification`, which now owns its own Koin module (`notificationModule`) with the notifier
   appearance supplied by `:app`. `core:notification` is **not** Android-only — it converts at P4 with a
   real platform seam (Android `NotificationManagerCompat`/WorkManager + iOS `UNUserNotificationCenter`;
   see §4). No other module re-split is needed; the rest of the graph stays as documented in
   `MODULE_STRUCTURE_GUIDELINES.md`, and `feature:scan` / `feature:app_update` keep their sanctioned
   `androidMain` domains.

---

## 7. Per-module definition of done (checklist)

A module is "KMP-migrated" when **all** of these hold. Track status by checking off modules here as
they land.

```
[ ] build.gradle.kts uses softcover.kmp.library (+ .compose for UI), targets declared
[ ] src/main → src/commonMain (+ androidMain for the platform pieces); no code in src/main left
[ ] all Timber.* replaced with the logging facade
[ ] all java.time replaced with kotlinx-datetime (or expect/actual formatter)
[ ] every platform type behind an expect/actual or a commonMain interface with an androidMain actual
[ ] iosArm64 + iosSimulatorArm64 + androidTarget all COMPILE (actuals may stub-throw NotImplemented)
[ ] tests split: commonTest (pure) vs androidUnitTest/jvmTest (MockK-based)
[ ] ./gradlew :module:check green
[ ] ./gradlew checkModuleGraph buildHealth ktlintCheck detekt green (whole build)
[ ] docs updated if a design-system/architecture element changed (DESIGN_SYSTEM.md / ARCHITECTURE.md)
```

### Module tracker

| Phase | Module | Status |
|-------|--------|--------|
| P0 | logging facade (Kermit `AppLog`) | ✅ done |
| P0 | kotlinx-datetime refactor | ✅ done |
| P0 | platform seams (EditionImageStorage / DataStore factories / Keystore) | ✅ done |
| P0 | KMP convention plugins | ✅ `softcover.kmp.library` done; `.compose` pending (P4) |
| P0 | version catalog prep | ◑ partial (Kermit + kotlinx-datetime + KMP/AGP-KMP plugins + coroutines-core + datastore-core/-okio + okio done; Compose-MP/Coil3/koin-mp pending) |
| P1 | `core:domain` | ✅ done (`commonMain` + `androidHostTest`; all iOS targets compile; `check` green) |
| P1 | `core:network` | ✅ done (`commonMain` + `androidHostTest`; Apollo on its multiplatform engine + `ApolloInterceptor`; all iOS targets compile; `check` green) |
| P1 | `core:preferences` | ✅ done (`commonMain` + `androidMain` + `iosMain` + `androidHostTest`; okio DataStore + Keychain/Keystore `SecureApiKeyStorage` behind a platform Koin module; all iOS targets compile; host tests green) |
| P1 | `core:identity` | ✅ done (pure `commonMain` move + `androidHostTest`; no platform seam needed; all iOS targets compile; `check` green) |
| P2 | `core:database` | ✅ done (`commonMain` + `androidMain` + `iosMain` + `androidHostTest`; Room KMP `@ConstructedBy` constructor, 37 migrations on `SQLiteConnection`, `BundledSQLiteDriver` + platform-Koin-module builder, `RoomRawQuery`; all iOS targets compile; `check` green) |
| P2 | `core:book` | ✅ done (`commonMain` + `androidMain` + `iosMain` + `androidHostTest`; okio `EditionImageStorage` behind `platformBookModule`, `Dispatchers.IO` → injected `AppDispatchers.io`; all iOS targets compile; host tests + `check` green) |
| P3 | `core:deadlines` | ✅ done (pure `commonMain` move + `androidHostTest`; no platform seam needed, like `core:identity`; all iOS targets compile; `check` green) |
| P3 | `core:personal` | ✅ done (pure `commonMain` move + `androidHostTest`; no platform seam needed, like `core:deadlines`; all iOS targets compile; `check` green) |
| P3 | `core:lists` | ✅ done (all-`commonMain` move + `androidHostTest`; `Dispatchers.IO` → injected `AppDispatchers.io` in `ListsRemoteDataSource` like `core:book`; all iOS targets compile; `check` green) |
| P3 | `core:profile` | ✅ done (`commonMain` + `androidMain` + `iosMain` + `androidHostTest`; okio `ProfileCacheDataStore` behind `platformProfileModule` like `core:preferences`; all iOS targets compile; `check` green) |
| P3 | `core:library` | ✅ done (pure `commonMain` move + `androidHostTest`; no platform seam needed, like `core:identity`; all iOS targets compile; `check` green) |
| P3 | `core:connectivity` | ✅ done (`commonMain` + `androidMain` + `iosMain` + `androidHostTest`; `ConnectivityDataSource` behind `expect val platformModule` — Android `ConnectivityManager` callback (+ `core-ktx`), iOS `NWPathMonitor`; rest a plain `commonMain` move; fixed a `PendingListWriteSyncer` DI omission; all iOS targets compile; `check` green) |
| P4 | `core:designsystem` (CMP) | ☐ |
| P4 | `core:notification` | ☐ — real platform seam: `SoftcoverNotifier` + permission requester + content/appearance contracts in `commonMain`; Android `NotificationManagerCompat` + channels + WorkManager and iOS `UNUserNotificationCenter` + categories behind `platformNotificationModule`. Channels are Android-only; `PendingIntent` / `@DrawableRes`+`@ColorRes` models need platform-neutral re-expression first. |
| P5 | `feature:library` | ☐ — pure leaf (commonMain move) |
| P5 | `feature:lists` | ☐ — pure leaf |
| P5 | `feature:onboarding` | ☐ — pure leaf |
| P5 | `feature:profile` | ☐ — pure leaf |
| P5 | `feature:reading` | ☐ — pure leaf (notification-permission requester comes from `core:notification`'s common contract) |
| P5 | `feature:explore` | ☐ — near-leaf; one `android.content.Context` use to verify / `expect`-wrap |
| P5 | `feature:settings` | ☐ — near-leaf; one `android.os.Build` SDK check → `expect`/actual |
| P5 | `feature:book_detail` | ☐ — small seam: external-link `Intent` + `Toast` → common open-URL/notify contract with Android/iOS actuals |
| P5 | `feature:scan` | ☐ — CameraX/MLKit scanner (in `core:designsystem`) + camera-permission requester stay androidMain; non-camera logic to commonMain |
| P5 | `feature:session` | ☐ — `ReadingSessionService` (foreground service) + media-style notification stay androidMain; iOS needs a background / Live-Activity equivalent |
| P5 | `feature:app_update` | ☐ — Play `AppUpdateManager` + in-app-update flow stay androidMain (sanctioned); shared `AppUpdateState` already in `core:domain` |
| P6 | `:orchestration` | ☐ |
| P6 | `:app` (stays Android shell) + iOS/desktop entry points | ☐ |

---

## 8. One-paragraph TL;DR

The module graph is already KMP-shaped and needs **no re-split**. The real work is three foundation
tasks done up front — migrate `java.time` to **kotlinx-datetime**, replace **Timber with Kermit**
behind a facade, and add **`com.android.kotlin.multiplatform.library`-based convention plugins** —
after which you convert modules lowest-tier-first: `domain → network → preferences → identity →
database → book → (deadlines/personal/lists/profile/library/connectivity) → designsystem (CMP, Coil 3)
+ notification (Android `NotificationManagerCompat`/WorkManager + iOS `UNUserNotificationCenter` seam)
→ features → orchestration`. The Play `AppUpdateManager` / CameraX feature domains stay `androidMain`; `:app`
stays the Android shell with new thin iOS/desktop entry points added at the end. The sharp edges are
`java.time` (forced by the iOS target), Room-KMP setup, the Keystore-encrypted API key, MockK being
JVM-only in tests, and the Coil 2→3 bump in the design system.
