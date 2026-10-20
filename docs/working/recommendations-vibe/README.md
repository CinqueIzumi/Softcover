# Personal recommendations from Hardcover vibes

Hardcover keeps a server-generated list of personal recommendations for every reader, and lets supporters
build their own lists ("vibes"). This plan brings both into Softcover: a Recommendations rail on Explore first,
then the follow-ups below.

## Now

- **State:** written 2026-10-21. The worktree `../Softcover-recommendations-vibe` is on the local branch
  `recommendations-vibe`, which starts from `origin/release/3.3.0` and hasn't been pushed. Phase 1 has been
  built: the code is reviewed, `styleCheck` passed, and `check koverXmlReport -x lint` passed. No GitHub
  issue exists; the user will file one.
- **Next:** phase 2, step 2a, designed with the user first.
- **Open questions:** see `## Risks`.
- **Uncommitted:** nothing. Phase 1 is committed on the local branch, not pushed.

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

**Shape.** A ranked feed in `:core:book` that goes through the generic `refreshRankedBooks`, like trending,
upcoming and recent.

- It is stored in `trending_books` / `trending_meta` under one named `"recommendations"` key. The key column
  is free-form and already shared by prefixed keys, so there is no Room schema change and no migration.
- The sign-out wipe clears it, and the orphan prune counts its rows.
- The owned-books filter stays in Explore, because `:core:lists` depends on `:core:book`.

| Step | Scope | Who | Status |
|---|---|---|---|
| 1a | Data, see below | `softcover-implementer` | [x] |
| 1b | Explore, see below | `softcover-implementer` | [x] |
| 1c | Tests, see below | two `softcover-test-writer` agents in parallel | [x] |
| 1d | `styleCheck`, `softcover-reviewer`, `check koverXmlReport -x lint`, then the user's device check | main | [x] |
| 1e | Rename the shared ranked-list storage, see below | `softcover-implementer`, then `softcover-test-writer` | [x] |

**1a: data.**

- **Query:** `GetSystemVibeBookIds.graphql` in `core/network/.../graphql/query/`.
- **Remote:** `BooksRemoteDataSource.fetchSystemVibeBookIds(vibe: SystemVibe, limit): List<Int>?`, matching the
  vibe on type and slug. It injects
  `GetUserIdUseCase`, as `QuoteRemoteDataSource` does.
  - It returns `null` when the reader has no such vibe.
  - It throws when `cached_book_ids` is not an array.
  - It skips elements that are neither a number nor a numeric string, with a `toString().toIntOrNull()`
    fallback.
- **Module edge:** `implementation(project(":core:identity"))` (not `api`), plus `includes(identityModule)`
  in `bookModule`.
- **Repository:** `observeRecommendedBooks`, `observeRecommendedFetchedAt` and
  `refreshRecommendedBooks(maxAge)`, behind their own `Mutex`.
  - An empty result is stamped, so loading settles.
  - Checked at most hourly; the books are hydrated only when the ranking changed, and pull to refresh always
    hydrates.
- **Use cases:** `ObserveRecommendedBooksUseCase`, `ObserveRecommendedFetchedAtUseCase` and
  `RefreshRecommendedBooksUseCase`, bound in `bookModule`.
- **Docs:** the `vibe_type` entry, and the Local Storage line in `docs/reference/architecture.md`
  (which feeds share the trending tables).
- **Verify:** compile only, `:core:book` / `:core:network`, plus `checkModuleGraph`. Tests are red until 1c.

**1b: Explore.**

- **Use cases:**
  - `ObserveOwnedBookIdsUseCase`, extracted from `GetBecauseYouReadBooksUseCase.buildExcludedBookIds`, which
    then uses it.
  - `GetRecommendedBooksUseCase`: rank order, owned books out, 25 cap.
- **Collector:** `RecommendedBooksCollector`, shaped like `TrendingBooksCollector`: refresh with `maxAge`
  24h, loading = `fetchedAt == null && !settled`.
- **State:** `recommended` card set, header, books and loading fields, plus `RECOMMENDED_STAND_IN_CARDS`.
- **Cards and rails:**
  - `SURFACE_RECOMMENDED`.
  - `ExploreRail.RECOMMENDED`, with card widths in both the mobile and the jvm `ExploreCardRail`.
  - The `UnreleasedCoverOverlaysCollector` input.
  - The Because-you-read card mapper gets a shape name, since it now has two users.
- **Section:** `RecommendedSection` takes Because you read's shape: `AnimatedVisibility` on "loading or has
  cards", with a `trailingGap`. `DiscoverySections` re-nests, so a collapse animates rather than jumps.
- **Refresh:** `OnRefreshAction` refreshes the list alongside trending (`maxAge` zero). `isRefreshing`
  waits for both.
- **DI:** the ScreenModel, `ExploreDependencies` and `ExploreModule`.
- **Previews:** add `ExploreMockData` so the loaded previews show cards.
- **Stability:** run `:feature:explore:stabilityDump`.
- **Stale KDoc** (through `document-code`): the jvm layout's rail order, the `UnreleasedCoverOverlaysCollector`
  claim about `OnRefreshAction`, and the rail list in `BookCardModelsCollector`.

**1c: tests.**

- **`:core:book`:**
  - jsonb parsing;
  - refresh: skipped while fresh, an unchanged ranking only re-stamped, a changed one or a forced refresh hydrated, an empty response keeps the
    cached ranking, the 50 cap.
- **Explore:**
  - the filter: order kept, 25 cap, live re-filter;
  - the collector's loading state;
  - `OnRefreshAction` running both refreshes;
  - the recommended card set.

**1e: rename the shared ranked-list storage.** `trending_books` / `trending_meta` hold every ranked list
(trending, upcoming, recent, recommendations), so the trending name misleads. No behaviour change.

- **Room:** rename the tables (e.g. `ranked_books` / `ranked_books_meta`), the `period` column (e.g. `listKey`)
  and the entities, with a migration that keeps every row. Needs a data-survival test and the full-chain
  migration test.
- **Code:** rename the `BookDao` methods (`observeTrendingBooks(period)` → `observeRankedBooks(key)`, …) and
  the matching `BooksLocalDataSource` internals. Feed-specific repository and use-case names stay
  (`observeTrendingBooks(period)` is still trending).
- **Docs:** the Local Storage line in `docs/reference/architecture.md`, and the storage-key KDocs.
- **Verify:** compile, `:core:database` and `:core:book` host tests, then the full gate.

## Phase 2: follow-ups

Each step is designed with the user before it is built.

| Step | Scope | Status |
|---|---|---|
| 2a | Library: when the Want to read shelf is empty, show the top recommendations with an add button | [ ] |
| 2b | Top Picks (`vibe_type` 3) for a thin reading history: show it until the reader has rated a few books, then Recommendations. Ties into issue #202 (onboarding starter list) | [ ] |
| 2c | Investigate first: can a system vibe take `vibe_exclusions_attributes`? Then "Not for me" on a recommended card, synced as an exclusion if it can be, otherwise hidden locally like Up next (and listed in Settings' hidden suggestions) | [ ] |
| 2d | Featured public vibes (`featured: true`) on Explore, as rails or tiles beside Browse by mood | [ ] |

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

- A Recommended feed in the Trending widget: it is being prepared on the current branch.
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
