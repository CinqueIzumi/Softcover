# Widgets (#93): local plan

Branch `93-widgets` (worktree `../Softcover-93`), based on `release/3.2.0`, local only.
Scope: #97 infrastructure, #98 Currently Reading, #99 Random from Want-to-Read, #100 Trending this
week, #103 per-widget configuration. Out: #101 activity calendar (after 3.4.0), iOS/desktop widgets,
second wave #206–208, bundled fonts in widgets (Glance cannot use them).

**Working agreement:** one step at a time. Every step runs implementer → test-writer → styleCheck →
reviewer. No commits or pushes unless asked.

## Decisions

- Android only. Widgets follow palette, dark mode (day/night providers for System) and Glance's
  dynamic colour. System serif/sans stand in for Fraunces/Inter. Widgets paint on the app's paper
  (`background` / `onBackground`), never Glance's `widgetBackground` (that maps to
  `secondaryContainer`, an accent tint).
- Room chain merges with other branches are handled at merge time. Ignore the active
  `domain-out-of-uistate` tracker and dim mode.
- Trending persisted in Room per period; Explore and Reading's `TrendingBooksLoader` read it as the
  one source.
- Wavy progress drawn as a bitmap by a pure `CanvasDrawScope` renderer matching `ProgressBar`.
- Config via a widget configuration activity, shown on every placement, reconfigurable on 12+. A
  widget with settings names its source in the header and tapping it opens its settings; the app's
  Settings has a Widgets page (S8).
- **Currently Reading:** portrait 2×2 compact and 2×3 tall, blurred cover backdrop. ‹ › cycle the
  Reading shelf (most recently updated first, wraps), kept by id per instance, falling back to the
  first. No settings screen. Tap opens Reading; "xx% ✎" opens the progress sheet. Empty: "Between
  books".
- **Random pick:** same portrait look, "WHAT NEXT?" eyebrow, shuffle button, tap opens the book.
  "Pick from": Want to Read (default), Owned (unread) or any user list (unread); a deleted list falls
  back to Want to Read. Re-pick cadence daily (default) / weekly / only on shuffle; re-pick if the
  book leaves the pool.
- **Trending:** top 10 per period at two per page with ‹ › and "1/5", ranked list at 4×3+. Cover →
  book detail, header → Explore. Period and refresh (daily / twice daily) settings. Explore stays
  offline-gated.
- Focus Mode: a widget tap opens the app into Focus Mode; the request applies underneath.
- Signed out: "Sign in to Softcover".

## Steps

### S1. Glance spike + module ✅
`:feature:widget` (KMP, Android code in `androidMain`), Glance 1.2.0, wired from `:orchestration`.

### S2. "User books" queries + trending in Room ✅
`observeBooks()` / `getSortedAllUserBooks` require `user_books` or `list_books`, so trending never
leaks into library-derived surfaces. `trending_books` + fetched-at meta (migrations and data-survival
tests), one `replaceTrendingBooks` transaction, `refreshTrendingBooks(maxAge)` behind
`ObserveTrendingBooksUseCase` / `RefreshTrendingBooksUseCase`; callers migrated.

### S3. Opening the app from a widget ✅
`ExternalNavigationRequest` in `:core:presentation` (tab, book, verdict, Focus Mode),
`AppEntryPoint.intent` with a strict `softcover://open/...` codec, consumed by `RootScreen` /
`BottomBarScreen`, cleared on sign-out.

### S4. Shared widget layer ✅
`startPlatformAppServices`; `WidgetRefresher` + pure `refreshSignals` (placement-gated, debounced,
per-source error isolation) fed by `RefreshingWidgetReceiver`; `SoftcoverWidgetTheme`; cover bitmap
loading shared in `:core:uibinding`; one dependency bundle per widget surface.

### S5. Currently Reading widget ✅
Models built in a `Flow` off-composition; cover and wavy bar for the shown book only; cycle callback
does its read-modify-write inside `updateAppWidgetState`.

### S6. Settings screen + Random pick + maintenance worker ✅
TOAD configuration activity in the app theme (loads once, rejects ids not owned by its receiver,
clears the pick only when settings changed); per-instance Glance state; hourly unique maintenance
work (KEEP), cancelled when no Softcover widget remains.

### S7. Trending widget ✅
Unique periodic refresh work (network-constrained, `UPDATE` at the shortest placed cadence, only the
periods in use, one-time catch-up on reschedule, `retry` on failure). Crash-audit fixes: settings
screens hold only ids, save repaints, workers retry before Koin starts.

### S7b. Update progress from the Currently Reading widget ✅
`UpdateProgressActivity` hosts the app's `UpdateProgressBottomSheet` with a widget-local copy of
Reading's sheet logic (shared extraction is a separate issue).

### S8. Widgets section in Settings ✅
`HomeScreenWidgets` in `:core:presentation` (Android impl in `presentation/placed/`, unsupported on
iOS/desktop). Settings › Personalisation › Widgets lists placed instances with their summary and
Edit, plus Add rows when the launcher can pin. Widget settings screens have a top bar with Back.

### S9. Module layout ✅
Done before the convergence pass so its findings cite the final paths. `:feature:widget` follows the
feature-module layout: `domain/{model,util}`, `data/{datastore,mapper}` (Preferences serialisers split
out of the models, keys unchanged), `presentation/` by kind (`action`, `collector`, `event`, `state`,
`screenmodel`, `screen`, `mapper`, `model`, plus `glance`, `callback`, `activity`, `render`, `component`,
`theme`, `util`, `refresh`, `worker`, `placed`) and `di/`. Generic random-pick action/collector names
gained a `RandomPick` prefix. Tests mirror their units; stability baseline regenerated (it was stale
since the trending paging and top-bar commits).

### S8b. Convergence pass
A deliberately critical sweep of all widget code on the branch: `:feature:widget` and the code it added or
changed elsewhere (trending per period in `:core:book` / `:core:database`, the external-request
contract in `:core:presentation` / `:orchestration`, Book Detail's verdict entry). Parallel reviewers on
disjoint lenses, each finding verified against the code before it counts:
- crashes and lifecycle: process death, receivers and callbacks without Koin, Glance sessions, RemoteViews
  bitmap limits, activities finishing early, WorkManager timing;
- data correctness: stale or mismatched state between widgets, Room and per-instance prefs, sign-out,
  deleted lists/books, offline;
- looks: every widget at every size in light/dark/palettes, against the app's design system (from the
  user's screenshots; no emulator driving before S11);
- simplification: over-engineering, one-caller layers, duplicated logic across the three widgets that
  should be shared, dead code, comments that fail the document-code rules, layer rules (presentation
  reads the `data/datastore` stores directly, with no use case).
Findings ranked, verified, then fixed; tests and audit after.

### S10. Docs
`docs/reference/design-system/patterns/widgets.md` (`patterns.md` is at its 8 KB budget: link only),
architecture deltas.

### S11. Device crash sweep (last)
Claude drives Medium_Phone (emulator-5554) with a scripted run per widget: place, resize through
every size, tap every target, reconfigure, remove, and a cold start after process death
(`am stop-app`). Clear the crash buffer first; `adb logcat -b crash` must stay empty after every run.
Maintenance work enqueued by pre-S9 builds (KEEP) still names the old worker class path, so remove
every widget once first so it is cancelled and re-enqueued under the new path.

## Risks

- The user-books query change shifts what Library, Explore and sync see; S2 tests it.
- RemoteViews bitmap limits: downsample covers, reuse bitmaps across size buckets.
