---
name: project_rhaydus_foundation_batch_f9_f10_f8
description: F9 (SecureStorage) + F10 (NetworkAvailabilityProvider) + F8 (offline-sync) adoption on hotfix/3.0.3 — review notes and verified-clean areas
type: project
---

Reviewed the combined Softcover (`hotfix/3.0.3`) + rhaydus-foundation (`release/0.3.0`) uncommitted diff
adopting F9/F10/F8. See [[project_rhaydus_foundation_upstream_migration]].

**Verified clean (deep-traced, no issues):**
- F9 migration ordering/idempotency/crash-safety in `ApiKeyLocalDataSourceImpl.migrateLegacyKeyIfNeeded()`:
  foundation store wins → legacy secure (Keystore/Keychain) → plain-text DataStore, each leg short-circuits,
  every read wrapped in `runCatchingCancellable`. Legacy copy is only deleted *after* a confirmed write to the
  new store (`migrateFromLegacySecureStorage()` checks `written` before calling `legacySecureStorage?.delete()`).
  `AndroidLegacySecureApiKeyStorage.read()`/`delete()` never generates a Keystore key when the alias is absent
  (explicitly tested). `IosLegacySecureApiKeyStorage` CFRetain/CFRelease pairs are a correct 1:1 carve-out of
  the deleted `IosSecureApiKeyStorage`. Desktop wiring binds no `LegacySecureApiKeyStorage`; `getOrNull()` in
  Koin correctly returns null for an unbound type (confirmed, not just assumed).
- **One exception**: `migrateFromPlainTextSettings()`'s final `appSettingsDataStore.store.updateData{...}`
  call (line ~174) is NOT wrapped, unlike every other call in the migration path — an escape here reaches the
  unsupervised `init{ launch{} }` coroutine (bare `SupervisorJob`, no exception handler) and can crash the
  process. **This is pre-existing**, carried over verbatim from the pre-adoption code (`git show HEAD:...`
  has the identical gap) — not a new regression, but still live and worth a follow-up fix.
- Doc claims cross-checked against actual foundation source and confirmed byte-accurate: Android Keystore
  alias `rhaydus_secure_storage` (was `softcover_api_key`), file name sanitizer turns `api_key` into
  `secure_api_95_key.enc` (underscore → `_95_`, ASCII code of `_`), iOS service
  `nl.rhaydus.secure_storage`/account `api_key` (was service `nl.rhaydus.softcover`/account
  `softcover_api_key`). `docs/reference/architecture.md` and `CLAUDE.md` were both updated in the same diff
  (no stale foundation-library-not-wired claims left).
- F8: `DefaultOfflineWriteDrainer` (foundation) is a faithful, verified-equivalent generalization of the two
  deleted app-local syncers — transient→increment+halt / terminal→delete+continue, hint only on `SYNCED`,
  snapshot-then-clear on `drain()`, `DrainPolicy.inDrainRetries` semantics (`1` = no retry, literal repeat
  count) correctly wired per queue (`USER_BOOK_IN_DRAIN_RETRIES = 1`, `LIST_IN_DRAIN_RETRIES = 3` matches the
  old list syncer's 3-total-attempt backoff loop), `maxAttempts` default 5 preserved untouched in both Room
  DAOs (`getPending(maxAttempts: Int = 5)`, not touched by this diff). `isTransient` correctly discriminates
  per queue in `ConnectivityModule.kt` (`RetryableSyncException` only for user-book; list always transient,
  matching the old asymmetry where the list syncer never distinguished terminal failures).
- `PendingUserBookWriteStore`/`PendingListWriteStore.getPending()` deleting an unparseable-`kind` row inline
  (`mapNotNull` + `return@mapNotNull null` after `dao.delete()`) is safe: the whole drain is
  `drainMutex`-serialized in the foundation drainer, so there's no concurrent second drain that could be
  mid-replay on the same row.
- Koin erasure: `UserBookWriteDrainerImpl`/`ListWriteDrainerImpl` correctly use named-subinterface delegation
  (`class X(delegate: OfflineWriteDrainer<...>) : DomainInterface, OfflineWriteDrainer<...> by delegate`) so
  the two `OfflineWriteDrainer<Int, Kind>` / `OfflineWriteDrainer<Unit, Unit>` instantiations, which erase to
  the same class, don't collide as Koin bindings. `single<UserBookWriteQueue> { get<PendingUserBookWriteStore>() }`
  correctly returns the same singleton as the concrete-type registration (Koin `get<T>()` inside a module DSL
  resolves the module's own registered singleton, doesn't construct a new instance) — verified, not just
  assumed.
- `.claude/hooks/block-slow-gradle.sh` narrowing (allow `compileKotlinIos*`/`compileIosMainKotlinMetadata*`,
  keep blocking `link*`/`*Framework`/`binaries`/`embedAndSign`/simulator test runs/`publish`/bare
  `build`/non-debug `assemble`) traced against every task name the review brief asked about
  (`:app:assembleRelease`, `linkDebugFrameworkIosArm64`, `iosSimulatorArm64Test`, `publishToMavenLocal`,
  `./gradlew build`) — all still correctly blocked. One low-severity nit: the `|`/`||`/`&&`/`;` segment split
  via `sed` doesn't respect shell quoting, so a literal separator char inside a quoted arg (e.g. a `--tests`
  filter) mis-splits the command — not practically exploitable as a bypass since Gradle task names can't
  contain those characters, but a real deviation from correct parsing.

**Only two other findings surfaced**: a dangling KDoc reference to the deleted `SecureApiKeyStorage` type in
`PlatformPreferencesModule.kt` (should say `SecureStorage`), and the hook quoting nit above. No architecture
violation found in `:core:domain` now `api`-depending on `nl.rhaydus:offline-sync` — the "domain depends on
nothing" tier rule (foundation-vendored, describes the internal app-module dependency graph) already tolerated
external/foundation libs (`kotlinx.datetime`, `core-common`) before this change; `offline-sync` is the same
category, not a data/presentation-layer coupling.
