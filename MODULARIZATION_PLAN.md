# Modularization Plan

This document describes the planned restructuring of the Softcover codebase ahead of a Gradle
multi-module split (and, later, Kotlin Multiplatform). It is a **planning document**: it records
*what* will change and *why*, in the order changes should land. No build wiring is created here —
every step below is a single-module refactor that keeps the app shippable, so the eventual
`include(":feature:x")` becomes mechanical and acyclic.

The companion document [MODULE_STRUCTURE_GUIDELINES.md](MODULE_STRUCTURE_GUIDELINES.md) defines the
target categorization (tiers, what is `core` vs `feature` vs orchestration, naming) that this plan
moves the code toward. Read it first; this plan is the migration path to that end state.

> Status legend: ☐ not started · ◐ in progress · ☑ done. Update inline as steps land.
> Authored 2026-06-01.

---

## 1. Why restructure before splitting

The current code lives in one Gradle module (`:app`) with a `core/` + `feature/` package split.
The layering *within* features is already clean — a dependency audit found **zero
`domain → data/presentation` violations**, and only `app_update/domain` touches Android types (the
in-app update flow legitimately needs `Activity`/`AppUpdateManager`). That clean layering is what
makes a module split tractable.

What is *not* yet ready is the **horizontal coupling between features**. A cross-feature import
audit surfaced three structural problems that Gradle would reject or that would force ugly module
boundaries. Each has a distinct fix. The rest of this document is those fixes, sequenced.

### 1.1 The dependency audit (snapshot, 2026-06-01)

Cross-feature import edges (`caller → target (import count)`), highest-signal first:

```
library      → settings   (40)      book_detail → books      (23)
library      → books      (18)      book_detail → deadlines  (13)
library      → lists      (15)      book_detail → lists      (11)
reading      → books      (13)      books       → settings   (12)
reading      → settings    (8)      reading     → deadlines   (8)
reading      → profile     (8)      onboarding  → settings    (6)
library      → deadlines   (6)      session     → personal    (6)
book_detail  → settings    (6)      scan        → books       (5)
explore      → books      (11)      ...plus navigation edges (see §4)
```

Three findings drive the plan:

1. **`settings` and `books` are shared kernels disguised as features.** Of all cross-feature
   imports into `settings`, **75 of 76 are `domain`** — pure config value types
   (`LibrarySortMode`, `SortDirection`, `DateStyle`, `LibraryGridLayout`, `LibrarySortSettings`),
   preference-read use cases, and user-identity use cases (`GetUserIdUseCase`,
   `InitializeUserIdAndBooksUseCase`, `ResetUserDataUseCase`). Of all imports into `books`, **64 of
   76 are `domain`** — `BooksRepository` plus the book-mutation use cases (`MarkBookAsReadUseCase`,
   `RecordBookProgressUseCase`, `RemoveBookFromLibraryUseCase`, `AddBookByIsbnUseCase`, …) that six
   other features call. These are services, not screens.

2. **True cycles that Gradle forbids:** `settings ↔ library`, `settings ↔ books`,
   `settings ↔ lists`, `settings ↔ profile`. They exist because `settings` owns cross-feature
   *orchestration* use cases (`ResetUserDataUseCase`, `InitializeUserIdAndBooksUseCase`, and
   coordination calling `RefreshLibraryUseCase` / `GetAllUserListsUseCase` / `ProfileRepository`)
   that sit in the wrong tier.

3. **Navigation cycles:** `reading/explore/library/scan → book_detail` is only `BookDetailScreen`
   + `BookInitialCover` (opening the screen), and the back-edge `book_detail → reading` is one
   presentation enum (`ProgressSheetTab`). Classic "feature A imports feature B's screen" coupling.

---

## 2. Target shape (summary)

The end state is **horizontal `:core:*` modules at the bottom, one vertical `:feature:*` module per
feature on top, and `:app` as the orchestration + navigation host at the very top.** One Gradle
module per feature — *not* a module per layer. The `domain`/`data`/`presentation` split stays as
**packages inside** each feature module (which become `commonMain`/`androidMain` source sets under
KMP later).

```
:app                      nav host + cross-feature ORCHESTRATION use cases (top tier)
   │
:feature:book_detail      aggregator; depends DOWN on the features it composes
   │
:feature:{library, reading, explore, lists, deadlines, profile, scan,
          session, onboarding, personal, connectivity, settings, app_update}
   │
:core:book                BooksRepository + book-operations use cases (the real shared "books")
   │
:core:domain  :core:preferences  :core:identity   shared model, config value types, user id
:core:designsystem (toad, theme, components)  :core:network  :core:database
```

Tier rule: a module may depend only on modules **below** it. See
[MODULE_STRUCTURE_GUIDELINES.md](MODULE_STRUCTURE_GUIDELINES.md) §"Tiers" for the authoritative
definition.

---

## 3. The pre-split refactors (ordered)

Each step is independently shippable and touches only packages/imports — no Gradle module is created
until §5. Re-run the cross-feature import audit after each step to confirm the edge it targets is
gone.

### Step 1 — Lift shared value types into `core/domain` ☐

**Move** the preference/config enums and value objects that other features consume out of
`feature/settings/domain` into `core/domain/model/`:
`LibrarySortMode`, `SortDirection`, `DateStyle`, `LibraryGridLayout`, `LibrarySortSettings`, and any
remaining `BookStatus`-style classification enums still filed under a feature.

- **Why:** these are pure, headless-consumable business/config types — by the §"Placing a new type"
  rule in `ARCHITECTURE.md` they belong in domain, not a feature. `Book` already lives in
  `core/domain/model` — this follows the same pattern.
- **Blast radius:** import-only churn across `library`, `books`, `reading`, `book_detail`,
  `onboarding`, `profile`. No logic changes.
- **Done when:** no feature imports a value type from `feature/settings`.

### Step 2 — Extract preferences + identity contracts into core ☐

**Move** the `SettingsRepository` interface, its preference-**read** use cases
(`GetDateStyleAsFlowUseCase`, `GetLibrarySortSettingsAsFlowUseCase`,
`GetLibraryGridLayoutAsFlowUseCase`, `GetEnabledStatusCodesAsFlowUseCase`, etc.), and the
user-identity use cases (`GetUserIdUseCase`, and the read side of user-id storage) into new
`core/preferences/` and `core/identity/` packages.

- The `settings` **feature** keeps only the settings *screens* and the write/mutation paths that are
  genuinely settings-screen concerns.
- **Why:** preference reads and user identity are cross-cutting infrastructure (like DataStore
  itself), consumed by nearly every feature. Keeping them in a "feature" is what creates most of the
  inbound pressure on `settings`.
- **Done when:** the only inbound edges left on `feature/settings` are write-path calls from screens
  that genuinely belong to settings.

### Step 3 — Extract book-operations into `core/book` ☐

**Move** `BooksRepository` and the cross-feature book use cases (mark-as-read/reading/want-to-read,
record/update progress, update rating/review/edition, add-by-ISBN, resolve-by-ISBN, reorder shelf,
the `Get*UserBooks*` queries) out of `feature/books` into `core/book/`.

- Any book-**screen** UI that is genuinely its own surface stays in a thin `feature` (or folds into
  `library`). The cross-cutting prefetch helpers (`rememberBookDetailPrefetcher`,
  `prefetchBookDetailOnPress`, `LocalBookDetailPrefetcher`) move to `core/presentation`.
- **Why:** `books` is doing double duty as a feature *and* the book-operations service the rest of
  the app is built on. Splitting the service out lets every feature depend on `core/book` without
  depending on a sibling feature.
- **Done when:** no feature imports `feature/books`; book operations come from `core/book`.

### Step 4 — Move orchestration use cases up to `:app` ☐

**Move** the cross-feature orchestration use cases — `ResetUserDataUseCase`,
`InitializeUserIdAndBooksUseCase`, and any refresh/sync coordination that reaches into
`RefreshLibraryUseCase` / `GetAllUserListsUseCase` / `ProfileRepository` — out of `feature/settings`
into the top tier (an `app/`-level `orchestration/` package, or a `core/sync` module if it grows).

- **Why:** these are the edges that create the `settings ↔ library/lists/profile` cycles. They are
  *allowed* to know about multiple features — but only from the top tier, which depends downward on
  all of them. Relocating them **breaks all four `settings ↔ *` cycles** at once.
- **Done when:** the audit shows no `settings → {library, lists, profile, books}` edges.

### Step 5 — Break navigation cycles with a routing contract ☐

**Introduce** a navigation contract in `core/presentation` (a route key, or a `BookDetailNavigator`
interface) so `reading`, `explore`, `library`, and `scan` open the book-detail surface **without
importing `BookDetailScreen`**. **Move** the shared presentation types they pass across the boundary
— `BookInitialCover`, and `book_detail`'s `ProgressSheetTab` — into `core/presentation/model/`.

- **Why:** removes the `* → book_detail` screen imports and the `book_detail → reading` back-edge,
  leaving `book_detail` as a clean top-of-graph aggregator with no inbound feature edges.
- **Done when:** no feature imports `feature/book_detail`; cross-feature navigation goes through the
  contract.

### Step 6 — Acknowledge `app_update`'s Android domain ☐

**No code change.** `feature/app_update/domain` is the only domain layer that imports Android types
(`AppUpdateRepository`, `StartAppUpdateFlowUseCase` need `Activity`/`AppUpdateManager`). This is
correct. Recorded here so it is a deliberate decision, not a surprise: under KMP this module's domain
stays in `androidMain`, and it must not be a target for `commonMain` extraction.

---

## 4. Verification between steps

After each step, re-run the cross-feature import audit and confirm the targeted edge is gone and no
new cycle appeared:

```bash
cd app/src/main/java/nl/rhaydus/softcover
for f in feature/*/; do
  name=$(basename "$f")
  grep -rhoE "import nl\.rhaydus\.softcover\.feature\.[a-z_]+" "$f" 2>/dev/null \
    | sed -E 's/.*feature\.//' | grep -v "^${name}$" | sort | uniq -c \
    | while read c t; do echo "  $name -> $t ($c)"; done
done
```

The exit criterion for the whole of §3 is: **the only cross-feature edges that remain point from
`:app` and `:feature:book_detail` downward** — i.e. the graph is a DAG that matches the tiers in §2.

---

## 5. The mechanical split (after §3 is green)

Only once the audit is acyclic and core-pointing:

1. Create `:core:*` and `:feature:*` modules; add a `build.gradle.kts` per module (a convention
   plugin in `build-logic/` keeps these uniform).
2. Move each package folder into its module unchanged; fix `package`/`import` paths.
3. Wire `include(...)` in `settings.gradle.kts` and declare module dependencies following the tier
   order in §2 — never upward.
4. Split each feature's Koin module into a per-module `module { }`, aggregated at `:app`.

No business logic changes in §5 — it is folder moves plus build files. The hard thinking is all in
§3.

---

## 6. What this sets up (KMP, later — out of scope here)

This plan deliberately stops at an Android multi-module structure. It is shaped so the later KMP
conversion is a per-module `commonMain`/`androidMain` source-set split rather than a re-architecture:
`core/domain`, `core/preferences`, `core/identity`, and `core/book` are the natural first
`commonMain` candidates; `app_update`, `scan`, notification/WorkManager code, and Play in-app updates
stay `androidMain`. That migration is tracked separately and is not part of this document.
