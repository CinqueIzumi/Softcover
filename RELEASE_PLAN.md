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

## 2.3.0 — Library filtering + book-detail metadata + custom-list MVP

> **Release notes (Google Play):**
> Filter your library by genre, format, year, ownership or rating — active chips show what's narrowing the view. Create custom lists with a name and add or remove any book straight from its detail page. Drag any book to reorder it within any shelf or list — Want-to-Read, Currently Reading, Read, or any custom list. Long-press a cover to enter bulk-select mode and move a stack of books between shelves, into a list, or out of your library in one go. Book detail now lists publisher, imprint and ISBN, with quick links out to Bookshop.org, Amazon, library.org and author sites. List changes made while offline or during a Hardcover hiccup now retry automatically the next time the app starts or your connection returns.

- **Step 2.2** — Library filter chips (M)
- **Step 2.5** — Bulk select mode (M) — long-press to enter selection; bulk actions for move-to-shelf, add-to-list (the same `ChooseListsBottomSheet` book detail uses, now generalised to multi-book with tristate membership) and remove. Subsumes the bulk-select wiring originally scheduled for 5.5 in 2.6.0.
- **Step 2.7** — Drag-to-reorder books within a custom list (M) — long-press + drag on a row in any custom list. Persists back to Hardcover via the list-position mutation; manual sort mode per list that coexists with Step 2.1's sort options.
- **Step 2.11** — Custom-list MVP: name-only creation + add/remove from book detail (M) — carved out of 5.3 + 5.5 and pulled forward. New `CreateList` + `AddListBook` mutations, name-only creation form, and an "Add to list" sheet on book detail. Rename/delete/reorder/share/curated discovery stay deferred to 2.6.0 (5.3); the ink-fill chip animation polish stays deferred to 2.6.0 (5.5). The bulk-select wiring was pulled forward into 2.5 in this release.
- **Step 2.12** — Persistent retry queue for list write mutations (S) — *deps: 2.7, 2.11 (same release)*. Narrow slice of 9.7 (offline mutation queue): Room-backed queue for `CreateList`, `AddListBook`, `RemoveListBook` and the custom-list reorder mutation, drained on app start and on network reconnect. Conflict UI and queueing for non-list mutations stay deferred to 9.7 in 3.2.0.
- **Step 2.13** — Drag-to-reorder books within built-in shelves (S) — long-press + drag on Want-to-Read, Currently Reading, Read. Local-only (stored in Room) — Hardcover does not model a user-defined position on these shelves, so no server mutation and no retry-queue entry. Manual sort mode per shelf that coexists with Step 2.1's sort options. Reading-screen ordering is deferred to a later release.
- **Step 4.4** — Publisher / imprint / ISBN inline (S)
- **Step 4.5** — External links strip (S) 

---

## 2.4.0 — Personal data lights up

The first release where the corpus from 0.3 surfaces to the user: rating, review, your own tags, a streak strip, and a reading-session surface that grows into a focus mode and a lock-screen ongoing notification — plus scan-to-add, a layer of book-detail editorial polish, and a batch of smaller fixes.

> **Release notes (Google Play):**
> Your reading gets personal. Rate and review any book — reviews now support bold, italic and hide-able spoilers. Add your own genre, mood, tag and content-warning tags (with spoiler flags). Track reading sessions with a live timer, Focus Mode and a lock-screen notification — pause, resume or update your page without opening the app; pausing skips idle time. A 21-day streak strip on Reading. Preview any edition before shelving, and scan a barcode to add a book or exact edition. Plus fixes.

- **Step 3.1** — Personal rating control on book detail (S) — *deps: 0.3*
- **Step 3.2** — Personal review drafting (M) — *deps: 0.3*
- **Step 3.5** — Reading session timer + Focus Mode + lock-screen notification (L) — *deps: 0.3*. Start/pause/resume/stop from the Reading featured card; pausing excludes idle time from the recorded duration (honest reading time). A persistent peek bar above the bottom nav shows the active book, a live timer and controls, and taps into **Focus Mode** — a distraction-free full-screen editorial surface (cover, `statHero` timer, inline page update via the shared `HeroStatNumberField`, pause/resume/stop). The lock-screen / shade surface is a **plain ongoing foreground-service notification** carrying the edition cover, a live chronometer, pause/resume + stop, and an inline "update page" reply — deliberately *not* a `MediaSession`/media notification (that competes with real audio apps like Spotify for the single media slot and drags in the speaker-output chip), and kept persistent via a re-post-on-dismiss `deleteIntent` since Android 14+ ignores `setOngoing` for FGS notifications. The activity uses `windowSoftInputMode=adjustNothing` so Compose owns the IME inset (single keyboard-avoidance source).
- **Step 3.8** — Streak strip on Reading screen header (S) — *deps: 3.5 (same release, ship in order)*
- **Improvement** — Preview & switch editions without a shelf entry (S) — *prerequisite for Step 6.8*. The book-detail "Change edition" action now works for any book, not just shelved ones. Picking an edition for a book with no user book is an ephemeral, never-cached preview (`BookDetailUiState.previewEdition`); a single `displayedEdition` resolver drives every edition-derived surface — cover, page count / duration, the publisher + ISBN strip, the "Find it" links, and the share card — while user-book-gated surfaces (mark-as-owned, progress, deadline) stay on the persisted edition. For a shelved book the selection still persists to the user book as before. Adding a previewed off-shelf book via Want-to-Read / Read creates the user book with the previewed edition (`edition_id` threaded through the create mutations).
- **Step 6.8** — Barcode scan to add books / editions (M) — *pulled forward from 2.9.0 and broadened*. A scan banner / affordance that resolves an ISBN/barcode to a specific edition and adds the book — or the exact edition — to the library, on top of the original drop-into-search behaviour. Camera-permission gate + offline ISBN-only fallback. An unrecognised ISBN is created on Hardcover from the scan rather than dropped, and editionless results are no longer discarded.
- **Step 10.10 (tagging slice)** — User tags on book detail (S) — *pulled forward from 3.3.0*. Add your own **Genre / Mood / Tag / Content-warning** tags to any shelved book through Hardcover's complete-set `upsert_tags` contract — naming a tag that doesn't exist creates it server-side, removing one re-sends the set without it, and each tag carries a per-tag spoiler flag. Tags render as a flat chip row below the shelf bar (above rating/review) and feed the personal reading-update share card, with spoiler-flagged tags excluded from the shareable image. The tag editor is free-text add (no `_ilike` search — disallowed by the API on cost). The library filter-by-tag and custom-list-creation halves of 10.10 stay deferred to 3.3.0.
- **Fixes** — Assorted smaller fixes and polish (S) — bucket for minor bug fixes batched into this release.

---

## 2.5.0 — Highlights, sessions log, reading log, personal tagging

Closes out Phase 3 — including the personal tagging, mood-logging and private-notes surfaces — and tucks in a few small Phase 4 wins (including awards, moved down from 2.4.0). This is a dense release; if it becomes too heavy in practice, the personal-tagging trio (3.9 / 3.10 / 3.11) is the natural slice to defer to a follow-up 2.5.x.

> **Release notes (Google Play):**
> Save the passages you love. A new Passages section on every book detail holds your highlights, and the Notes & Highlights inbox lets you search across every quote you've saved. A Reading Sessions log timelines every session you've tracked. Re-reads finally make sense — each book detail tracks every read-through separately with its own dates, rating and notes. Make any book yours: tag authors and books with the identities and representation that matter to you (LGBTQ+, BIPOC, country of birth, gender — all private), log how each book made you feel with personal moods (overall or chapter-by-chapter), and keep private notes against the book or any character — separate from the review you might publish. Book detail now surfaces literary awards and accolades when relevant. Content warnings get an opt-in reveal, and you can add your own private warnings alongside the community list. Reviews can be filtered by friends, top-rated, recent and language.

- **Step 3.3** — Personal highlights section + quick-add from Reading (M)
- **Step 3.4** — Notes & Highlights inbox screen (M)
- **Step 3.6** — Reading Sessions log screen (S)
- **Step 3.7** — Reading log (multiple read-throughs) (M)
- **Step 3.9** — Personal identity & representation tagging (M) — *deps: 0.3 (2.2.0)*. Author identity tags (gender, BIPOC, LGBTQ+, country of birth) + book representation tags (LGBTQ+ / BIPOC characters). Strictly local. Feeds 7.13 (2.10.0) and 7.14 (2.11.0).
- **Step 3.10** — Personal moods (book + chapter) (M) — *deps: 0.3 (2.2.0)*. Private mood log distinct from the community moods used for discovery.
- **Step 3.11** — Personal notes (book + characters) (M) — *deps: 0.3 (2.2.0)*. Private notes field separate from the personal review (3.2), with per-character notes when characters are available.
- **Step 4.2** — Awards & accolades (S) — *moved down from 2.4.0*.
- **Step 4.3** — Content warnings collapsible (S) — covers community-sourced trigger warnings.
- **Step 4.9** — Personal trigger warnings (S) — *deps: 4.3 (same release, ship in order)*. User-added warnings on top of community ones.
- **Step 4.7** — Reviews filters & sorts (S)

---

## 2.6.0 — Author, Series, Lists

Three new screens in one release. They're independent enough to land together and they all unblock subsequent discovery work.

> **Release notes (Google Play):**
> Three new screens. Tap any author byline for a full Author page with their works, accolades and series. Tap a series eyebrow for the full reading-order checklist and aggregate progress. Browse and manage your custom lists as a first-class surface, with rename, delete, reorder, share and a curated/community discovery section on top of the create-and-add flow that landed in 2.3.0. Finishing the last book in a series triggers a quiet celebration. Book detail gains genre and mood chips, plus audiobook finish-date predictions.

- **Step 5.1** — Author detail screen (M)
- **Step 5.2** — Series detail screen (M)
- **Step 5.3** — Lists screen (M) — standalone Lists surface + rename, delete, reorder, share, privacy, header fields, curated/community discovery. Basic name-only creation already shipped in 2.11 (2.3.0).
- **Step 5.4** — Series-completion cascade (S) — *deps: 5.2 (same release)*
- **Step 5.5** — Add-to-list polish: ink-fill chip animation (S) — *deps: 2.11 (2.3.0)*. Upgrades each row of the `ChooseListsBottomSheet` to the ink-fill chip animation (A.1.5) with the `commit` haptic per toggle. Core write path shipped in 2.11; bulk-select wiring shipped with 2.5 in 2.3.0.
- **Step 4.1** — Genre/mood chips (S)
- **Step 4.6** — Audiobook predicted finish (S)

---

## 2.7.0 — Discovery, first wave

Explore gets richer. Each step here is fundamentally a new section on an existing screen, so the release is mostly Compose work over data that's already reachable.

> **Release notes (Google Play):**
> Explore gets richer. Browse by genre or mood, separate New & Noteworthy from Most Anticipated, and discover Award winners across Booker, Pulitzer, Hugo and more. A curated Lists carousel lets you tour staff picks and themed collections. Audience now reads as its own filter — Young Adult, Middle Grade and New Adult sit alongside genre rather than being mixed into it. Every book detail page now suggests similar reads, and stalled series get gentle "pick this back up" nudges instead of quietly drifting.

- **Step 6.1** — Genre/mood browser (M)
- **Step 6.2** — New & noteworthy + Most anticipated (S)
- **Step 6.3** — Award winners carousel (S)
- **Step 6.4** — Curated lists carousel (S) — *deps: 5.3 (2.6.0)*
- **Step 6.10** — Similar books carousel on book detail (S)
- **Step 6.11** — Continue-series nudges (S)
- **Step 6.14** — Audience as a separate classification from genre (M) — *deps: 6.1 (same release, ship in order)*. Design phase first to confirm what's recoverable from Hardcover's tag taxonomy; surfaces an audience eyebrow on book detail and an independent audience filter on the genre/mood browser.

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

## 2.9.0 — Profile depth + sharing

> **Release notes (Google Play):**
> Profile becomes editable — set your name, bio and avatar in-app. A handful of new stats land: genre and rating distributions, reading seasons across twelve months, top authors, format split (print/ebook/audio) and personal records like longest haul and fastest read. Share a book as an editorial card image, a plain link or a send-to-a-friend deep link.

- **Step 7.1** — Edit profile + Settings shortcut (S)
- **Step 7.4** — Genre + rating distributions (M)
- **Step 7.5** — Reading seasons (S)
- **Step 7.6** — Author top-list + format split + records (S)
- **Step 4.8 (remaining modes)** — Share book sheet: link + deep-link modes (S) — *image mode shipped in 2.2.0; this release adds the three-mode sheet on top.*

*(Barcode scan moved to 2.4.0 — see Step 6.8, pulled forward and broadened to scan-to-add.)*

---

## 2.10.0 — Stats Atlas + Goals

The big personal-stats consolidation. Step 7.7 (12-week heatmap) ships here knowing it will be subsumed by 7.12 in 2.11.0; the interim shipping order is intentional because 7.12 depends on 7.7 being in the codebase.

> **Release notes (Google Play):**
> Set a yearly reading goal. A new Reading Challenge tile tracks your pace with a wavy progress bar, and a wizard helps you scope it — books, pages or genre diversity. A 12-week streak heatmap replaces the simple streak stat, and a time-of-day heatmap shows when you read most. The new Reading Stats Atlas pulls every chart into one long editorial spread, including a diversity & representation section driven by the author and book tags you've set yourself. Optionally toggle a public activity log so others can see your finishes.

- **Step 7.2** — Yearly reading challenge tile + screen (M) — *deps: 3.1 (2.4.0)*
- **Step 7.3** — Goal setup wizard (S)
- **Step 7.7** — Streak heatmap on Profile (M) — *deps: 3.5 (2.4.0); subsumed by 7.12 next release*
- **Step 7.8** — Time-of-day reading heatmap (M) — *deps: 3.5 (2.4.0)*
- **Step 7.9** — Reading Stats Atlas screen (M)
- **Step 7.10** — Public activity log (M)
- **Step 7.13** — Diversity & representation stats (M) — *deps: 3.9 (2.5.0), 7.9 (same release)*. Stats Atlas section driven by the user's private author identity and book representation tags.

---

## 2.11.0 — Calendars + recap + early Settings wins

7.12 folds 7.7's heatmap surface into the activity calendar in this release — the two-step path was a dependency artefact, not a duplication.

> **Release notes (Google Play):**
> The Reading Activity Calendar lands. A full month-grid view of every day you read, with the covers you touched, your sessions and your finishes shown in each day cell. Pinch out to a 12-month overview that replaces the standalone streak heatmap. Year in Books returns as a December recap with shareable slides — and now you can generate a wrap-up for any scope you like: a day, a week, a month, since you joined. Every slide exports as an editorial image card to share with friends who don't track their reading. New pace cards on each Reading row predict your finish date, and Library tabs surface an "Up against the clock" section when deadlines loom. Plus data export and finer account and privacy controls.

- **Step 7.12** — Reading Activity Calendar (M) — *deps: 3.5, 3.7 (2.4.0/2.5.0), 7.7 (2.10.0)*
- **Step 7.11** — Year in Books recap (M) — seasonal; gate the in-app surface behind a December trigger
- **Step 7.14** — Custom-scope wrap-up generator (M) — *deps: 7.11 (same release, ship in order)*. Generalises Year in Books to arbitrary scopes (day / week / month / year / since-join / custom range); each slide exports via the share surface (4.8 / 0.2). The year-scope route shares the generator with 7.11's December trigger.
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

- **Step 9.7** — Offline mutation queue (M) — *deps: 2.12 (2.3.0) for the underlying queue infra*. Extends the 2.12 queue to progress logging, session writes, ratings, reviews and highlights, and adds shake-on-conflict UI plus a surfaced pending-sync indicator.
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

- **Step 2.3** — Smart shelves as virtual tabs (M). Held back from 2.3.0; value not yet established. Re-evaluate once 2.3.0's filter chips (Step 2.2) have shipped — if users naturally reach for facet filtering to express "owned & unread" / "started but stalled" / "quick wins", smart shelves are redundant; if filtering alone doesn't surface those slices, bring this back into a future release.
- **Step 2.9** — "Since last read" delta on Reading rows (S). Pulled out of 2.3.0. Re-evaluate before bringing back into the plan.
- **Step 11.1** — Book club / group reading (L). Stretch; not on the roadmap horizon. Bring back into the release plan only when there's a deliberate decision to commit to the social arc beyond the friend feed.

---

## Cross-release dependency map at a glance

```
2.2.0 (0.3)
  ├─> 2.4.0 (3.1, 3.5) ──> 2.10.0 (7.2, 7.7, 7.8)
  │                            └──> 2.11.0 (7.12)
  ├─> 2.5.0 (3.3, 3.4, 3.7) ─> 2.11.0 (7.12)
  ├─> 2.5.0 (3.9) ──> 2.10.0 (7.13)
  └─> 2.8.0 (6.5) ───> 2.13.0 (8.11)

2.3.0 (2.11) ──> 2.6.0 (5.5)
2.3.0 (2.12) ──> 3.2.0 (9.7)

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

The longest dependency chain is **2.2.0 → 2.4.0 → 2.10.0 → 2.11.0** (foundations → sessions → heatmap → activity calendar). Everything else branches off earlier than that, so the release order has comfortable slack — most releases can slip a slot without cascading. The personal-tagging chain (2.2.0 → 2.5.0 → 2.10.0) and the wrap-up chain (… → 2.11.0 internal 7.11 → 7.14) both fit comfortably inside that envelope.
