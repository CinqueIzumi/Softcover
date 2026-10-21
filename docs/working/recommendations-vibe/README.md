# Personal recommendations from Hardcover vibes

Hardcover keeps a server-generated list of personal recommendations for every reader, and lets supporters
build their own lists ("vibes"). This plan brings both into Softcover: a Recommendations rail on Explore first,
then the follow-ups below.

## Now

- **State:** phase 1 (1a–1e) is built, device-checked and committed in four commits on the local branch
  `recommendations-vibe` (worktree `../Softcover-recommendations-vibe`, from `origin/release/3.3.0`, not
  pushed). No GitHub issue yet.
- **Next:** design 2c with the user (investigate `vibe_exclusions_attributes` on a system vibe first). Work in
  the worktree; the main checkout's `ACTIVE.md` lacks this. The device's local Want to read rows and cached
  Recommendations were deleted for the 2b check; a pull to refresh restores them.
- **Open questions:** none.
- **Verification:** `check koverXmlReport -x lint` passed after the last code change.
- **Uncommitted:** nothing.

## What the API offers

Checked live on 2026-10-21.

- `vibes(where:{user_id:{_eq:$id}, vibe_type:{_eq:$type}})` returns a reader's own vibes. Their privacy
  setting is 3 (private), but they can still be read with the reader's own token. Filter on the `user_id`
  column: filtering through the `user` relationship returns `[]`. `users` has no `vibes` relationship, so the
  query can't be nested under `me {}`.
- `vibe_type` values come from the data, not the schema: see "Unresolvable API enums" in
  `docs/reference/architecture.md`. Recommendations' slug is `recommendations`, Top Picks' `top-picks`.
- `cached_book_ids` is `jsonb` (Apollo's untyped `Any`): a ranked array of up to 250 book ids.
- The server generates the rows itself (`books_generated_at`), so a reader may have none yet. The list
  leaves out read, reading and DNF books; want-to-read books still appear.
- Featured public vibes have `featured: true` (e.g. "Romantasy", "Hidden Gem Fantasy").
- Supporter status: `users.pro`, `membership`, `membership_ends_at`, readable through `me {}`.
- Writes: `insert_vibe` / `update_vibe` / `delete_vibe` take `VibeInput`: signals, exclusions and
  `dynamic_signals` (category, count, rating range, weight). The schema doesn't list the allowed
  `signal_type` / `category` strings. Signals can't be read back.

## Phase 1: the Recommendations rail on Explore

Agreed with the user:

- **Placement:** the first rail, directly under the Featured card and above "Up next in your series".
- **Heading:** eyebrow "Recommended for you", headline "Based on what you've read".
- **Size:** store the top 50 ranked ids, show 25.
- **Filter:** hide books that are on a shelf or in a list, as Because you read does. Re-filter live when the
  library changes.
- **No list yet:** the rail stays hidden.

Built, device-checked and committed (1a–1e): a ranked `"recommendations"` feed in `:core:book` stored in
the shared `ranked_books` tables, and the Explore rail.

## Phase 2: follow-ups

Each step is designed with the user before it is built.

| Step | Scope | Status |
|---|---|---|
| 2a | Library: when the Want to read shelf is empty, show the top recommendations with an add button | [x] |
| 2b | Top Picks (`vibe_type` 3) as the fallback when Recommendations is missing or empty after the owned filter, see below | [x] |
| 2c | Investigate first: can a system vibe take `vibe_exclusions_attributes`? Then "Not for me" on a recommended card, synced as an exclusion if it can be, otherwise hidden locally like Up next (and listed in Settings' hidden suggestions) | [ ] |
| 2d | Featured public vibes (`featured: true`) on Explore, as rails or tiles beside Browse by mood | [ ] |

### Step 2b: Top Picks fallback

Agreed with the user:

- **When:** the rails (Explore, Library) show Top Picks only when Recommendations is missing or leaves nothing
  after the owned filter. No rating-count threshold.
- **Heading:** Explore "Top picks" / "Popular with readers right now"; Library keeps the eyebrow "Not on your
  shelf yet" with the headline "Top picks".
- **Storage:** the same named-feed path as Recommendations, under its own ranked-list key. No schema change.

| Step | Scope | Who | Status |
|---|---|---|---|
| 2b-1 | `:core:book` path takes a `SystemVibe`; the feed refreshes both and reports its source; both screens' headers follow it; pull to refresh covers both | `softcover-implementer` | [x] |
| 2b-2 | Tests, `styleCheck`, `softcover-reviewer`, `check koverXmlReport -x lint`, the user's device check; the header masks while the source is undecided | main | [x] |

### Step 2a: recommendations on Library's Want to read shelf

Agreed with the user:

- **When:** while the Want to read shelf holds fewer than 5 books. On an empty shelf the rail sits under the
  empty-state message; otherwise it sits under the shelf's books. A book added drops out and the next one fills in.
- **Layout:** a sideways-scrolling row of 10 column cards, each with a bookmark toggle that adds the book to
  Want to read. Tapping a cover opens Book Detail.
- **Copy:** no mention of Hardcover. Eyebrow "Not on your shelf yet", headline "Recommended for you".
- **No list, or nothing left after the filter:** the rail stays hidden and the shelf shows what it does today.

| Step | Scope | Who | Status |
|---|---|---|---|
| 2a-1 | `:core:component`: an optional `BookCardTrailing.Bookmark` on `BookCardVariant.Column`, below the text, emitting `TrailingClicked`; gallery preview | `softcover-implementer` | [x] |
| 2a-2 | Move `ObserveOwnedBookIdsUseCase` and `GetRecommendedBooksUseCase` from `feature/explore` to `:core:lists`; the display cap becomes a parameter (Explore 25, Library 10) | `softcover-implementer` | [x] |
| 2a-3 | Library: collector (refresh, hourly check), state, card mapper, the rail in the Want to read tab, add action via `MarkBookAsWantToReadUseCase`, pull to refresh | `softcover-implementer` | [x] |
| 2a-4 | Tests, a shared `ObserveRecommendedFeedUseCase` replacing the two near-identical collectors, `styleCheck`, `softcover-reviewer`, `check koverXmlReport -x lint` | main | [x] |
| 2a-5 | The user's device check; the bookmark moved onto the cover's top-end corner, the empty state got `Spacing.section` padding | user | [x] |

## Phase 3: personal genre vibes for supporters in Because you read

For a supporter, Because you read uses a private vibe the app keeps for each genre. Its signals are the genre
plus the reader's history (e.g. dynamic signals from 4★+ reads), so the rail ranks books for this reader
rather than by genre popularity. A non-supporter keeps today's `GetBooksByGenreTag` rail. The same rail is
also the fallback while a new vibe has no books yet.

| Step | Scope | Status |
|---|---|---|
| 3a | API spike: capture the `signal_type` / `category` strings the website sends for a genre vibe; measure how long `books_generated_at` takes after `insert_vibe`; check for a per-user vibe limit | [ ] |
| 3b | Design: supporter detection (`me { pro }`), an opt-in (the app writes to the reader's account), one reused vibe per genre, recovery after a reinstall by a fixed title or slug, and clean-up | [ ] |
| 3c | Build, then tests, review and gates | [ ] |

## Risks

- **List length:** the filter can leave fewer than 25 of the 50 stored ids for a reader with big lists.
  Storing 100 costs no extra request, only rows.
- **id element type:** the runtime type of the `cached_book_ids` elements is unverified. The parser accepts
  numbers and numeric strings.
- **Domain list on the state:** `recommendedBooks` puts another domain list on `ExploreScreenUiState`,
  matching its siblings. Step 5b of `domain-out-of-uistate` moves it with the rest. The UiState is already
  UNSTABLE in the baseline.
- **Phase 3 hinges on 3a:** if the signal strings can't be found, or generation takes hours, the phase is
  reworked or dropped.

## Out of scope

- A Recommended feed in the Trending widget: in progress elsewhere.
- The reader's own vibes as rails.
- A vibe builder.
- "More like this" on Book Detail.
- A vibe from a list.
- Mood vibes: until phase 3 shows the mechanism works.
- A "see all 250" screen.

## Decisions

- 2026-10-21: the list is checked at most hourly and its books re-fetched only when the ranked ids changed, not on a fixed 24-hour age; comparing ids needs no schema change, unlike storing `books_generated_at`.
- 2026-10-21: system vibes are fetched by a named `SystemVibe` kind matched on type and slug, not the newest
  row of a type.
- 2026-10-21: the ranking lives in the shared trending tables under a named key, not a new table: this needs
  no migration and matches upcoming/recent.
- 2026-10-21: the user id is resolved in the data source, not passed down. The cached ranking isn't keyed by
  user, so a `userId` parameter would invite serving one reader's ranking as fresh to another.
