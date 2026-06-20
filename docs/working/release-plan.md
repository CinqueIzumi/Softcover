# Release Plan
A release-by-release slicing of [roadmap-steps.md](roadmap-steps.md). Current shipped version is **2.4.0**.

Each release below mixes a foundational/plumbing step with user-visible features so every drop feels substantial. Step references map directly to `roadmap-steps.md` — when a step ships, delete it there (per its maintenance rule). This file is the *order* of the steps, not a replacement for them.

This is an **internal** doc: the step list, dependencies, and bundling reasoning. The **user-facing** per-version copy lives in the public [ROADMAP.md](../../ROADMAP.md) (which doubles as the source for Google Play / App Store release notes at ship time). Keep the two in lockstep: any reorder/cut/add here updates the matching `ROADMAP.md` section in the same change.

Scope key from steps file: **S** ≈ 1–2 day, **M** ≈ 3–6 day, **L** ≈ 7+ day.

**Versioning convention:**
- **3.0.0** — first release after the Kotlin Multiplatform migration: the public **iOS + desktop launch**, carrying the post-2.4.0 fixes. The launch is this release, so it takes the major bump.
- **3.x.0** — additive feature releases.
- Majors are a deliberate call, **not auto-reserved**. The Friend Feed adds a 5th nav tab (a structural change) — flagged as a **`4.0.0` candidate**, your call when it lands.

Dependencies are noted only where they cross a release boundary; same-release deps are obvious from order within the section.

> **Ordering note:** this plan was re-sequenced around an explicit priority order (fixes → high-prio features → medium-prio → backlog), not the original phase order. Some backlog items therefore land later than their old slots. The detail-enrichment cluster was promoted ahead of the book-detail-IA step (4.12 / "K") so the restructure accounts for everything on the screen.

---

## 3.0.0 — iOS + desktop launch (+ fixes)

The first release on the new Kotlin Multiplatform foundation — the public iOS & desktop debut — anchored on a stable fix set. Ships when the fixes are ready, including the two awaiting investigation (logout, lock-screen notif).

- ✅ **Fix (done)** — Gate list fetch on `updated_at`; skip re-fetching a list's contents when its `updated_at` hasn't advanced. *(Gated on a composite signature: `updated_at` + `list_books` count + max(`list_books.updated_at`).)*
- ✅ **Fix (done)** — Finished-date coalesce: `user_book_reads.finished_at ?? user_books.last_read_date ?? finished-journal updated_at ?? user_books.created_at`. Same chain in the date-finished sort SQL and the Read-tab year filter; `created_at` chosen as the stable final fallback, and the existing finished-journal signal kept after `last_read_date`.
- ✅ **Fix (done)** — Progress updates reflect immediately in the recently-reading strip (reactive propagation on the progress write). *(A successful progress/mark-read write optimistically marks today as an active reading date in the profile cache via the `core:domain` `MarkReadingActivityTodayUseCase` contract — implemented in `core:profile` — so the streak strip lights up without a server refresh.)*
- ✅ **Fix (done)** — Use the action date (`action_at`) for progress updates so the reading streak is computed from the day the user actually read, not the write timestamp.
- ✅ **Fix (done)** — Retry book-progress (and similar) user-book mutations on server error; drain on startup/reconnect. *(Apollo failures are classified into retryable transport/server errors — offline, an online network failure, or a 5xx/408/429 → `RetryableSyncException` — versus non-retryable successful-call-with-error responses (GraphQL errors, empty body, 4xx). The existing `PendingUserBookWrite` queue now enqueues progress / mark-read / rating / review on **either** offline or server-unavailable, and on drain a row the server actually rejected is discarded immediately instead of being retried. Narrow slice of Step 9.7; the 2.12 queue infra is reused.)*
- ✅ **Fix (done)** — Reading-streak settings toggle: new `readingStreakEnabled` pref (default on) + a row in the Appearance settings section. Gates **both** the strip's render and its data fetch — `ReadingActivityCollector` observes the pref and, when off, cancels the activity observe and clears the cached activity. *(grouped with the next)*
- ✅ **Fix (done)** — Show the reading-streak strip on the empty Reading state too (honors the toggle above). Render gate unified to `streakEnabled && recentReadingActivity.isNotEmpty()` across both headers and the empty state; the strip (and its expand sheet) now appears for a brand-new reader as an all-unlit 21-day grid.
- **Fix** — Covers not updating on refresh: invalidate the locally-cached cover when the cover URL changes (carries a diagnosis step).
- **Fix** — Graceful invalid-token re-auth without a destructive logout (decouple "update token" from the data-wiping logout). *Slice of B.6.8 / Step 8.7, pulled forward.* *(grouped with the next)*
- **Fix** — Logout fully clears local data. *High-prio; awaiting detailed repro. Shares the clear-data contract with the token-reauth fix.*
- **Fix** — Audiobook detection by **type** (not `audioSeconds != null`) + allow time entry when duration is null.
- **Fix** — Desktop: no login-screen flash at startup for already-authenticated users (gate first navigation on resolved auth state).
- **Fix (B)** — Focus Mode notification displays on the lock screen. *Gap on shipped Step 3.5; awaiting investigation; Android only.*

---

## 3.1.0 — Fast wins

Small, high-value features on already-shipped foundations.

- **Step 10.15** — Auto-resize coverless title text (S).
- **Improvement** — Batch edition-by-ISBN with an `_or` filter (S) — internal refactor; cleaner fetch path and an enabler for Step 8.6 (ISBN-list import).
- **Step 10.16** — Local tag cache + tag suggestions (S–M) — works around the API's no-`_ilike` tag search.
- **Step 3.12** — Rating/review prompt on mark-as-read (S–M) — reuses shipped 3.1 / 3.2 controls.

---

## 3.2.0 — Widgets

- **Step 9.3 (first wave)** — Widget infra + currently-reading, random-from-Want-to-Read, trending-this-week, reading-activity-calendar widgets (L) — Android-first (Glance). Second-wave widgets (streak / quote / year-in-books) land in 3.15.0.
- **Step 3.7** — Reading log (multiple read-throughs) (M) — *pulled forward* as the activity-calendar widget's finish-date data dependency (sessions already shipped).

---

## 3.3.0 — Series + shelving

- **Step 5.2** — Series detail screen (M).
- **Step 5.4** — Series-completion cascade (S) — *deps: 5.2 (same release)*.
- **Step 2.14** — Directly add a book to Currently Reading (S–M).
- **Step 4.10** — Format in the edition selector (S).

---

## 3.4.0 — Book-detail enrichment I

*Promoted ahead of the detail-IA step (4.12) so the restructure accounts for it.*

- **Step 4.1** — Genre/mood chips (S).
- **Step 4.2** — Awards & accolades (S).
- **Step 4.3** — Content warnings collapsible (S).
- **Step 4.9** — Personal trigger warnings (S) — *deps: 4.3 (same release)*.
- **Step 4.6** — Audiobook predicted finish (S).
- **Step 4.11** — romance.io link (spike + S/M) — extends the shipped links strip; spike owner TBD.

---

## 3.5.0 — Personal data on book detail II

*The personal-data surfaces that live on book detail — all add to its scroll, so they precede 4.12.*

- **Step 3.3** — Personal highlights / Passages + quick-add from Reading (M).
- **Step 3.9** — Personal identity & representation tagging (M) — *deps: 0.3 (shipped)*.
- **Step 3.10** — Personal moods (book + chapter) (M) — *deps: 0.3 (shipped)*.
- **Step 3.11** — Personal notes (book + characters) (M) — *deps: 0.3 (shipped)*.
- **Step 4.7** — Reviews filters & sorts (S).

---

## 3.6.0 — Inboxes + book-detail IA

- **Step 3.4** — Notes & Highlights inbox screen (M) — *deps: 3.3 (3.5.0)*.
- **Step 3.6** — Reading Sessions log screen (S).
- **Step 4.8** — Share sheet: link + deep-link modes (S).
- **Step 4.12** — Book-detail tabs / sectioning (M) — lands **after** all detail-enrichment (3.3–3.5.0 + 4.x). Design spike first.

---

## 3.7.0 — Author, Lists

- **Step 5.1** — Author detail screen (M).
- **Step 5.3** — Lists screen (M) — full CRUD + curated/community discovery on top of the shipped name-only MVP.
- **Step 5.5** — Add-to-list polish: ink-fill chip animation (S).
- **Step 10.10** — Tag system + library-side list creation (M) — *deps: 5.3 (same release)*.
- **Step 10.11** — Lent-out tracking on owned editions (S).

---

## 3.8.0 — Discovery I (Explore sections)

- **Step 6.1** — Genre/mood browser (M).
- **Step 6.2** — New & noteworthy + Most anticipated (S).
- **Step 6.3** — Award winners carousel (S).
- **Step 6.4** — Curated lists carousel (S) — *deps: 5.3 (3.7.0)*.
- **Step 6.10** — Similar books carousel on book detail (S).
- **Step 6.11** — Continue-series nudges (S).
- **Step 6.14** — Audience as a separate classification (M) — *deps: 6.1 (same release)*.

---

## 3.9.0 — Discovery II + personalization

- **Step 6.5** — "Because you read X" row (M) — *deps: Phase 3 (3.5.0)*.
- **Step 6.6** — Recommendations / For You screen (M) — *deps: 6.5 (same release)*.
- **Step 6.7** — Search filters & sorts (M).
- **Step 6.9** — Author spotlight tile (S) — *deps: 5.1 (3.7.0)*.
- **Step 6.12** — New Releases calendar, Want-to-Read first cut (M) — *deps: 6.2 (3.8.0)*.

---

## 3.10.0 — Profile depth + goals

- **Step 7.1** — Edit profile + settings shortcut (S).
- **Step 7.4** — Genre + rating distributions (M).
- **Step 7.5** — Reading seasons (S).
- **Step 7.6** — Author top-list + format split + records (S).
- **Step 7.2** — Yearly reading challenge tile + screen (M) — *deps: 3.1 (shipped)*.
- **Step 7.3** — Goal setup wizard (S).

---

## 3.11.0 — Stats Atlas + heatmaps

- **Step 7.7** — Streak heatmap on Profile (M) — *deps: 3.5 (shipped); subsumed by 7.12 next release*.
- **Step 7.8** — Time-of-day reading heatmap (M) — *deps: 3.5 (shipped)*.
- **Step 7.9** — Reading Stats Atlas screen (M).
- **Step 7.10** — Public activity log (M).
- **Step 7.13** — Diversity & representation stats (M) — *deps: 3.9 (3.5.0), 7.9 (same release)*.
- **OPEN — "L" (expanded statistics + image export)** — *unplaced*. Natural home is this release (around 7.9 / 7.13), but it stays open until the metrics are specified and the "own database" question is settled (which may pull a new-data-source foundation earlier).

---

## 3.12.0 — Calendars + recap

- **Step 7.12** — Reading Activity Calendar (M) — *deps: 3.7 (3.2.0), 7.7 (3.11.0)*.
- **Step 7.11** — Year in Books recap (M) — seasonal; gate behind a December trigger.
- **Step 7.14** — Custom-scope wrap-up generator (M) — *deps: 7.11 (same release)*.

---

## 3.13.0 — Settings restructure (Appearance, a11y, account)

- **Step 8.1** — Theme variants screen (S).
- **Step 8.2** — Spine colour (accent palette) picker (S).
- **Step 8.3** — Accessibility settings (S).
- **Step 8.4** — Notification controls (S).
- **Step 8.8** — Language, region, default tab, default progress unit (S).
- **Step 8.9** — About screen + per-version changelog (S).
- **Step 8.12** — In-app Roadmap screen (S) — renders the public `ROADMAP.md`, fetched at runtime.
- **Step 8.7** — Account, cache, privacy (S).
- **Step 8.5** — Data export (S).

---

## 3.14.0 — Notifications + onboarding + import

- **Step 9.1** — Notification triggers (M) — *deps: 8.4 (3.13.0)*.
- **Step 9.2** — Activity feed / Notifications inbox screen (M) — *deps: 9.1 (same release)*.
- **Step 8.6** — Data import from Goodreads / Storygraph / ISBN list (M) — benefits from the 3.1.0 `_or` batch-ISBN improvement.
- **Step 8.10** — Onboarding goal + theme + import + notifications + better error UI (M) — *deps: 7.3 (3.10.0), 8.1 (3.13.0), 8.6 (same release)*.
- **Step 8.11** — Curated starter list step (M) — *deps: 6.5 (3.9.0)*.

---

## 3.15.0 — Offline, backup, a11y polish + more widgets

- **Step 9.7** — Offline mutation queue, full (M) — *deps: 2.12 (shipped); extends the 3.0.0 progress-retry fix* to ratings/reviews/sessions/highlights + shake-on-conflict.
- **Improvement (M)** — Pending-sync indicator (+ tap-to-force-sync) — surfaces the 9.7 queue's pending count; rides with 9.7 so it isn't a near-empty indicator.
- **Fix/polish (N)** — Error feedback when a progress update via the reading-session notification fails; messaging aligns with the 3.0.0 retry behavior. Android.
- **Step 9.8** — Backup & restore (M) — *deps: 8.5 (3.13.0)*.
- **Step 9.9** — Voice & TalkBack polish (M).
- **Step 9.3 (second wave)** — streak, quote-of-day, year-in-books widgets — *deps: widget infra (3.2.0)*.
- **Step 9.4** — Quote-of-day surface + notification + widget link (S) — *deps: 9.3 second wave (same release)*.
- **Step 9.6** — Wear OS complication + quick-settings tile (M).
- **Step 9.5** — App shortcuts (S).

---

## 4.0.0 — Friend Feed (social)  ·  MAJOR candidate

Adds a 5th bottom-nav tab — a structural nav change. Conventionally a major bump; your call whether this is `4.0.0` or a `3.x` minor.

- **Step 9.10** — Friend Feed (L) — *deps: 5.1, 5.3 (3.7.0)*.
- **Step 6.13** — New Releases calendar: author/series follow tints (S) — *deps: 6.12 (3.9.0), 9.10 (same release)*.

---

## 4.1.0+ — Motion polish + long tail

Heavy on small polish; reorder freely within. Step 0.2 (share/render foundation) shipped, so export/share-dependent items are unblocked.

- **Step 10.1** — Long-press cover peek (M).
- **Step 10.2** — Hero parallax on book detail (S).
- **Step 10.3** — Bottom-bar collapse on scroll (M) — *retunes for the 5-tab dock from 4.0.0*.
- **Step 10.4** — Cover-back flip on long-press in detail (M).
- **Step 10.5** — Reading-session breathe (S).
- **Step 10.6** — Status callout ribbons + cover-tinted hero scrim (S).
- **Step 10.7** — Audiobook waveform + two-tone pacing bar (S).
- **Step 10.8** — Editorial flourishes (S).
- **Step 10.9** — Multi-method progress entry (M).
- **Step 10.12** — Inline edition swap from Library (S).
- **Step 10.13** — Pace card per active book + deadline urgency pinned section (S).
- **Step 10.14** — Library export as shelf card (S) — *deps: 8.5 (3.13.0)*.

---

## Out-of-band / not scheduled

- **Step 2.3** — Smart shelves as virtual tabs (M). The filter chips (Step 2.2) shipped in 2.3.0, so this re-evaluation is **now due** — if users naturally reach for facet filtering to express "owned & unread" / "started but stalled" / "quick wins", smart shelves are redundant; otherwise bring this back into a future release.
- **Step 2.9** — "Since last read" delta on Reading rows (S). Pulled out of 2.3.0. Re-evaluate before bringing back.
- **Step 11.1** — Book club / group reading (L). Stretch; not on the roadmap horizon. Bring back only on a deliberate decision to commit to the social arc beyond the friend feed.

---

## Cross-release dependency map at a glance

```
3.2.0 (3.7) ──> 3.12.0 (7.12)
3.5.0 (3.9) ──> 3.11.0 (7.13)
3.5.0 (Phase 3) ──> 3.9.0 (6.5) ──> 3.14.0 (8.11)

3.7.0 (5.1, 5.3) ──┬──> 3.8.0 (6.4)
                   ├──> 3.9.0 (6.9)
                   └──> 4.0.0 (9.10)

3.8.0 (6.2) ──> 3.9.0 (6.12) ──> 4.0.0 (6.13)

3.10.0 (7.3) ──> 3.14.0 (8.10)
3.11.0 (7.7) ──> 3.12.0 (7.12)
3.13.0 (8.4) ──> 3.14.0 (9.1)
3.13.0 (8.5) ──┬──> 3.15.0 (9.8)
               └──> 4.1.0+ (10.14)
3.2.0 (widget infra) ──> 3.15.0 (9.3 second wave, 9.4)
4.0.0 (5-tab dock) ──> 4.1.0+ (10.3 retune)
```

The longest remaining chains are **3.2.0 → 3.11.0 → 3.12.0** (reading log → heatmap → activity calendar) and **3.7.0 → 3.8.0 → 3.9.0 → 4.0.0** (Author/Lists → discovery → releases calendar → follow tints). Everything else branches off earlier, so the release order keeps comfortable slack — most releases can slip a slot without cascading.
