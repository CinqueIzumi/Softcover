# Component Library Migration — Implementation Tracker

> **Lifecycle.** This is a *working* tracker for a single rollout, not a roadmap document. The
> roadmap lives in GitHub Issues (see `CLAUDE.md` § Roadmap). **Delete this file in the same PR that
> completes the migration.** While it exists it is the source of truth for migration progress —
> update the checkboxes as work lands, in the same commit as the work.

**Rollout model:** one branch, one PR, merged all at once. Stages below are *commit* boundaries on
that branch, not separate pull requests. Every stage boundary must leave the branch compiling.

## Now

- **State:** S0–S4 done — G1, G2 and G3 are closed, `:core:designsystem` is tokens only with zero
  project dependencies. The migration is **paused** while token-hygiene Steps 07–09 run
  (`docs/working/token-hygiene/README.md`).
- **Next:** S5 — Primitives (§ 7.1), starting with chips, **started fresh** once the hygiene steps
  land. There is no S5 plan file on this branch; an older chips attempt exists only in `refs/stash`
  and is **not** the plan. Pinned budget for `foundations.md` (the one remaining design-system doc
  pinned by the token-hygiene gate) gets restored on S12.
- **Countdown:** Step 09 in `docs/working/token-hygiene/README.md` — 0 of 5 sessions.
- **Open questions:** none.
- **Verification:** the reliable per-change gates below; run through `scripts/gradle-quiet.sh`.
- **Uncommitted:** none.

**Branch:** `275-migrate-every-component-into-a-corecomponent-library-driven-by-ui-models`
**Issue:** [#275](https://github.com/CinqueIzumi/Softcover/issues/275) — tag `E.1`, labels
`area:cross-cutting` / `kind:tech` / `scope:L`, no milestone. Keep its Stages and Acceptance
checkboxes in step with § 5 and § 6 here.

> **Local verification caveats — read before trusting a green run.** Two pre-existing toolchain
> problems on this machine:
>
> 1. **The aggregate `check` lifecycle cannot complete** — `:app:compileDebugJavaWithJavac` fails
>    inside `JdkImageTransform` (`jlink` from Homebrew JDK 26.0.2 against `android-37.0`).
> 2. **detekt cannot run on JDK 26 at all.** Workaround: prefix with JDK 21 —
>    `JAVA_HOME=/Users/bartpeereboom/Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home ./gradlew styleCheck`
>
> Run Gradle through `scripts/gradle-quiet.sh`, not `./gradlew` directly. The plain `test` lifecycle
> task runs no KMP suites (`:app` / `:desktopApp` only); use
> `JAVA_HOME=.../jbr-21.0.11/... ./gradlew testAndroidHostTest --continue` under JDK 21 for the KMP
> suites instead.
>
> Reliable per-change gates: `checkModuleGraph`, `<module>:projectHealth`,
> `<module>:compileKotlinJvm`, `ktlintCheck`, and `<module>:detektJvmMain` under JDK 21.

---

## 1. Goal

Move every component the app renders into a first-class component library, whether it is currently
reused or not. Each component is driven by a **UI model**; families of near-duplicate components
collapse into **one** component that renders differently based on the model passed in.

## 2. Baseline (measured 2026-08-21, `release/3.2.0` @ `ada51050`)

| Metric | Value |
|---|---|
| `@Composable` declarations, whole repo | ~650 |
| …in `core:designsystem` at baseline | 132 |
| …feature-local | ~500 |
| Lines in composable-bearing `commonMain` files | 29,159 |
| …concentrated in six `*Shelf.kt` files | 12,182 |
| Types named `*UiModel` in the repo at baseline | 0 |

| File | Lines |
|---|---|
| `feature/book_detail/.../screen/BookDetailShelf.kt` | 2,864 |
| `feature/profile/.../screen/ProfileShelf.kt` | 2,368 |
| `feature/library/.../screen/LibraryShelf.kt` | 2,050 |
| `feature/explore/.../screen/ExploreShelf.kt` | 1,853 |
| `feature/reading/.../screen/ReadingShelf.kt` | 1,620 |
| `feature/settings/.../screen/SettingsShelf.kt` | 1,427 |

## 3. Target module shape

```
:core:designsystem     Tokens ONLY — theme, color roles, editorial typography, shape,
                       spacing, motion, icon catalog, illustrations, modifiers,
                       shared-element transition scopes. Zero project dependencies (G2).

:core:component        THE LIBRARY. Every component + its UI model + its preview fixtures.
                       Depends on :core:designsystem only.
                       BANNED (gated): :core:domain, :core:book, any data-area module,
                       Koin, Voyager, Apollo.

:core:uibinding        Adapters: domain model -> UI model, for mappings needed by 2+ features.
                       `api`-depends on :core:component + :core:designsystem + :core:domain
                       (decided: `api`, so a consuming feature sees both sides of a mapping
                       without re-declaring them — see § 3a).

:core:presentation     The non-component residents evicted from designsystem: navigation
                       contracts, session controllers, API-error mapping, splash/re-auth
                       state, the book-detail prefetcher, the cover-persister seam, and
                       `presentationModule`. See `module-structure.md` for the full roster.
                       `api`-depends on :core:domain and :core:component;
                       `implementation` on :core:book. NOT on :core:designsystem.
```

All four are tier `core`, so `tierOf()` in the root build classifies them automatically from their
path. The ban list that gates `:core:component`'s edges lives in § 6.

### 3a. `:core:uibinding` dependency visibility — decided: `api`

`:core:uibinding` re-exposes its edges with `api`, so a feature depending on it sees the domain type,
the UI model, and the tokens without re-declaring all three.

Policy:

- `api(project(":core:domain"))`, `api(project(":core:component"))`,
  `api(project(":core:designsystem"))` from `:core:uibinding` need no allowlist row — `:core:domain`
  is a dependency-free contract module, the `:core:component` row is written and cites this section,
  and `:core:designsystem` is a leaf now that G2 has landed.
- A shared mapper needing a genuinely data-area type (realistically only `:core:book`'s
  `IsbnEditionMatch` / `CreatedBook`) **pre-approves** adding the
  `":core:uibinding" to ":core:<data>"` row to `allowedApiDataEdges` — write it when the edge exists,
  not speculatively.

### Package layout inside `:core:component`

One directory per family. Model, component, and preview fixtures live together. **`+` = landed,
`·` = planned.** Kept in step with `GalleryFamily` (`gallery/GalleryFamily.kt`), whose KDoc points
back here.

```
component/
+ badge/       Badge  BadgeUiModel/Tone/Variant/Dimensions  CoverOverlay(+UiModel)
+              DeadlineSummaryLine(+UiModel, +Tone)
+ callout/     Banner  BannerUiModel  BannerTone            · Callout
+ celebration/ MarkAsReadBurst  MarkAsReadBurstUiModel
+ chip/        Chip  ChipUiModel  ChipEvent
+ control/     ThemePreviewTile  ColorPalettePreviewTile  PreviewTile
+              ThemeTilePainting  RichTextFormattingToolbar
+              · Toggle  · SegmentedControl  · TextField                     S8
+ cover/       Cover  CoverUiModel  CoverVariant  CoverSource
+              CoverDimensions  CoverlessTitleCover  MonogramCoverMetrics
+ dialog/      SoftcoverDatePickerDialog  PickerDates (internal)   — owes R1
+ gallery/     GalleryRegistry  GalleryEntry  GalleryFamily
+              GalleryFixture  UiModelPreviews
+ lists/       ChooseListsBottomSheet  ChooseListsUiModel/RowUiModel
+              ChooseListsVariant  ChooseListsEvent  ListMembership
+ progress/    UpdateProgressBottomSheet  ProgressSheetUiModel
+              ProgressSheetEvent  ProgressSheetTab  ProgressSheetMedium
+              · ProgressIndicator                                           S9
+ richtext/    RichText  RichTextUiModel  RichTextRun/Mark/Paragraph
+              RichTextEditing  RichTextEditorBuffer  ClickableText(+UiModel)
+ share/       ShareCard  ShareCardUiModel  ShareCardDimensions
+              one file per card body  ShareCardNumerals
+ sheet/       LoadingSheet  LoadingSheetUiModel  LoadingSheetEvent
+              · SheetScaffold  · SheetHeader  · SheetRow  · SheetFooter     S6
+ state/       EmptyState  EmptyStateUiModel   · Skeleton  · ErrorState
+ statistic/   StatNumber  StatNumberUiModel  StatNumberFormat
+              · StatTile  · Chart  · Legend
+ topbar/      TopBar  TopBarUiModel/Event/Navigation/Surface
+              SearchTopBar(+UiModel, +Event)   · BackBar
+ verdict/     VerdictBlock  VerdictSheet  VerdictSheetContext
· bookcard/    BookCard  BookCardUiModel/Variant/Content/Decorations
·              BookCardEvent  BookCardKey                                    S7
· row/         ListRow  ListRowUiModel                                       S6
· header/      SectionHeader  PageMasthead  SidebarLabel                     S5/S6
```

**No `*Previews.kt` third file.** Fixtures live on the model's companion via `UiModelPreviews<T>`
(`:core:component/gallery/`), so forgetting one is a compile error rather than a missing file.

**The gallery splits across two modules.** The registry (`:core:component/gallery/`) is pure data; the
screen lives in `feature:settings`, because it is a shipped, navigable screen and G1 bans
`:core:component` from Voyager. See § 5a.

---

## 4. The UI model contract — moved

**The contract is normative and lives in
[`docs/reference/design-system/component-contract.md`](../reference/design-system/component-contract.md)
§ 7 (R1–R11). Read it there — never re-copy the rules here.**

---

## 5. Stages

One branch. One commit (or a small run of commits) per stage. **Each stage boundary compiles.**

- [x] **S1 — Module scaffolding.** Created `:core:component`, `:core:uibinding`,
      `:core:presentation`; wired `settings.gradle.kts`; added `kotlinx-collections-immutable` to the
      version catalog; extended `checkModuleGraph` with the § 6 ban list.
- [x] **S2 — Contract & gallery scaffold.** The contract lives in
      `docs/reference/design-system/component-contract.md` § 7. Landed the `UiModelPreviews<M>`
      fixture interface and the `GalleryRegistry` / `GalleryEntry` / `GalleryFamily` scaffold in
      `:core:component`, plus `ComponentGalleryScreen` and its easter-egg trigger in
      `feature:settings` (§ 5a).
- [x] **S3 — Evict non-components.** Moved nav / session / error / state / DI / prefetch out of
      `:core:designsystem` into `:core:presentation` — 27 files, 92 re-pointed consumers. No UI or
      behaviour change.
- [x] **S4 — Tokens-only designsystem.** Moved every remaining `core:designsystem` component into
      `:core:component` as a UI model; `:core:designsystem` ends at zero project dependencies (G2, G3
      closed). Ten sub-commits, ordered consumer-first per the direction rule — **a mover may depend
      on a stayer; a stayer may never depend on a mover**:
      - [x] S4-1 — broke the theme's domain coupling (`SpinePalette` token vs. `ColorPalette`
            preference).
      - [x] S4-2a — debug screens out of `:core:designsystem`, into `app/src/debug/`.
      - [x] S4-2b — the review / verdict / share block; `RichTextUiModel` + its `:core:uibinding`
            mapper.
      - [x] S4-3 — `UpdateProgressBottomSheet`, `ChooseListsBottomSheet`; first full-contract
            (R1–R9) components.
      - [x] S4-4 — `EditionImage` → `Cover`; killed the `:core:book` edge (G3).
      - [x] S4-5a — the domain-free primitives (`Chip`, `TopBar`, `StatNumber`, `Banner`,
            `EmptyState`, `LoadingSheet`, `MarkAsReadBurst`, `ClickableText`, the Appearance preview
            tiles).
      - [x] S4-5b — the domain-typed family (`Badge`, `CoverOverlay`, `DeadlineSummaryLine`); emptied
            `:core:designsystem`'s dependency block (G2).
      - [x] S4-6 — closed the stage: `ShareCard.kt` split per body.
- [ ] **S5 — Primitives** (§ 7.1): chips/pills, badges/overlays, headers/labels, dividers, skeletons.
- [ ] **S6 — Rows & sheet chrome** (§ 7.2).
- [ ] **S7 — `BookCard`** (§ 7.3). The main event, and the risk concentration point.
- [ ] **S8 — Screen states** (§ 7.4): empty states, callouts, banners, offline/error, top bars.
- [ ] **S9 — Statistics** (§ 7.5): stat tiles, charts, legends, progress indicators.
- [ ] **S10 — Shelf teardown.** The six `*Shelf.kt` files shrink to layout + composition.
- [ ] **S11 — Contract retrofit (R10 + R11).** Sweeps every call site that builds a UI model in
      composition, or passes a loose render parameter, before the migration's final verification.
      **Completion bar: `component-contract.md` § 7.4a ends with no exceptions.**
      **R10 holdouts:** `StatNumberUiModel` (profile's four stat sites), `MarkAsReadBurstUiModel`
      (book detail / reading / motion debug), file-constant `TopBarUiModel`s on nine static bars,
      `ClickableTextUiModel` (onboarding, roadmap), `ChipUiModel.copy(selected = …)` (Library filter
      draft), `LoadingSheetUiModel` (onboarding), inline `ChipUiModel`s (share cards, Component
      Gallery). **R11 holdouts:** `RichText`, `StatNumber`, `ClickableText`, `MarkAsReadBurst`,
      `CoverlessTitleCover`, and `VerdictBlock` / `VerdictSheet` (whose R1 exception closes here too).
      Two edges stay open on purpose: a component resolving its own copy from `composeResources`
      (`offlineBannerUiModel()`), and the preview fixtures / Component Gallery, which construct
      models by definition. Settle book detail's scroll-derived `TopBarSurface` here too.
      - [ ] Fold `ShareCardPalette`'s two parallel colour `when`s into a per-variant lookup.
- [ ] **S12 — Close out.** Final gate values (G4, G5, G6), `docs/reference/design-system/` rewrite,
      gallery completeness pass, trim the component-path parenthetical in `CLAUDE.md`, delete this
      file. **Restore the doc budget** pinned by the token-hygiene gate: `foundations.md` back
      under the generic `docs/reference/**/*.md 30KB`.
      - [ ] Decide the cover-radius drift (`HiddenSeriesStack`'s 3dp vs. every other flat cover's
            4dp; Reading's three thumbnails at 6/8/10dp).
      - [ ] Decide the `"tnum"` token (~17 call sites today; one `:core:designsystem` token instead).

### 5a. The Component Gallery — decided: shipped easter egg

Not debug-only: `commonMain`, so it renders on Android, iOS and desktop. Registry
(`GalleryRegistry` / `GalleryEntry`) lives in `:core:component/gallery/`; the screen
(`ComponentGalleryScreen`) lives in `feature:settings`, since G1 bans `:core:component` from Voyager.
Trigger: seven taps on `VersionFooter`, each within two seconds of the last, `milestone` haptic on the
seventh. See `component-contract.md` § 7.5 for the shipped anatomy.

---

## 6. Gates

Convention does not hold a boundary — `:core:designsystem` is the proof. Every rule below is a build
failure.

**Practices carried forward from the stages that landed:** an absence gate (a dependency edge, an
allowlist row) lands in the same commit as the deletion that empties it, never a commit scheduled
later. Cite symbols in this file, not line numbers — line numbers drift, symbols do not. Splitting a
file always widens the visibility of whatever crosses the new file boundary; expect it rather than
treating a split as free. Hunting an unused import set: delete every import and let the compiler name
what it cannot resolve, round by round, rather than eyeballing which imports are dead.

- [x] **G1 — `:core:component` ban list.** `checkModuleGraph`'s `componentLibraryAllowedProjects` /
      `componentLibraryBannedGroups` / `conventionProvidedCoordinates`, paired with a scoped detekt
      `ForbiddenImport` rule (`component-contract.md` § 7.4) that catches usage the coordinate check
      cannot see.
- [x] **G2 — `:core:designsystem` has zero project dependencies.** `zeroProjectDependencyModules` in
      `checkModuleGraph`, paired with a detekt `ForbiddenImport` on `core.domain.**` scoped to
      `**/core/designsystem/**`.
- [x] **G3 — The `:core:designsystem` → `:core:book` api allowlist row is gone.**
- [ ] **G4 — Composable budget ratchet.** A `checkComponentBudget` task counting `@Composable`
      declarations outside `:core:component` (excluding `*Preview` functions and platform
      `expect`/`actual`s). Ceiling set to the post-migration measured value; a rise fails the build.
      Wire into `check`. S12's to write.
- [ ] **G5 — Doc rule.** `docs/reference/design-system/` updated in the same change as any
      design-system change — already enforced by `rhaydus-kotlin:code-reviewer`, which treats a
      design-system change with no doc update as a blocker.
- [ ] **G6 — `./gradlew check` green**, including `styleCheck` (type-resolved detekt across every
      module) and `ktlintCheck`.

### 6a. Gate audit — still open

- **`:app:projectHealth` is unreachable on this machine** (the pre-existing `JdkImageTransform`
  failure), so `:app`'s dependency declarations are ungated locally. CI on a working toolchain covers
  it.
- **`checkComponentBudget` (G4) is not written yet** — the count is still falling; S12's to add.

**Test posture, decided:** no Compose UI tests in this repo, none planned — coverage is unit tests on
every UI model and mapper, plus the Component Gallery as the visual-acceptance surface.

---

## 7. Family checklists

Line numbers are from the § 2 baseline commit (spot-verified against the tree) and will drift as
stages land — they are a starting address, not a guarantee. Paths are abbreviated: strip
`src/<sourceSet>/kotlin/nl/rhaydus/softcover/` out of the real path. The source set is read off the
file suffix — `.jvm.kt` -> `jvmMain`, `.mobile.kt` -> `mobileMain`, `.android.kt` -> `androidMain`,
`.ios.kt` -> `iosMain`, no suffix -> `commonMain`. So
`feature/library/presentation/screen/LibraryScreenLayout.jvm.kt` is
`feature/library/src/jvmMain/kotlin/nl/rhaydus/softcover/feature/library/presentation/screen/LibraryScreenLayout.jvm.kt`.

### 7.0 The `:core:designsystem` migration (S4)

These two land in S4, before every family below. The share cards are the reference implementation (`component-contract.md` § 7.3); rich text blocks `QuoteShareCardBody` and `VerdictSheet`.

#### Share cards — 1,161 lines -> `share/` in `:core:component`, one file per body

> **DONE.** `share/` moved and was renamed per R8 in S4-2b; S4-6 split `ShareCard.kt` into eight
> files, with every declaration carried across byte-for-byte.

- [x] Keep `ShareCard(content:)` dispatch + `ShareCardSignOff` + `ShareCardDimensions` in `ShareCard.kt` (`share/ShareCard.kt:68,959`)
- [x] `BookShareCardBody` -> own file (`share/BookShareCardBody.kt`, + `buildBookStatsLine`)
- [x] `ReadingUpdateShareCardBody`, `ReadingUpdateReaderIdentity` -> own file (`share/ReadingUpdateShareCardBody.kt`)
- [x] `StatShareCardBody` -> own file (`share/StatShareCardBody.kt`)
- [x] `QuoteShareCardBody` -> own file (`share/QuoteShareCardBody.kt`) — the R4 rich-text gap closed in S4-2b, so this was the mechanical half
- [x] `YearRecapShareCardBody` -> own file (`share/YearRecapShareCardBody.kt`)
- [x] `ReadingLifeShareCardBody` + its parts (`MiniScallopPortrait`, `ReadingLifeRidgeline`, `ReadingLifeGenreRanking`, `ReadingLifeGenreRow`, `ReadingLifeFooterStat`, `ReadingLifeDivider`, `normalizedReadingLifeMonths`, `readingLifeInitials`) -> `share/ReadingLifeShareCardBody.kt`
- [x] Rename per R8: `ShareContent` -> `ShareCardUiModel`; `BookShareContent`, `QuoteShareContent`, `StatShareContent`, `YearRecapShareContent`, `ReadingLifeShareContent`, `ReadingUpdateShareContent` -> `*ShareCardUiModel`
- [x] Update the two mappers that build them: `feature/book_detail/presentation/component/ReadingUpdateShareContentMapper.kt` (and its existing test) and the `ProfileShareBottomSheet` / `ReadingLifeSharePreview` construction sites in `feature/profile`

#### Rich text — the one non-mechanical R4 conversion

Six components take `ReviewDocument` / `ReviewParagraph` / `ReviewRun` / `ReviewMark` straight from
`:core:domain`. R4 forbids that in `:core:component`, so the library needs its own
`RichTextUiModel` + `RichTextRun` + `RichTextMark`, with the domain -> UI mapping in
`:core:uibinding` (two consumers: book_detail and the quote share card, so R6 promotes it
immediately).

- [x] `RichTextUiModel` / `RichTextRun` / `RichTextMark` in `:core:component`
- [x] `ReviewDocument` -> `RichTextUiModel` mapper in `:core:uibinding`, with tests
- [x] `ReviewRichText` — `core/designsystem/presentation/component/ReviewRichText.kt`
- [x] `ReviewDocumentText` — `core/designsystem/presentation/component/ReviewDocumentText.kt`
- [x] `ReviewMark` / `ReviewMarkType` — `core/designsystem/presentation/component/ReviewMark.kt`, `ReviewMarkType.kt`
- [x] `ReviewEditorBuffer` — `core/designsystem/presentation/component/ReviewEditorBuffer.kt`
- [x] `VerdictBlock`, `VerdictScoreAndCaption` — `core/designsystem/presentation/component/VerdictBlock.kt`
- [x] `ReviewCard` — `feature/book_detail/presentation/screen/BookDetailShelf.kt`. It takes `BookReviewUiModel`, whose `body` is a `RichTextUiModel`, so no domain type reaches it. The *component* stays feature-local — pulling the card itself into the library is S6's row work
- [x] Existing `ReviewRichTextTest` re-pointed at the new model (it currently asserts on `ReviewDocument`)
- [ ] `RichTextFormattingToolbar` still owes R1 (S6).

### 7.1 Primitives (S5)

#### Chips & pills — 29 -> `Chip` + `ChipUiModel`

- [x] `PillChip`, `PillChipLabel` — **DONE in S4-5a.** `PillChip` became `Chip` (`core/component/chip/`, with `ChipUiModel` + `ChipEvent`); `PillChipLabel` survives as its private helper. Four features consume it.
- [ ] `AddFilledPill`, `AddOutlinePill`, `MembershipPill`, `OnListChip` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3; `MembershipPill` now takes a resolved label, so only the three pill chromes are left to consolidate)
- [ ] `FormatChip` — `core/component/control/RichTextFormattingToolbar.kt` (moved + renamed in S4-2b; still a bespoke `Surface`, does not call `Chip`)
- [ ] `SearchChromePill` — `core/component/topbar/SearchTopBar.kt` (moved in S4-5a; still bespoke)
- [ ] `ActiveFilterChip`, `ClearAllChip`, `LibraryFilterChipRow` — `feature/library/presentation/component/LibraryFilterChipRow.kt:96,136,40`
- [ ] `ArrangeChip`, `LayoutChipRow`, `SortChipRow` — `feature/library/presentation/component/LibraryArrangeSheet.kt:304,201,251`
- [ ] `FilterPillControl`, `RearrangeHintChip` — `feature/library/presentation/component/LibraryControlLine.kt:209,154`
- [ ] `SelectionActionPill` — `feature/library/presentation/screen/LibraryShelf.kt:1905`
- [x] `ConcealableTagChip` — `feature/book_detail/presentation/screen/BookDetailShelf.kt`. **Already routed through the library `Chip`** (it is a thin `Chip(model = …)` wrapper); consolidation effectively done in S4-5a
- [ ] `DashedTagOpenerChip`, `ExternalLinkPill` — `feature/book_detail/presentation/screen/BookDetailShelf.kt`. Still bespoke `Surface`es (dashed border / bordered pill), untouched
- [ ] `AddPill` — `feature/book_detail/presentation/component/TagNamingField.kt:135`; `TagChip`, `TagChipName` — `feature/book_detail/presentation/component/TagChip.kt:46,134`
- [ ] `TrackingNowChip` — `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:437`
- [ ] `RecentSearchChip`, `SortChip`, `FlowRowMoodChips` — `feature/explore/presentation/screen/ExploreShelf.kt:1260,1426,1405`
- [ ] `SetProgressChip` — `feature/reading/presentation/screen/ReadingShelf.kt:1136`
- [ ] `UpdatePillButton` — `feature/settings/presentation/screen/SettingsShelf.kt:1273`
- [ ] `SortLabelControl` — `feature/library/presentation/component/LibraryControlLine.kt:107`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

#### Headers & labels — 17 -> `SectionHeader` + `PageMasthead` + `SidebarLabel`

Kills all three cross-module name collisions.

- [ ] `SectionLabel` × 4 — `app/src/debug/.../MotionDebugScreen.kt`, `feature/book_detail/presentation/screen/BookDetailShelf.kt:2535`, `feature/profile/presentation/screen/ProfileShelf.kt:136`, `feature/reading/presentation/screen/ReadingShelf.kt:1178`
- [ ] `EditorialHeader` × 2 — `core/component/progress/EditorialHeader.kt` (moved in S4-3; now takes a `title: String`), `feature/reading/presentation/screen/section/EditorialHeader.kt:27`
- [ ] `SidebarSectionLabel` × 2 — `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:416`, `feature/settings/presentation/screen/section/SettingsCategorySidebar.kt:129`
- [ ] `SmallSectionLabel`, `InlineAccentLabel` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:1128,1100`
- [ ] `SectionIntro` — `feature/profile/presentation/screen/ProfileShelf.kt:402`
- [ ] `SectionHeaderBar` — `feature/explore/presentation/screen/section/DesktopExploreSectionShared.kt:15`
- [ ] `AlsoReadingSectionHeader` — `feature/reading/presentation/screen/ReadingShelf.kt:1205`
- [ ] `SearchResultsHeader`, `HiddenSuggestionsGroupHeader` — `feature/explore/presentation/screen/ExploreShelf.kt:1472`, `feature/explore/presentation/screen/HiddenSuggestionsShelf.kt:177`
- [ ] `LibraryTabsGroupHeader`, `RowLabel`, `SettingsPageHeader`, `SidebarHeader`, `DesktopPaneHeader` — `feature/settings/presentation/screen/SettingsShelf.kt:625,968`, `SettingsScreenLayout.mobile.kt:222`, `section/SettingsCategorySidebar.kt:110`, `section/DesktopPaneHeader.kt:15`
- [ ] `MastheadHeader` — `feature/library/presentation/screen/section/MastheadHeader.kt:37`
- [ ] `ProfileHeader` — `feature/profile/presentation/screen/ProfileScreenLayout.mobile.kt:196`
- [ ] `DesktopExploreHeader`, `DesktopLibraryHeader`, `DesktopReadingHeader` — `feature/explore/presentation/screen/section/DesktopExploreHeader.kt:27`, `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:481`, `feature/reading/presentation/screen/ReadingScreenLayout.jvm.kt:220`
- [ ] `ArrangeSubLabel` — `feature/library/presentation/component/LibraryArrangeSheet.kt:191`
- [ ] `ChangeEditionHeader` — `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:208`
- [ ] `ChooseListsHeader` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3; now takes a `ChooseListsVariant` + the jacket slot)
- [ ] `ShelvesSheetHeader` — `feature/library/presentation/component/LibraryShelvesSheet.kt:90`
- [ ] `TagEditorHeader` — `feature/book_detail/presentation/component/TagEditorHeader.kt:37`
- [ ] `SelectionHeader` — `feature/library/presentation/screen/LibraryShelf.kt:1793`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

#### Badges & cover overlays — 12 -> `Badge` + `CoverOverlay`

- [x] `DeadlineBadge` -> `Badge` + `BadgeUiModel` / `BadgeTone` / `BadgeVariant` — `core/component/badge/` (S4-5b)
- [x] `DeadlineCoverOverlay` -> `CoverOverlay` + nullable `CoverOverlayUiModel` — `core/component/badge/` (S4-5b)
- [x] `UnreleasedBadge` -> the same `Badge`, `BadgeTone.Release`; `UnreleasedBadgeStyle` moved to `:core:uibinding` as a mapper input and gained a third entry (S4-5b)
- [x] `DeadlineSummaryLine` -> `DeadlineSummaryLine` + `DeadlineSummaryUiModel` / `DeadlineSummaryTone` — `core/component/badge/` (S4-5b; not originally listed here, it is the family's third member)
- [ ] `BookmarkGlyph` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3)
- [ ] `LibraryDeadlineCountdownBadge`, `CoverGridOverlay`, `SelectionCircleIndicator` — `feature/library/presentation/screen/LibraryShelf.kt:1083,1020,1658`
- [ ] `OwnedCoverBadge` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:585`
- [ ] `SelectedCheckBadge` — `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:414`
- [ ] `FolioIndicator` — `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:204`
- [ ] `GripOrPinGlyph` — `feature/settings/presentation/screen/SettingsShelf.kt:922`
- [ ] `SpoilerToggleIcon` — `feature/book_detail/presentation/component/TagChip.kt:102`
- [ ] `deadlineProgressByBook` becomes UI-typed (still domain-typed, on `LibraryUiState` / `ReadingUiState`).
- [ ] Visual pass on the deadline trio (`Badge`, `CoverOverlay`, `DeadlineSummaryLine`) — never watched rendering from its new models.
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

#### Skeletons — 9 -> `Skeleton` + `SkeletonUiModel`

- [ ] `EditorialSectionHeaderSkeleton`, `FeaturedCardSkeleton`, `RailCardSkeleton`, `TrendingCardSkeleton`, `BecauseYouReadCardSkeleton`, `SeriesCardSkeleton`, `MoodTileSkeleton` — `feature/explore/presentation/screen/ExploreShelf.kt:132,326,499,579,605,689,1185`
- [ ] `RoadmapSkeleton`, `RoadmapSkeletonLine` — `feature/settings/presentation/screen/RoadmapContent.kt:449,487`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

#### Dividers & rules — 5 -> `Divider` + `DividerUiModel`

- [ ] `DebugRowDivider` — `app/src/debug/.../DebugRoutesSection.kt:106`
- [ ] `ReadingLifeDivider` — `core/component/share/ReadingLifeShareCardBody.kt` (S4-6 split)
- [ ] `HorizontalBreak` — `feature/settings/presentation/screen/RoadmapContent.kt:379`
- [ ] `QuoteRule` — `feature/lists/presentation/screen/CreateListSheetContent.kt:176`
- [ ] `OrTypeItDivider` — `feature/onboarding/presentation/screen/OnboardingShelf.kt:237`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

### 7.2 Rows & sheet chrome (S6)

#### List rows — 22 -> `ListRow` + `ListRowUiModel`

- [ ] `ChooseListsRow`, `NewListRow` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3; `ChooseListsRow` now takes one `ChooseListsRowUiModel`)
- [ ] `WhenReadRow` — `core/component/progress/WhenReadRow.kt` (moved in S4-3, unchanged)
- [ ] `DebugNavigationRow` — `app/src/debug/.../DebugRoutesSection.kt:68`
- [ ] `HapticRow` — `app/src/debug/.../MotionDebugScreen.kt:137`
- [ ] `AboutLinkRow`, `AboutNavigationRow`, `AboutUsernameRow`, `AboutRow` — `feature/settings/presentation/screen/AboutContent.kt:192,232,275,307`
- [ ] `SettingsToggleRow`, `SettingsSelectableRow`, `ReorderableRow` — `feature/settings/presentation/screen/SettingsShelf.kt:445,517,853`
- [ ] `SettingsMenuRow` — `feature/settings/presentation/screen/SettingsScreenLayout.mobile.kt:248`
- [ ] `SettingsSidebarRow` — `feature/settings/presentation/screen/section/SettingsSidebarRow.kt:30`
- [ ] `ShelfSidebarRow` — `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:427`
- [ ] `ShelvesSheetRow`, `ShowTitlesToggleRow` — `feature/library/presentation/component/LibraryShelvesSheet.kt:128`, `LibraryArrangeSheet.kt:227`
- [ ] `ShelveRow`, `DeadlineRow` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:959,1547`
- [ ] `StreakStripSheetRow` — `feature/reading/presentation/component/StreakStrip.kt:203`
- [ ] `BecauseYouReadGenreSheetRow`, `DismissSheetOption`, `SearchFocusRecentRow` — `feature/explore/presentation/screen/ExploreShelf.kt:1811,965,1348`
- [ ] `ShareEntryRow` — `feature/profile/presentation/screen/ProfileShelf.kt:426`
- [ ] `ExplainerStepRow`, `PasteFromClipboardRow` — `feature/onboarding/presentation/screen/OnboardingShelf.kt:404,178`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

#### Sheet chrome — extract from 18 sheets -> `SheetScaffold` + `SheetHeader` + `SheetRow` + `SheetFooter`

Chrome only; each sheet's **body** stays a feature composable (`component-contract.md` § 7.6).

- [ ] `ChooseListsBottomSheet` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3; already R1/R2-shaped, so S6 owes it chrome extraction only)
- [ ] `UpdateProgressBottomSheet`, `ProgressBottomSheetContent` — `core/component/progress/UpdateProgressBottomSheet.kt`; `TabSwitcher` — `core/component/progress/TabSwitcher.kt` (moved in S4-3; already R1/R2-shaped, so S6 owes it chrome extraction only)
- [ ] `VerdictSheet` — `core/component/verdict/VerdictSheet.kt` — **also owes R1**
- [x] `SoftcoverLoadingDialog`, `SoftcoverLoadingSheet` — **DONE in S4-5a.** The sheet became `LoadingSheet` (`core/component/sheet/`, + model + event, consumed by both onboarding layouts); the dialog was deleted as dead. Sheet *chrome* extraction is still owed on `LoadingSheet` — that is this section's S6 work, not this box
- [ ] `LibraryFilterSheet`, `FilterSheetFooter`, `EmptyFacetMessage`, `TagSearchField` — `feature/library/presentation/component/LibraryFilterSheet.kt:76,394,442,309`
- [ ] `LibraryArrangeSheet` — `feature/library/presentation/component/LibraryArrangeSheet.kt:81`
- [ ] `LibraryShelvesSheet` — `feature/library/presentation/component/LibraryShelvesSheet.kt:50`
- [ ] `BulkRemoveConfirmationDialog` — `feature/library/presentation/screen/LibraryShelf.kt:1981`
- [ ] `TagEditorBottomSheet` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:81`
- [ ] `EditionBottomSheetSelector`, `EditionBottomSheetContent` — `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:60,87`
- [ ] `ShareBookBottomSheet` — `feature/book_detail/presentation/component/ShareBookBottomSheet.kt:57`
- [ ] `SoftcoverDatePickerDialog` — `core/component/dialog/SoftcoverDatePickerDialog.kt` (+ its `internal` `PickerDates.kt` helpers); arrived loose-parameter from main's 3.1.3 hotfix, so it **owes R1 + R11** (model + event), not just chrome
- [ ] `BecauseYouReadGenreSheet`, `ContinueSeriesDismissSheet`, `ContinueSeriesMenuSheet` — `feature/explore/presentation/screen/ExploreShelf.kt:1756,911,861`
- [ ] `StreakStripSheet`, `StreakStripSheetContent` — `feature/reading/presentation/component/StreakStrip.kt:161,171`
- [ ] `ProfileShareBottomSheet`, `LogOutConfirmBottomSheet` — `feature/profile/presentation/screen/ProfileShelf.kt:1890,2074`
- [ ] `CreateListSheet`, `CreateListSheetContent` — `feature/lists/presentation/screen/CreateListSheet.kt:15`, `CreateListSheetContent.kt:83`
- [ ] `UnknownIsbnSheet` — `feature/scan/presentation/component/UnknownIsbnSheet.kt:28`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

### 7.3 `BookCard` (S7) — 21 -> 1

The main event. All 21 call sites sit in five features; several participate in shared-element
transitions via `bookCoverTransitionKey`; several sit inside selection modes and lazy grids where a
stability regression is a dropped frame, not a compile error.

Variant mapping — record the assignment before writing code:

| Source | Target variant |
|---|---|
| `GridBookCell` — `feature/library/presentation/screen/LibraryShelf.kt:1302` | `Grid` |
| `CoverOnlyCell` — `LibraryShelf.kt:1273` | `CoverOnly` |
| `CompactRow` — `LibraryShelf.kt:1357` | `Row(Compact)` |
| `LargeRow` — `LibraryShelf.kt:1440` | `Row(Large)` |
| `LayoutBookEntry` — `LibraryShelf.kt:834` | dispatcher -> replaced by `BookCard` |
| `LayoutEditionEntry` — `LibraryShelf.kt:1165` | dispatcher -> replaced by `BookCard` |
| `LibraryGridCover`, `SelectableCover` — `LibraryShelf.kt:1056,1599` | -> `Cover` component |
| `DiscoveryRailCard` — `feature/explore/presentation/screen/ExploreShelf.kt:428` | `Rail` |
| `FeaturedCard` — `ExploreShelf.kt:181` | `Featured` |
| `TrendingCard` — `ExploreShelf.kt:540` | `Rail` |
| `BecauseYouReadCard` — `ExploreShelf.kt:583` | `Rail` |
| `SeriesCard`, `UnreleasedSeriesCard` — `ExploreShelf.kt:613,746` | `Rail` + series badge |
| `SearchResultRow` — `ExploreShelf.kt:1512` | `Row(Compact)` |
| `MoodTile` — `ExploreShelf.kt:1079` | `Tile` |
| `HiddenBookRow`, `HiddenSeriesRow` — `feature/explore/presentation/screen/HiddenSuggestionsShelf.kt:227,317` | `Row(Compact)` + restore trailing |
| `SeriesCoverStack` — `HiddenSuggestionsShelf.kt:413` | -> `Cover(stacked)` |
| `FeaturedBookCard` — `feature/reading/presentation/screen/ReadingShelf.kt:374` | `Featured` |
| `FeaturedBackdropCard` — `ReadingShelf.kt:438` | `Featured(backdrop = true)` |
| `FeaturedCover` — `ReadingShelf.kt:608` | -> `Cover` |
| `CompactBookEntry` — `ReadingShelf.kt:954` | `Row(Compact)` |
| `PickUpNextTile` — `ReadingShelf.kt:1388` | `Tile` |
| `LovedBookCard` — `feature/profile/presentation/screen/ProfileShelf.kt:1755` | `Rail` |
| `EditionItem` — `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:275` | `Row(Large)` + selected state |
| `StackedJackets` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3) | -> `Cover(stacked)` |

Checklist:

- [ ] `BookCardUiModel` / `BookCardVariant` / `BookCardContent` / `BookCardDecorations` / `BookCardEvent` / `BookCardKey`
- [x] `Cover` + `CoverUiModel` + `CoverVariant` (21 entries) + `CoverDimensions` — **DONE in S4-4**, with `CoverMapper` / `CoverSourceResolver` in `:core:uibinding` and `CoverModelsCollector` in four features
- [x] coverless-monogram — **DONE in S4-4** (`CoverlessTitleCover`, `MonogramCoverMetrics`)
- [ ] stacked — **not on `Cover`**: still `StackedJackets` (`core/component/lists/ChooseListsBottomSheet.kt`) and `SeriesCoverStack` (`feature/explore/.../HiddenSuggestionsShelf.kt`)
- [ ] selection — **not on `CoverUiModel`**: `SelectableCover` and `LibraryGridCover` still wrap `Cover` locally in `LibraryShelf.kt`
- [ ] deadline overlay — shipped as a *separate* component, `CoverOverlay` in `badge/` (S4-5b). Decide in S7 whether it folds onto `CoverUiModel` or stays beside it
- [ ] `BookCard` with per-variant private layouts
- [ ] Preview fixtures covering every variant × decoration combination
- [ ] Mappers: `feature:library`, `feature:explore`, `feature:reading`, `feature:profile`, `feature:book_detail` (promote to `:core:uibinding` per R6 where two features converge)
- [ ] Mapper unit tests (via `unit-test-writer`)
- [ ] Shared-element transition keys verified on library -> book detail and explore -> book detail
- [ ] Skippability verified: no per-item lambda allocation in the library grid
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

### 7.4 Screen states (S8)

#### Empty states — 8 -> `EmptyState` + `EmptyStateUiModel`

- [ ] `ChooseListsEmptyState` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3; hand-rolled `Column` + two `Text`s, does not use `EmptyState`)
- [x] `OfflineScreenContent` — **DONE in S4-5a.** `OfflineGuard.kt` was deleted whole; the content became `EmptyState` + `EmptyStateUiModel` (`core/component/state/`) behind an `offlineEmptyStateUiModel()` factory, called from four screen layouts. `rememberIsOnline` went to `core/presentation/connectivity/OnlineState.kt`
- [ ] `EmptyListScreen` — `feature/library/presentation/screen/LibraryShelf.kt:1696`
- [ ] `EmptyCurrentlyReadingScreen` — `feature/reading/presentation/screen/ReadingShelf.kt:1238`
- [ ] `HiddenSuggestionsEmptyState` — `feature/explore/presentation/screen/HiddenSuggestionsShelf.kt:497`
- [ ] `TagEditorEmptyState` — `feature/book_detail/presentation/component/TagEditorCollection.kt:109`
- [ ] `EmptyEntriesCard` — `feature/settings/presentation/screen/SettingsShelf.kt:1100`
- [ ] `EmptyDetailPane` — `orchestration/presentation/BookDetailPaneHost.kt:65`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

#### Callouts & banners — 8 -> `Callout` + `Banner`

The four `*Callout`s are one component with a tone variant.

- [ ] `StatusCallout`, `ReadInfoCallout`, `WantToReadInfoCallout`, `DnfInfoCallout` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:1663,1599,1649,1627`
- [ ] `ScanEditionUpdateBanner` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:1762`
- [x] `ConnectivityBanner` — **DONE in S4-5a.** Became `Banner` + `BannerUiModel` + `BannerTone` (`core/component/callout/`), called once from `RootScreen.kt`. The `Callout` half of this family's target is still open
- [ ] `RoadmapErrorBanner` — `feature/settings/presentation/screen/RoadmapContent.kt:123`
- [ ] `PaceNudgeRibbon` — `feature/reading/presentation/screen/ReadingShelf.kt:1520`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

#### Top bars — 9 -> `TopBar` + `SearchTopBar` + `BackBar`

`TopBar` and `SearchTopBar` stay separate (`component-contract.md` § 7.6).

- [x] `SoftcoverTopBar`, `SoftcoverSearchTopBar`, `SearchChromeBarcodeButton`, `SearchChromeInputArea` — **DONE in S4-5a.** `TopBar` and `SearchTopBar` in `core/component/topbar/` (with `TopBarUiModel`/`Event`/`Navigation`/`Surface` and `SearchTopBarUiModel`/`Event`); the two chrome helpers stayed private inside `SearchTopBar.kt`. ~12 screen layouts consume them. Only `BackBar` is left of this family
- [x] `SoftcoverTopBarAction` — **GONE, deleted in S4-5a** rather than migrated. No successor type: `TopBarUiModel` carries `title` / `subtitle` / `navigation` / `surface`, and a screen's own actions go in the trailing slot
- [ ] `TagEditorTopBar` — `feature/book_detail/presentation/component/TagEditorTopBar.kt:17`
- [ ] `DesktopBookDetailTopBar` — `feature/book_detail/presentation/screen/BookDetailScreenLayout.jvm.kt:135`
- [ ] `OnboardingTopBar` — `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:158`
- [ ] `DesktopSettingsBackBar` — `feature/settings/presentation/screen/DesktopSettingsBackBar.kt:28`
- [ ] `HiddenSuggestionsDesktopBackBar` — `feature/explore/presentation/screen/HiddenSuggestionsScreenLayout.jvm.kt:82`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

#### Controls & fields — 14 -> `Toggle` + `SegmentedControl` + `TextField`

- [ ] `TimeField` — `core/component/progress/TimeField.kt` (moved in S4-3)
- [ ] `RichTextFormattingToolbar` — `core/component/control/RichTextFormattingToolbar.kt` (was `ReviewFormattingToolbar`; moved + renamed in S4-2b. In the library, but not yet the `Toggle`/`SegmentedControl`/`TextField` consolidation this group is about)
- [ ] `LensToggle`, `LensSegment` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:632,686`
- [ ] `ShareCardVariantToggle` — `feature/book_detail/presentation/component/ShareBookBottomSheet.kt:226`
- [ ] `TagNamingField` — `feature/book_detail/presentation/component/TagNamingField.kt:49`
- [ ] `SelectCircleControl` — `feature/library/presentation/component/LibraryControlLine.kt:264`
- [ ] `BookmarkToggle`, `BecauseYouReadGenreControl` — `feature/explore/presentation/screen/ExploreShelf.kt:1642,1694`
- [ ] `YearMetricToggle`, `HideUntaggedAuthorsToggle` — `feature/profile/presentation/screen/ProfileShelf.kt:579,1069`
- [ ] `EyeToggle` — `feature/settings/presentation/screen/SettingsShelf.kt:1020`
- [ ] `KeyField` — `feature/onboarding/presentation/screen/OnboardingShelf.kt:266`
- [ ] `NameHeroField`, `PrivacyProseToggle` — `feature/lists/presentation/screen/CreateListSheetContent.kt:214,335`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

### 7.5 Statistics & progress (S9)

The `/dataviz` skill conventions apply to everything in the chart group.

#### Stat tiles — 7 -> `StatTile` + `StatTileUiModel`

- [x] `AnimatedStatNumber` (×2 overloads), `StatPulseText` — **DONE in S4-5a.** Became `StatNumber` + `StatNumberUiModel` + `StatNumberFormat` (`core/component/statistic/`); `StatPulseText` stayed its private helper. Note this migrated the *number primitive* only — `StatTile` below is untouched
- [ ] `ReadingLifeFooterStat` — `core/component/share/ReadingLifeShareCardBody.kt` (S4-6 split)
- [ ] `HeroStatCard`, `StatTile`, `SmallStatTile` — `feature/profile/presentation/screen/ProfileShelf.kt:230,292,341`
- [ ] `FeaturedProgressStat` — `feature/reading/presentation/screen/ReadingShelf.kt:765`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

#### Charts & legends — 11 -> `Chart` family + `Legend`

- [ ] `MiniBar` — `core/component/control/PreviewTile.kt` (moved in S4-5a)
- [ ] `ReadingLifeRidgeline`, `ReadingLifeGenreRanking`, `ReadingLifeGenreRow` — `core/component/share/ReadingLifeShareCardBody.kt` (S4-6 split)
- [ ] `GenreRankedBars`, `GenreRankedBar`, `GenreBarTrack` — `feature/profile/presentation/screen/ProfileShelf.kt:832,865,907`
- [ ] `YearColumnChart` — `feature/profile/presentation/screen/ProfileShelf.kt:617`
- [ ] `GenderProportionBar`, `GenderLegend` — `feature/profile/presentation/screen/ProfileShelf.kt:1115,1142`
- [ ] `DemographicProportionBar`, `DemographicLegend`, `DemographicLegendRow` — `feature/profile/presentation/screen/ProfileShelf.kt:1307,1362,1401`
- [ ] `RatingsHistogramChart`, `RatingsAverageRow` — `feature/profile/presentation/screen/ProfileShelf.kt:1646,1577`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

#### Progress — 6 -> `ProgressIndicator` + `ProgressUiModel`

- [ ] `EditorialProgressIndicator` — `core/component/progress/EditorialProgressIndicator.kt` (moved in S4-3)
- [ ] `LibraryWaveProgressRow` — `feature/library/presentation/screen/LibraryShelf.kt:1141`
- [ ] `ProgressBlock` — `feature/reading/presentation/screen/ReadingShelf.kt:1100`
- [ ] `FocusProgressBar` — `feature/session/presentation/screen/FocusModeShelf.kt:307`
- [ ] `WavyConnector`, `WavySineLine` — `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:360,390`
- [ ] Move the family's section files into `:core:component` (Step 08 split them in place).
- [ ] Move the family's `[detail]` entries (see `components.md`) into KDoc on the UI model and delete them (Step 07).

### 7.6 Deliberately out of scope

Not components; they stay where they are. Recorded so a later session does not "discover" them as
gaps.

- **Screen composition roots** — `*Screen`, `*ScreenLayout`, `*Shelf`, `Content`, `*Overlays`.
  S10 shrinks them; it does not move them.
- **Navigation shells** — `orchestration/presentation/`: `App`, `DesktopApp`, `CompactNavShell`,
  `WideNavShell`, `TabRootHost`, `BottomFloatingBar`, `DockedBottomNavigationBar`,
  `NavigationRailBar`, `EditorialSidebar`, `SidebarItem`, `BookDetailPaneHost`, `ReAuthDialog`.
- **Platform `expect`/`actual`** — `BarcodeScanner` (common/android/ios/jvm),
  `isCameraAvailable`, `isCameraPermissionGranted`, `rememberCameraPermissionRequester`,
  `Theme.{android,ios,jvm}`, `TransientNavArg.{android,jvm}`. An `expect`/`actual` is a reason a
  declaration needs platform bodies, not a reason it stays out of the library — `EditionImage`'s
  platform bodies moved to `core/uibinding/cover/LocalImageSource.{android,ios,jvm}.kt` plus
  `core/presentation/cover/CoverImagePersisterProvider.kt` while the composable itself became `Cover`.
- **Debug screens** — `MotionDebugScreen`, `ShareCardDebugScreen`, `DebugRoutesSection`, now in
  `app/src/debug/`. Not components, so not converted to UI models. `DebugRoutesContent`, the seam
  that binds them per build type, is in `:core:presentation`.
- **Share cards are IN scope** — see § 7.0. They keep their own family — do not fold them into
  `BookCard` — but they migrated in S4 with everything else in `:core:designsystem`.
- **`@Preview` functions.** Excluded from the G4 budget count.

---

## 8. Risks

| Risk | Where | Mitigation |
|---|---|---|
| **Compose stability regression** — a `List` in a UI model makes every grid item recompose per frame | S7, library grid + explore rails | R3: `kotlinx-collections-immutable`, `ImmutableList` everywhere. Verify with the compiler metrics report before S12. |
| **Lambda-allocation regression** — per-item `onClick` defeats skipping | S7 | R1: one hoisted `onEvent`, key on the model. |
| **Shared-element transitions break** | S7 | R7: key resolved by the mapper, carried on `BookCardKey`. Manually verify library -> detail and explore -> detail. |
| **Long-lived red branch.** All-at-once means the module split's compile breakage is resolved inside the branch. | S3, S4 | Stage boundaries must compile. Commit per stage so the PR is reviewable commit-by-commit even though it merges once. |
| **Session loss mid-migration** | any | This file. Update checkboxes in the same commit as the work, and record the branch name in the header. |
| **New component docs regrow `components.md`** | any | Split done (token-hygiene 07a): the file is an index; new component docs go into KDoc on the UI model plus one index line. |
| **Reviewer load.** A single PR of this size is not reviewable in the normal way. | merge | Commit-per-stage discipline; run `rhaydus-kotlin:code-reviewer` per stage, not once at the end. |

---

## 9. How to resume in a new session

Read `## Now` and the one section it names. `git log --oneline main..HEAD`. Pick up Next.

---

## Appendix A — Decisions taken

| Question | Decision | Where it lands |
|---|---|---|
| Gallery reachability | **Shipped easter egg**, not debug-only. Seven taps on `VersionFooter`. Registry in `:core:component`, screen in `feature:settings`, `commonMain` so it works on all three platforms. | § 5a |
| `:core:uibinding` dependency visibility | **`api`.** Costs nothing at the gate — all 44 domain models live in `:core:domain`, which is not a data-area module, so no allowlist row is required. A `:core:uibinding -> :core:<data>` row is **pre-approved** if a mapper ever needs one; write it when the edge exists, not speculatively. | § 3a |
| `ShareCard.kt` (1,161 lines) | **Split per body while renaming, in S4** — not deferred to S12. It is already the contract's reference implementation; one pass, not two. | `component-contract.md` § 7.3, § 7.0 |
| `SoftcoverDatePickerDialog` (main's 3.1.3 hotfix) | **`:core:component` `dialog/`, moved as-is at the merge** — `:core:designsystem` is tokens-only (G2), so it could not land where main put it. | § 7.2 Sheet chrome |
| Easter-egg gesture spec | Seven taps, a two-second window between consecutive taps, `milestone` haptic on unlock, `noRippleClickable` so the footer looks untouched. Counting logic in `SecretTapCounter`, unit-tested. | § 5a |
| Fixture size in the release binary | Not measured yet; not expected to block. Measure at S12. | S12 |
