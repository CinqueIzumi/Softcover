# ROADMAP.md

A long-form catalogue of upgrades for Softcover — split into *look & feel* (visual, motion, haptics, decoration) and *features* (new data, screens, expansions). This is a brainstorming surface, not a plan: items are not ordered or scoped here. The sequenced pickup order lives in [ROADMAP_STEPS.md](ROADMAP_STEPS.md).

> Style note: every entry below is meant to fit the editorial voice in [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md). Where an idea pulls toward "Material dashboard" rather than "editorial spread", that's flagged. Anything brand-new that adds a foundation, component, or pattern must update `DESIGN_SYSTEM.md` in the same change (per the maintenance rule).

---

## Part A — Look & feel

### A.1 Motion vocabulary

The system already has: press scale, mark-as-read burst, shake-on-error, lazy-item mutation, staggered entry, cover-to-detail morph, wavy progress, animated stat number, expressive easing.

The vocabulary still has gaps:

- **A.1.1 Long-press cover peek.** Long-press on any book cover (carousel card, library tile, reading featured cover, search result) → cover lifts slightly, dims its neighbours, and reveals a translucent peek card with title, byline, deadline state, and shelf chip. Releasing without dragging dismisses; dragging up commits to detail. Replaces the need to commit to navigation just to glance at a book.
- **A.1.2 Hero parallax on book detail.** Blurred backdrop scrolls at ~0.5x while the cover and metadata scroll at 1x. Adds depth without leaving the editorial register. Suppressed under reduced motion.
- **A.1.3 Tab-root cross-fade with vertical drift.** Currently `EnterTransition.None` between tabs. A 200ms cross-fade with a 12dp vertical drift makes tab changes feel like turning to a new spread in a magazine rather than instant teleportation.
- **A.1.4 Bottom-bar collapse on scroll.** Docked/floating bar collapses to a thin pill on downward scroll, expands again on upward scroll. Keeps content area larger while the reader is moving through a long shelf or detail page. Mirrors the print idea of a footer that retreats while you're mid-spread.
- **A.1.5 Shelf-chip ink-fill on selection.** When a shelf chip on book detail toggles to "selected", primary colour fills from the leading edge in a 180ms ink wipe rather than swapping background colour instantly.
- **A.1.6 Mark-as-read "slide to shelf".** When a book is committed to Read from the Reading screen, the row slides down/out, the bottom-nav Library tab does a single subtle "pulse" indicating the book has joined a shelf there. Connects the celebration to the destination, not just the moment.
- **A.1.7 Progress sheet number tween.** When the user types a number, the wavy progress bar tweens to the new percentage on a single 300ms ease; today this animates via `animateFloatAsState` but the *number* changes feel discrete on rapid typing — debounce by 80ms then tween.
- **A.1.8 Empty-state quote sway.** The decorative `quoteGlyph` in empty states does a very subtle, slow infinite drift (±2deg over 12s) — barely perceptible but stops the page feeling frozen. Suppressed under reduced motion.
- **A.1.9 Pull-to-refresh eyebrow swap.** While refreshing, the screen's eyebrow swaps from its normal label to a contextual one ("Refreshing your shelf…", "Catching up on your reading…"). Reverts on completion with a brief italic flash.
- **A.1.10 Carousel page edge hint.** Carousels show a 2dp accent bar tucked under the rightmost partially-visible card on first composition, fading after 1s, as a "scroll me" affordance — replaces the conventional left-edge gradient hint.
- **A.1.11 Cover-back flip on long-press in detail.** On book detail, long-pressing the hero cover flips it 180° to a generated "jacket back" with description, ISBN, publisher, format. A literal print metaphor that fits the editorial tone better than another sheet.
- **A.1.12 Stat number micro-tick.** When `AnimatedStatNumber` is tweening, a single hairline accent bar under the digit pulses on each integer crossing — turns the count-up into a perceptible event rather than a fade.
- **A.1.13 Drag-to-reorder for "Currently reading" priority.** Reordering a book in the Reading list uses a press-and-hold lift with a soft shadow, drop-with-snap. Pair with haptics (§A.2.4).
- **A.1.14 Reading-session timer breathe.** When a reading session is active (see B.2.1), the cover of the currently-reading book "breathes" with a 4s in/out scale of ±0.5%. Almost invisible — but if you look at the screen during reading, the book is alive.
- **A.1.15 Series-completion cascade.** When the user marks the *last* book of a series as Read, every cover of that series in the carousel does a short staggered fade-to-monochrome-then-back, ending in a "Complete" stamp overlay. A second tier of celebration above mark-as-read.

### A.2 Haptics vocabulary

The system has `commit` and `reject`. Two states is intentional, but a few extensions are worth considering.

- **A.2.1 `select` (light tick).** Adds a soft tick on neutral selection events: shelf chip toggle to a non-read state, segmented switch in the progress sheet, tab change in carousel filters. Distinct from `commit` (which celebrates) and `reject` (which rolls back).
- **A.2.2 `threshold` (single firm tap).** Fires when the user crosses a meaningful boundary — pull-to-refresh trigger point, drag-to-reorder pickup, long-press peek activation. Single firm tap, no celebratory texture.
- **A.2.3 `tickle` (rapid repeated soft taps).** While dragging a slider/picker (progress, rating). Each integer crossing fires one tick. Used on the rating control and the progress page-number input.
- **A.2.4 `lift` and `drop`.** Drag-to-reorder pickup → `lift`; settle → `drop`. Both are short, distinguishable taps. The haptic should *say* the item has left and returned to the page.
- **A.2.5 `milestone`.** A two-pulse haptic that fires once on natural progress events: completing a year-end reading goal, hitting a 30-day streak, finishing the last book in a series. Distinct from `commit` so the user feels "this is bigger than a save".

`Haptics helper` (DS §4) is the single entry point — adding these is an extension to that helper, not new call-site code.

### A.3 Visual refinements

- **A.3.1 Cover shadow + edge highlight tuned to cover colour.** `EditionImage` already paints a soft shadow; tint it from the cover's dominant edge colour rather than neutral so a teal cover sits on a teal-tinted bloom. Treat as a tonal change, not a glow.
- **A.3.2 Drop-cap on book descriptions.** First letter of the description on book detail rendered as a Fraunces drop-cap (3 lines tall, primary-tinted). A direct print-magazine borrow.
- **A.3.3 Print-style folio on stat tiles.** Hero stat tiles carry a small "—01—" / "—02—" folio number in the bottom corner using `eyebrowSmall`. Pure decoration but reinforces the "magazine spread" register.
- **A.3.4 Quote-glyph as section flourish.** Pages with a single dominant block of running prose (a long review, a book description, the profile bio) get a single huge low-alpha `quoteGlyph` floating in the page margin, anchored to the section's leading edge.
- **A.3.5 Page-edge serif on carousels.** A 1px hairline at the leading edge of every carousel section column, half-page tall, `onSurfaceVariant` at low alpha — the visual equivalent of the gutter line in a printed table of contents.
- **A.3.6 Dim mode (third theme).** In addition to light + dark, a "Dim" theme — warm low-contrast palette inspired by a reading lamp. Optional; sits as a third option in Appearance.
- **A.3.7 Editorial accent palettes.** Today primary is one fixed accent. Offer 3–4 named alternate palettes ("Vellum", "Ink", "Foxed", "Sea") that swap the primary/tertiary pair while keeping the editorial scheme. Branded as "Spine colour" in settings, not as a generic theme picker.
- **A.3.8 Cover-tinted hero scrim.** On book detail, the gradient over the blurred backdrop currently uses neutrals; pull a single dominant cover colour into the lower edge of the gradient so the hero feels like it's *of* the book, not generic.
- **A.3.9 Status callout ribbons.** When a book is marked Read, the status callout in detail gets a small "Finished — 12 Mar" ribbon icon — a literal bookmark ribbon glyph that fits the print register better than another chip.
- **A.3.10 Audiobook waveform under wavy progress.** Audiobook progress also paints a very low-alpha waveform stripe under the wavy bar — a visual reminder you're listening, not reading.
- **A.3.11 Two-tone wavy bar near goals.** When the wavy progress bar represents pacing (deadline pace, daily-goal pace), the part *behind today* paints in primary and the part *ahead of today* in primary at 40% alpha. The bar communicates *where you should be*, not just *where you are*.

### A.4 Decorative patterns

- **A.4.1 Pull-quote of the day.** A featured highlight (see B.4.7 — user highlights) surfaces on the Reading screen header or Profile, treated as an `Editorial quote` pattern (DS §5).
- **A.4.2 Spine row.** A horizontal row of cover *spines* (~24dp wide each) instead of full covers — used on Profile to show "Read in 2026" without monopolising vertical space. Each spine is the cover image cropped to the leftmost slice with the title rotated 90°. Pure typographic decoration.
- **A.4.3 Editorial divider.** A short Fraunces ornamental glyph (e.g. fleuron) between major sections on long screens like Profile and Settings — used at most once per scrollable page.
- **A.4.4 Folio footer.** Bottom of every root screen (just above the nav bar) renders a small "Softcover · No. 12" footer in `eyebrowSmall` with the local date. Doubles as a sign-off and reinforces the magazine register.

---

## Part B — Per-screen feature expansions

### B.1 Library

- **B.1.1 Sort within a tab.** A small sort affordance (paired with the existing layout switcher) supporting: date added, date finished, title, author, rating, progress, deadline urgency, page count. Persists per tab.
- **B.1.2 Filter chips above the grid.** Inline chip row showing the active filters (genre, format, year, owned, rating range) with mutation animations. Tap a chip to remove.
- **B.1.3 Smart shelves.** Auto-computed tabs alongside user statuses: "Owned & unread", "Started but stalled" (Currently Reading with no progress in 30d), "Finished this year", "Highest rated", "Quick wins" (<200pp), "Long hauls" (>500pp).
- **B.1.4 Bulk select mode.** Long-press a cover → enter selection mode with a top-bar swap (count + actions). Bulk actions: move shelf, mark as read, add to list, remove. Pairs with `select` haptic on each toggle.
- **B.1.6 Drag-to-reorder Want-to-Read.** The Want-to-Read tab supports manual ordering — a user-defined priority queue. Reorder uses A.1.13 + A.2.4 haptics.
- **B.1.7 Deadline urgency pinned section.** When any book on the active tab has a deadline within 14 days, render an "Up against the clock" editorial section at the top, separate from the grid.
- **B.1.8 Year filter on Read.** A horizontal year chip row over the Read tab (2026 · 2025 · 2024 · …). Tapping a year filters; combines with smart shelves.
- **B.1.9 Stats summary per tab.** Subtitle line under the page title shows tab-specific aggregate ("24 titles · 8,402 pages") instead of generic count copy.
- **B.1.10 Inline edition swap.** Long-press a tile → edition picker quick action without going into book detail.
- **B.1.11 Tag system.** User-defined freeform tags ("dnf-but-might-revisit", "lent-to-mom") visible as a chip strip under each book in list layout, and filterable from B.1.2.
- **B.1.12 Custom-list creation in-app.** Today lists are toggleable visibility but not creatable. Add a "+" entry at the tab strip that opens a sheet to create a list, then drag-to-fill from any shelf.
- **B.1.13 Library export.** Export current view as CSV or as a styled "shelf card" image (see B.7.4 sharing).

### B.2 Reading

- **B.2.1 Reading session timer.** "Start session" affordance on the featured card — captures duration, starting and ending page, can pause/resume. Sessions feed B.2.5 streaks and the Stats Atlas (C.3). Session UI lives as a peek bar above the bottom nav while active.
- **B.2.2 "Plan today" prompt.** Above the featured card, a one-line editorial nudge: "Aim for 32 pages today to stay on pace with *The Wager*." Dismissible. Built from deadline data already present.
- **B.2.3 Streak indicator.** A small heatmap-strip near the greeting: last 21 days, each day a small dot, today highlighted. Tap to expand to a fuller calendar view.
- **B.2.4 Reorder currently-reading priority.** Drag the order in which books are shown; first non-featured becomes featured. Use A.1.13 + A.2.4.
- **B.2.5 Pace card per book.** Below the progress strip in each compact row, an italic editorial line: "At your weekly average, you'll finish on 18 March." Lives next to the deadline line — both never appear together, the more informative one wins.
- **B.2.6 Quick-add highlight.** A "Save a passage" action on the featured card opens a tiny sheet for typing/dictating a quote + optional page number. Feeds the Notes & Highlights inbox (C.7).
- **B.2.7 Audiobook mini-player.** When the active book is an audiobook with a connected playback target (or just a local stopwatch), the featured card shows play/pause + 30s skips alongside the wavy bar. Editorial styling, not Material chrome.
- **B.2.8 "Since you last read" delta.** When opening the screen, the compact rows briefly show "+18 pages since yesterday" / "+34 min Tuesday" in the eyebrow slot, fading to the normal eyebrow after 3s.
- **B.2.9 Empty-state nudges that adapt.** The empty state today reads generically. Adapt it: if Want-to-Read is non-empty, surface the top 3 as "Pick up next"; if Want-to-Read is also empty, surface a Trending tile.
- **B.2.10 Multiple progress entry methods.** Today the sheet supports page/percent/time. Add: barcode/cover OCR of a page number (camera reads "247" off a real page), voice ("I'm on page two hundred forty-seven"), and slider on a thumb-friendly track for fast skim updates.

### B.3 Explore

- **B.3.1 Genre/mood browser.** A new editorial section "By the genre you're in" with chips: Fiction, Non-fiction, History, Memoir, Speculative, etc. Tapping enters a filtered Browse subscreen.
- **B.3.2 New & noteworthy.** A separate carousel for recent releases distinct from "Trending" (which is engagement-weighted).
- **B.3.3 Most anticipated.** Carousel of upcoming releases (uses the `UnreleasedBadge` component). "Releasing soon" → tap to add to Want-to-Read or set a release-day reminder.
- **B.3.4 "Because you read X" personalisation.** Algorithmic row keyed to the user's last-finished or top-rated book. Title eyebrow names the source ("BECAUSE YOU LOVED *PIRANESI*").
- **B.3.5 Award winners.** Curated carousel of Booker, Pulitzer, Hugo, Nebula, etc. Editorial section per category — could rotate the featured prize weekly.
- **B.3.6 Curated lists / staff picks.** A horizontally-scrolling tile of *lists* (not books). Each tile shows 3 stacked cover spines + a list title + curator avatar. Tapping enters the list screen (C.6).
- **B.3.7 Author spotlight.** A single full-width "Author of the week" tile pulling the author's photo + a one-line bio + their highest-rated work.
- **B.3.8 Search filters & sorts.** When a search is active, surface a chip row: year range, format (print/audio/ebook), rating threshold, language, page-count range. Sort: relevance, rating, year, popularity.
- **B.3.9 ISBN/barcode scan.** Floating action in the search bar opens the camera to scan a book in the wild. Result drops into search results.
- **B.3.10 Cover-art grid view.** A toggle on search results: text-rows (default) vs. cover-only grid for visual browsing. Reuses the cover-only mode the Library already implements.
- **B.3.11 Continue-series intelligence.** Today Explore shows a "Up next in your series" row. Add: "You haven't touched *Foundation* in 6 months — pick up where you left off?" — gentler re-engagement nudges as separate cards in the same row.

### B.4 Book detail

- **B.4.1 Personal rating.** A 5-star (or 1–10) personal rating field below the shelf-chip row, with `tickle` haptic on each star pass. Distinct from the community rating already shown.
- **B.4.2 Personal review.** A "Write a few words" affordance — opens a sheet with editorial typography in the input field. Stores draft locally; publish when the user is ready.
- **B.4.3 Personal highlights / quotes.** A "Voices" section already shows community reviews. Add a personal "Passages" section above it: highlights the user has saved from this book, with optional page numbers. Tappable to share or add as the home-widget quote.
- **B.4.4 Reading log (multiple read-throughs).** Some books get re-read. Replace the single-status approach with a log of read-throughs: each entry is start date + end date + rating + optional note. The summary line in detail shows "Read 2× — 2023, 2026".
- **B.4.5 Similar books carousel.** A "If this resonated…" section under reviews — algorithmic similar-book recommendations, editorial-styled.
- **B.4.6 Series carousel with progress.** Series eyebrow currently shows position; expand it into a real carousel of the full series with shelf state on each cover (read/unread/owned).
- **B.4.7 Genre & mood chips.** Tappable chip row under metadata. Tapping a chip drops into the Explore genre filter (B.3.1).
- **B.4.8 Awards & accolades.** When a book has awards, a small inline strip in the editorial section style: eyebrow "RECOGNITION" → italic display "Booker Prize, 2023".
- **B.4.9 Content warnings / trigger tags.** Collapsible "Warnings (4)" section near the about block — content notes from the community, opt-in to reveal.
- **B.4.10 Publisher / imprint / ISBN inline.** Today these are only in the edition picker. Surface a small metadata strip below the about block.
- **B.4.11 Author micro-card → author screen.** Tapping the byline opens an Author screen (C.5).
- **B.4.12 Share book sheet.** A "Share" action in the overflow menu opens a sheet with three share modes: shareable image card, plain text link, "send to a friend" deep link. Image card uses cover + title + user rating + a quote (if highlighted) — composed in the editorial visual style.
- **B.4.13 Add to a custom list.** Beyond shelves, "Add to a list…" sheet with the user's lists + ability to create new.
- **B.4.14 Audiobook ETA.** If audiobook and the user has a known listening pace (avg minutes/day), the deadline summary swaps to a predicted finish date.
- **B.4.15 External links.** Bookshop.org, Amazon, library.org, author website. A small "FIND IT" eyebrow with a row of icon-only links.
- **B.4.16 Reviews filters & sorts.** Inside the "Voices" section, chip row: friends only, top-rated, recent, with spoilers, in your language.
- **B.4.17 Lent-out tracking.** "Loaned to" field on owned editions — name + date + reminder option.
- **B.4.18 Add-to-list action sheet.** Today the data layer has read + remove-from-list but no add-to-list mutation, so "Owned" is added via a separate `MarkEditionAsOwned` mutation and other lists can only have books *taken out* of them. Adds a real add-to-list GraphQL mutation and surfaces a sheet from book detail (and from bulk-select in Library, B.1.4): the user's lists rendered with their current spine-count and a "create new list" entry at the bottom. Selecting a list toggles membership with the existing chip-ink-fill animation (A.1.5) and `commit` haptic. Differs from B.4.13 in that it ships the *write path* — B.4.13 assumed the mutation existed. Treat "Owned" as a special-cased list that still routes through `MarkEditionAsOwned` so the rest of the surface remains uniform.

### B.5 Profile

- **B.5.1 Edit profile.** Name, bio, avatar — editable in-app, not just read-only.
- **B.5.2 Yearly reading challenge.** A goal-of-N-books-this-year tile near the top: hero stat (books read / target) with wavy progress, italic editorial subhead ("You're 3 books ahead of pace"). Pair with `milestone` haptic on goal completion.
- **B.5.3 Streak with calendar heatmap.** Replace the single "streak" stat with an interactive 12-week heatmap. Tap a cell to see what was read that day.
- **B.5.4 Genre distribution.** A small editorial chart: each genre is a horizontal bar in a primary tint, ordered by share. The chart is captioned in italic body, not labelled as a "data viz".
- **B.5.5 Rating distribution.** Histogram of how the user rates their own books — surfaces whether the user is generous or stern. Editorial framing: "You rate kindly — average 4.1 across 84 books."
- **B.5.6 Reading time of year.** A 12-month bar chart of pages read per month, framed as "Your reading seasons" with italic copy.
- **B.5.7 Time-of-day reading heatmap.** Built from session data (B.2.1) — when in the day the user reads most. Framed as "You're a morning reader" / "You read late".
- **B.5.8 Author top-list.** "Most read by" — a list of the user's top 5 authors with read count + average rating.
- **B.5.9 Format split.** Hairline pie or simple ratio bar: print / ebook / audiobook.
- **B.5.10 Longest book / fastest read.** A "Records" tile: "Your longest haul: *The Power Broker*, 1,296 pp." / "Your quickest read: *The Old Man and the Sea*, 2 days."
- **B.5.11 Public activity log.** A toggleable timeline ("On 12 Feb you finished *Lonesome Dove*…"). Could double as the social-share source.
- **B.5.12 Year in books card.** A shareable end-of-year recap (see C.4).
- **B.5.13 Follow / followers.** If Hardcover supports social, surface follower lists with a follow button.
- **B.5.14 Settings shortcut.** Subtle gear icon in the top-right of profile so users don't have to bounce to the Settings tab.

### B.6 Settings

- **B.6.1 Notification controls.** Toggles for: deadline reminders, daily reading nudge, weekly recap, release-day reminders (for Want-to-Read books with future release dates), friend activity (if social lands). Each is a switch row with editorial copy.
- **B.6.2 Theme variants.** Light / Dark / Dim / System with a preview tile. Dynamic Material You toggle. "Spine colour" picker for editorial accent palettes (A.3.7).
- **B.6.3 Accessibility.** Explicit reduced-motion toggle (independent of system), text-size adjustment for book descriptions and reviews, high-contrast variant, screen reader announcements toggle.
- **B.6.4 Data sync controls.** Sync frequency, Wi-Fi only toggle, background sync limits.
- **B.6.5 Data export.** CSV of library, JSON of highlights/notes, image cards of any view.
- **B.6.6 Data import.** Goodreads CSV, Storygraph CSV, simple ISBN list. Maps to Hardcover via API.
- **B.6.7 Cache management.** Clear image cache, clear search history, sign out everywhere.
- **B.6.8 Account.** Change API key in-app (not just on first run), revoke, link other accounts.
- **B.6.9 Privacy.** Public/private reading toggle, hide finish dates, hide ratings, anonymize activity.
- **B.6.10 Language / region.** App language picker (where translations exist), region for date formats and currency conversion if external links lead to shops.
- **B.6.11 Widgets settings.** Per-widget configuration (which book, which stat, refresh frequency).
- **B.6.12 Default tab on launch.** Reading / Library / Explore as the landing tab.
- **B.6.13 Default progress unit.** Page vs percent vs time (per format).
- **B.6.14 About.** OSS licenses, third-party credits, contact, GitHub link, version (already shown).

### B.7 Onboarding

- **B.7.1 Reading goal setup step.** A fourth onboarding page after API key: "How many books this year?" with a styled `statLarge` numeric field and three preset chips (12 / 24 / 50). Skippable.
- **B.7.2 Theme pick step.** Visual preview tiles for Light / Dark / Dim. Skippable.
- **B.7.3 Import step.** "Coming from Goodreads or Storygraph? Import your shelves." File picker for CSV, with editorial loading sheet. Skippable.
- **B.7.4 Notification opt-in.** A native dialog request gated by a small editorial copy block explaining what notifications they'd receive. Skippable.
- **B.7.5 Curated starter list.** "Pick three books you love" grid — feeds the personalisation engine (B.3.4).
- **B.7.6 API key error UI.** Inline error state below the input field on invalid key, with retry guidance. Today the failure is silent.
- **B.7.7 "What's new" mini-onboarding on upgrade.** A one-page editorial spread shown after major releases summarising new features. Auto-dismissable.

---

## Part C — New screens

### C.1 Author detail
Cover-style hero (author photo blurred + portrait card), eyebrow "AUTHOR", italic display name, bio in italic body, "Works" carousel (with shelf states on each cover), "Most acclaimed" highlight, "Series" sub-list, optional "Influences" if data exists. Reached from any byline.

### C.2 Series detail
A spine row of all books in the series (covers + position numbers), reading-order checklist (read / reading / unread), aggregate progress stat ("3 of 7 read"), editorial summary, "Read order" toggle (publication vs in-universe). Reached from the series eyebrow on book detail and from carousel cards labelled with series.

### C.3 Reading Stats Atlas
A dedicated stats deep-dive screen pushed from the Profile. Pages: Overview (the existing 4 hero stats), Year (B.5.6), Genres (B.5.4), Ratings (B.5.5), Pace (B.5.7), Authors (B.5.8), Format (B.5.9), Records (B.5.10), Streaks (B.5.3). Editorial section per page; rendered as one long scrollable spread, not a tabbed view.

### C.4 Year in Books
A standalone, time-limited screen surfaced from Profile (and via a notification in Dec). 8–10 editorial slides: books read, top genre, top author, most surprising rating, longest book, the quote you saved most, the month you read the most, your top-rated book, the friend who shared the most overlap. Each slide is a shareable image card. Echoes Spotify Wrapped but tuned to the print register.

### C.5 Lists screen (root tab candidate, or settings sub-page)
The user's custom lists + curated/community lists. Lists are first-class — covers stacked as spines on the index, items inside shown with the same shelf anatomy as the library. CRUD: create, rename, reorder books in list, share list, follow others' lists.

### C.6 Reading Challenge
A focused screen for the active yearly goal (and historical goals). Hero stat (books / target), italic pace summary, the actual books read so far rendered as covers in a calendar grid (one per finish-date cell), goal-edit affordance, share button. Surfaces `milestone` haptic on completion.

### C.7 Notes & Highlights
An inbox of every quote/highlight the user has saved across books, sorted by date or grouped by book. Each highlight is rendered with the `Editorial quote` pattern (DS §5) — the quote glyph, italic pull quote, byline naming the book and page. Filters by book, search across all highlights, share an individual highlight as an image card.

### C.8 Reading Sessions log
A timeline view of all reading sessions: date, book, duration, pages added. Stacked as cards in reverse chronological order with the cover at the leading edge. Doubles as the source for "Time-of-day reading" (B.5.7) and the streak.

### C.9 Recommendations / For You
A personalisation screen reached from Explore. Three-tier layout: "Because you finished X" (3 rows), "Because you love Author Y" (rows), "Picked from your saved highlights" (a sentiment-matching row). Editorial framing — not a "you may also like" grid.

### C.10 Activity feed / Notifications
A subdued inbox of soft nudges and friend-activity items. Notifications: "*The Wager* releases tomorrow." / "You haven't logged a session in 6 days." / "Maya finished *Piranesi* — rated 5★." Each item is an editorial row, never a Material list-item. Pull-to-refresh, swipe-to-dismiss.

### C.11 Quote of the day
A single editorial spread, opened from the home-screen widget or a notification, showing one of the user's saved highlights styled as a full-screen pull quote. Tap → goes to the book; share → image export.

### C.12 Book club / group reading
(*Stretch.*) A surface for a small group to read together — shared annotations, current-pages tracking, a chat panel. Out of scope for a single contributor but worth flagging as the natural endpoint of the social arc.

### C.13 Goal setup wizard
A standalone wizard reached from settings/Profile to set a reading challenge: book count, page count, genre diversity, mood diversity. Powers C.6.

### C.14 Reading Activity Calendar
*Rough plan — keep loose until Phase 7's session data lands.* A full-screen calendar (month grid, with a year-overview zoom-out) where each day cell shows what the user *did* that day with their reading: pages read, time read, finishes, ratings published, highlights saved. Day cells render editorial-style — a tiny stacked spine row of covers touched that day, with the dominant cover acting as a tinted background. Tap a day → an editorial sheet with the full per-day breakdown (sessions, page deltas, books touched, links into book detail / sessions log / highlights). Pinches to a 12-month overview that doubles as the streak heatmap (subsumes B.5.3 in its richer form). Reached from Profile, from the Reading screen's streak strip (B.2.3), and from the Stats Atlas (C.3). Sources: reading sessions (B.2.1 / Phase 3), reading log finish dates (B.4.4), personal highlights (B.4.3). Out of scope for the first cut: forward-looking "planned reading" entries — those would collide with deadline pacing already covered elsewhere.

### C.15 New Releases Calendar
*Rough plan — keep loose until B.3.3 lands.* A calendar surface that flips the activity calendar inside-out: instead of past activity, future-dated book releases plotted onto a month grid. Day cells highlight when a book on the user's Want-to-Read shelf releases (primary tint, full intensity) and when followed authors / followed series release anything (lower-intensity tint). Optional toggle to include "Most anticipated" globally curated releases (B.3.3) as a third tint. Tap a day → editorial sheet listing the releases with quick-actions (set release-day reminder via D.1, jump to book detail, pre-order link via B.4.15). Reached from Explore (a "Coming up" tile next to the existing carousels) and from Want-to-Read in Library. Sources: edition `release_date` already present on book data, the Want-to-Read shelf, the future-author-follow surface (B.5.13). The author/series follow tints are deferred until follow infra exists; first cut can ship Want-to-Read only and still feel complete.

### C.16 Friend Feed
A new root-level surface as **the fifth bottom-nav tab** (preferred) — fallback host if the 5-tab dock proves too crowded is a segmented switcher at the top of Profile (Profile / Friends), never Explore (would conflate discovery and social). Shows a chronological stream of activity from people the user follows on Hardcover. Each entry is an editorial row, never a Material list-item — eyebrow with the friend's display name + relative time ("MAYA · 2H AGO"), an italic verb phrase describing the event ("started reading", "finished", "rated 4 stars", "added to *Owned*", "saved a passage from", "wants to read"), the book cover at the leading edge, and an optional editorial pull-quote when the event is a review or highlight. Event types in scope, mapped to existing data:
  - **Status changes** — want-to-read / currently-reading / read / DNF (Hardcover exposes per-user reading state).
  - **Progress updates** — "Maya is on page 247 of *Piranesi*" (uses the same data backing B.2.8 and progress mutations).
  - **Reviews & ratings** — surfaces friend reviews as full editorial pull-quotes with byline; tap to open the review on the book detail Voices section, filtered to friend (B.4.16).
  - **Highlights** — when friends share saved passages publicly.
  - **List activity** — "Maya added 3 books to *Best of 2026*" with a spine row of the additions.
  - **Goal milestones** — "Maya finished her 2026 challenge" (fires `milestone` haptic, A.2.5, when surfaced).
Patterns: pull-to-refresh with an eyebrow swap (A.1.9), reduced-motion-aware enter staggers, long-press an event to peek into the book (A.1.1). Per-friend muting is **explicitly out of scope** for the first cut. Detail interactions route into existing screens (book detail, author detail when C.1 lands, list detail when C.5 lands), so the feed stays a *surface* and not a destination. Cross-cuts with C.10 (Activity feed / Notifications): the notifications inbox is *yours* (deadlines, releases, your-streak), the friend feed is *theirs* (other people's reading); both can share the same editorial row primitive but their information architecture stays separate. Hardcover's API exposes the follow graph + activity events directly, so the feed reads from a real source rather than a synthesised poll loop.

---

## Part D — Cross-cutting capabilities

### D.1 Notifications
Local + (optional) push. Triggers: deadline reminders, release-day for Want-to-Read futures, weekly recap, monthly milestone, year-end recap drop, friend activity. All editorial copy, no system-default boilerplate. Each is opt-in (B.6.1).

### D.2 Widgets
- **D.2.1 Currently reading.** Cover + title + wavy progress + page count, tap to open Reading.
- **D.2.2 Streak.** Heatmap strip of the last 14 days + today's session count.
- **D.2.3 Quote of the day.** Pulls from user highlights, refreshes daily.
- **D.2.4 Year in books.** Pages-read hero stat with sparkline of monthly cadence.

### D.3 Sharing surface
A single shared "share card" composition that renders into PNG/JPG: editorial layout, cover + a chosen stat or quote + Softcover sign-off. Used by Book detail, Profile, Library, Year in Books. All shares route through this surface so the brand is consistent.

### D.4 Wear OS / quick-settings tile
- Currently-reading complication for Wear faces.
- Quick-tile in Android quick settings → start/stop a reading session without opening the app.

### D.5 App Shortcuts (long-press launcher icon)
- "Start a reading session"
- "Mark current book +20 pages"
- "Open featured book"
- "Add by ISBN"

### D.6 Voice & accessibility actions
- "Hey Google, add a session to *The Wager*."
- Talkback-friendly editorial announcements ("Book detail: *Lonesome Dove* by Larry McMurtry, currently reading, 312 of 945 pages.")

### D.7 Offline / sync improvements
- Today the app shows an offline placeholder on Explore. Extend offline reading to: writing reviews, logging sessions, marking progress, all queued and synced on reconnect with mutation-rejection shake on conflict.

### D.8 Backups & restore
- Export all user-generated content (highlights, sessions, reviews, lists) as a single archive. Restore in Settings. Doubles as account migration if Hardcover moves.

### D.9 Testing & telemetry of motion
- A "Motion debug" hidden screen (long-press version footer 5x) that lets the team trigger every animation and haptic in isolation, audit reduced-motion behaviour, and verify timing. Not for end users.

### D.10 Per-feature in-app changelog
- A small "What's new" route from Settings → About showing per-version editorial release notes. Pairs with B.7.7.

---

## Part E — Items deliberately *not* on the list

For completeness — things that look obvious but the editorial register argues against.

- **Carousels with auto-advancing slides.** Foreign to the print register; never on this app.
- **Emoji reactions on reviews.** Material-app pattern; the editorial voice would prefer prose responses or stars.
- **Toast-style mass notifications.** Push the user out of the editorial spread. Use the in-app activity feed (C.10) instead.
- **Gamification badges with cartoon icons.** Achievements are fine, but rendered as engraved-looking "marks" in `quoteGlyph` style, not as game-app badges.
- **A separate "Trending now" with stale data.** Trending exists in Explore (B.3) — don't duplicate.
- **Filled-icon families on a single surface.** Already a foundation rule (DS §2.6). Restating because B.3 and B.4 will tempt new icon usage.
