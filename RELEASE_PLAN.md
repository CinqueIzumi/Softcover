# RELEASE_PLAN.md

A release-by-release slicing of [ROADMAP_STEPS.md](ROADMAP_STEPS.md). Current shipped version is **2.1.0**.

Each release below mixes a foundational/plumbing step with user-visible features so every drop feels substantial. Step references map directly to `ROADMAP_STEPS.md` — when a step ships, delete it there (per its maintenance rule). This file is the *order* of the steps, not a replacement for them.

Scope key from steps file: **S** ≈ 1–2 day, **M** ≈ 3–6 day, **L** ≈ 7+ day.

**Versioning convention used here:**
- **2.x.0** — additive features and improvements
- **3.0.0** — reserved for the bottom-nav restructure (Friend Feed adds a 5th tab; that's a structural shift worth the major bump)

Dependencies are noted only where they cross a release boundary; same-release deps are obvious from order within the section.

---

## 2.2.0 — Foundations & first depth pass

The plumbing release. Step 0.3 has no UI but unblocks all personal-data work; the rest of the release gives users visible Library/Reading polish so the version still feels like a real drop.

> **Release notes (Google Play):**
> Currently Reading now lands instantly on launch — the rest of your library catches up in the background. Per-tab sorting (date, title, author, rating, progress, deadline, page count) with a year filter on Read. Reorder tabs from Settings or by long-press. Reading suggests today's pages and surfaces Want-to-Read picks when you're idle. Share any book as an editorial card. List covers prefer the edition you've added. Plus pull-to-refresh on Explore, IME-aware bottom sheets, and bug fixes.

- **Step 0.3** — Personal-data layer (M) — schema + repos for ratings, reviews, highlights, sessions, reading log. No UI.
- **Step 2.1** — Sort within Library tabs (M)
- **Step 2.4** — Per-tab stats subtitle + year filter on Read (S)
- **Step 2.7b** — Reorder library tabs/shelves (S) — drag-to-reorder list in Settings → Library is the canonical entry; long-press on a tab in the Library strip is a shortcut into the same reorder mode. Order is persisted per user.
- **Step 2.8** — "Plan today" nudge on Reading (S)
- **Step 2.10** — Adaptive empty Reading state (S)
- **Step 4.8 (image-only first cut)** — Share book as generated image (S) — overflow "Share" on book detail renders a `ShareContent.Book` via `ShareCard`, captures it as a PNG, and hands the result to an `ACTION_SEND` chooser. Link and deep-link modes are deferred to the full Step 4.8 in 2.9.0.
- **Improvement** — Two-phase startup + per-tab pull-to-refresh (S) — `BooksRepository.refreshUserBooks` is now dispatched by a `RefreshScope` sealed type (`All` / `ByStatus` / `ByList`) with per-scope in-flight coalescing on `ApplicationScope`. `initializeBooks` awaits a currently-reading refresh, then launches the full library + lists refresh on the application scope, so the home screen lands ahead of the long tail of Want-to-Read / Read entries. Pull-to-refresh now scopes per surface: Reading refreshes only currently-reading; Library refreshes only the active tab's status or list. Visibility toggles are now display-only — the library always fetches all four statuses regardless of which tabs are shown (the old fetch-time gating via `enabledStatusCodes` / `alwaysCachedCodes` is gone).
- **Fix** — Prefer the user's library edition for list-book covers (S) — list rows now resolve their cover from the user's own userBook edition first, then the book's `default_cover_edition`, then the raw `list_books.edition_id`. The owned list still preserves the original edition. All user lists are now cached locally (not just enabled tabs) so the resolver sees the user's edition across every list.
- **Fix** — Fall back to the book's default cover for list editions with no cover URL (XS) — `BookListsCollector` now substitutes the parent book's `coverUrl` into editions whose own `url` and `localImagePath` are both null, so owned-list (and any custom-list) cells render the book cover instead of an empty tile.
- **Fix** — Refresh list editions and book metadata on user-books refresh (S) — list-only books had their edition rows (covers) and book rows (descriptions) cached only once on first hydration; the user-books refresh path now re-fetches all referenced book + edition IDs with a network-forced fetch policy and batched id chunking, so manual refresh actually updates them.
- **Fix** — Make bottom sheets scrollable and IME-aware (XS) — `UpdateProgressBottomSheet`, `ShareBookBottomSheet`, the edition selector, and the continue-series dismiss sheet now lift their content above the keyboard and scroll when needed, so the primary action (e.g. "Update progress") stays reachable without first dismissing the IME.
- **Fix** — Global "Something went wrong" snackbar on Apollo failures (S) — Apollo network errors, GraphQL errors and empty-data responses now surface a single transient snackbar across every screen (hosted once at the root of `MainActivity`, not only inside `BottomBarScreen`/`OnboardingScreen`), and `SnackBarManager` drops new toasts while one is already on screen so errors no longer queue up.

---

## 2.3.0 — Library filtering + book-detail metadata

> **Release notes (Google Play):**
> Filter your library by genre, format, year, ownership or rating — active chips show what's narrowing the view. Smart shelves surface "Owned & unread", "Started but stalled", "Quick wins" and more alongside your normal tabs. Drag any book to reorder it within any list — Want-to-Read, Currently Reading, Read, or any custom list. Long-press a cover to enter bulk-select mode and move books in batches. Reading rows briefly show your progress since last open. Book detail now lists publisher, imprint and ISBN, with quick links out to Bookshop.org, Amazon, library.org and author sites.

- **Step 2.2** — Library filter chips (M)
- **Step 2.3** — Smart shelves as virtual tabs (M)
- **Step 2.5** — Bulk select mode (M)
- **Step 2.7** — Drag-to-reorder books within any library list (M) — long-press + drag on a row in any tab (Want-to-Read, Currently Reading, Read, custom lists). Persisted as a manual sort mode per tab that coexists with Step 2.1's sort options.
- **Step 2.9** — "Since last read" delta on Reading rows (S)
- **Step 4.4** — Publisher / imprint / ISBN inline (S)
- **Step 4.5** — External links strip (S)

---

## 2.4.0 — Personal data lights up

The first release where the corpus from 0.3 surfaces to the user. Pair big personal-data steps with smaller drag/reorder polish.

> **Release notes (Google Play):**
> Your reading becomes personal. Rate any book on its detail page and draft your own review. Start a reading session straight from the Reading screen — the timer captures duration and page deltas, and a 21-day streak strip sits above your greeting. Book detail surfaces literary awards and accolades when relevant.

- **Step 3.1** — Personal rating control on book detail (S) — *deps: 0.3*
- **Step 3.2** — Personal review drafting (M) — *deps: 0.3*
- **Step 3.5** — Reading session timer (M) — *deps: 0.3*
- **Step 3.8** — Streak strip on Reading screen header (S) — *deps: 3.5 (same release, ship in order)*
- **Step 4.2** — Awards & accolades (S)

---

## 2.5.0 — Highlights, sessions log, reading log

Closes out Phase 3 and tucks in two small Phase 4 wins.

> **Release notes (Google Play):**
> Save the passages you love. A new Passages section on every book detail holds your highlights, and the Notes & Highlights inbox lets you search across every quote you've saved. A Reading Sessions log timelines every session you've tracked. Re-reads finally make sense — each book detail tracks every read-through separately with its own dates, rating and notes. Content warnings get an opt-in reveal. Reviews can be filtered by friends, top-rated, recent and language.

- **Step 3.3** — Personal highlights section + quick-add from Reading (M)
- **Step 3.4** — Notes & Highlights inbox screen (M)
- **Step 3.6** — Reading Sessions log screen (S)
- **Step 3.7** — Reading log (multiple read-throughs) (M)
- **Step 4.3** — Content warnings collapsible (S)
- **Step 4.7** — Reviews filters & sorts (S)

---

## 2.6.0 — Author, Series, Lists

Three new screens in one release. They're independent enough to land together and they all unblock subsequent discovery work.

> **Release notes (Google Play):**
> Three new screens. Tap any author byline for a full Author page with their works, accolades and series. Tap a series eyebrow for the full reading-order checklist and aggregate progress. Browse and manage your custom lists as a first-class surface — create, rename and add books to any list with a new Add to list sheet. Finishing the last book in a series triggers a quiet celebration. Book detail gains genre and mood chips, plus audiobook finish-date predictions.

- **Step 5.1** — Author detail screen (M)
- **Step 5.2** — Series detail screen (M)
- **Step 5.3** — Lists screen (M)
- **Step 5.4** — Series-completion cascade (S) — *deps: 5.2 (same release)*
- **Step 5.5** — Add-to-list write path + action sheet (M) — *deps: 5.3 (same release)*
- **Step 4.1** — Genre/mood chips (S)
- **Step 4.6** — Audiobook predicted finish (S)

---

## 2.7.0 — Discovery, first wave

Explore gets richer. Each step here is fundamentally a new section on an existing screen, so the release is mostly Compose work over data that's already reachable.

> **Release notes (Google Play):**
> Explore gets richer. Browse by genre or mood, separate New & Noteworthy from Most Anticipated, and discover Award winners across Booker, Pulitzer, Hugo and more. A curated Lists carousel lets you tour staff picks and themed collections. Every book detail page now suggests similar reads, and stalled series get gentle "pick this back up" nudges instead of quietly drifting.

- **Step 6.1** — Genre/mood browser (M)
- **Step 6.2** — New & noteworthy + Most anticipated (S)
- **Step 6.3** — Award winners carousel (S)
- **Step 6.4** — Curated lists carousel (S) — *deps: 5.3 (2.6.0)*
- **Step 6.10** — Similar books carousel on book detail (S)
- **Step 6.11** — Continue-series nudges (S)

---

## 2.8.0 — Personalisation + Releases calendar (first cut)

> **Release notes (Google Play):**
> Recommendations tuned to you. New "Because you read…" rows on Explore key off your top-rated and recently-finished books, and a dedicated For You screen brings them together. Search gains filters and sorts — year, rating, format, language, popularity. The new Releases Calendar plots upcoming releases from your Want-to-Read shelf onto a month grid, with quick release-day reminders and deep links into book detail.

- **Step 6.5** — "Because you read X" personalisation row (M) — *deps: Phase 3 (2.4.0–2.5.0)*
- **Step 6.6** — Recommendations / For You screen (M) — *deps: 6.5 (same release)*
- **Step 6.7** — Search filters & sorts (M)
- **Step 6.9** — Author spotlight tile (S) — *deps: 5.1 (2.6.0)*
- **Step 6.12** — New Releases calendar, Want-to-Read first cut (M) — *deps: 6.2 (2.7.0)*

---

## 2.9.0 — Profile depth + scan + sharing

> **Release notes (Google Play):**
> Profile becomes editable — set your name, bio and avatar in-app. A handful of new stats land: genre and rating distributions, reading seasons across twelve months, top authors, format split (print/ebook/audio) and personal records like longest haul and fastest read. Scan a barcode from the search bar to find any book in the wild. Share a book as an editorial card image, a plain link or a send-to-a-friend deep link.

- **Step 7.1** — Edit profile + Settings shortcut (S)
- **Step 7.4** — Genre + rating distributions (M)
- **Step 7.5** — Reading seasons (S)
- **Step 7.6** — Author top-list + format split + records (S)
- **Step 6.8** — ISBN/barcode scan (M)
- **Step 4.8 (remaining modes)** — Share book sheet: link + deep-link modes (S) — *image mode shipped in 2.2.0; this release adds the three-mode sheet on top.*

---

## 2.10.0 — Stats Atlas + Goals

The big personal-stats consolidation. Step 7.7 (12-week heatmap) ships here knowing it will be subsumed by 7.12 in 2.11.0; the interim shipping order is intentional because 7.12 depends on 7.7 being in the codebase.

> **Release notes (Google Play):**
> Set a yearly reading goal. A new Reading Challenge tile tracks your pace with a wavy progress bar, and a wizard helps you scope it — books, pages or genre diversity. A 12-week streak heatmap replaces the simple streak stat, and a time-of-day heatmap shows when you read most. The new Reading Stats Atlas pulls every chart into one long editorial spread. Optionally toggle a public activity log so others can see your finishes.

- **Step 7.2** — Yearly reading challenge tile + screen (M) — *deps: 3.1 (2.4.0)*
- **Step 7.3** — Goal setup wizard (S)
- **Step 7.7** — Streak heatmap on Profile (M) — *deps: 3.5 (2.4.0); subsumed by 7.12 next release*
- **Step 7.8** — Time-of-day reading heatmap (M) — *deps: 3.5 (2.4.0)*
- **Step 7.9** — Reading Stats Atlas screen (M)
- **Step 7.10** — Public activity log (M)

---

## 2.11.0 — Calendars + recap + early Settings wins

7.12 folds 7.7's heatmap surface into the activity calendar in this release — the two-step path was a dependency artefact, not a duplication.

> **Release notes (Google Play):**
> The Reading Activity Calendar lands. A full month-grid view of every day you read, with the covers you touched, your sessions and your finishes shown in each day cell. Pinch out to a 12-month overview that replaces the standalone streak heatmap. Year in Books returns as a December recap with shareable slides. New pace cards on each Reading row predict your finish date, and Library tabs surface an "Up against the clock" section when deadlines loom. Plus data export and finer account and privacy controls.

- **Step 7.12** — Reading Activity Calendar (M) — *deps: 3.5, 3.7 (2.4.0/2.5.0), 7.7 (2.10.0)*
- **Step 7.11** — Year in Books recap (M) — seasonal; gate the in-app surface behind a December trigger
- **Step 8.5** — Data export (S)
- **Step 8.7** — Account, cache, privacy (S)
- **Step 10.13** — Pace card per active book + deadline urgency pinned section (S)

---

## 2.12.0 — Settings restructure (Appearance + a11y + niceties)

Pure Settings density release with one tangible new feature (10.11) so it doesn't feel like all knobs.

> **Release notes (Google Play):**
> Make it yours. Light, Dark, Dim and System themes with preview tiles. Pick a Spine colour — four hand-tuned editorial palettes that personalise the accent without leaving the type-led look. Granular accessibility controls (reduced motion, text size, high contrast, screen-reader announcements). Notification toggles per type. Language, region and default-tab preferences. A new About screen lists every previous release. Plus track who you've lent your books to.

- **Step 8.1** — Theme variants screen (S)
- **Step 8.2** — Spine colour (accent palette) picker (S)
- **Step 8.3** — Accessibility settings (S)
- **Step 8.4** — Notification controls (S)
- **Step 8.8** — Language, region, default tab, default progress unit (S)
- **Step 8.9** — About screen + per-version changelog (S)
- **Step 10.11** — Lent-out tracking on owned editions (S)

---

## 2.13.0 — Notifications + Activity inbox + onboarding refresh

> **Release notes (Google Play):**
> Soft nudges, not noise. Opt in to deadline reminders, release-day pings, weekly recaps and milestone notifications — every type toggles independently. A new in-app Activity inbox collects everything you might want to come back to. Import your shelves from Goodreads or Storygraph during onboarding, or paste an ISBN list. New users now get a guided goal-setting and theme-pick flow, plus clearer errors when an API key fails.

- **Step 9.1** — Notification triggers (M) — *deps: 8.4 (2.12.0)*
- **Step 9.2** — Activity feed / Notifications inbox screen (M) — *deps: 9.1 (same release)*
- **Step 8.6** — Data import from Goodreads / Storygraph / ISBN list (M)
- **Step 8.10** — Onboarding goal + theme + import + notifications + better error UI (M) — *deps: 7.3 (2.10.0), 8.1 (2.12.0), 8.6 (same release)*
- **Step 8.11** — Curated starter list step in onboarding (M) — *deps: 6.5 (2.8.0)*

---

## 3.0.0 — Friend Feed

Major version because it adds a 5th bottom-nav tab — a structural change to the app's navigation. The follow-tint follow-up for the releases calendar lands the same release since follows go live here.

> **Release notes (Google Play):**
> Softcover goes social. A new Friends tab streams what the people you follow are reading — progress updates, ratings, reviews, highlights and finished books — all rendered as editorial spreads, never noisy list rows. The Releases Calendar gains author and series tints so books you're following light up wherever they appear. Plus long-press shortcuts on the launcher icon: start a session, log progress, jump to your featured book.

- **Step 9.10** — Friend Feed (L) — *deps: 5.1, 5.3 (2.6.0)*
- **Step 6.13** — New Releases calendar: author/series follow tints (S) — *deps: 6.12 (2.8.0), 9.10 (same release)*
- **Step 9.5** — App shortcuts (S)

---

## 3.1.0 — Widgets + first motion polish

> **Release notes (Google Play):**
> Bring your reading to the home screen. Four new widgets: Currently Reading with wavy progress, a streak heatmap strip, a Quote of the Day rotating through your saved passages, and a Year in Books mini-stat. Tap any widget to land on a matching surface inside the app. Wear OS gains a Currently Reading complication and a quick-tile to start sessions. Long-press any cover anywhere in the app for a translucent peek.

- **Step 9.3** — Widgets (M) — *deps: 3.5 (2.4.0), 7.7 (2.10.0)*
- **Step 9.4** — Quote of the day surface + notification + widget link (S) — *deps: 9.3 (same release)*
- **Step 9.6** — Wear OS complication + quick-settings tile (M)
- **Step 10.1** — Long-press cover peek (M)
- **Step 10.5** — Reading-session breathe (S)

---

## 3.2.0 — Resilience + polish

> **Release notes (Google Play):**
> Read anywhere, sync anywhere. Reviews, progress, ratings and highlights logged offline now queue and sync the moment you reconnect — conflicts surface as a gentle shake. Back up everything you've created (highlights, sessions, reviews, lists) to a single archive, then restore it on a new device. TalkBack announcements are tuned for the editorial layouts. Visual touches: bookmark ribbons on finished books, cover-tinted hero scrims, audiobook waveforms under the wavy progress bar.

- **Step 9.7** — Offline mutation queue (M)
- **Step 9.8** — Backup & restore (M) — *deps: 8.5 (2.11.0)*
- **Step 9.9** — Voice & TalkBack polish (M)
- **Step 10.6** — Status callout ribbons + cover-tinted hero scrim (S)
- **Step 10.7** — Audiobook waveform + two-tone pacing bar (S)

---

## 3.3.0 — Final motion + long-tail features

Closing release for the current roadmap horizon. Heavy on small polish; the two genuinely new features (tags + library export) anchor it.

> **Release notes (Google Play):**
> The finishing pass. Book detail gains a parallax hero and a long-press flip to a generated jacket back. The bottom bar elegantly collapses while you scroll. Multiple new ways to log progress — voice, OCR the page number off a real page, or a thumb-friendly slider. Tag any book with anything you like and filter your library by it. Long-press a cover to swap editions without opening the book. Export any library view as a styled shelf-card image.

- **Step 10.2** — Hero parallax on book detail (S)
- **Step 10.3** — Bottom-bar collapse on scroll (M) — *now retunes for the 5-tab dock from 3.0.0*
- **Step 10.4** — Cover-back flip on long-press in detail (M)
- **Step 10.8** — Editorial flourishes (S)
- **Step 10.9** — Multi-method progress entry (M)
- **Step 10.10** — Tag system + custom list creation (M) — *deps: 5.3 (2.6.0)*
- **Step 10.12** — Inline edition swap from Library (S)
- **Step 10.14** — Library export as shelf card (S) — *deps: 8.5 (2.11.0)*

---

## Out-of-band / not scheduled

- **Step 11.1** — Book club / group reading (L). Stretch; not on the roadmap horizon. Bring back into the release plan only when there's a deliberate decision to commit to the social arc beyond the friend feed.

---

## Cross-release dependency map at a glance

```
2.2.0 (0.3)
  ├─> 2.4.0 (3.1, 3.5) ──> 2.10.0 (7.2, 7.7, 7.8)
  │                            └──> 2.11.0 (7.12)
  ├─> 2.5.0 (3.3, 3.4, 3.7) ─> 2.11.0 (7.12)
  └─> 2.8.0 (6.5) ───> 2.13.0 (8.11)

2.6.0 (5.1, 5.3) ──┬──> 2.7.0 (6.4)
                   ├──> 2.8.0 (6.9)
                   ├──> 3.0.0 (9.10)
                   └──> 3.3.0 (10.10)

2.7.0 (6.2) ──> 2.8.0 (6.12) ──> 3.0.0 (6.13)

2.10.0 (7.3) ──> 2.13.0 (8.10)
2.11.0 (8.5) ──┬──> 3.2.0 (9.8)
               └──> 3.3.0 (10.14)
2.12.0 (8.4) ──> 2.13.0 (9.1)
3.0.0 (5-tab dock) ──> 3.3.0 (10.3 retune)
```

The longest dependency chain is **2.2.0 → 2.4.0 → 2.10.0 → 2.11.0** (foundations → sessions → heatmap → activity calendar). Everything else branches off earlier than that, so the release order has comfortable slack — most releases can slip a slot without cascading.
