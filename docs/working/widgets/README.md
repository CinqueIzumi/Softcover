# Widgets (#93): local plan

Local only, not committed. Branch `93-widgets` (worktree `../Softcover-93`), based on `release/3.2.0`.
Scope: #97 infrastructure, #98 Currently Reading, #99 Random from Want-to-Read, #100 Trending this
week, #103 per-widget configuration. Out: #101 activity calendar (after 3.4.0), iOS/desktop widgets,
second wave #206–208, bundled fonts in widgets (Glance cannot use them).

**Working agreement:** one step at a time, then stop for the user to verify. Every step runs
implementer → test-writer → styleCheck → reviewer. No commits or pushes unless asked.

## Decisions

- Android only. Widgets follow palette, dark mode (day/night providers for System) and Glance's
  dynamic colour. System serif/sans stand in for Fraunces/Inter.
- Room: treat v51 (this base) as current; migrate 51 → 52. Chain merges with other branches are
  handled later, out of scope. Ignore the active `domain-out-of-uistate` tracker and dim mode.
- Trending persisted in Room; Explore and Reading's `TrendingBooksLoader` read it as the one source.
- Wavy progress drawn as a bitmap by a pure `CanvasDrawScope` renderer matching `ProgressBar`.
- Config via a widget configuration activity (on placement, skippable, reconfigurable on 12+).
- **Currently Reading:** 2×2 and 4×2. ‹ › cycle through the Reading shelf (most recently updated
  first, wraps); "2/4" on 4×2; arrows hidden for one book; the cycled book is kept by id and stays
  after progress on another book; falls back to the first if it leaves the shelf. No settings
  screen. Tap opens the Reading tab. Empty: "Between books".
- **Random from Want-to-Read:** 2×2 and 4×2, "WHAT NEXT?" eyebrow, shuffle button, tap opens the
  book. Pick kept per instance; re-pick cadence setting daily (default) / weekly / only on shuffle;
  re-pick if the book leaves Want-to-Read.
- **Trending this week:** 4×2 cover row with rank numerals; 4×3+ ranked list of five (plain
  `Column`). Cover → book detail, header → Explore. Refresh setting daily (default) / twice daily
  (2 API requests per refresh).
- Signed out: "Sign in to Softcover".

## Steps

### S1. Glance spike + module ✅
`:feature:widget` (KMP, Android code in `androidMain`, `androidResources.enable`), Glance 1.2.0,
placeholder widget + receiver, wired from `:orchestration` androidMain, stability baseline, roster
docs updated. All gates green.

### S2. "User books" queries + trending in Room
- `observeBooks()` and `getSortedAllUserBooks` require the book to be in `user_books` or
  `list_books` (the invariant `getOrphanBookIds` implies), so trending never leaks into Because You
  Read, genre options, Continue Series or the sync snapshot. Test it.
- `trending_books(bookId, rank)` + one-row fetched-at meta; migration 51 → 52 + migration test and
  data-survival test; added to `deleteAllLocalData`.
- Orphan cleanup excludes `trending_books` and runs inside the trending replace transaction;
  `redirectBookId` also remaps `trending_books`.
- Write books via `hydrateReferencedBooks(forceNetwork = false)` + rank-only replace; never
  re-cache existing user books.
- `refreshTrendingBooks(maxAge: Duration)`: pull-to-refresh `ZERO`, screen mount 24h, worker the
  configured interval. `ObserveTrendingBooksUseCase` + `RefreshTrendingBooksUseCase` (Result
  convention) replace `GetTrendingBooksUseCase`; remove the trending `SessionValueCache`.
- Migrate callers: `TrendingBooksCollector`, `OnRefreshAction`, `TrendingBooksLoader`,
  `ExploreScreenScreenModel`, `ReadingScreenScreenModel`, `ExploreModule`, `ReadingModule`,
  `BookModule`; drop Explore's library overlay. Loading flag = never fetched / in flight.
- Fix the existing trending tests (`BooksRepositoryImplTest`, `GetTrendingBooksUseCaseTest`, the
  Explore/Reading dependency fixtures).

### S3. Opening the app from a widget
`ExternalNavigationRequest` sealed type in `:core:presentation` (`OpenTab(Reading|Explore)`,
`OpenBook(id)`), impl in orchestration. `AppEntryPoint` builds intents; `MainActivity` parses them;
`BottomBarScreen` consumes (tab switch, `BookDetailPresenter.open`). Cleared on sign-out. Move Focus
Mode onto the same contract.

### S4. Shared widget layer
- `expect fun startPlatformAppServices(koin)` called from `startAppServices`.
- Refresher: trigger only, debounced, inactive when no widget is placed.
- Widget theme from `softcoverColorScheme`; exhaustive `ThemeMode` mapping.
- Move the Coil → Bitmap step into `:core:uibinding` androidMain (second consumer after
  `ReadingSessionService.ensureCover`); downsample, `allowHardware(false)`, reuse bitmaps.
- One constructor-injected dependency bundle per widget surface (Koin verify covers it).
- Decide the worker base (promote a generic one, or the module's own `CoroutineWorker`).

### S5. Currently Reading widget
Responsive sizes, cycling `ActionCallback`s storing book id per instance, pure selection function,
wavy-progress bitmap. UI models built in a `Flow` off-composition; `provideContent` immediately with
a loading model. No `android:configure`.

### S6. Settings screen + Random pick + maintenance worker
TOAD configuration activity in the app theme. Skip writes defaults, `RESULT_OK`, and updates (also on
API ≤ 30). Settings in per-instance Glance state (ids/enums only). Pure pick/cadence function,
evaluated lazily in `provideGlance`. Shuffle action. One unique periodic maintenance worker,
scheduled in `onEnabled`, updated on config change, cancelled in `onDisabled`; `updatePeriodMillis=0`.

### S7. Trending widget
Cover-row and ranked-list layouts. The worker refreshes trending at the shortest interval among
placed instances, network-constrained, skipped when signed out.

### S8. Docs
`docs/reference/design-system/patterns/widgets.md` (`patterns.md` is at its 8 KB budget: link only),
architecture deltas.

## Risks

- The user-books query change shifts what Library, Explore and sync see; S2 tests it.
- Glance compatibility with Compose 1.11 / expressive Material 3 alpha (S1 passed the build).
- RemoteViews bitmap limits: downsample covers, reuse bitmaps across size buckets.
