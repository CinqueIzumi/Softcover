# Roadmap Steps
A sequenced pickup order for the ideas in [idea-catalogue.md](idea-catalogue.md). Each step is a self-contained chunk of work that delivers visible value or unlocks downstream steps. Where a step depends on earlier work, the dependency is named.

Items are referenced by their idea-catalogue.md tag (e.g. `B.4.1`).

Scope key: **S** ≈ 1–2 day, **M** ≈ 3–6 day, **L** ≈ 7+ day. Scope is a rough hint, not an estimate.

**Maintenance rule.** When a step is finished it is deleted from this file. Do **not** renumber the remaining steps — gaps are intentional so references in commits, conversations, and other docs stay valid against the original numbers.

---

## Phase 2 — Library & Reading depth

The two most-used surfaces gain shelf management depth and reading-flow nudges. Most of this is presentation-layer; no new persistence beyond Phase 0.

### Step 2.3 — Smart shelves as virtual tabs (M)
"Owned & unread", "Started but stalled", "Finished this year", "Quick wins", "Long hauls". Computed in domain; reuse the existing tab UI. *(B.1.3)*

### Step 2.9 — "Since last read" delta on Reading rows (S)
Eyebrow on compact rows briefly shows the page/time delta since previous open, fading to the regular eyebrow. *(B.2.8)*

### Step 2.14 — Directly add a book to Currently Reading (S–M)
One-step "start reading" action from book detail's shelf bar, search, and the add flows that puts a book straight onto Currently Reading instead of add-to-shelf-then-change-status. May chain create → set-status (→ start date) mutations on top of the shipped create-userBook path. Pairs with session start (Step 3.5). *(B.2.11)*

---

## Phase 3 — Personal data: rating, review, highlight, log, session

These steps light up the personal-data layer from Step 0.3. Many of the largest "feels missing" gaps live here.

### Step 3.3 — Personal highlights section on book detail + quick-add from Reading (M)
"Passages" section above the community Voices section. Add a quick-add affordance on the Reading featured card. *(B.4.3, B.2.6)*

### Step 3.4 — Notes & Highlights inbox screen (M)
Aggregate every saved highlight across books. Editorial-quote pattern per row, search/filter, share single highlights via Step 0.2 surface. *(C.7)*

### Step 3.6 — Reading Sessions log screen (S)
Reverse-chronological timeline of all sessions. Reached from Profile. *(C.8)*

### Step 3.7 — Reading log (multiple read-throughs) on book detail (M)
Replace single-status display with a log of read-throughs (start, end, rating, optional note). Detail summary becomes "Read 2× — 2023, 2026". *(B.4.4)*

### Step 3.9 — Personal identity & representation tagging (M, depends on 0.3)
Adds two private tagging surfaces driven by user-supplied data, plus the Room schema to back them. Strictly local — never written to Hardcover.

- **Author identity tags** on the byline (and on the Author detail screen once Step 5.1 lands): gender, BIPOC affiliation, LGBTQ+ affiliation, optional country of birth. Renders as an italic eyebrow line under the byline ("you tagged: woman · queer · Nigerian"), tappable to edit. *(B.4.19)*
- **Book representation tags** at the book level (distinct from author identity): LGBTQ+ characters, BIPOC characters/protagonists. Opt-in to reveal on book detail so spoilers don't leak. *(B.4.20)*

Both tag sets feed Step 7.13 (diversity stats) and Step 7.14 (custom-scope wrap-ups). Tag vocabularies are curated with a "your own" free-text fallback so users can use their own language.

### Step 3.10 — Personal moods (book + chapter) (M, depends on 0.3)
A private mood log. Book-level mood rating (one or more moods per book) and an optional per-chapter / per-percentage anchored mood ("at 62% — wrecked"). Distinct from B.3.1 community moods used for discovery — these never leave the device. Moods are **graded, not binary**: each mood carries an intensity, so a book reads as "tense 62% · sad 66% · funny 8%" and mood profiles can be averaged across a scope (Step 7.15 depends on this). Grading is optional — an ungraded mood reads as "present" — but the schema stores an intensity from day one, so don't ship a boolean column and migrate later. Mood column renders on book detail in italic with hairline intensity bars; per-chapter moods render as a thin mood ribbon along the wavy progress bar where anchors exist. Picker uses a curated mood vocabulary plus a free-text "your moods" option. *(B.4.21)*

### Step 3.11 — Personal notes (book + characters) (M, depends on 0.3)
A private notes field deliberately separated from Step 3.2 (personal review): notes are unfiltered marginalia that never publish. Two surfaces:

- **Book-level Notes** section on book detail (private), with optional chapter/page anchor per note.
- **Per-character notes** affordance for books where Hardcover exposes a character list (especially useful for romance — notes against love interests, the antagonist, an ensemble).

Notes appear in the Notes & Highlights inbox (Step 3.4) under a "private — notes" group; the inbox UI suppresses the share affordance for this group so private notes can't be exported accidentally. *(B.4.22)*

### Step 3.13 — Personal book traits, graded (M, depends on 0.3)
A private structured survey of *how a book read* — the counterpart to Step 3.10, which captures how it made the reader *feel*. Dimensions, each a graded scale rather than a yes/no tag: **pace** (slow · moderate · fast · variable), **plot- or character-driven** (one slider, plot → a mix → character), **characters** (likeable / believable / well-developed / multi-layered, plus "flaws a main focus?" and "diverse cast?", each answered yes · complicated · no), and **writing style** (simple · moderate · demanding, with optional vivid / poetic / unconventional flags).

Renders on book detail as a compact editorial panel — one row per dimension, hairline bars and italic labels, collapsed by default, expanded on tap. Every dimension is optional; an untouched book shows an invitation line, not an empty grid. Shares the Step 0.3 personal-data schema and the same strictly-local rule as Steps 3.9–3.11 — nothing is written back to Hardcover.

**Out of scope:** community aggregation of these traits. A per-book average needs a crowd we don't have; this is a solo instrument, and the request that prompted it was explicitly for personal use. Feeds Step 7.15 (how-you-read stats) and Step 7.14 (wrap-ups). *(B.4.30)*

### Step 3.14 — Acquisition source (S, depends on 0.3)
A single private per-book field — library, bought physical, bought digital, subscription/audio credit, borrowed, gifted, or free-text. One row in the Step 3.13 panel, one tap to set. Cheap to capture and it unlocks the "60% from the library this year" stat in Step 7.16. *(B.4.31)*

> **End of Phase 3:** The app has personal voice — the user's ratings, words, highlights, tags, moods, traits, and notes become a corpus the rest of the app can draw from. That corpus lives only on this device, so it must not go long without a portable export (Step 8.13).

---

## Phase 4 — Book detail enrichment

Surfaces existing metadata better. Most of these are presentation-only once the API returns the data.

### Step 4.1 — Genre/mood chips (S)
Tappable chip strip on book detail; tap drops into B.3.1 (Genre browser) when that exists, falls back to a placeholder filter until then. *(B.4.7)*

### Step 4.2 — Awards & accolades (S)
Editorial section under metadata with "RECOGNITION" eyebrow + italic display of the prize name. *(B.4.8)*

### Step 4.3 — Content warnings collapsible (S)
"Warnings (4)" collapsible near the about block; opt-in reveal. *(B.4.9)*

### Step 4.6 — Audiobook predicted finish (S)
When audiobook + known listening pace exists, deadline summary swaps to a predicted date. *(B.4.14)*

### Step 4.7 — Reviews filters & sorts (S)
Chip row inside the "Voices" section: friends only, top-rated, recent, with spoilers, in language. *(B.4.16)*

### Step 4.8 — Share book sheet: link + deep-link modes (S, depends on 0.2)
The image-share mode shipped in 2.2.0; this adds the remaining two modes (plain link, send-to-a-friend deep link) to the existing overflow "Share" sheet, all routed through Step 0.2. *(B.4.12)*

### Step 4.9 — Personal trigger warnings (S, depends on 4.3)
Extends the community-sourced content-warnings collapsible (Step 4.3) so the user can add their own private warnings to a book — for warnings the community hasn't tagged yet, or to mark which canonical warnings matter most for *them*. Same opt-in reveal as Step 4.3; user-added warnings render alongside the community list under a small "you noted" italic eyebrow. Strictly local. *(B.4.24)*

### Step 4.10 — Format in the edition selector (S)
Show each edition's format (ebook / physical / audiobook) in the edition selector, reading the edition format/type field. Pairs with the audiobook-by-type fix. *(B.4.27)*

### Step 4.11 — romance.io link on book detail (spike + S/M)
For romance titles, add a romance.io destination to the external links strip (Step 4.5, shipped). Feasibility spike first — resolve a book to its romance.io page (ISBN/slug URL vs. their API). Genre-gated. *(B.4.28)*

### Step 4.12 — Book-detail tabs / sectioning (M)
*Design spike before build.* Break the long book-detail scroll into tabs or sectioned navigation. The editorial register may favour section-nav / collapsibles over Material tabs — settle it in the spike. Sequence after the detail-enrichment surfaces (Steps 3.3, 3.9–3.11, 4.x) so the IA accounts for everything on the screen. Touches `../reference/design-system.md`. *(B.4.29)*

---

## Phase 5 — Author, Series, Lists

Three new screens that the byline + series eyebrow on book detail are already begging for. They also make the data-richening from Phase 4 land in a real navigation graph.

### Step 5.1 — Author detail screen (M)
New screen reached from any byline. Hero, bio, works carousel with shelf states, "Most acclaimed", series sub-list. *(C.1, B.4.11)*

### Step 5.2 — Series detail screen (M)
Spine row of the full series, reading-order checklist, aggregate progress stat, read-order toggle. Reached from series eyebrow and from B.4.6. *(C.2, B.4.6)*

### Step 5.3 — Lists screen (M)
User's custom lists + curated/community lists. Books inside use library anatomy. Basic name-only creation already shipped (the 2.3.0 custom-list MVP) — this step adds the standalone Lists screen, rename, delete, reorder of lists, share, description/privacy/header fields, and curated/community list discovery. *(C.5, B.1.12, B.4.13)*

### Step 5.4 — Series-completion cascade (S, depends on 5.2)
When the last book of a series is marked Read, all covers in that series cascade through a fade-to-monochrome-then-back, ending with a "Complete" stamp. *(A.1.15)*

### Step 5.5 — Add-to-list polish: ink-fill chip animation (S)
The core write path (`AddListBook` mutation, `RemoveListBook` reuse, the `ChooseListsBottomSheet` shared between book detail and library bulk-select, name-only list creation) and the bulk-select wiring on top of that sheet both shipped in 2.3.0 (custom-list MVP). This step is the remaining polish:
- Upgrade each sheet row's toggle to the ink-fill chip animation (A.1.5) with the `commit` haptic (deferred from the MVP).
- "Owned" stays special-cased and continues to route through `MarkEditionAsOwned` so the rest of the surface is uniform.
- **Why here:** finishes the list write-path *visual* surface; full Lists screen polish is owned by 5.3, tag system + library-side creation by 10.10. *(B.4.18, B.4.13, B.1.12)*

---

## Phase 6 — Discovery

Builds on Phase 3 (personal data) and Phase 5 (lists) for personalisation. Most of these are new Explore sections.

### Step 6.1 — Genre/mood browser (M)
New Explore section + sub-screen with chip filters. *(B.3.1)*

### Step 6.2 — New & noteworthy + Most anticipated (S)
Two carousels separate from "Trending": recent releases, and future-dated releases using `UnreleasedBadge`. *(B.3.2, B.3.3)*

### Step 6.3 — Award winners carousel (S)
Curated category-of-the-week carousel. *(B.3.5)*

### Step 6.4 — Curated lists carousel (S, depends on 5.3)
Editorial tile row of lists (not books) — 3 stacked spines per tile. *(B.3.6)*

### Step 6.5 — "Because you read X" personalisation row (M, depends on Phase 3)
Algorithmic recommendations keyed to the user's last-finished or top-rated book. Eyebrow names the source. *(B.3.4)*

### Step 6.6 — Recommendations / For You screen (M, depends on Phase 3 + 6.5)
A dedicated personalisation surface reached from Explore. *(C.9)*

### Step 6.7 — Search filters & sorts (M)
Filter chip row when search is active; sort modes (relevance, rating, year, popularity). *(B.3.8)*

### Step 6.9 — Author spotlight tile (S, depends on 5.1)
"Author of the week" full-width tile linking to author detail. *(B.3.7)*

### Step 6.10 — Similar books carousel on book detail (S)
"If this resonated…" section under the Voices block. *(B.4.5)*

### Step 6.11 — Continue-series nudges (S)
Stalled-series re-engagement cards in the existing series row. *(B.3.11)*

### Step 6.12 — New Releases calendar (M, depends on 6.2)
*Rough plan — design phase before build.* A calendar surface (month grid + 12-month zoom) plotting future-dated book releases. **First cut: Want-to-Read shelf only.** Curated "Most anticipated" is shown as a secondary low-intensity tint when the data is already at hand from Step 6.2. Tap a day → editorial sheet listing releases with quick-actions (set release-day reminder via D.1 / Step 9.1, jump to book detail, external pre-order link via Step 4.5). Reached from a new "Coming up" tile on Explore (next to Step 6.2's carousels) and from the Want-to-Read tab in Library. Open design questions to settle before building: whether the year-overview view collapses to a sparkline strip or a 12-cell mini-grid, and how to handle days with >3 releases (overflow chip vs. stacked spines). *(C.15, B.3.3)*

### Step 6.13 — New Releases calendar: author/series follow tints (S, depends on 6.12 + follow infra)
Extends the calendar with two additional tint intensities for releases from authors and series the user follows. Lands after the follow graph is wired (depends on whatever step first reads/writes follows — likely Step 9.10 or earlier if author-follow lands standalone). Each tint is distinct enough from Want-to-Read full-intensity and "Most anticipated" low-intensity to read cleanly in a single cell that combines two or more reasons. *(C.15)*

### Step 6.14 — Audience as a separate classification from genre (M, depends on 6.1)
*Rough plan — design phase before build.* Treats audience-style tags (Young Adult, Middle Grade, New Adult, Adult) as a distinct *classification* dimension from genre (Romance, Fantasy, Mystery, Literary Fiction). Surfaces on book detail as a small audience eyebrow above the genre chip strip ("YOUNG ADULT · ROMANCE") and on the genre/mood browser (Step 6.1) as an independent audience filter that composes with genre filters. Requires a mapping layer over Hardcover's tag taxonomy: open design questions before build — what's recoverable from Hardcover's `cached_tags`, whether the mapping is a hand-curated allowlist or pattern-derived, and how to handle ambiguous tags ("YA Fantasy" supplied as a single tag). Out of scope: mutating Hardcover state — we surface the regrouping in the UI only. *(B.4.23)*

---

## Phase 7 — Profile depth & stats

These steps stack a long, scrollable Profile and break it out into the Stats Atlas. Most depend on Phase 3 sessions data for full fidelity; the rest can launch earlier.

### Step 7.1 — Edit profile (S)
Name, bio, avatar editable in-app. Settings shortcut icon on Profile. *(B.5.1, B.5.14)*

### Step 7.2 — Yearly reading challenge tile + screen (M, depends on 0.4 + 3.1)
Goal hero stat tile on Profile, full Reading Challenge screen with goal-completion `milestone` haptic. *(B.5.2, C.6, A.2.5)*

### Step 7.3 — Goal setup wizard (S)
Standalone wizard to set books/pages/genre diversity goals. Reached from Profile and (optionally) onboarding. *(C.13)*

### Step 7.4 — Genre + rating distributions (M)
Editorial bar charts for genre share and rating histogram. Italic copy framing, no "chart" labelling. *(B.5.4, B.5.5)*

### Step 7.5 — Reading seasons (months) (S)
12-month bar chart of pages read. *(B.5.6)*

### Step 7.6 — Author top-list + format split + records (S)
Three Profile tiles. *(B.5.8, B.5.9, B.5.10)*

### Step 7.7 — Streak heatmap (M, depends on 3.5)
Replace the single streak stat with an interactive 12-week heatmap; tap a cell to see what was read that day. *(B.5.3)*

### Step 7.8 — Time-of-day reading heatmap (M, depends on 3.5)
Built from session data; editorial framing. *(B.5.7)*

### Step 7.9 — Reading Stats Atlas screen (M)
Single long scrollable spread aggregating 7.4–7.8 + the existing 4 hero stats. Pushed from Profile. *(C.3)*

### Step 7.10 — Public activity log (M)
Toggleable timeline of finishes/ratings/highlights. Privacy controls (B.6.9) gate visibility. *(B.5.11)*

### Step 7.11 — Year in Books recap (M, seasonal)
Time-limited screen surfaced in December via notification. 8–10 editorial slides, each shareable via Step 0.2. *(C.4, B.5.12)*

### Step 7.12 — Reading Activity calendar (M, depends on 3.5 + 3.7 + 7.7)
*Rough plan — design phase before build, and subsumes the streak heatmap once shipped.* A full-screen calendar with month grid + 12-month zoom-out where each day cell shows what the user did that day: pages read, time read, finishes, ratings published, highlights saved. Day cells render editorial-style — tiny stacked spine row of covers touched, dominant cover tinting the cell background. Tap a day → editorial sheet with per-day breakdown and deep links into book detail, the Sessions log (Step 3.6), and the Notes & Highlights inbox (Step 3.4). The 12-month overview replaces the standalone streak heatmap (Step 7.7); when this step ships, fold 7.7's surface into this screen rather than maintaining both. Reached from Profile, from the Reading screen's streak strip (Step 3.8), and from the Stats Atlas (Step 7.9). Out of scope for first cut: forward-looking "planned reading" entries. *(C.14, B.5.3)*

### Step 7.13 — Diversity & representation stats (M, depends on 3.9 + 7.9)
A new Stats Atlas section driven by the private tags from Step 3.9 (author identity + book representation). Surfaces: share of authors by gender, share of authors by BIPOC affiliation, share of authors by LGBTQ+ affiliation, country distribution, share of books with LGBTQ+ representation, share with BIPOC representation. Editorial framing ("38% women, 22% authors of colour, 14 countries this year") with italic copy and hairline bars — no dashboard chrome. Composes with the year filter on the Stats Atlas so the same section can scope to any year on record, and with the custom-scope generator (Step 7.14) so any wrap-up can include a diversity slide. Strictly personal — driven by the user's own private tags. *(B.5.15)*

### Step 7.14 — Custom-scope wrap-up generator (M, depends on 7.11)
Generalises Step 7.11 (Year in Books) into a wrap-up generator that takes any scope: day, week, month, year, "since you joined Softcover", or a custom date range. User picks the scope from a small sheet (preset chips + custom-range picker); the generator produces an editorial 6–10 slide spread tuned to the scope's density — a "day" wrap-up is leaner (single book, single session, single highlight) while a "year" wrap-up matches Step 7.11's density. Slides draw on every personal-data source the user has lit up: sessions (Step 3.5), highlights (Step 3.3), reading log (Step 3.7), personal ratings (Step 3.1), personal moods (Step 3.10), personal identity & representation tags (Step 3.9 + Step 7.13), graded book traits (Step 3.13 + Step 7.15), and publication/provenance (Step 7.16). A slide whose source the user never filled in is dropped, not rendered empty. Each slide is shareable via the Step 0.2 / Step 4.8 share surface so users can send a "this week in books" card to a friend who doesn't use the app — the image export is the point of the feature, not a garnish on it. Reached from Profile and from a "Wrap it up" affordance on the Stats Atlas (Step 7.9). The year-scope route shares the generator with Step 7.11's December trigger — both surface the same output. *(C.17, C.4)*

### Step 7.15 — "How you read" stats (M, depends on 3.13 + 7.9)
A Stats Atlas section driven by the graded traits (Step 3.13) and moods (Step 3.10) — the shape of the reading rather than its demographics. Surfaces: the plot-vs-character balance across the scope ("your year leaned character-driven — 69% of the books you graded"), the pace mix, the writing-style mix, and a mood profile (the moods reached for most, ranked by average intensity). Editorial register throughout — italic framing sentence, hairline bars, no dashboard chrome. Only counts books the user actually graded, and says so in the framing line rather than silently treating ungraded books as neutral. Scopes with the Atlas year filter; feeds Step 7.14. *(B.5.16)*

### Step 7.16 — Publication & provenance stats (S–M, depends on 7.9, + 3.9 for nationality, + 3.14 for source)
The classic reading-journal spread, most of it computable from data already held. Surfaces: **author nationality** (from Step 3.9's country-of-birth tag), **language + share translated** (from the edition's language field), **year-published distribution** (from the release date — separates a new-releases reader from a backlist reader), **audience mix** (YA / Middle Grade / Adult, on top of Step 6.14's classification), and **where you got it** (from Step 3.14). Format split (Step 7.6) and rating + genre distributions (Step 7.4) already exist — this closes the set so the Atlas covers everything a paper reading journal would. Each sub-stat degrades independently: a user who never tagged nationality still gets the language and year charts. Scopes with the Atlas year filter; feeds Step 7.14. *(B.5.17)*

---

## Phase 8 — Settings, theme, accessibility, onboarding

By this point Settings is dense enough to warrant restructuring; do it deliberately rather than letting it sprawl.

### Step 8.1 — Theme variants screen (S)
Light / Dark / Dim / System with preview tiles. The Material You toggle (already exposed in Phase 1, Step 1.1) lives alongside these as a secondary affordance — not the featured option. *(B.6.2, A.3.6)*

### Step 8.2 — Spine colour (accent palette) picker (S)
3–4 named alternate palettes (e.g. *Vellum*, *Ink*, *Foxed*, *Sea*); each is a curated primary/tertiary pair tuned to coexist with Fraunces italic and the editorial surface shades. This is the **featured** personalisation in Appearance — the editorial-register answer to Material You. Dynamic colour stays available but plays second fiddle: it's the platform option for users who want OS-level theming; Spine colour is the brand option for users who want personality without leaving the editorial scheme. Each palette gets a preview tile rendered in the same style as the theme-variants previews above. *(A.3.7)*

### Step 8.3 — Accessibility settings (S)
Reduced motion toggle (independent of system), text-size adjustment for book prose, high-contrast variant, screen-reader announcements toggle. *(B.6.3, D.6)*

### Step 8.4 — Notification controls (S, depends on 0.4)
Switch rows for deadline reminders, daily nudge, weekly recap, release-day, friend activity. *(B.6.1)*

### Step 8.5 — Data export (S)
CSV of library, JSON of highlights/notes, image cards via 0.2. *(B.6.5)*

### Step 8.6 — Data import (M)
Goodreads / Storygraph CSV file picker, ISBN-list paste. Maps to Hardcover. *(B.6.6)*

### Step 8.7 — Account, cache, privacy (S)
API key change in-app, revoke, clear caches, public/private toggles, anonymize activity. *(B.6.7, B.6.8, B.6.9)*

### Step 8.8 — Language, region, default tab, default progress unit (S)
Misc preferences. *(B.6.10, B.6.12, B.6.13)*

### Step 8.9 — About screen with licenses + per-version changelog (S)
In-app changelog route from Settings → About; same surface drives the post-upgrade "What's new". *(B.6.14, D.10, B.7.7)*

### Step 8.12 — In-app Roadmap screen (S)
Read-only "Roadmap" route from Settings → About that renders the public `ROADMAP.md`. Fetches the raw file from the repo at runtime (cached) so it's always current without an app release, with a build-time bundled copy as the offline / first-load fallback. No hand-maintained in-app copy. *(D.11)*

### Step 8.13 — Personal-data export & import (S–M, depends on 3.9 + 3.10 + 3.11 + 3.13 + 3.14)
A plain JSON export and import of the **private corpus** — identity & representation tags, moods, notes, graded traits, acquisition source — keyed by Hardcover book/author ids so a re-import re-attaches to the right books on a fresh install. Save to a file the user owns (share sheet / file picker per platform); import validates, reports what it matched, and skips what it couldn't resolve rather than failing the whole file.

**Why this is a step of its own and not a slice of Step 9.8.** This data exists nowhere but the device — Hardcover has no field for it. Between the corpus landing (Phase 3) and backup & restore (Step 9.8) there must not be a long stretch where a reinstall silently destroys the thing the user put the most work into; that risk is also the first objection any user raises before they'll invest in tagging at all. Ship the portable file early; Step 9.8's fuller single-archive backup (which also covers highlights, sessions, reviews, lists) still follows later and supersedes this as the *complete* answer. *(D.8)*

### Step 8.10 — Onboarding goal + theme + import + notifications + better error UI (M, depends on 7.3, 8.1, 8.6, 0.4, B.7.6)
Extend onboarding with skippable goal, theme, import, notification opt-in steps; surface inline error UI on invalid API keys. *(B.7.1–B.7.6)*

### Step 8.11 — Curated starter list step (M, depends on 6.5)
"Pick three books you love" in onboarding to seed personalisation. *(B.7.5)*

---

## Phase 9 — Cross-cutting capabilities

These reach across the app and depend on most prior phases.

### Step 9.1 — Notification triggers (M, depends on 0.4 + 8.4)
Wire the actual triggers: deadline reminders, release-day, weekly recap, monthly milestone, year-end drop, friend activity. *(D.1)*

### Step 9.2 — Activity feed / Notifications inbox screen (M, depends on 9.1)
Subdued in-app inbox of soft nudges and friend activity. Pull-to-refresh, swipe-to-dismiss. *(C.10)*

### Step 9.3 — Widgets (L, depends on widget infra + 3.5)
Android home-screen widgets (Glance). Builds the widget infrastructure once, then the widget family in two waves. **First wave:** currently-reading (D.2.1), random-from-Want-to-Read (D.2.5), trending-this-week (D.2.6), reading-activity-calendar (D.2.7 — needs reading-log Step 3.7). **Second wave:** streak (D.2.2), quote-of-the-day (D.2.3 → feeds Step 9.4), year-in-books (D.2.4). *(D.2)*

### Step 9.4 — Quote of the day surface + notification + widget link (S, depends on 9.3)
Full-screen pull-quote landing surface for the QotD widget/notification. *(C.11)*

### Step 9.5 — App shortcuts (S)
Long-press launcher icon → "Start a reading session", "+20 pages on current book", "Open featured book", "Add by ISBN". *(D.5)*

### Step 9.6 — Wear OS complication + quick-settings tile (M)
Currently-reading complication for Wear; quick-tile to start/stop a session. *(D.4)*

### Step 9.7 — Offline mutation queue (M)
Queue progress logging, session writes, ratings, reviews, highlights while offline; sync on reconnect with shake-on-conflict. The minimal queue infrastructure (Room-backed persistence, app-start + reconnect drain triggers, FIFO ordering per target) already shipped in [[2.12]] scoped to list mutations — this step extends it to the remaining mutation types and adds the shake-on-conflict UI and surfaced pending-sync indicator. *(D.7)*

### Step 9.8 — Backup & restore (M, depends on 8.5 + 8.13)
Single-archive export of **all** UGC — highlights, sessions, reviews, lists, *and* the private corpus already made portable by Step 8.13 (identity & representation tags, moods, notes, graded traits, acquisition source). Restore in Settings. Supersedes Step 8.13's standalone JSON as the complete answer; keep 8.13's format readable by the restore path so an older export still imports. Doubles as account migration if Hardcover moves. *(D.8)*

### Step 9.9 — Voice & TalkBack polish (M)
Custom TalkBack announcements for the editorial-styled screens; Assistant intents for "+pages" and "start session". *(D.6)*

### Step 9.10 — Friend Feed (L, depends on 5.1 + 5.3)
A new root-level surface as **the fifth bottom-nav tab** (preferred; fallback host is a segmented switcher at the top of Profile if the 5-tab dock proves too crowded). Streams friend activity chronologically — each row an editorial entry (eyebrow "FRIEND NAME · 2H AGO", italic verb phrase, leading cover, optional pull-quote for reviews/highlights). Event types: status changes, progress updates, reviews & ratings, highlights shared, list activity, goal milestones. Interactions: pull-to-refresh (with eyebrow swap A.1.9), long-press cover peek (A.1.1), `milestone` haptic when a goal-completion event scrolls into view. **Out of scope for first cut:** per-friend event-type muting (the social feed will not support muting specific people in this iteration). Routes detail interactions into existing surfaces (book detail, Author detail Step 5.1, Lists Step 5.3) — the feed itself stays a *surface*, not a destination. Stays separate from C.10 (Notifications inbox): that is the user's own nudges; the feed is other people's reading. Hardcover's API exposes the follow graph + activity events directly, so the feed reads from a real source. The 5-tab dock will need a follow-up pass on bottom-bar collapse timing (Step 10.3) and icon-weight balance — note this as a known-coming polish item when shipping. *(C.16, B.5.13, C.10)*

---

## Phase 10 — Decorative & motion polish (cumulative finish)

By this phase most features exist. These steps add the long-tail of motion and decoration. None unblock anything; they raise the floor.

### Step 10.1 — Long-press cover peek (M)
Universal long-press preview on every cover with `threshold` haptic at activation. *(A.1.1, A.2.2)*

### Step 10.2 — Hero parallax on book detail (S)
Blurred backdrop scrolls at 0.5x relative to the hero. *(A.1.2)*

### Step 10.3 — Bottom-bar collapse on scroll (M)
Docked/floating bar collapses to a thin pill on downward scroll, expands on upward. *(A.1.4)*

### Step 10.4 — Cover-back flip on long-press in detail (M)
Hero cover 180° flip to a generated "jacket back". *(A.1.11)*

### Step 10.5 — Reading-session breathe (S)
Active session breathes the cover at 4s ±0.5%. *(A.1.14)*

### Step 10.6 — Status callout ribbons + cover-tinted hero scrim (S)
Bookmark-ribbon glyph on the Read status line; cover-tinted lower edge on the hero gradient. *(A.3.9, A.3.8)*

### Step 10.7 — Audiobook waveform + two-tone pacing bar (S)
Decorative waveform under wavy audio progress; two-tone wavy bar for pacing situations. *(A.3.10, A.3.11)*

### Step 10.8 — Editorial flourishes (S)
Spine row on Profile, pull-quote of the day, fleuron divider, page-edge serif on carousels, quote-glyph marginal flourish. *(A.4.1–A.4.3, A.3.4, A.3.5)*

### Step 10.9 — Multi-method progress entry (M)
OCR a page number from camera, voice entry, slider track in the progress sheet. *(B.2.10)*

### Step 10.10 — Tag system + custom list creation in library (M, depends on 5.3)
Freeform tags filterable from the library filter chips; in-app list creation. *(B.1.11, B.1.12)*

### Step 10.11 — Lent-out tracking on owned editions (S)
"Loaned to" field on owned editions with optional reminder. *(B.4.17)*

### Step 10.12 — Inline edition swap from Library (S)
Long-press → quick edition picker without going into detail. *(B.1.10)*

### Step 10.13 — Pace card per active book + deadline urgency pinned section (S)
Italic finish-date prediction on each Reading row; "Up against the clock" section pinned at the top of Library tabs when relevant. *(B.2.5, B.1.7)*

### Step 10.14 — Library export (S, depends on 0.2 + 8.5)
Export current view as styled shelf card image. *(B.1.13)*

---

## Phase 11 — Stretch / out-of-scope flags

Listed for completeness only.

### Step 11.1 — Book club / group reading (L)
Shared annotations, current-pages tracking, chat. Substantial backend work; flag as a future arc, not a near-term step. *(C.12)*

---

## Reading order at a glance

```
Phase 0  ──┬──> Phase 1 ──> Phase 2 ──> Phase 3 ──┬──> Phase 4
           │                              │        │
           │                              │        └──> Phase 5 ──┐
           │                              │                       │
           │                              └──> Phase 6 <──────────┘
           │                              │
           │                              └──> Phase 7 ──> Phase 8 ──> Phase 9 ──> Phase 10
           │                                                                │
           └──────────────────────────────────────────────────────────────> Phase 11
```

- **Phase 0** is required by almost everything; do it first.
- **Phase 1** is independent — it can ship anytime after Phase 0 and dramatically lifts perceived polish.
- **Phase 2** is the cheapest functional depth and the most-used surfaces; pick it up before personal-data features.
- **Phase 3** unlocks Phases 6, 7, 9, 10. If only one phase after the foundations gets done, this is the highest-leverage one.
- **Phase 5** has to land before Phase 6 (Curated lists, Author spotlight) and Phase 10.10 (custom lists).
- **Phase 7** depends on Phase 3 for full fidelity but its first three steps (7.1, 7.3, 7.5, 7.6) can ship earlier with placeholders for session-derived data.
- **Phase 8** restructures Settings — better to wait until enough preferences exist to warrant restructuring.
- **Phase 9** is the cross-cutting capstone.
- **Phase 10** is the final coat of paint; reorder freely within it.

---

## Maintenance reminders

- Every step that introduces a new component or pattern must update the relevant section under `../reference/design-system/` in the same change (DS maintenance rule, CLAUDE.md).
- Substantial changes (new file, new feature module, layout/state/data flow changes) delegate to the `code-reviewer` agent before reporting work done.
- Test writing always delegates to the `unit-test-writer` agent.
- Roadmap docs are uncommitted by convention (per existing project practice); keep them local and update as steps complete.
