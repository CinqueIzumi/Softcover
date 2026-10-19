# Widget job quota

Widgets stop updating in the background while still showing content, until the user taps one. Every repaint costs
one JobScheduler job per placed instance, and a Glance session job runs 45–75 s, so the app exhausts its standby
quota and Android holds back every background job. Three fixes: repaint only on visible changes, drop the hourly
repaint of every instance, and render background repaints without a Glance session.

## Now

- **State:** Q1–Q3 built, reviewed and tested (widget suite, `styleCheck`, `checkR8KeepContract` pass). M0
  baseline (old build): 8 `SessionWorker` runs, 13 background job starts, 76 s runtime in the hour. Medium_Phone
  basic check passed: all four widgets render, taps work, a theme change and a forced maintenance run repaint
  with no job. Same hour on the fixed build: 0 `SessionWorker` runs, 1 background job start, 1.7 s runtime.
- **Next:** the user checks the Xiaomi 15T; commit on the user's go-ahead.
- **Open:** none.
- **Uncommitted:** Q1, Q2, Q3 and their tests.

## Why

- Reproduced on Medium_Phone (ACTIVE bucket): 79 job starts in the hour against a limit of 75, 897 in 24 h; a
  forced run of the midnight repaint job was held back on `WITHIN_QUOTA`. Of the last 35 job starts, 27 were
  `SessionWorker`.
- ACTIVE quota per rolling hour: 75 job starts, 20 min of job runtime. At 45–75 s per session, 16–26 session
  starts exhaust the runtime limit; stricter buckets and HyperOS allow far less.
- Glance treats a session as running while its job is `RUNNING` or `ENQUEUED`, and `update()` on a running
  session only queues an event, so a held-back session job swallows every update after it.
- Foreground work runs in-process through WorkManager's greedy scheduler, which is why a tap or app visit
  restores the widget.
- Multipliers: the Currently reading and Random pick signals are raw Room flows that re-emit on any write to
  the book tables (browsing caches books; a Trending refresh writes `books`); the maintenance worker repaints
  every instance every hour; a Trending refresh repaints Trending, Currently reading and Random pick.
- Only a Weekly Random pick needs a time-based repaint at an arbitrary hour (7 days after the pick); Daily
  picks and the quote of the day roll over at midnight, which the midnight job covers.

## Decisions

- Taps, settings changes and placement keep Glance sessions, including the loading frame and text-first rendering.
- Background repaints (workers, `WidgetRefresher` signals, maintenance) render with `GlanceAppWidget.compose()` and push through `AppWidgetManager.updateAppWidget`.
- The background path pushes every distinct model stage (text-first, then covers) and skips only the initial `Loading` model.
- A model stage that never arrives within the timeout pushes nothing and logs; the widget keeps its last picture.
- The background path skips an instance whose Glance session is live, tracked by our own registry in `provideSharedContent`, not Glance internals.
- Background renders for one instance run one at a time; a newer repaint cancels the one in flight.
- `compose()` receives the instance's real `AppWidgetManager.getAppWidgetOptions(id)`.
- Widget click actions stay `actionRunCallback` / `actionStartActivity`; a detekt rule forbids lambda actions in `:feature:widget`.
- Repaint signals pass only value changes (`distinctUntilChanged` before the first-value drop).
- Maintenance (safety-net repaint and image prune) runs daily; a one-off job repaints at the earliest Weekly Random pick expiry.

## Steps

| Step | Scope | Status |
|---|---|---|
| M0 | Baseline on Medium_Phone: fixed scenario (placed widgets, browse books, a Trending refresh, one hour in the background); record our job starts and runtime from `dumpsys jobscheduler` quota stats | [x] |
| Q1 | `WidgetRefresher.signal`: `distinctUntilChanged` on the source before `drop(1)`, so writes that change nothing a widget renders no longer repaint | [x] |
| Q2 | `WidgetMaintenanceScheduler`: daily period (`UPDATE`); Weekly Random pick expiry job (earliest future `pickedAt + 7 days` over placed Weekly widgets, `REPLACE`), rescheduled after every maintenance run; cancelled with the other maintenance work | [x] |
| Q3 | Background repaint path `BackgroundWidgetRenderer`: per-instance `compose()` of each distinct non-`Loading` model stage with real options, live-session registry, per-instance cancel-and-replace, 10 s timeout; `WidgetRefresher` and the workers route through it; detekt rule against lambda actions | [x] |
| V | Rerun the M0 scenario after Q1 + Q2 and after Q3; compare job starts and runtime; check text-first and cover arrival on a background repaint; then the user checks the Xiaomi 15T | [ ] |

Q1 and Q2 touch disjoint files and run as parallel implementers. Q3 follows them.

## Per-step routine

1. Implementer brief (Goal / Files / Verify / Report).
2. Build, then `softcover-test-writer` for the new logic (signal dedupe, expiry-time choice, stage filtering,
   live-session skip, cancel-and-replace), then `styleCheck`, then `softcover-reviewer`.
3. Stop for the user's go-ahead; the user runs the device checks.

## Risks

- A background push and a live session can still race at the moment a session starts; the session's next
  emission corrects it.
- A background render runs inside its caller's job; an uncached cover download lengthens that job up to the timeout.
- `WidgetRefresher` renders without a job, so a frozen process delays its push until it thaws.
- A future lambda action would break silently in background-rendered RemoteViews without the detekt rule.
- `Book` equality decides what counts as a visible change in Q1; a field the widget ignores still repaints.
