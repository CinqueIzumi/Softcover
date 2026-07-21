# Release Plan
A release-by-release slicing of [roadmap-steps.md](roadmap-steps.md). Current shipped version is **3.0.3**.

Each release below mixes a foundational/plumbing step with user-visible features so every drop feels substantial. Step references map directly to `roadmap-steps.md` — when a step ships, delete it there (per its maintenance rule). This file is the *order* of the steps, not a replacement for them.

This is an **internal** doc: the step list, dependencies, and bundling reasoning. The **user-facing** per-version copy lives in the public [ROADMAP.md](../../ROADMAP.md) (which doubles as the source for Google Play / App Store release notes at ship time). Keep the two in lockstep: any reorder/cut/add here updates the matching `ROADMAP.md` section in the same change.

**Maintenance rule.** A **shipped release is deleted from this file** — this doc is what's still coming, not a changelog. What shipped is recorded by the git tag, and the user-facing history belongs in the per-version changelog (Step 8.9). Drop the shipped section from `ROADMAP.md` in the same change.

Scope key from steps file: **S** ≈ 1–2 day, **M** ≈ 3–6 day, **L** ≈ 7+ day.

**Progress markers.** Within a release that hasn't shipped yet, a finished step is prefixed **✅** and stays in place until the whole release ships — at which point the entire section is deleted per the maintenance rule above. Anything unmarked is still outstanding, so the next release section doubles as the "what's left" list. The ✅ is a convenience view, not the source of truth: a step counts as done when it has been **deleted from `roadmap-steps.md`**, and the two should always agree.

**Versioning convention:**
- **3.x.0** — additive feature releases. (3.0.0 — the KMP-era iOS + desktop launch — shipped, along with hotfixes through 3.0.3.)
- **3.x.y** — patch releases (`y > 0`) are reserved for hotfixes shipped *between* planned feature drops; planned releases always bump the minor.
- Majors are a deliberate call, **not auto-reserved**. The Friend Feed adds a 5th nav tab (a structural change) — flagged as a **`4.0.0` candidate**, your call when it lands.

Dependencies are noted only where they cross a release boundary; same-release deps are obvious from order within the section.

> **Ordering note:** this plan was re-sequenced around an explicit priority order (fixes → high-prio features → medium-prio → backlog), not the original phase order. Some backlog items therefore land later than their old slots. The detail-enrichment cluster was promoted ahead of the book-detail-IA step (4.12 / "K") so the restructure accounts for everything on the screen.

---

## 3.1.0 — Fast wins

Small, high-value features on already-shipped foundations.

- ✅ **Step 10.15** — Auto-resize coverless title text (S) — **done** (`04116c58`, which also covers never leaving a cover slot blank). Already deleted from `roadmap-steps.md`.
- ✅ **Improvement** — Batch edition-by-ISBN with an `_or` filter (S) — **done**. `GetEditionByIsbn`'s two aliased selections collapsed into one `GetEditionsByIsbns($isbns: [String!]!)` query (`_or` over `isbn_13`/`isbn_10` `_in`); the repository/data-source surface is now batch-only (`fetchEditionMatchesForIsbns(isbns): Map<String, IsbnEditionMatch>`), so Step 8.6's ISBN-list import resolves a whole list in one round trip.
- ✅ **Step 10.16** — Local tag cache + tag suggestions (S–M) — **done**. A server-synced vocabulary of the user's own applied tags is cached in Room (`user_tag_vocabulary`) and surfaced as most-used-first / substring-narrowed suggestion chips in the tag editor; client-side filtering works around the API's no-`_ilike` tag search. Already deleted from `roadmap-steps.md`.
- ✅ **Step 3.12** — Rating/review prompt on mark-as-read (S–M) — **done**. A combined **Verdict sheet** (rating + review in one editorial prompt — the shared `core:designsystem` `VerdictSheet`, with a book-page `VerdictBlock` replacing the old separate rating row + review card) rises on a genuine finish from **book detail** and the **Reading screen**, via both the explicit "Mark as Read" affordance and reaching 100% progress. Bulk mark-as-read was intentionally excluded (a per-book prompt doesn't fit a bulk action). Already deleted from `roadmap-steps.md`.
- **Step 2.14** — Directly add a book to Currently Reading (S–M) — *pulled forward from 3.3.0 on user request.*

---

## 3.2.0 — Widgets

- **Step 9.3 (first wave)** — Widget infra + currently-reading, random-from-Want-to-Read, trending-this-week, reading-activity-calendar widgets (L) — Android-first (Glance). Second-wave widgets (streak / quote / year-in-books) land in 3.15.0.
- **Step 3.7** — Reading log (multiple read-throughs) (M) — *pulled forward* as the activity-calendar widget's finish-date data dependency (sessions already shipped).

---

## 3.3.0 — Series + shelving

- **Step 5.2** — Series detail screen (M).
- **Step 5.4** — Series-completion cascade (S) — *deps: 5.2 (same release)*.
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

This is the release a tracking-minded user is really waiting for: it's the whole private corpus in one drop. It's also the **heaviest release on the plan** — five M-steps. They share one Room schema and one book-detail panel, so splitting them costs more than it saves; but if it needs to shed weight, Step 3.13 + 3.14 are the clean cut line and move together to 3.6.0.

- **Step 3.3** — Personal highlights / Passages + quick-add from Reading (M).
- **Step 3.9** — Personal identity & representation tagging (M) — *deps: 0.3 (shipped)*.
- **Step 3.10** — Personal moods (book + chapter), **graded** (M) — *deps: 0.3 (shipped)*. Moods carry an intensity from day one; the schema must not ship as a boolean and migrate later (7.15 depends on it).
- **Step 3.11** — Personal notes (book + characters) (M) — *deps: 0.3 (shipped)*.
- **Step 3.13** — Personal book traits, graded (M) — *deps: 0.3 (shipped)*. Pace, plot-vs-character, characters, writing style. *(User request.)*
- **Step 3.14** — Acquisition source, "where I got it" (S) — *deps: 0.3 (shipped)*. Rides in 3.13's panel.
- **Step 4.7** — Reviews filters & sorts (S).

---

## 3.6.0 — Inboxes + book-detail IA

- **Step 3.4** — Notes & Highlights inbox screen (M) — *deps: 3.3 (3.5.0)*.
- **Step 3.6** — Reading Sessions log screen (S).
- **Step 8.13** — Personal-data export & import, JSON (S–M) — *deps: the Phase 3 corpus (3.5.0)*. **Pulled forward out of 9.8 deliberately**: the private corpus lands in 3.5.0 and lives nowhere but the device, so it cannot wait until 3.15.0 for a way out. Ships one release after the data exists. 9.8's full archive still follows and supersedes it.
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

- **Step 7.7** — Streak heatmap on Profile (M) — *deps: 3.5 (shipped); subsumed by 7.12 next release*. User asked for a **readable yearly** scope (Hardcover's own site heatmap is being removed as unreadable) — ship the 12-month view alongside the 12-week one, not 12-week only.
- **Step 7.8** — Time-of-day reading heatmap (M) — *deps: 3.5 (shipped)*.
- **Step 7.9** — Reading Stats Atlas screen (M).
- **Step 7.10** — Public activity log (M).
- **Step 7.13** — Diversity & representation stats (M) — *deps: 3.9 (3.5.0), 7.9 (same release)*.
- **Step 7.15** — "How you read" stats (M) — *deps: 3.13 (3.5.0), 7.9 (same release)*. Plot-vs-character balance, pace mix, writing-style mix, mood profile. *(User request.)*
- **Step 7.16** — Publication & provenance stats (S–M) — *deps: 7.9 (same release), 3.9 + 3.14 (3.5.0), 6.14 (3.8.0) for the audience mix*. Nationality, language & share-translated, year published, audience, where you got it.
- ~~**OPEN — "L" (expanded statistics + image export)**~~ — **closed.** The metrics are now specified (7.13 + 7.15 + 7.16) and the image export is 7.14's share surface in 3.12.0. The "own database" question is settled too: the private corpus lives in our own Room tables and is made portable by Step 8.13 — it is **not** smuggled into Hardcover's private-notes field.

---

## 3.12.0 — Calendars + recap

- **Step 7.12** — Reading Activity Calendar (M) — *deps: 3.7 (3.2.0), 7.7 (3.11.0)*.
- **Step 7.11** — Year in Books recap (M) — seasonal; gate behind a December trigger.
- **Step 7.14** — Custom-scope wrap-up generator (M) — *deps: 7.11 (same release); draws on 7.13 / 7.15 / 7.16 (3.11.0)*. Day / week / month / year / since-you-joined, every slide exportable as an image via the 0.2 share surface — the "send my month to a friend who doesn't use a tracking app" ask. *(User request.)*

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
- **Step 9.8** — Backup & restore (M) — *deps: 8.5 (3.13.0), 8.13 (3.6.0)*. The full single-archive backup; supersedes 8.13's standalone JSON while still reading its format.
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
3.5.0 (3.9) ──> 3.11.0 (7.13, 7.16)
3.5.0 (3.13) ──> 3.11.0 (7.15) ──┐
3.5.0 (3.14) ──> 3.11.0 (7.16) ──┴──> 3.12.0 (7.14)
3.5.0 (Phase 3 corpus) ──> 3.6.0 (8.13) ──> 3.15.0 (9.8)
3.5.0 (Phase 3) ──> 3.9.0 (6.5) ──> 3.14.0 (8.11)
3.8.0 (6.14) ──> 3.11.0 (7.16)

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
