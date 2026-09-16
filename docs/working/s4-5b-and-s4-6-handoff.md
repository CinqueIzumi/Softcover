# S4-5b and S4-6 — hand-off

> **Delete this file once S4-6 has landed.** It exists only so the two remaining S4 commits can be
> executed in fresh sessions without re-deriving what a long session already worked out. It is a
> *hand-off*, not a second source of truth: where it disagrees with
> [`component-library-migration.md`](component-library-migration.md) or with
> [`docs/reference/design-system/component-contract.md`](../reference/design-system/component-contract.md),
> **those are right and this is stale**. Its removal belongs in the same commit as S4-6.

## Where the migration stands

Last landed: **`9697f651` — "Move the remaining domain-free primitives into :core:component"** (S4-5a).
Branch `275-migrate-every-component-into-a-corecomponent-library-driven-by-ui-models`, issue #275.

`:core:designsystem` is down to **five component files, 245 lines** — everything else is tokens:

| File | Lines | Signature today |
|---|---|---|
| `DeadlineBadge.kt` | 38 | `DeadlineBadge(status: DeadlineStatus, modifier)` |
| `DeadlineCoverOverlay.kt` | 39 | `DeadlineCoverOverlay(progress: DeadlineProgress?, modifier, content)` |
| `DeadlineSummaryLine.kt` | 93 | `DeadlineSummaryLine(progress: DeadlineProgress, dateStyle: DateStyle, modifier, foreground: Color?)` |
| `UnreleasedBadge.kt` | 69 | `UnreleasedBadge(releaseDate: LocalDate, modifier, style: UnreleasedBadgeStyle)` + `LocalDate.formatCompactRelease()` / `formatLongRelease()` |
| `UnreleasedBadgeStyle.kt` | 6 | `enum { Compact, Prominent }` |

All five are in `core/designsystem/src/commonMain/kotlin/nl/rhaydus/softcover/core/designsystem/presentation/component/`.

## Read these first

1. `component-contract.md` § 7 — normative, **R1–R11**. Two rules matter more than usual here:
   - **R10** — a UI model is never built in composition; it arrives on the `UiState`.
   - **R11** — a component takes only `model`, `onEvent` and `modifier`. No `style`, no `color`, no
     `foreground`, no `style: UnreleasedBadgeStyle` beside the model.

   Both were written *during* S4-5a and are **not** satisfied by the components that already landed;
   their retrofit is the new **S11** stage. **S4-5b is the first commit expected to satisfy them from
   the start** — do not copy the shape of `RichText` / `StatNumber` / `ClickableText`, which are named
   holdouts.
2. `component-library-migration.md` § 5g (the direction rule), § 5i (the declared-but-unassigned
   hazard), § 5k (S4-4's cover work — the closest precedent), and § 5l (S4-5a's outcome).

---

## Commit 1 — S4-5b: the deadline / unreleased family, and G2

> **LANDED — this section is spent.** Read
> [`component-library-migration.md`](component-library-migration.md) § 5m for what actually happened,
> not this. Three things below turned out to be wrong: `DeadlineSummaryUiModel` does **not** carry a
> `foreground: Color?` (a theme ink cannot be resolved in a collector — it is a
> `DeadlineSummaryTone`), the call-site table misses two consumers of the moving date formatters
> (`ExploreShelf.kt:243` and `RoadmapContent.kt:180`), and it was **three** dependencies that had to
> leave `:core:designsystem`, not two — `core-common` went with them. Kept only so § 5m's corrections
> have something to point at; it goes with the rest of this file in S4-6.

### What moves

Into `core/component/src/commonMain/kotlin/nl/rhaydus/softcover/core/component/badge/`:

- **`Badge.kt` + `BadgeUiModel(label, tone)` + `BadgeTone`** — one component covering both families.
  `DeadlineBadge`'s three status tones (`primaryContainer` / `errorContainer` / `surfaceVariant`) and
  `UnreleasedBadge`'s `primary` release tone become `BadgeTone` entries. `UnreleasedBadgeStyle`'s
  Compact/Prominent distinction is **copy plus placement, not anatomy** — the resolved label goes on
  the model and the caller places it, so the enum does not cross the boundary.
- **`CoverOverlay.kt` + `CoverOverlayUiModel?` + a trailing `content` slot** — `DeadlineCoverOverlay`'s
  shape. Nullable model, exactly as `Cover(model: CoverUiModel?)` handles the one-emission lag (§ 5k).
- **`DeadlineSummaryLine.kt` + `DeadlineSummaryUiModel(dateText, paceText, foreground: Color?)`** —
  the date string and the pace string are resolved by the mapper, and **`foreground` rides on the
  model** (R11; it is a parameter today only because the rule did not exist).

`git rm` all five originals.

### Mappers — straight to `:core:uibinding`

`core/uibinding/src/commonMain/.../deadline/DeadlineMapper.kt` and `.../release/UnreleasedMapper.kt`.
Promoted past R6's feature-local start because each lands with three consumers already.

- `DeadlineProgress.toBadgeUiModel()`, `.toCoverOverlayUiModel()`,
  `.toDeadlineSummaryUiModel(dateStyle, foreground)`.
- `LocalDate.formatCompactRelease()` / `formatLongRelease()` move here from `UnreleasedBadge.kt`.
  **This is what takes `kotlinx-datetime` off `:core:designsystem`** — verified: `UnreleasedBadge.kt`
  is the module's only user of it.

Conventions: extension function on the domain type, named `toXUiModel`, one `androidHostTest` sibling
per mapper. `CoverSourceResolver.kt` and `SpinePaletteMapper.kt` are the templates.

### The call sites (verified, with line numbers from before S4-5a — expect drift)

| Component | Consumers |
|---|---|
| `DeadlineBadge` | `feature/book_detail/.../BookDetailShelf.kt:1572` (`progress.status`), `feature/library/.../LibraryShelf.kt:1357` (`deadlineProgress.status`) |
| `DeadlineCoverOverlay` | `feature/reading/.../ReadingShelf.kt:626, 1004` — library deliberately does **not** use it (see the comment at `LibraryShelf.kt:1023`) |
| `DeadlineSummaryLine` | `feature/library/.../LibraryShelf.kt:1532`, `feature/reading/.../ReadingShelf.kt:579` (passes `foreground`), `:1077` |
| `UnreleasedBadge` | `feature/book_detail/.../BookDetailShelf.kt:452` (`Prominent`; the `Book` is already unwrapped at the layout boundary in both `BookDetailScreenLayout.*`), `feature/explore/.../ExploreShelf.kt:442, 743` (`Compact` default) |

### R9 / R10 wiring — the real work

Every model is built in a collector, never at the call site. Copy `CoverModelsCollector` +
`CoverModelsSnapshot` (added in `d3137d8a`) exactly: derive off `scope.state`, `distinctUntilChanged`,
one writer per field, registered in the feature's Koin module.

**`:feature:explore` is the awkward one.** `ExploreShelf.kt:442,743` have a live domain `Book` inside
`DiscoveryRailCard` / the series card and call `book.isUnreleased` / `book.effectiveReleaseDate` in
composition. Thread a per-book model map from a collector, the shape S4-4 used for cover models. S7
replaces these cards wholesale; this is the interim that keeps R9 honest.

**Before reporting done, grep that every new `UiState` field is both declared AND assigned** — § 5i
records that a declared-but-unpopulated field passes every gate this repo has and has already caused
one silent bug.

### G2 — both halves land here

Dropping the last two dependencies is **forced, not optional**: `onUnusedDependencies` is
`severity("fail")`, so `api(project(":core:domain"))` and `api(libs.kotlinx.datetime)` must go in the
same commit that empties them. That makes `:core:designsystem` zero-project-dependency, which is G2's
substance — so assert it here rather than leaving the property to hold by accident:

1. **Dependency gate** — extend `checkModuleGraph` in the root `build.gradle.kts` (beside
   `componentLibraryAllowedProjects` / `bannedReverseEdges`, ~lines 370-400) to fail if
   `:core:designsystem` declares any `project(...)` dependency at all.
2. **Source gate** — a detekt `ForbiddenImport` on `nl.rhaydus.softcover.core.domain.**` scoped to
   `**/core/designsystem/**` in `config/detekt/detekt.yml`, pairing with the existing `:core:component`
   rule. § 6 explains why the two are not redundant.
3. **Verify both by deliberately reintroducing a violation and watching the build fail**, then
   reverting. § 6a's standing rule: an ungated rule reads exactly like a passing one.

Tick G2 (and both sub-boxes) in § 6, and the S4-5b box in § 5.

### Docs owed (G5 blocks merge)

- `components.md` — the **Deadline badge / cover overlay / summary line** bullet and the
  **Unreleased badge** bullet both still describe the old signatures and say `core:designsystem`.
- `module-structure.md:73` — the `core:designsystem` row still says "**Being split**" and "until the
  component library lands, the reusable components themselves". After this commit it is tokens only,
  with zero project dependencies. The `core:component` row's family list needs `badge/` adding (it was
  removed in S4-5a review because the directory did not exist yet).
- `component-library-migration.md` — tick the boxes, update the status header, and append a
  `### 5m. S4-5b outcome` section in the house voice.

### Suggested subject line

`Move the deadline and unreleased badges into :core:component`

---

## Commit 2 — S4-6: split the share-card bodies, and close the docs

G2 and G3 are both done by the time this runs, and `:core:designsystem` is already tokens-only, so
S4-6 is what the tracker's § 7.0 still has open plus the doc sweep.

### The `ShareCard.kt` split — 1,061 lines, one file

Pure file surgery: no type changes, no signature changes, no behaviour change. Keep
`ShareCard(content:)`'s `when` dispatch, `ShareCardSignOff` and `ShareCardDimensions` in
`ShareCard.kt`; move each body to its own file in the same `share/` package. Current declarations:

| Move to its own file | Currently at |
|---|---|
| `BookShareCardBody` (+ `buildBookStatsLine`) | `:129` |
| `ReadingUpdateShareCardBody` + `ReadingUpdateReaderIdentity` | `:240, :380` |
| `StatShareCardBody` | `:411` |
| `QuoteShareCardBody` | `:436` |
| `YearRecapShareCardBody` | `:475` |
| `ReadingLifeShareCardBody` + `MiniScallopPortrait`, `ReadingLifeRidgeline`, `ReadingLifeGenreRanking`, `ReadingLifeGenreRow`, `ReadingLifeFooterStat`, `ReadingLifeDivider`, `normalizedReadingLifeMonths`, `readingLifeInitials` | `:520, :680, :724, :827, :854, :890, :927` |

The nine `@Preview` functions at `:991`–`1057` render *from* `ShareCardUiModel.previews`; keep that
property (R5 — the preview set and the gallery set must stay one list) and put each preview beside the
body it exercises. The one-type-per-file ktlint rule applies to types, not to composables, so grouping
a body with its own private parts in one file is correct.

Tick the six open boxes under § 7.0's "Share cards" heading.

### Docs

Whatever the S4-5b sweep did not already catch. The heavyweight item the tracker flags for later
(splitting `components.md` per family, mirroring the § 3 package layout) is **S12's**, not this
commit's — do not start it here.

### Suggested subject line

`Split the share-card bodies one per file`

---

## Verification, both commits

This machine's caveats: the aggregate `check` lifecycle cannot complete (a pre-existing
`JdkImageTransform` failure in `:app:compileDebugJavaWithJavac`), and detekt needs JDK 21.

```bash
./gradlew checkModuleGraph ktlintCheck
./gradlew compileKotlinJvm :desktopApp:compileKotlin :app:compileDebugKotlin :app:compileReleaseKotlin
./gradlew compileKotlinIosSimulatorArm64
./gradlew :core:component:projectHealth :core:designsystem:projectHealth :core:uibinding:projectHealth   # + every touched feature
JAVA_HOME=/Users/bartpeereboom/Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home ./gradlew styleCheck
./gradlew :core:uibinding:testAndroidHostTest :feature:library:testAndroidHostTest                      # + touched features
```

Baseline to compare against: **3,346 unit tests, 5 failures** — all five are the pre-existing
`AndroidLegacySecureApiKeyStorageTest` cases in the untouched `:core:preferences` (no Android Keystore
on the JVM host), reproducible on `main`.

Per `CLAUDE.md`: logic → `rhaydus-kotlin:rhaydus-logic`, render → `rhaydus-kotlin:rhaydus-ui`, tests →
`unit-test-writer` (never inline), review → `rhaydus-kotlin:code-reviewer` once per commit, style →
the `style-check` skill. **Commit only when asked.**

## Two traps this migration has already sprung

- **A model field derived from something a branch never computes.** S4-5a's one real regression: one
  `TopBarUiModel` built from a scroll-derived flag was reused on the book page's offline branch, which
  returns before the `LazyColumn` composes — so the flag was pinned to `false` and the bar rendered
  transparent. Collapsing call-site derivations into a model field is right, and it makes the field's
  *inputs* the thing to check.
- **"X already does it" is as likely to be an unswept violation as a pattern.** R11 exists because
  `StatNumber` and `ClickableText` copied `RichText`'s loose `style` parameter. Check the rule, not the
  neighbour.
