# Softcover — Architecture & Modularization Review

> **Status:** Advisory read-out — **open items only**. Everything that has been implemented and
> verified in the code has been **deleted from this file** (2026-07-14 sweep); what remains below is
> what still needs doing. Item IDs are **never reused or renumbered**, so a gap (M1–M8, B1, B3, B4, D1,
> D2, D4, P2, T1, T2, T4, C1, DC1) means "that one shipped".
>
> **Date:** 2026-06-17 (review) · 2026-06-24/26 (Tier 1 + Tier 2 implementation) · 2026-07-14 (resolved
> items pruned).
> **Method:** Full read-only sweep of the module graph + build files, data/domain layer,
> presentation/TOAD layer, build-logic/platform seams, and the test suite — cross-referenced against
> the Doveletter curated Android/Kotlin knowledge base.

## Context

The headline from the original review still holds: **the layering is genuinely good** — acyclic feature
graph (machine-enforced), no feature→feature imports, domain interfaces split from impls, consistent
TOAD, convention-plugin build with near-zero per-module boilerplate.

The whole of **Tier 1 (modularization, M1–M8)**, **Tier 1b (build, B1/B3/B4)**, and the structural half
of **Tier 2** (D1 single error model, D2 offline-sync extraction) have since shipped, along with the
two HIGH test gaps (T1 Room migrations, T2 the Apollo cache resolver), the error-state contract (P2),
Turbine standardization (T4), and the DC1 write-replay divergence. The keystone result:
`:core:designsystem` went from `api`-exporting **7 data modules** to `:core:book` + `:core:domain`
only, with a gate (M4) that stops it regressing.

What's left is **hardening, not structure**: release-build hygiene (R1/S1), a few correctness gaps in
sync and caching (D3/D5/DC2), test breadth (T3/T5/T6), and a long tail of low-severity polish.

---

## TIER 1b — Build setup & platform seams

- **B2 [LOW] AGP-9 / KSP compatibility debt.** `gradle.properties` sets `android.builtInKotlin=false`
  (+ related flags) to work around KSP not yet supporting AGP 9's built-in Kotlin. Track it; flip back
  and drop the explicit Kotlin-android plugin once KSP catches up. Verify the Room KSP path still works
  after the switch. Cost: trivial (later). *(Also carried as a fast-track fix in `now.md`.)*

---

## TIER 2 — Data & Domain layer

### D3 [LOW] Offline write replay is opportunistic, not guaranteed
Pending writes drain only on the next library refresh (`UserBookWriteDrainer`). If the user edits
offline and never reopens the library before the process dies, the write waits indefinitely. The
Doveletter **WorkManager internals** piece is the relevant lens: durable, constraint-gated
(`NetworkType.CONNECTED`) background replay is the standard answer on Android. KMP wrinkle: you'd need
a per-platform "schedule sync" seam (WorkManager on Android, BGTaskScheduler/equivalent on iOS, no-op/
on-launch on desktop). At minimum add an app-resume drain trigger now (`MainActivity.onResume()` still
only calls `checkForAppUpdateUseCase()`) and consider the durable scheduler later. Cost: low (resume
trigger) / medium (full per-platform scheduler). *Feeds Step 9.7 (offline mutation queue, 3.15.0).*

### D5 [LOW] No cache TTL / invalidation story for Room-cached entities
Room entities are cached indefinitely; freshness relies on aggressive refresh-on-entry. Acceptable
today, but worth an explicit decision (per-entity `fetchedAt` + staleness check, or documented
"refresh-on-entry is the contract"). Cost: low (doc) / medium (TTL).

---

## TIER 3 — Presentation / TOAD

### P1 [MEDIUM] TOAD boilerplate is heavy and one-action-per-file amplifies it
`:feature:book_detail` is 92 common files, **46 of them single `UiAction` subclasses**. The pattern is
consistent and correct, but the file-count tax is real and discourages small features / encourages
copy-paste drift. Options (foundation-level, so flag upstream to rhaydus rather than fork locally):
group related actions into one sealed file per concern; or a light codegen/template for the
UiState/Action/Event/ScreenModel/Dependencies quintet. Cost: low locally (grouping) / foundation
change (codegen). Mostly a "name the cost" item — don't fork the foundation convention unilaterally.

### P3 [LOW] A few justified-but-fragile UI state syncs
`library` shelf keeps a `mutableStateList` synced via `LaunchedEffect` (commented: avoids a blank
frame); `explore` holds file-level scroll/list state to survive movableContent moves on resize. Both
are documented and defensible, but they're the kind of thing that breaks silently on refactor. Worth a
shared helper (e.g. a `rememberSyncedList`) so the pattern is in one tested place rather than re-derived
per screen. Cost: low.

---

## TIER 4 — Testing strategy

Baseline is strong: ~243 test files, strict **MockK-only** discipline, `runTest` + JUnit5 + Kotest
throughout, TOAD presentation logic genuinely tested, and flow assertions now standardized on Turbine
(T4). What's left uncovered:

### T3 [MEDIUM] `core:personal` mutations under-tested *(half done)*
The offline write queue/drainer replay path **is** now covered (`UserBookWriteReplayTest`,
`PendingUserBookWriteStoreTest`). The gap that remains is `:core:personal`: **2 of 18** session/highlight
use cases have tests (pause/resume only). Start/stop, add/update/delete highlight, and the reading-log
writes are critical user-data mutations — exactly the paths where a silent bug loses a user's reading
progress. Cost: medium. (Delegate to `unit-test-writer`, MockK only.) *Pairs with the coroutine-safety
test audit already queued in `now.md`.*

### T5 [LOW] No Compose UI / screenshot / instrumented tests anywhere
Only `androidHostTest` unit tests exist — zero `runComposeUiTest` / `createComposeRule` / Paparazzi /
Roborazzi. Given a design-system-heavy multiplatform UI, a small screenshot-test or `runComposeUiTest`
suite on 2-3 critical flows (add-a-book, record-progress) would catch render/transition regressions CI
currently can't see. Also confirm Kover coverage is actually *gated*, not just *configured*. Cost:
medium (new test type + CI wiring).

### T6 [LOW] Per-class duplicated `stub*` fixtures *(partly addressed)*
One shared fixture file now exists (`core/book/.../BookTestFixtures.kt`), but most test classes still
re-declare their own `stubBook()`/`stubEdition()`. A mapper/model change still ripples across dozens of
files. Extract the rest into a shared `commonTest` fixtures module. Cost: low-medium.

---

## TIER 5 — Concurrency & data-consistency

The concurrency story is good: a single DI-owned `ApplicationScope`, disciplined `AppDispatchers` usage,
correct `Mutex`-based inflight dedup, no race-prone lazy init, flows using `distinctUntilChanged` /
`collectLatest` correctly. Two gaps remain:

### C2 [LOW-MEDIUM] `ApiKeyLocalDataSourceImpl` launches an untracked scope in `init`
`core/preferences/.../ApiKeyLocalDataSource.kt:49`: `init { CoroutineScope(io + SupervisorJob()).launch { … } }`
— the scope is never stored or cancelled. Low practical impact (it's a singleton), but architecturally
unsound; wire it through `ApplicationScope` like the syncers do. Cost: trivial.
(`ConnectivityDataSourceImpl` jvm also self-owns a polling scope — safe as an app-lifetime singleton, but
wiring it to `ApplicationScope` would be consistent.)

### DC2 [LOW] Latent race: a local mutation during an in-flight refresh can be clobbered
`refreshUserBooks()` drains pending writes *before* fetching, and the inflight `Mutex` serialises
refreshes — good. But a local mutation enqueued *while a refresh is mid-flight* isn't covered by the
(now field-scoped) `preserveSyncedWrites`, which only protects drainer-synced IDs — so the about-to-land
server snapshot can overwrite it. Usually serialises correctly via network latency. Deliberately deferred
when DC1 was fixed; still open. Cost: low.

---

## TIER 6 — Navigation & accessibility

Navigation is **exemplary** and needs no structural change (see the original read-out: acyclic
`AppNavigatorImpl` seam, per-book-keyed detail pane, saveable tab state, correct font scaling). The
items here are **gaps/opportunities**, not defects:

### N1 [LOW] Deep linking is Android-only and not type-safe
The only deep link is the Focus-Mode `Intent` extra. There's no HTTP-scheme routing (e.g.
`hardcover.app/book/123` → BookDetail) and no Voyager type-safe link builder. Adding type-safe deep
links for the major destinations (book by id/ISBN, profile by username, a list) would unlock notification
taps, share-sheet targets, and web links. Cost: medium. *Prerequisite for Step 4.8's deep-link share mode
(3.6.0) and Step 9.1's notification taps (3.14.0).*

### N2 [LOW] Back stack isn't restored across process death
Tab selection and desktop window geometry survive, but pushed screens (BookDetail, sub-settings) are not
saved — on process death the user lands at the root. Acceptable for a mainstream app; if full-stack
recovery becomes a requirement, use Voyager's saveable stack / `rememberSaveable` on navigator items.
Cost: medium.

### A1 [LOW] Accessibility baseline is strong; screen-reader semantics could be richer
Good: the `RhaydusIconResource` token carries `contentDescription` so descriptions travel with icons;
shared components label their icon buttons; decorative images correctly use `contentDescription = null`;
Material components meet touch-target minimums. Opportunities: add explicit roles
(`semantics { heading() }`, list/card semantics) on the library/explore grids — there are currently zero
`heading()` calls repo-wide — and, if UI tests arrive (T5), introduce **debug-gated** `testTag`s
(currently zero, which is fine). Cost: low, incremental. *Overlaps Step 9.9 (Voice & TalkBack polish,
3.15.0).*

---

## TIER 7 — Security, release-hardening, i18n & DB efficiency

Security fundamentals are **solid**: API-key storage is platform-correct (Android Keystore AES/GCM, iOS
Keychain, desktop KSafe), no tokens/keys/PII are logged, the error wrapping does not echo the
Authorization header, and the Apollo endpoint is hardcoded HTTPS. The findings are concentrated in
release-build hardening:

### R1 [MEDIUM] Release builds have minification DISABLED and empty ProGuard rules
`app/build.gradle.kts:43` sets `isMinifyEnabled = false`, and `app/proguard-rules.pro` is still just the
template comments. Consequences for a shipped release: larger APK, no obfuscation, class/method names
leak in stack traces. Re-enabling R8 is the right end state but **not a one-liner** — it needs keep rules
for the reflection-touching libraries: **Koin** (DI definitions), **Apollo** (generated operations/
adapters), **Room** (DAO/entities), **kotlinx-serialization** (`@Serializable` + serializers), and the
TOAD `UiState`/`UiAction`/`UiEvent` types. Plan: enable `isMinifyEnabled = true`, add the keep rules,
then do a **full release smoke test** (DI graph resolves, Apollo queries deserialize, Room opens) before
shipping. Cost: medium (rules + release verification). **This is now shipping to real users on Google
Play — it is the highest-value open item in this doc.**

### S1 [MEDIUM] `allowBackup="true"` with empty backup rules — confirmed bad
`AndroidManifest.xml:23` has `allowBackup="true"`, and both `res/xml/backup_rules.xml` and
`res/xml/data_extraction_rules.xml` are still the **empty Android Studio templates** — nothing excludes
`filesDir/api_key.enc` or the Room DB. adb/cloud backup can therefore carry the encrypted key and the
whole local database off-device. Exclude both. Cost: trivial. **Verified still open (2026-07-14) — was
"not read in this pass" at review time; it is now confirmed, not suspected.**

### S2 [LOW] No certificate pinning
Absent. Acceptable for a book-tracker (pinning adds key-rotation fragility and isn't Play-required for
non-financial apps) — recorded as a **conscious gap, not work**. Relies on platform default cleartext
blocking (correct on modern API levels).

### DB1 [LOW] Missing index on `ReadingJournalEntity.userBookId`
That column is filtered and `GROUP BY`-ed in `BookDao` reading-stats queries but has no index (other hot
entities like `UserBookEntity` are indexed on `bookId`/`statusCode`). Add `@Index("userBookId")` (or a
composite `(userBookId, updatedAt)`). Needs a migration bump. Cost: low. *Worth doing before the Stats
Atlas (3.11.0) leans on those queries harder.*

### DB2 [LOW] `BookDao` is too large — split into area-scoped DAOs
`BookDao` has grown past **100 functions** (book/edition/list/journal queries all on one DAO) and trips
detekt's `TooManyFunctions` threshold; it's `@Suppress("TooManyFunctions")`-ed — a deliberate stopgap,
not a fix. Split into area-scoped DAOs (`BookDao` / `EditionDao` / `BookListDao` / `ReadingJournalDao`),
each owning its own queries; the single `@Database` keeps seeing all of them. Removes the suppression.
Cost: medium (mechanical move + re-point call sites; no schema change).

### I1 [LOW] A couple of hardcoded desktop strings; RTL declared but unverified
`feature/reading/.../ReadingScreenLayout.jvm.kt:248,282` still hold literal English ("Now reading",
"Refreshing…"). Externalize for completeness (mobile is the primary target, so low impact).
`supportsRtl="true"` is declared but unverified — spot-check key screens use `padding(start=/end=)` not
`left=/right=`. Cost: low. *Pairs with Step 8.8 (language & region, 3.13.0).*

### B2-bis [LOW] No authored baseline/startup profiles
Confirmed absent. Not a blocker; a baseline profile would cut cold-start meaningfully on first launch
(Doveletter cites ~15-30%). Consider for a release-readiness pass. Cost: low-medium.

---

## Cross-cutting / smaller nits

- **Platform TODOs** (not architectural, just gaps): iOS barcode scanner stub
  (`feature/scan/.../BarcodeScanner.ios.kt:19` — `// TODO(iOS): real AVCaptureSession + Vision`), iOS
  gallery save stub (`core/designsystem/.../EditionImage.ios.kt:3`), density-aware share-capture
  limitation, onboarding snackbar-hidden-behind-modal bug. Track these as issues.
- **Document the wide-but-correct reach**: `:feature:book_detail` depends on 9 core modules. That's
  legitimate for its size; a one-line note in `../reference/architecture.md` heads off "is this a smell?"
  reviews. Still not written.
- **Verified clean (no action needed), so you know it was checked**: Compose stability across the app
  (LazyList `key {}` present everywhere, one correct `derivedStateOf`, no `GlobalScope`, modifiers
  stable/remembered, `LaunchedEffect` keys correct), 100% `AppLog` (no `println`/`Log.*`),
  `:feature:settings` / `:feature:app_update` / `:core:notification` platform isolation, and the
  `:core:designsystem` component catalog (no foundation-duplicating components found).

---

## Priority summary (what's left, in order)

| # | Change | Severity | Cost | Why |
|---|--------|----------|------|-----|
| R1 | Enable R8 + keep rules (Koin/Apollo/Room/serialization/TOAD) + release smoke test | MED | Med | Shipping unobfuscated, unminified builds to Play today |
| S1 | Exclude `api_key.enc` + the Room DB from `allowBackup` | MED | Trivial | Confirmed: backup can exfiltrate the encrypted key + whole DB |
| T3 | Test the 16 untested `core:personal` mutations | MED | Med | User-data-loss paths, thin coverage |
| P1 | Flag TOAD boilerplate upstream (don't fork the convention) | MED | Low | Name the cost; foundation's call |
| D3 | App-resume drain trigger; durable scheduler later | LOW | Low/Med | Offline writes can wait indefinitely |
| DB1 | Index `ReadingJournalEntity.userBookId` | LOW | Low | Before the Stats Atlas leans on those queries |
| C2 | Wire `ApiKeyLocalDataSource` init scope to `ApplicationScope` | LOW | Trivial | Untracked scope |
| DC2 | Close the mutation-during-refresh race | LOW | Low | Deferred when DC1 landed |
| D5 | Cache TTL decision (or document refresh-on-entry as the contract) | LOW | Low/Med | Explicit > implicit |
| DB2 | Split `BookDao` into area-scoped DAOs; drop the suppression | LOW | Med | 100+ functions on one DAO |
| N1/N2 | Type-safe deep links; back-stack restore across process death | LOW | Med | Unblocks share deep-links + notification taps |
| A1 | Richer screen-reader semantics; debug-gated testTags if UI tests arrive | LOW | Low | Accessibility |
| T5/T6 | Compose UI/screenshot tests + gate Kover; finish shared test fixtures | LOW | Med | Coverage breadth |
| I1 | Externalize desktop strings + RTL spot-check | LOW | Low | i18n |
| P3 | Shared `rememberSyncedList` helper | LOW | Low | Fragile pattern re-derived per screen |
| B2/B2-bis | AGP-9/KSP flip-back when KSP catches up; baseline/startup profile | LOW | Low/Med | Build + startup hygiene |
| S2 | Cert pinning | — | — | Conscious gap; no action planned |

## Verification (when/if any item is greenlit)
- After any module-dependency change: `./gradlew styleCheck` (module-graph + dependency-analysis gates),
  then a full build incl. the iOS-release caveat.
- Logic changes → `code-reviewer` agent + `unit-test-writer` for the touched repos/use cases, narrow
  `--tests` filters.

---

## Coverage note

Topics swept and documented: module graph & dependency hygiene, build-logic/convention plugins &
platform seams, data/domain layer, presentation/TOAD, testing strategy, coroutine/concurrency hygiene,
Apollo↔Room consistency, Voyager navigation, accessibility/semantics, Compose performance/stability,
logging, security, release/R8 hardening, i18n, DB efficiency.

**Not reviewed** (out of scope, not reviewed-and-clean): analytics/telemetry & crash-reporting wiring,
CI pipeline specifics, signing/release-distribution config, and the Apollo `.graphqls` schema (skipped by
policy — so `@typePolicy` correctness and schema-level concerns are unverified).
