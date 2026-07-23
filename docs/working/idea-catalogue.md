# Idea catalogue
A long-form catalogue of upgrades for Softcover — split into *look & feel* (visual, motion, haptics, decoration) and *features* (new data, screens, expansions). This is a brainstorming surface, not a plan: items are not ordered or scoped here. The sequenced pickup order lives in [roadmap-steps.md](roadmap-steps.md); the public, user-facing view is [ROADMAP.md](../../ROADMAP.md).

> Style note: every entry below is meant to fit the editorial voice in [design-system.md](../reference/design-system.md). Where an idea pulls toward "Material dashboard" rather than "editorial spread", that's flagged. Anything brand-new that adds a foundation, component, or pattern must update the relevant section under `../reference/design-system/` in the same change (per the maintenance rule).

> **Maintenance rule.** A **shipped idea is deleted from this file** — what's left is what's still wanted. Tags are **never reused or renumbered**, so a gap (or a reference from another doc to a tag that isn't here) means "that one shipped", exactly as with `roadmap-steps.md`. Where an idea shipped only in part, the entry stays and is narrowed to the part that remains.

---

## Part A — Look & feel

### A.1 Motion vocabulary

The system already has: press scale, mark-as-read burst (with the slide-to-shelf exit + Library tab pulse), shake-on-error, lazy-item mutation, staggered entry, cover-to-detail morph, tab-root cross-fade with vertical drift, wavy progress, animated stat number with its integer-crossing micro-tick, expressive easing.

The vocabulary still has gaps:

- **A.1.1 Long-press cover peek.** Long-press on any book cover (carousel card, library tile, reading featured cover, search result) → cover lifts slightly, dims its neighbours, and reveals a translucent peek card with title, byline, deadline state, and shelf chip. Releasing without dragging dismisses; dragging up commits to detail. Replaces the need to commit to navigation just to glance at a book.
- **A.1.2 Hero parallax on book detail.** Blurred backdrop scrolls at ~0.5x while the cover and metadata scroll at 1x. Adds depth without leaving the editorial register. Suppressed under reduced motion.
- **A.1.4 Bottom-bar collapse on scroll.** Docked/floating bar collapses to a thin pill on downward scroll, expands again on upward scroll. Keeps content area larger while the reader is moving through a long shelf or detail page. Mirrors the print idea of a footer that retreats while you're mid-spread.
- **A.1.5 Shelf-chip ink-fill on selection.** When a shelf chip on book detail toggles to "selected", primary colour fills from the leading edge in a 180ms ink wipe rather than swapping background colour instantly.
- **A.1.7 Progress sheet number tween.** When the user types a number, the wavy progress bar tweens to the new percentage on a single 300ms ease; today this animates via `animateFloatAsState` but the *number* changes feel discrete on rapid typing — debounce by 80ms then tween.
- **A.1.8 Empty-state quote sway.** The decorative `quoteGlyph` in empty states does a very subtle, slow infinite drift (±2deg over 12s) — barely perceptible but stops the page feeling frozen. Suppressed under reduced motion.
- **A.1.9 Pull-to-refresh eyebrow swap.** While refreshing, the screen's eyebrow swaps from its normal label to a contextual one ("Refreshing your shelf…", "Catching up on your reading…"). Reverts on completion with a brief italic flash.
- **A.1.10 Carousel page edge hint.** Carousels show a 2dp accent bar tucked under the rightmost partially-visible card on first composition, fading after 1s, as a "scroll me" affordance — replaces the conventional left-edge gradient hint.
- **A.1.11 Cover-back flip on long-press in detail.** On book detail, long-pressing the hero cover flips it 180° to a generated "jacket back" with description, ISBN, publisher, format. A literal print metaphor that fits the editorial tone better than another sheet.
- **A.1.13 Drag-to-reorder for "Currently reading" priority.** Reordering a book in the Reading list uses a press-and-hold lift with a soft shadow, drop-with-snap. The gesture and its `lift`/`drop` haptics already exist on the library's custom-list grids and the settings tab order — this is about bringing them to the Reading list, not building them.
- **A.1.14 Reading-session timer breathe.** While a reading session is active, the cover of the currently-reading book "breathes" with a 4s in/out scale of ±0.5%. Almost invisible — but if you look at the screen during reading, the book is alive.
- **A.1.15 Series-completion cascade.** When the user marks the *last* book of a series as Read, every cover of that series in the carousel does a short staggered fade-to-monochrome-then-back, ending in a "Complete" stamp overlay. A second tier of celebration above mark-as-read.

### A.2 Haptics vocabulary

The foundation `Haptics` helper (DS §4) now carries the full vocabulary — `commit`, `reject`, `select`, `threshold`, `lift`, `drop`, `tickle`, `milestone` — and `select` / `threshold` / `lift` / `drop` are wired into real surfaces (shelf chips, library filters, the streak strip, pull-to-refresh, list drag-reorder).

Two exist in the helper but have **no production call site** — the gap is the call sites, not the haptic:

- **A.2.3 `tickle` (rapid repeated soft taps).** While dragging a slider/picker. Each integer crossing fires one tick. Wants the rating control and the progress page-number input.
- **A.2.5 `milestone`.** A two-pulse haptic for natural progress events: completing a year-end reading goal, hitting a 30-day streak, finishing the last book in a series. Distinct from `commit` so the user feels "this is bigger than a save". Its call sites arrive with the reading challenge (B.5.2) and the series-completion cascade (A.1.15) — wire it then rather than inventing an event for it now.

### A.3 Visual refinements

- **A.3.1 Cover shadow + edge highlight tuned to cover colour.** `EditionImage` already paints a soft shadow; tint it from the cover's dominant edge colour rather than neutral so a teal cover sits on a teal-tinted bloom. Treat as a tonal change, not a glow.
- **A.3.3 Print-style folio on stat tiles.** Hero stat tiles carry a small "—01—" / "—02—" folio number in the bottom corner using `eyebrowSmall`. Pure decoration but reinforces the "magazine spread" register.
- **A.3.4 Quote-glyph as section flourish.** Pages with a single dominant block of running prose (a long review, a book description, the profile bio) get a single huge low-alpha `quoteGlyph` floating in the page margin, anchored to the section's leading edge.
- **A.3.5 Page-edge serif on carousels.** A 1px hairline at the leading edge of every carousel section column, half-page tall, `onSurfaceVariant` at low alpha — the visual equivalent of the gutter line in a printed table of contents.
- **A.3.6 Dim mode (third theme).** In addition to light + dark, a "Dim" theme — warm low-contrast palette inspired by a reading lamp. Optional; sits as a third option in Appearance.
- **A.3.7 Editorial accent palettes.** Today primary is one fixed accent. Offer 3–4 named alternate palettes ("Vellum", "Ink", "Foxed", "Sea") that swap the primary/tertiary pair while keeping the editorial scheme. Branded as "Spine colour" in settings, not as a generic theme picker.
- **A.3.8 Cover-tinted hero scrim.** On book detail, the gradient over the blurred backdrop currently uses neutrals; pull a single dominant cover colour into the lower edge of the gradient so the hero feels like it's *of* the book, not generic.
- **A.3.9 Status callout ribbons.** When a book is marked Read, the status callout in detail gets a small "Finished — 12 Mar" ribbon icon — a literal bookmark ribbon glyph that fits the print register better than another chip.
- **A.3.10 Audiobook waveform under wavy progress.** Audiobook progress also paints a very low-alpha waveform stripe under the wavy bar — a visual reminder you're listening, not reading.
- **A.3.11 Two-tone wavy bar near goals.** When the wavy progress bar represents pacing (deadline pace, daily-goal pace), the part *behind today* paints in primary and the part *ahead of today* in primary at 40% alpha. The bar communicates *where you should be*, not just *where you are*.
- **A.3.12 Auto-resize coverless title text.** On coverless book placeholders, shrink the title font to fit cleanly rather than breaking words across lines, keeping long titles legible on the typographic tile.

### A.4 Decorative patterns

- **A.4.1 Pull-quote of the day.** A featured highlight (see B.4.7 — user highlights) surfaces on the Reading screen header or Profile, treated as an `Editorial quote` pattern (DS §5).
- **A.4.2 Spine row.** A horizontal row of cover *spines* (~24dp wide each) instead of full covers — used on Profile to show "Read in 2026" without monopolising vertical space. Each spine is the cover image cropped to the leftmost slice with the title rotated 90°. Pure typographic decoration.
- **A.4.3 Editorial divider.** A short Fraunces ornamental glyph (e.g. fleuron) between major sections on long screens like Profile and Settings — used at most once per scrollable page.
- **A.4.4 Folio footer.** Bottom of every root screen (just above the nav bar) renders a small "Softcover · No. 12" footer in `eyebrowSmall` with the local date. Doubles as a sign-off and reinforces the magazine register.

---

## Part B — Per-screen feature expansions

### B.1 Library

- **B.1.3 Smart shelves.** Auto-computed tabs alongside user statuses: "Owned & unread", "Started but stalled" (Currently Reading with no progress in 30d), "Finished this year", "Highest rated", "Quick wins" (<200pp), "Long hauls" (>500pp).
- **B.1.7 Deadline urgency pinned section.** When any book on the active tab has a deadline within 14 days, render an "Up against the clock" editorial section at the top, separate from the grid.
- **B.1.10 Inline edition swap.** Long-press a tile → edition picker quick action without going into book detail.
- **B.1.11 Tag system (remaining half).** The tag *write* path shipped — a tag editor on book detail backed by a save-tags mutation. What's left is the **library side**: a chip strip of the user's own tags under each book in list layout, and filtering the library by them. Today the library filter chips filter *community genre tags*, not the user's own — so a user can tag a book "lent-to-mom" and then not be able to find it that way.
- **B.1.12 Custom-list creation in-app.** Today lists are toggleable visibility but not creatable. Add a "+" entry at the tab strip that opens a sheet to create a list, then drag-to-fill from any shelf.
- **B.1.13 Library export.** Export current view as CSV or as a styled "shelf card" image (see B.7.4 sharing).
- **B.1.14 Swipe between shelves.** Restore the horizontal swipe across library shelves that the 3.1.0 masthead redesign retired along with the pill tab row. Mobile only (desktop switches shelves from its permanent sidebar). A `HorizontalPager` backs the content area, the masthead follows the drag at its halfway point, and the tab-select action fires only once the pager settles. Because the tab row is gone, the swipe would otherwise be blind — so it ships with a **shelf neighbour rail**: one hairline row naming the shelf either side, each end tappable to move one across. The whole thing sits behind an **opt-in** "Swipe between shelves" preference (Settings → Appearance → Display, default **off**), so the redesign's sheet-only switcher stays the out-of-the-box behaviour and the gesture is there for the readers who miss it. The pager stays the container either way, so there is one presentation to maintain rather than two. *(User request.)*
- **B.1.15 Deadline readout back on Library and Reading.** Restore the `DeadlineSummaryLine` — the deadline date plus the per-day pace still needed to make it — to the three places the 3.1.0 redesigns dropped it: Library's List (large) row, the Reading featured-hero card, and the Reading secondary rows. All three kept a *status* mark (a badge or a status word) but lost the two facts a reader actually plans around, "by when" and "how much a day", leaving book detail the only screen that still answered either. The redesign rationale was that the countdown badge and the wavy progress line already carried a book's reading state; they carry how it's going, not what it would take. Book detail's fuller `DeadlineRow` (which additionally names how far behind schedule the reader is) was never touched and stays as-is, so this is a restoration to the 3.0.0 readout rather than a new design. *(User request.)*

### B.2 Reading

- **B.2.4 Reorder currently-reading priority.** Drag the order in which books are shown; first non-featured becomes featured. Needs an ordering preference to persist (there is none today); the drag gesture and haptics come free from A.1.13.
- **B.2.5 Pace card per book.** Below the progress strip in each compact row, an italic editorial line: "At your weekly average, you'll finish on 18 March." Lives next to the deadline line — both never appear together, the more informative one wins.
- **B.2.6 Quick-add highlight.** A "Save a passage" action on the featured card opens a tiny sheet for typing/dictating a quote + optional page number. Feeds the Notes & Highlights inbox (C.7).
- **B.2.7 Audiobook mini-player.** When the active book is an audiobook with a connected playback target (or just a local stopwatch), the featured card shows play/pause + 30s skips alongside the wavy bar. Editorial styling, not Material chrome.
- **B.2.8 "Since you last read" delta.** When opening the screen, the compact rows briefly show "+18 pages since yesterday" / "+34 min Tuesday" in the eyebrow slot, fading to the normal eyebrow after 3s.
- **B.2.10 Multiple progress entry methods.** Today the sheet supports page/percent/time. Add: barcode/cover OCR of a page number (camera reads "247" off a real page), voice ("I'm on page two hundred forty-seven"), and slider on a thumb-friendly track for fast skim updates.
- **B.2.11 Direct add to Currently Reading.** A one-step "start reading" action (from book detail's shelf bar, search, and the add flows) that puts a book straight onto Currently Reading rather than add-to-shelf-then-change-status. May chain create → set-status (→ start date) mutations on top of the shipped create-userBook path. Pairs with session start (B.2.1).
- **B.2.12 Log a progress update at a chosen date & time.** Today a progress update is stamped "now" (server-side). Add a date & time picker to the progress-entry flow so the user can record that they read to a given page/percent at an earlier moment ("I read to here at 9 pm yesterday"). **The progress write path already exists and works** — `UpdateReadingProgress` → `update_user_book_read` with a `DatesReadInput` (`BooksRemoteDataSource.updateBookProgress`). `DatesReadInput` **already carries `action_at: timestamptz`** (full date + time) **and `action: String`**, but both are left `Optional.Absent` today, so the server infers the event and timestamps it now. Backdating is therefore the *only* missing capability: pass a user-chosen `action_at`, set `action = "progress_updated"` (already in code as `JournalEventType.ProgressUpdated.eventName`), thread the timestamp through `updateBookProgress` **and** its offline-replay twin `replayUpdateBookProgress`, and surface a date/time picker in the progress sheet (default now, overridable). No new mutation, no widened fragment, no empirical `event` discovery — those were mistaken assumptions in the original triage note, which wrongly routed this through a non-existent `reading_journals` / `insert_reading_journal` write and wrongly claimed `DatesReadInput` has no per-update timestamp. **The backdate also applies to finishing**: when a backdated progress update reaches 100% (or the sheet's "Mark as Read" is used with a picked time), the finish is dated too — the finish path (`MarkBookAsRead` → `insert_user_book` with `UserBookCreateInput`) sets `user_date` to the picked local date and `action_at` to the full timestamp (that input has no `action` field). Still distinct from general read-through `started_at`/`finished_at` editing (B.4.4 / Step 3.7), which is arbitrary post-hoc date editing of the read row, not dating the finish as it happens. *(User request.)*
- **B.2.13 Edit & delete past reading-progress entries.** A reading-history management surface: let the user correct or remove a progress entry after the fact (fix a wrong page, re-date it, delete a mis-logged bump), not just append new ones as B.2.12 allows. This is a genuinely new capability — the app has no delete/amend path for a logged entry today. Implementation route to be settled during the step (direct `reading_journals` CRUD — `update_reading_journal` / `delete_reading_journal` exist in the schema and `ReadingJournalUpdateType` also carries `action_at` — vs. amending the `user_book_read` row). Reuses B.2.12's date/time picker for the re-date affordance. *(User request.)*

### B.3 Explore

- **B.3.1 Genre/mood browser.** A new editorial section "By the genre you're in" with chips: Fiction, Non-fiction, History, Memoir, Speculative, etc. Tapping enters a filtered Browse subscreen.
- **B.3.2 New & noteworthy.** A separate carousel for recent releases distinct from "Trending" (which is engagement-weighted).
- **B.3.3 Most anticipated.** Carousel of upcoming releases (uses the `UnreleasedBadge` component). "Releasing soon" → tap to add to Want-to-Read or set a release-day reminder.
- **B.3.4 "Because you read X" personalisation.** Algorithmic row keyed to the user's last-finished or top-rated book. Title eyebrow names the source ("BECAUSE YOU LOVED *PIRANESI*").
- **B.3.5 Award winners.** Curated carousel of Booker, Pulitzer, Hugo, Nebula, etc. Editorial section per category — could rotate the featured prize weekly.
- **B.3.6 Curated lists / staff picks.** A horizontally-scrolling tile of *lists* (not books). Each tile shows 3 stacked cover spines + a list title + curator avatar. Tapping enters the list screen (C.6).
- **B.3.7 Author spotlight.** A single full-width "Author of the week" tile pulling the author's photo + a one-line bio + their highest-rated work.
- **B.3.8 Search filters & sorts.** When a search is active, surface a chip row: year range, format (print/audio/ebook), rating threshold, language, page-count range. Sort: relevance, rating, year, popularity.
- **B.3.10 Cover-art grid view.** A toggle on search results: text-rows (default) vs. cover-only grid for visual browsing. Reuses the cover-only mode the Library already implements.
- **B.3.11 Continue-series intelligence.** Today Explore shows a "Up next in your series" row. Add: "You haven't touched *Foundation* in 6 months — pick up where you left off?" — gentler re-engagement nudges as separate cards in the same row.
- **B.3.12 Name the Explore hero.** The card opening the Explore feed is the upcoming release (out within the next 30 days) that the most readers already have on a shelf — but it shipped with no opener at all, so it read as an arbitrary book carrying an "Arriving <date>" chip. Name it in place: a DS §2.3 inline 20×1 hairline eyebrow ("MOST ANTICIPATED · NEXT 30 DAYS", the window interpolated from the shared `FEATURED_RELEASE_WINDOW_DAYS` domain constant so copy and query can't drift) as the card's own first row, and "N readers waiting" beside the date badge for the ranking basis. Deliberately *not* a full `EditorialSectionHeader` like the rails below it — accent bar, headline and a description sentence cost most of the opening screen for a single weekly-rotating card. *(User request.)*
- **B.3.13 Back leaves search first.** On mobile, the system back press while an Explore search is open goes straight past the search and out of the screen, so a reader who searched has no back-shaped way out of their own results — the × in the pill is the only exit. Make back a ladder instead: with a query or a mood browse on screen it clears the search and hands the feed back, with only the focus surface open it closes that, and on the resting feed it belongs to the shell as before. Each rung is an existing action (`OnClearSearchAction` / `OnSearchDismissedAction`) behind its own `NavigationBackHandler`, the same shape as the Library's selection/rearrange modes; the chrome's focus is never touched from the screen side. Desktop gets the same intent through its own reflex — **Esc clears the search** — as a single rung, since its field is persistent and has no focus surface beneath it. *(User request.)*

### B.4 Book detail

- **B.4.3 Personal highlights / quotes.** A "Voices" section already shows community reviews. Add a personal "Passages" section above it: highlights the user has saved from this book, with optional page numbers. Tappable to share or add as the home-widget quote.
- **B.4.4 Reading log (multiple read-throughs).** Some books get re-read. Replace the single-status approach with a log of read-throughs: each entry is start date + end date + rating + optional note. The summary line in detail shows "Read 2× — 2023, 2026".
- **B.4.5 Similar books carousel.** A "If this resonated…" section under reviews — algorithmic similar-book recommendations, editorial-styled.
- **B.4.6 Series carousel with progress.** Series eyebrow currently shows position; expand it into a real carousel of the full series with shelf state on each cover (read/unread/owned).
- **B.4.7 Genre & mood chips.** Tappable chip row under metadata. Tapping a chip drops into the Explore genre filter (B.3.1).
- **B.4.8 Awards & accolades.** When a book has awards, a small inline strip in the editorial section style: eyebrow "RECOGNITION" → italic display "Booker Prize, 2023".
- **B.4.9 Content warnings / trigger tags.** Collapsible "Warnings (4)" section near the about block — content notes from the community, opt-in to reveal.
- **B.4.11 Author micro-card → author screen.** Tapping the byline opens an Author screen (C.5).
- **B.4.12 Share book sheet.** A "Share" action in the overflow menu opens a sheet with three share modes: shareable image card, plain text link, "send to a friend" deep link. Image card uses cover + title + user rating + a quote (if highlighted) — composed in the editorial visual style.
- **B.4.13 Add to a custom list.** Beyond shelves, "Add to a list…" sheet with the user's lists + ability to create new.
- **B.4.14 Audiobook ETA.** If audiobook and the user has a known listening pace (avg minutes/day), the deadline summary swaps to a predicted finish date.
- **B.4.16 Reviews filters & sorts.** Inside the "Voices" section, chip row: friends only, top-rated, recent, with spoilers, in your language.
- **B.4.17 Lent-out tracking.** "Loaned to" field on owned editions — name + date + reminder option.
- **B.4.19 Author identity tags (personal, optional).** A quiet panel on the byline (and on the Author detail screen once C.1 lands) where the user can flag — privately, for themselves — the author's gender, BIPOC affiliation, LGBTQ+ affiliation, and (optional) country of birth. Strictly local; never written back to Hardcover. Editorial framing: a single italic eyebrow line under the byline ("you tagged: woman · queer · Nigerian"), not a Material chip cluster. Feeds the new diversity & representation stats (B.5.15) and any future wrap-ups (C.17).
- **B.4.20 Book representation tags (personal).** "Who's in this book?" — personal flags at the *book* level for LGBTQ+ characters and BIPOC characters/protagonists, distinct from author identity tagging (B.4.19). Same private/local storage rule as above. Renders as a separate italic line ("you tagged: queer leads · sapphic") below the about block, opt-in to reveal so spoilers don't leak. Feeds B.5.15 and C.17.
- **B.4.21 Personal moods (book + chapter).** A private mood log. The user can rate one or more moods for the book overall (e.g., "devastated", "hopeful", "exhausted") and, optionally, for a per-chapter / per-percentage anchor ("at 62% — wrecked"). Distinct from B.3.1 (community moods used for discovery): these moods never leave the device. Moods are **graded, not binary** — each mood the user lights up carries an intensity, so "tense 62% · sad 66% · funny 8%" is expressible and the mood profile can be averaged across a shelf or a year (feeds B.5.16). Surfaces as an italic mood column on book detail with hairline intensity bars, and as a per-chapter mood ribbon along the wavy progress bar where anchors exist. Picker uses a small curated vocabulary plus a "your moods" free-text option; grading is optional — a mood left ungraded reads as "present".
- **B.4.22 Personal notes (book + characters).** A private notes field deliberately *separate* from B.4.2 (personal review): the review is the user's polished take that they may publish; notes are unfiltered marginalia that never publish. Two surfaces: a book-level "Notes" section on book detail, and a per-character note affordance when Hardcover exposes a character list for the book (especially useful for romance — notes against a love interest, the antagonist, an ensemble). Notes can carry an optional chapter/page anchor and appear in the Notes & Highlights inbox (C.7) under a "private — notes" group, never shareable from the inbox.
- **B.4.23 Audience as a separate classification from genre.** Treat audience-style tags (Young Adult, Middle Grade, New Adult, Adult) as a distinct *classification* dimension from genre (Romance, Fantasy, Mystery, Literary Fiction). Renders on book detail as a small audience eyebrow above the genre chip strip ("YOUNG ADULT · ROMANCE"), and on the genre/mood browser (B.3.1) as an independent audience filter that composes with genre filters. Requires a mapping layer over Hardcover's tag taxonomy — design phase first to confirm what's recoverable from their data. Out of scope: forcing a reclassification when Hardcover labels a book "Young Adult Fantasy" as a genre — we'd surface our re-grouping in the UI without mutating Hardcover state.
- **B.4.24 Personal trigger warnings (extends B.4.9).** Beyond surfacing community warnings (B.4.9 / Step 4.3), let the user add their own private warnings to a book — for warnings the community hasn't tagged, or to mark which canonical warnings matter most for *them* ("for me, this one really matters"). Same opt-in reveal as B.4.9; user-added warnings get a small "you noted" italic eyebrow when displayed alongside the community list.
- **B.4.25 Local tag cache + tag suggestions.** Cache the user's own applied tags locally (aggregated across **all** their tagged books) and surface them as suggestions / autocomplete in the tag editor. Works around the API's no-`_ilike` tag-search limit by sourcing suggestions from the local vocabulary. Builds on the shipped tagging slice (B.1.11 partial).
- **B.4.26 Rating/review prompt on mark-as-read.** When a book is marked Read from any surface (Reading screen, book detail, bulk-select), prompt the user to add a rating and/or review — reusing the personal-rating (B.4.1) and personal-review (B.4.2) controls in an editorial prompt sheet.
- **B.4.27 Format in the edition selector.** Show each edition's format (ebook / physical / audiobook) in the edition selector so editions are distinguishable at a glance. Reads the edition format/type field.
- **B.4.28 romance.io link.** For romance titles, add a romance.io destination to the external "Find it" links strip. Feasibility spike first — how to resolve a book to its romance.io page (ISBN/slug URL vs. their API). Genre-gated so it only shows for romance.
- **B.4.29 Book-detail tabs / sectioning.** Book detail has grown long; break its vertical scroll into tabs or sectioned navigation. Design spike first — the editorial register may favour section-nav / collapsibles over Material tabs. Sequence after the detail-enrichment surfaces exist so the IA accounts for them.
- **B.4.30 Personal book traits (graded).** A private, structured survey of *how a book read*, filled in by the user and stored locally alongside the other personal data (B.4.19–B.4.22). Where B.4.21 captures how the book made the reader *feel*, this captures what the book *was*. Dimensions, each a small graded scale rather than a yes/no tag:
  - **Pace** — slow · moderate · fast · variable.
  - **Plot- or character-driven** — a single slider from plot, through "a mix", to character.
  - **Characters** — likeable, believable, well-developed, multi-layered; plus "are the characters' flaws a main focus?" and "diverse cast?". Each answered yes / complicated / no rather than a binary.
  - **Writing style** — simple · moderate · demanding, with optional style flags (vivid, poetic, unconventional).

  Renders on book detail as a compact editorial panel — hairline bars and italic labels, one row per dimension, no dashboard chrome — collapsed by default and expanded on tap. Every dimension is optional; a book with nothing filled in shows an invitation line rather than an empty grid. Strictly personal and never written back to Hardcover. Feeds the "how you read" stats (B.5.16) and the wrap-up generator (C.17), which is the whole point: the user wants to be able to ask "were the books I read this year character-driven or plot-driven?" and get an answer. Community aggregation of these traits is **explicitly out of scope** — a per-book average needs a crowd we don't have; this is a solo instrument.
- **B.4.31 Acquisition source ("where I got it").** A single private per-book field recording where the copy came from — library, bought physical, bought digital, subscription/audio credit, borrowed, gifted, or the user's own free-text. One row in the same personal panel as B.4.30, one tap to set. Trivial to capture and it unlocks a stat the reading-journal crowd asks for constantly ("60% from the library this year"). Feeds B.5.17.

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
- **B.5.15 Diversity & representation stats.** A Reading Stats Atlas (C.3) section driven by B.4.19 + B.4.20: share of authors by gender / BIPOC / LGBTQ+ affiliation / country, share of books with LGBTQ+ or BIPOC representation. Editorial framing ("38% women, 22% authors of colour, 14 countries represented this year") with italic copy and hairline bars — no dashboard chrome. Strictly personal — driven by the user's own private tags, never aggregated server-side. Composes with the Atlas scope selector (B.5.19) so a user can scope the same stats to any period on record.
- **B.5.16 "How you read" stats.** A Stats Atlas section driven by the graded traits (B.4.30) and moods (B.4.21) — the shape of the reading itself rather than its demographics. Surfaces: the plot-vs-character balance across the scope ("your year leaned character-driven — 69% of the books you graded"), the pace mix, the writing-style mix, and a mood profile (the moods the user reached for most, ranked with their average intensity). Same editorial register as B.5.15 — italic copy, hairline bars, a single sentence of framing per chart, no dashboard. Scopes with the Atlas scope selector (B.5.19) and feeds the wrap-up generator (C.17).
- **B.5.18 GitHub-style activity contribution graph.** The dense full-year form of the reading heatmap: 52 week-columns × 7 day-rows of small squares, each square tinted by that day's reading activity (sessions logged, pages/minutes read, finishes) on a four- or five-step primary-tint scale, with month labels along the top and a low-key "less → more" legend. Distinct from B.5.3 (a 12-week strip) and from C.14's month-grid calendar: this is the year-at-a-glance contribution graph a reader recognises from GitHub, scoped to a single year from the Atlas scope selector (B.5.19) — being a full-year form, it follows the static-year periods and ignores the rolling windows. Keep it in the editorial register — hairline gridlines, tint steps drawn from the scheme rather than GitHub's greens, italic framing caption ("You read on 214 days this year") rather than a raw cell count. Tapping a cell opens the same per-day editorial sheet C.14 uses (books touched, session and page deltas, links into detail), so it shares C.14's day-detail surface rather than inventing its own. Can ship as the year-overview rendering of C.14 or as a standalone Profile/Atlas tile; sources are the same reading sessions (B.2.1) and reading-log finish dates (B.4.4). *(User request.)*
- **B.5.17 Publication & provenance stats.** The classic reading-journal spread, most of which we can compute from data we already hold. Surfaces: **author nationality** (from B.4.19's country-of-birth tag), **language and share translated** (from the edition's language field), **year-published distribution** (from the edition/book release date — separates "I only read new releases" from "I read the backlist"), **audience mix** (YA / Middle Grade / Adult, on top of B.4.23's classification), and **where you got it** (from B.4.31). Format split already exists (B.5.9) and rating + genre distributions already exist (B.5.4, B.5.5) — this section completes the set so the Atlas covers everything a paper reading journal would. Scopes with the Atlas scope selector (B.5.19) and feeds C.17.
- **B.5.19 Stats Atlas scope selector.** One scope control at the head of the Reading Stats Atlas (C.3) that every section below it reads from — the thing B.5.15, B.5.16, B.5.17 and B.5.18 currently assume exists ("the Atlas year filter") without anything defining it. Two kinds of scope in one picker: **rolling windows** (last 30 days, last 6 months, last 12 months) and **static periods** (each calendar year the user has reading data for — 2025, 2026, … — plus "all time"). Editorial register: a row of eyebrow-cased chips under the Atlas masthead with a quiet italic "showing 2026" line, not a Material filter bar or a dropdown. The selection is one piece of Atlas state threaded into every section's query so no section computes its own window; a section with no data in the chosen scope says so in its framing sentence rather than rendering an empty chart, and the picker offers only years the user actually read in. Prerequisite for B.5.15 / B.5.16 / B.5.17 and for B.5.18's year scoping, and the natural companion to the wrap-up generator (C.17) — the two should share one scope vocabulary and one date-range type rather than each inventing their own.
- **B.5.20 Full genre breakdown.** The genre stats stop at the top 5, so they read as a summary rather than an answer — a reader who wants to know *everything* they read has no route to it. Give the genre section an explicit "show every genre" route: the top 5 stays the default glanceable form (on Profile and in the Atlas alike), and a quiet expand affordance opens the complete ranking — every genre the scope's finished books carry, ordered by share, in the same hairline-bar editorial register. The cap currently lives in the data layer (`toGenreSlices` truncates the aggregate before mapping), which is the wrong home for what is a presentation choice — lifting it is the prerequisite, so either form renders off one query rather than two. Long tails need a real answer: a heavy reader carries dozens of one-book genres, so the expanded form is its own scrollable surface with a count in its framing line ("48 genres across 212 books"), not an unbounded inline list. The share-of-assignments caveat survives expansion intact — a book carries several genres, so the shares do not sum to 100, and the full view must not invite a whole-of-library reading by adding a total or an "everything else" remainder row. Composes with the Atlas scope selector (B.5.19); the Profile genre stack keeps its top 5 and gains the tap-through. *(User request.)*

- **B.5.21 Hide untagged authors in the author breakdown.** "Who you read" counts every author it knows about, tagged or not, so the untagged share stays honest — but on a shelf where most authors carry no demographic data that same honesty makes the tagged shares unreadable ("Women 8%" when it means 8% of *everyone*, not 8% of the ones we can see). Give the section one switch that flips it to the other reading: drop the untagged authors and renormalise. The exclusion is **per bar, not per author** — gender renormalises over the gender-tagged authors, BIPOC over the BIPOC-tagged, LGBTQ+ over its own — so the three no longer share a denominator and every caption has to name its own total. Persisted, so a reader who wants the tagged-only reading gets it on every visit. Default stays off: the all-authors reading is the truthful one, and the switch is the opt-in to a narrower claim, never the other way round. *(User request.)*

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
A dedicated stats deep-dive screen pushed from the Profile. Pages: Overview (the existing 4 hero stats), Year (B.5.6), Genres (B.5.4), Ratings (B.5.5), Pace (B.5.7), Authors (B.5.8), Format (B.5.9), Records (B.5.10), Streaks (B.5.3). Editorial section per page; rendered as one long scrollable spread, not a tabbed view. Carries the scope selector (B.5.19) at its head, which every section reads from.

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
*Rough plan — keep loose until B.3.3 lands.* A calendar surface that flips the activity calendar inside-out: instead of past activity, future-dated book releases plotted onto a month grid. Day cells highlight when a book on the user's Want-to-Read shelf releases (primary tint, full intensity) and when followed authors / followed series release anything (lower-intensity tint). Optional toggle to include "Most anticipated" globally curated releases (B.3.3) as a third tint. Tap a day → editorial sheet listing the releases with quick-actions (set release-day reminder via D.1, jump to book detail, pre-order link via the external "Find it" links). Reached from Explore (a "Coming up" tile next to the existing carousels) and from Want-to-Read in Library. Sources: edition `release_date` already present on book data, the Want-to-Read shelf, the future-author-follow surface (B.5.13). The author/series follow tints are deferred until follow infra exists; first cut can ship Want-to-Read only and still feel complete.

### C.17 Custom-scope wrap-up
A generalisation of C.4 (Year in Books) to arbitrary scopes: day, week, month, year, "since you joined Softcover". User picks the window from a small sheet (preset chips + a custom-range picker); the wrap-up generator produces an editorial 6–10 slide spread tuned to the scope's density — a "day" wrap-up is leaner (single book, single session, single highlight), a "year" wrap-up matches C.4's density. Slides draw on every personal-data source available: sessions (B.2.1), highlights (B.4.3), reading log (B.4.4), personal ratings (B.4.1), personal moods (B.4.21), personal identity & representation tags (B.4.19, B.4.20), graded book traits (B.4.30), and publication/provenance data (B.5.17). Each slide is shareable via D.3 (image export) so the user can send a "this week in books" card to a friend who doesn't use the app. Reached from Profile and from a "Wrap it up" affordance on the Reading Stats Atlas (C.3). Subsumes C.4's annual recap as the year scope; C.4 stays as the seasonal December surface, and both routes reach the same generator.

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
- **D.2.5 Random from Want-to-Read.** Surfaces a random book from the user's Want-to-Read shelf; tap to open. A "what next?" nudge.
- **D.2.6 Trending this week.** Shows trending books (from Explore's trending data); tap to open.
- **D.2.7 Reading activity calendar.** A mini month / streak view of the user's reading activity (sessions + finishes); reads the same data as the full Reading Activity Calendar screen (C.14).

### D.3 Sharing surface — *the surface is built; the callers aren't*
The shared "share card" composition **shipped**: one editorial layout (cover + a chosen stat or quote + Softcover sign-off), five content types (Book / ReadingUpdate / Quote / Stat / YearRecap), rendered to an image through the foundation's capture seam. Every share in the app routes through it, so the brand stays consistent.

What remains is **entry points**: only book detail can share today. Profile, Library (B.1.13), the Notes & Highlights inbox (C.7), Year in Books (C.4) and the wrap-up generator (C.17) each need to hand their content to this surface — that work belongs to those items, not here. This entry stays only as the contract they render against.

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
- **Must cover the private corpus.** The personal data in B.4.19–B.4.22, B.4.30 and B.4.31 (identity & representation tags, moods, notes, graded traits, acquisition source) lives *only* on the device — Hardcover has nowhere to put it. That makes a reinstall, a lost phone, or a platform switch a total loss of the thing the user invested the most effort in, and it is the single most-cited objection to tagging anything at all. The archive is therefore not a nice-to-have that can trail the feature by several releases: a plain JSON export + import of the private corpus should land **immediately after** the corpus exists, with the fuller single-archive backup following later.
- **Storage decision (settled).** The private corpus stays in our own Room tables and is portable via this export. It is explicitly *not* smuggled into Hardcover's private-notes field as prefixed/structured text — that would abuse a field for a purpose it wasn't designed for, could break without warning, and bends someone else's database to our schema. Local + export is the honest answer to durability.

### D.9 Testing & telemetry of motion
- A "Motion debug" hidden screen (long-press version footer 5x) that lets the team trigger every animation and haptic in isolation, audit reduced-motion behaviour, and verify timing. Not for end users.

### D.10 Per-feature in-app changelog
- A small "What's new" route from Settings → About showing per-version editorial release notes. Pairs with B.7.7.

### D.11 In-app roadmap viewer
- A read-only "Roadmap" route from Settings → About that renders the public [`ROADMAP.md`](../../ROADMAP.md) so users can see what's coming. **Single source of truth:** the screen fetches the raw `ROADMAP.md` from the repo at runtime (cached) and renders the markdown, with a build-time bundled copy as the offline / first-load fallback — there is no hand-maintained in-app copy to drift. Pairs with D.10 (changelog is *shipped*, roadmap is *coming*).

---

## Part E — Items deliberately *not* on the list

For completeness — things that look obvious but the editorial register argues against.

- **Carousels with auto-advancing slides.** Foreign to the print register; never on this app.
- **Emoji reactions on reviews.** Material-app pattern; the editorial voice would prefer prose responses or stars.
- **Toast-style mass notifications.** Push the user out of the editorial spread. Use the in-app activity feed (C.10) instead.
- **Gamification badges with cartoon icons.** Achievements are fine, but rendered as engraved-looking "marks" in `quoteGlyph` style, not as game-app badges.
- **A separate "Trending now" with stale data.** Trending exists in Explore (B.3) — don't duplicate.
- **Filled-icon families on a single surface.** Already a foundation rule (DS §2.6). Restating because B.3 and B.4 will tempt new icon usage.
