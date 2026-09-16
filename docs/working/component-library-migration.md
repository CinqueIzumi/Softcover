# Component Library Migration — Implementation Tracker

> **Lifecycle.** This is a *working* tracker for a single rollout, not a roadmap document. The
> roadmap lives in GitHub Issues (see `CLAUDE.md` § Roadmap). **Delete this file in the same PR that
> completes the migration.** While it exists it is the source of truth for migration progress —
> update the checkboxes as work lands, in the same commit as the work.

**Rollout model:** one branch, one PR, merged all at once. Stages below are *commit* boundaries on
that branch, not separate pull requests. Every stage boundary must leave the branch compiling.

**Status:** `S4 DONE — S5 NEXT`. Every sub-commit of S4 has landed. `:core:designsystem` is tokens
only with zero project dependencies, and **G1, G2 and G3 are all closed**. It took **ten commits
against the six the re-cut below planned** — S4-2 and S4-5 each split into a/b, and the
Compose-resource packaging incident (§ 5m) cost two more. The two plan counts at lines 377 and 381
are left as the record of what was planned; neither is the outcome. The next stage is
**S5 — Primitives** (§ 7.1). **§ 5g is the most important thing to read before continuing:** the
direction rule re-cut S4's sub-commits, and the original ordering was impossible. **§ 5k** records
S4-4's outcome — the `:core:book` edge is dead and **G3 is closed**, `CoverVariant` is the second and
larger instance of R2's per-variant metrics table, and R9 finally reached the list-shaped surfaces.
**§ 5m** records S4-5b's — G2, and the three dependencies that had to leave in one commit. **§ 5n**
records S4-6's — the `ShareCard.kt` split, and why a stage kept shrinking under its own gates.

**Branch:** `275-migrate-every-component-into-a-corecomponent-library-driven-by-ui-models`
**Issue:** [#275](https://github.com/CinqueIzumi/Softcover/issues/275) — tag `E.1`, labels
`area:cross-cutting` / `kind:tech` / `scope:L`, no milestone. Keep its Stages and Acceptance
checkboxes in step with § 5 and § 6 here.

> **Local verification caveats — read before trusting a green run.** Two pre-existing toolchain
> problems on this machine, both verified pre-existing by stashing the branch and reproducing them:
>
> 1. **The aggregate `check` lifecycle cannot complete.** `:app:compileDebugJavaWithJavac` fails
>    inside `JdkImageTransform` (`jlink` from Homebrew JDK 26.0.2 against `android-37.0`).
> 2. **detekt cannot run on JDK 26 at all** — every detekt task dies with a bare `> 26.0.2` version
>    parse error, including on modules this branch never touches. **Workaround that works:** prefix
>    with JDK 21 —
>    `JAVA_HOME=/Users/bartpeereboom/Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home ./gradlew styleCheck`
>
> Also note: `styleCheck` **was** red from S1 to S3 for a reason unrelated to the migration —
> `SoftcoverColorSchemeTest.kt:171` had an `UnusedParameter` finding, reproduced with the branch
> stashed, and out of the on-touch compliance policy's reach because nothing in the migration touched
> that file. **S4-1 does touch it** (it re-points the test onto `SpinePalette`), so the finding is
> fixed — see § 5f. Per-module detekt is green everywhere the migration has been.
>
> Reliable per-change gates: `checkModuleGraph`, `<module>:projectHealth`,
> `<module>:compileKotlinJvm`, `ktlintCheck`, and `<module>:detektJvmMain` under JDK 21.

---

## 1. Goal

Move every component the app renders into a first-class component library, whether it is currently
reused or not. Each component is driven by a **UI model**; families of near-duplicate components
collapse into **one** component that renders differently based on the model passed in.

## 2. Baseline (measured 2026-08-21, `release/3.2.0` @ `ada51050`)

> **Frozen snapshot — do not edit forward.** Every number, path and line reference in § 2 describes
> the tree at `ada51050`, before the first migration commit. Several are now false by design: the
> repo had **0** `*UiModel` types then and has ~30 now; `:core:designsystem` had 132 `@Composable`
> declarations and has 16. That is the migration working, and a baseline edited to match the present
> measures nothing. **§ 5's outcome sections and § 7's checkboxes are the live state**; read those for
> what is true today. The one instruction in § 2 that *has* expired is the re-measure prompt below —
> it was written for someone about to start, and the migration is five stages in.

Re-measure before starting; if these numbers have moved materially, the checklists below need a
refresh pass first.

| Metric | Value |
|---|---|
| `@Composable` declarations, whole repo | **~650** (567 top-level `fun` declarations after a preview-inclusive sweep) |
| …in `core:designsystem` | 132 |
| …feature-local | ~500 |
| Lines in composable-bearing `commonMain` files | 29,159 |
| …concentrated in six `*Shelf.kt` files | 12,182 |
| Types named `*UiModel` in the repo | **0** |
| Working instances of the pattern under another name | **1** — the `share/` package |

Reproduce the count with:

```bash
for d in core/* feature/* orchestration app; do
  echo "$d $(grep -rho '@Composable' $d/src 2>/dev/null | wc -l)"
done
```

The six concentration points:

| File | Lines |
|---|---|
| `feature/book_detail/.../screen/BookDetailShelf.kt` | 2,864 |
| `feature/profile/.../screen/ProfileShelf.kt` | 2,368 |
| `feature/library/.../screen/LibraryShelf.kt` | 2,050 |
| `feature/explore/.../screen/ExploreShelf.kt` | 1,853 |
| `feature/reading/.../screen/ReadingShelf.kt` | 1,620 |
| `feature/settings/.../screen/SettingsShelf.kt` | 1,427 |

### Why the current module cannot be the library

`core:designsystem` `api`-depends on `:core:domain` **and** `:core:book`, and alongside components it
houses navigation contracts (`AppNavigator`, `ScreenDestination`, `TabDestination`, `TransientNavArg`,
`BookDetailPresenter`, `CreateListPresenter`), the Koin module (`DesignSystemModule`), a prefetcher
that calls `FetchBookByIdUseCase` (`BookDetailPrefetcher`), session controllers, API-error mapping,
and `SplashState`/`ReAuthState`. Components take domain types directly —
`DeadlineBadge(status: DeadlineStatus)`, `EditionImage(edition: BookEdition)`. A library cannot be a
library while it imports use cases.

There is also an existing allowlist row recording the leak:
`":core:designsystem" to ":core:book"` in `allowedApiDataEdges`. **That row disappearing is a
measurable outcome of this migration.**

> **All of that paragraph is now history — the whole section describes a module that no longer
> exists in this shape.** S3 moved the navigation contracts, the Koin module, the prefetcher, the
> session controllers, the error mapping and the state holders into `:core:presentation` (§ 5e). S4
> finished the job: `:core:designsystem` declares **zero** `project(...)` dependencies, the
> `:core:book` allowlist row is gone (G3, S4-4), the `:core:domain` edge went with the `Deadline*`
> trio (G2, S4-5b), and no component takes a domain type because no component lives there at all.
> `EditionImage` itself is gone — it became `Cover` in `:core:component` during S4-4.

### Confirmed duplication

Same-named composables declared in multiple modules today:

- `SectionLabel` × 4 — `app/src/debug/.../MotionDebugScreen.kt` (was `core/designsystem/.../debug/`),
  `feature/book_detail/.../BookDetailShelf.kt:2535`, `feature/profile/.../ProfileShelf.kt:136`,
  `feature/reading/.../ReadingShelf.kt:1178`
- `EditorialHeader` × 2 — `core/designsystem/.../component/UpdateProgressBottomSheet.kt:250`,
  `feature/reading/.../ReadingScreenLayout.mobile.kt:158`
- `SidebarSectionLabel` × 2 — `feature/library/.../LibraryScreenLayout.jvm.kt:416`,
  `feature/settings/.../SettingsScreenLayout.jvm.kt:260`

---

## 3. Target module shape

```
:core:designsystem     Tokens ONLY — theme, color roles, editorial typography, shape,
                       spacing, motion, icon catalog, illustrations, modifiers,
                       shared-element transition scopes.
                       Target: ZERO project dependencies.

:core:component        THE LIBRARY. Every component + its UI model + its preview fixtures.
                       Depends on :core:designsystem only.
                       BANNED (gated): :core:domain, :core:book, any data-area module,
                       Koin, Voyager, Apollo.

:core:uibinding        Adapters: domain model -> UI model, for mappings needed by 2+ features.
                       `api`-depends on :core:component + :core:designsystem + :core:domain
                       (decided: `api`, so a consuming feature sees both sides of a mapping
                       without re-declaring them — see § 3a).

:core:presentation     LANDED IN S3. The non-component residents evicted from designsystem:
                       AppNavigator, AppEntryPoint, ScreenDestination, TabDestination,
                       TransientNavArg, BookInitialCover, BookDetailPresenter,
                       CreateListPresenter, ActiveSession(Controller), SessionAuthenticator,
                       ReadingSessionLauncher, SessionFormatting, LibraryTab, ApiErrorMessage,
                       ApiFailureHandling, SplashState, ReAuthState, BookDetailPrefetcher,
                       LibraryNavPulseKey, LocalAppUpdate, and presentationModule.
                       `api`-depends on :core:domain and :core:component (the latter added in
                       S4-4, for ActiveSession's CoverUiModel — it carries an allowlist row);
                       `implementation` on :core:book. NOT on :core:designsystem.
                       (The "TOAD wiring" this line used to claim never existed there — § 5e.)
```

All four are tier `core`, so `tierOf()` in the root build classifies them automatically from
their path. No tier-map change is needed; the new **ban list** is (see § 6).

### 3a. `:core:uibinding` dependency visibility — decided: `api`

`:core:uibinding` re-exposes its edges with `api`, so a feature depending on it sees the domain type,
the UI model, and the tokens without re-declaring all three.

**What that was predicted to cost at the gate: nothing. It cost two rows, and the rule itself was
rewritten.** The prediction held only for the *domain* edge: all **44** domain models live in
`:core:domain`, which is an infra/contract module and may `api`-expose freely, and `:core:book` owns
only two models of its own (`IsbnEditionMatch`, `CreatedBook`) alongside its repository and use
cases. That part is still true and still means no `:core:<data>` row was needed.

What the prediction missed is that the **rule's scope was not fixed**. It covered only
`dataAreaModules` when this was written; § 6a's audit widened it and renamed it **`apiSignOffModules`**,
which now includes `:core:component`, `:core:presentation` and `:core:uibinding` — added *by this
migration*, after `:feature:book_detail` declared `api(project(":core:component"))` with no
justification and no gate noticed. So the migration's own `api` edges became sign-off-bearing, and
two rows were written:

- **`":core:uibinding" to ":core:component"`** — the deliberate re-export this section decided on.
- **`":core:presentation" to ":core:component"`** — `ActiveSession`'s `CoverUiModel` (S4-4), which
  this section did not anticipate at all.

**The transferable point:** "no allowlist row needed" is a claim about a rule, not about the code,
and a rule that a migration is actively rewriting cannot be quoted as a fixed cost. State the edge
you intend; re-check the gate that governs it at the commit that adds the edge.

Policy recorded so nothing blocks mid-migration:

- `api(project(":core:domain"))`, `api(project(":core:component"))`,
  `api(project(":core:designsystem"))` from `:core:uibinding` — the `:core:component` row is written
  and cites this section; the other two need none.
- Should a shared mapper genuinely need a *data-area* type (realistically only `:core:book`'s
  `IsbnEditionMatch` / `CreatedBook`), **this decision pre-approves adding the
  `":core:uibinding" to ":core:<data>"` row** to `allowedApiDataEdges` without further sign-off.
  Write the row when the edge exists — not speculatively, or the allowlist rots into noise the way
  the designsystem row did.
- The row anticipated for `:core:presentation` -> `:core:book` was **never needed**: the evicted
  `BookDetailPrefetcher` consumes `FetchBookByIdUseCase` and `PersistEditionImageUseCase`, but
  `implementation(project(":core:book"))` was enough — the use-case types never surfaced in its public
  API. The row `:core:presentation` *did* need was to `:core:component`, for a reason this section
  never saw coming.

### Package layout inside `:core:component`

One directory per family. Model, component, and preview fixtures live together.

One directory per family; model, component and fixtures live together. **`+` = landed, `·` = planned.**
Kept in step with `GalleryFamily` (`gallery/GalleryFamily.kt`), whose KDoc points back here.

```
component/
+ badge/       Badge  BadgeUiModel/Tone/Variant/Dimensions  CoverOverlay(+UiModel)
+              DeadlineSummaryLine(+UiModel, +Tone)                          S4-5b
+ callout/     Banner  BannerUiModel  BannerTone            · Callout        S4-5a
+ celebration/ MarkAsReadBurst  MarkAsReadBurstUiModel                       S4-5a
+ chip/        Chip  ChipUiModel  ChipEvent                                  S4-5a
+ control/     ThemePreviewTile  ColorPalettePreviewTile  PreviewTile
+              ThemeTilePainting  RichTextFormattingToolbar                  S4-2b/S4-5a
+              · Toggle  · SegmentedControl  · TextField                     S8
+ cover/       Cover  CoverUiModel  CoverVariant  CoverSource
+              CoverDimensions  CoverlessTitleCover  MonogramCoverMetrics    S4-4
+ gallery/     GalleryRegistry  GalleryEntry  GalleryFamily
+              GalleryFixture  UiModelPreviews                               S2
+ lists/       ChooseListsBottomSheet  ChooseListsUiModel/RowUiModel
+              ChooseListsVariant  ChooseListsEvent  ListMembership          S4-3
+ progress/    UpdateProgressBottomSheet  ProgressSheetUiModel
+              ProgressSheetEvent  ProgressSheetTab  ProgressSheetMedium     S4-3
+              · ProgressIndicator                                           S9
+ richtext/    RichText  RichTextUiModel  RichTextRun/Mark/Paragraph
+              RichTextEditing  RichTextEditorBuffer  ClickableText(+UiModel) S4-2b
+ share/       ShareCard  ShareCardUiModel  ShareCardDimensions
+              one file per card body  ShareCardNumerals                     S4-2b/S4-6
+ sheet/       LoadingSheet  LoadingSheetUiModel  LoadingSheetEvent          S4-5a
+              · SheetScaffold  · SheetHeader  · SheetRow  · SheetFooter     S6
+ state/       EmptyState  EmptyStateUiModel   · Skeleton  · ErrorState      S4-5a
+ statistic/   StatNumber  StatNumberUiModel  StatNumberFormat
+              · StatTile  · Chart  · Legend                                 S4-5a
+ topbar/      TopBar  TopBarUiModel/Event/Navigation/Surface
+              SearchTopBar(+UiModel, +Event)   · BackBar                    S4-5a
+ verdict/     VerdictBlock  VerdictSheet  VerdictSheetContext               S4-2b
· bookcard/    BookCard  BookCardUiModel/Variant/Content/Decorations
·              BookCardEvent  BookCardKey                                    S7
· row/         ListRow  ListRowUiModel                                       S6
· header/      SectionHeader  PageMasthead  SidebarLabel                     S5/S6
```

**No `*Previews.kt` third file.** The original plan gave each family one; S2 landed R5 as the
`UiModelPreviews<T>` interface instead, so fixtures live on the model's companion inside the model's
own file and forgetting them is a compile error rather than a missing file.

Two directories now mean something other than the plan intended, which is fine but worth saying out
loud: `control/` holds the Appearance preview tiles and the rich-text toolbar rather than form
controls, and `sheet/` holds `LoadingSheet` rather than the chrome primitives S6 will add beside it.

**The gallery splits across two modules.** The *registry* — the list of every component paired with
its `previews` fixtures — is pure data and lives in `:core:component/gallery/`. The *screen* lives in
`feature:settings`, because it is a shipped, navigable screen and G1 bans `:core:component` from
Voyager. See § 5a.

---

## 4. The UI model contract — moved

**The contract is normative and lives in
[`docs/reference/design-system/component-contract.md`](../reference/design-system/component-contract.md)
§ 7 (R1–R11). Read it there.**

This section used to carry a drafting copy alongside it, marked "canonical as of S2" but kept for the
record. By S4-6 the copy had drifted far enough to be actively misleading, so it was deleted rather
than re-synced:

- it never gained **R9** (mapping happens off the composition), **R10** (a model is never built in
  composition) or **R11** (model, `onEvent`, `modifier` — nothing else), all three written mid-migration;
- its R3 said `kotlinx-collections-immutable` was not in the version catalog and had to be added — it
  went in during **S1**, which this file's own § 5 bullet records twenty lines further down;
- its R5 predated `UiModelPreviews<T>`, so it described fixtures as a plain companion property rather
  than the compile-checked interface that shipped in S2;
- its § 4.3 worked example — the part explicitly sold as *"read it before writing any new
  component"* — still pointed at `core/designsystem/presentation/share/`, a directory deleted in
  S4-2b, named types (`ShareContent`, `BookShareContent`) renamed by R8 in the same commit, and
  listed two "gaps to close" that are both closed.

**The lesson, which is the reason this is a deletion and not an update.** A normative rule set with
two homes has one real home and one trap; the trap is the copy that is *nearly* right, because it
reads as authoritative and nothing fails when it rots. This repo already made that call once — the
Roadmap section of `CLAUDE.md` records retiring four layered planning docs because a shipped item had
to be deleted from up to five places. The same argument applies to a contract. **Do not re-copy the
rules here.** Where this tracker needs to talk about a rule, it cites the number and links.

What stays here is the *history* the canonical doc does not carry: § 5i records why R9 had to be
written mid-migration, § 5l why R10 did, § 5m why `DeadlineSummaryTone` exists instead of a
`foreground` parameter, and the **S11** stage (§ 5) carries the R10/R11 retrofit with its holdout list.

---


## 5. Stages

One branch. One commit (or a small run of commits) per stage. **Each stage boundary compiles.**

- [x] **S1 — Module scaffolding.** DONE. Created `:core:component`, `:core:uibinding`,
      `:core:presentation`; wired `settings.gradle.kts`; three module build files
      (`softcover.kmp.library` + `softcover.kmp.compose`); added `kotlinx-collections-immutable`
      (0.4.0) to `gradle/libs.versions.toml`; extended `checkModuleGraph` with the § 6 ban list.
      Nothing moved yet. **See § 5b for two findings that changed the design.**
- [x] **S2 — Contract & gallery scaffold.** DONE. The contract is written into
      `docs/reference/design-system/component-contract.md` as § 7 of the design system, citing
      `ShareCard` as the reference implementation (its § 7.3) — **that doc is the only home for the
      contract; § 4 here kept a drafting copy until S4-6 deleted it.** Landed the `previews` fixture pattern as a
      compile-checked interface (`UiModelPreviews<M>`), the `GalleryRegistry` / `GalleryEntry` /
      `GalleryFixture` / `GalleryFamily` scaffold in `:core:component`, and the
      `ComponentGalleryScreen` + its TOAD wiring + the easter-egg tap gesture in `feature:settings`.
      **See § 5d for the gesture spec (previously open) and three implementation notes.**
- [x] **S3 — Evict non-components.** DONE. Moved nav / session / error / state / DI / prefetch, plus
      `LocalAppUpdate`, `LibraryNavPulseKey`, `BookInitialCover` and `LibraryTab`, out of
      `:core:designsystem` into `:core:presentation` — 27 files — and re-pointed 92 consumer files
      across 11 modules. No UI change, no behaviour change. **See § 5e for what this stage settled,
      including the one thing S3's own description here had wrong.**
- [x] **S4 — Tokens-only designsystem.** DONE. Move the existing `core:designsystem` components into
      `:core:component`, converting each to a UI model as it moves (they cannot land domain-typed —
      the gate rejects it). Write their mappers per R6. Includes the `share/` package: split the six
      card bodies one-per-file, rename `*ShareContent` -> `*ShareCardUiModel` per R8, and close its
      R4 gap via the rich-text item in § 7.0. `:core:designsystem` ends at zero project
      dependencies; delete the `":core:designsystem" to ":core:book"` allowlist row.

      **Measured before starting** (§ 2's "132 components" was a `@Composable`-count estimate; these
      are the files): 44 `commonMain` files leave, plus 3 `EditionImage` platform actuals and 3
      `androidMain` debug screens — 105 `@Composable` declarations of the module's 118, ~7,700 lines,
      of which 2,364 are two files (`UpdateProgressBottomSheet` 1,203, `ShareCard` 1,161). **66
      consumer files** across 11 modules (122 import lines) get re-pointed. Seven sub-commits, each
      compiling; § 5f records what each settled.

      **Re-cut after § 5g** — the original ordering moved shared leaves before their consumers, which
      the direction rule makes impossible. Six sub-commits, ordered consumer-first (it took ten —
      see the status header):

      - [x] **S4-1 — Break the theme's domain coupling.** `SpinePalette` token, `SoftcoverTheme` on a
            resolved `darkTheme: Boolean`, `LocalThemeConfiguration` + `ThemeMode.isDark()` to
            `:core:presentation`, `ColorPalette.toSpinePalette()` as `:core:uibinding`'s first
            resident. **See § 5f.**
      - [x] **S4-2a — Debug screens out of `:core:designsystem`.** `MotionDebugScreen`,
            `ShareCardDebugScreen`, `DebugRoutesSection` -> `app/src/debug/`; `DebugRoutesContent` ->
            `:core:presentation`. Pure relocation, no UI models. Drops them out of S4-2b's closure,
            removes them from the release binary, and lets `:core:designsystem` drop Voyager.
            **See § 5g.**
      - [x] **S4-2b — The review / verdict / share block.** `RichTextUiModel` + the bidirectional
            `:core:uibinding` mapper, `RichText` (ex-`ReviewDocumentText`), the editor helpers,
            `RichTextFormattingToolbar`, `VerdictBlock`, `VerdictSheet`, `VerdictSheetContext`, and the
            whole `share/` package with its R8 rename and R4 fix. **They moved together because the
            direction rule left no alternative** (§ 5g), not because it was convenient. **See § 5h.**
      - [x] **S4-3 — The two remaining big sheets**: `UpdateProgressBottomSheet` (+ `ProgressSheetTab`,
            `ProgressSheetTabMapping` -> `:core:uibinding`), `ChooseListsBottomSheet` (+
            `ListMembership`), `PreviewData` -> `:core:domain`. Both landed **fully R1–R9 compliant**,
            not just R4-clean — the library's first sealed event lambdas. **See § 5j.**
      - [x] **S4-4 — `EditionImage` -> `Cover`**: `CoverUiModel` / `CoverSource` / `CoverVariant` /
            `CoverDimensions`, `LocalCoverImagePersister`, the `:core:uibinding` resolver + mappers,
            `CoverlessTitleCover` + `MonogramCoverMetrics`, the three platform actuals, and **29 call
            sites in 11 files across 5 features** (plus 2 `rememberEditionImageRequest` and 1
            non-Compose `resolveEditionImageSource` — 32 across 13 files; the "38 in 9" this line used
            to claim was wrong in both directions). Killed the `:core:book` edge and **closed G3**.
            **See § 5k.**
      - [x] **S4-5 — The remaining primitives**, in two commits (§ 5l):
            - [x] **S4-5a — the domain-free primitives.** `PillChip` -> `Chip`, `SoftcoverTopBar` ->
                  `TopBar` (+ `SearchTopBar`), `AnimatedStatNumber` -> `StatNumber`,
                  `ConnectivityBanner` -> `Banner`, `OfflineScreenContent` -> `EmptyState`,
                  `SoftcoverLoadingSheet` -> `LoadingSheet`, `MarkAsReadBurst`, `ClickableText`, and
                  the two Appearance preview tiles + `PreviewTileFrame` / `MiniBar` /
                  `ThemeTilePainting`; `:core:component`'s own `compose.resources`; `rememberIsOnline`
                  hoisted to `:core:presentation`; `SoftcoverLoadingDialog` and `SoftcoverTopBarAction`
                  deleted as dead.
            - [x] **S4-5b — the domain-typed family.** DONE. The `Deadline*` trio and the
                  `Unreleased*` pair became `Badge` / `CoverOverlay` / `DeadlineSummaryLine` in
                  `:core:component`'s `badge/`, with their `:core:uibinding` mappers and six R9/R10
                  collectors across five features. Emptied `:core:designsystem`'s dependency block
                  and landed **G2**, both halves. **See § 5m.**
      - [x] **S4-6 — Close the stage.** DONE. Its gate work had all been forced early (G3 by S4-4,
            G2 by S4-5b), so what landed was the `ShareCard.kt` per-body split (§ 7.0) plus a close-out
            that turned out to be the larger half: an audit of **this file** against the tree deleted
            § 4's drifted second copy of the component contract, corrected § 3a's inverted
            allowlist analysis, ticked seven § 7 boxes whose work had landed in S4-5a, and re-pointed
            a dozen dead module paths. The hand-off doc is deleted. **See § 5n.**
- [ ] **S5 — Primitives** (§ 7.1): chips/pills, badges/overlays, headers/labels, dividers, skeletons.
- [ ] **S6 — Rows & sheet chrome** (§ 7.2).
- [ ] **S7 — `BookCard`** (§ 7.3). The main event, and the risk concentration point.
- [ ] **S8 — Screen states** (§ 7.4): empty states, callouts, banners, offline/error, top bars.
- [ ] **S9 — Statistics** (§ 7.5): stat tiles, charts, legends, progress indicators.
- [ ] **S10 — Shelf teardown.** The six `*Shelf.kt` files shrink to layout + composition.
- [ ] **S11 — Contract retrofit (R10 + R11).** The two rules written mid-migration, swept in one
      pass because they are fixed at the same call sites.

      **R10 — no UI model is built in composition.** Every model arrives on the `UiState` that feeds
      it. The call sites that predate the rule: `StatNumberUiModel` at profile's four stat sites,
      `MarkAsReadBurstUiModel` in book detail / reading / the motion debug screen, the file-constant
      `TopBarUiModel`s on the nine static bars, `ClickableTextUiModel` in onboarding and the roadmap,
      `ChipUiModel`'s `copy(selected = …)` against the Library filter sheet's composition-local draft,
      `LoadingSheetUiModel` in onboarding, and the `ChipUiModel`s built inline in the share cards
      (`share/ReadingUpdateShareCardBody.kt` since S4-6's split) and the Component Gallery screen.

      **R11 — a component takes only its model, its event lambda and a modifier.** Every loose render
      parameter becomes a model property, expressed as a surface-named variant plus a metrics table
      wherever the treatment genuinely differs per surface (`CoverDimensions.forVariant` is the
      pattern). The holdouts, all of them landed before the rule existed: `RichText` (`style`,
      `color`, `maxLines`, `overflow`, `onClick`, `onTextLayout`), `StatNumber` (`style`, `color`,
      `autoSize`, `maxLines`), `ClickableText` (`style`, `inlineContent`), `MarkAsReadBurst` (`color`,
      `secondaryColor`), `CoverlessTitleCover` (`title`), and `VerdictBlock` / `VerdictSheet`, whose
      loose parameters R11 condemns for the same reason R1 already does — so S11 closes that R1
      exception at the same time, and § 7.4a should end up naming **no** standing exceptions.

      **This stage runs before S12**, so the migration's final verification is verifying a library
      that actually satisfies its own contract. Two edges R10 names and deliberately leaves open are
      this stage's to settle: a component whose copy the **library** owns and resolves from its own
      `composeResources` (`offlineBannerUiModel()`, `offlineEmptyStateUiModel()`), since a resource
      read exists only in composition; and the **preview fixtures and the Component Gallery**, which
      construct models by definition. The third — a model depending on a value only composition has
      (book detail's scroll-derived `TopBarSurface`) — is the one to think hardest about, since R11
      also sends `TopBar`'s `scrollBehavior` looking for a home. Decide each, write the decision into
      the rule, and make the code match.

- [ ] **S12 — Close out.** Final gate values, `docs/reference/design-system/` rewrite, gallery
      completeness pass, trim the transitional component-path parenthetical in `CLAUDE.md`'s
      design-system maintenance rule down to `core/component/` (§ 5e), delete this file.

### 5b. S1 findings — two things the plan had wrong

**1. The G1 external ban cannot be a blanket group ban.**
`KmpLibraryConventionPlugin` injects `implementation(libs.library("koin-core"))` into **every** KMP
module's `commonMain` (`build-logic/src/main/kotlin/KmpLibraryConventionPlugin.kt`). A blanket
`io.insert-koin` ban would therefore fail on a dependency `:core:component` never declared. The gate
now skips a `conventionProvidedCoordinates` set (`koin-core`, `kotlinx-coroutines-core`) — mirroring
the set the dependency-analysis config already treats as uniformly provided — and still catches every
Koin artifact a module must *opt into* to do DI from UI (`koin-compose`, `koin-android`,
`koin-androidx-compose`). Verified against a deliberate violation: the gate rejected `:core:domain`,
`voyager-navigator`, and `koin-compose`, while correctly allowing `:core:designsystem` and ignoring
the injected `koin-core`.

**2. Scaffolded modules must declare ZERO project dependencies.**
The unused-dependency check is `severity("fail")` (the `dependencyAnalysis` block in the root build), so a
declared-but-unused `api(project(":core:designsystem"))` in an empty module breaks the dependency
health gate. Dependencies are therefore declared by the stage that first *uses* them —
`:core:component -> :core:designsystem` in S2, the `:core:presentation` edges in S3, the
`:core:uibinding` edges in S4. The G1 allowlist already names the permitted edges, so nothing is
blocked by their absence.

Also confirmed, contrary to a risk flagged before starting: applying `softcover.kmp.compose` to a
source-less module is safe. Every Compose artifact the convention plugin injects sits on the
unused-dependency exclude list, and all three modules pass `projectHealth` clean while empty.

### 5c. S1 review outcome

`rhaydus-kotlin:code-reviewer` reviewed the S1 diff. Verdict: minor changes, all applied below. It
found one thing that mattered.

**The G1 Koin exemption was a real hole, and the comment claiming otherwise was wrong.**
The original comment reasoned that "an unused koin-core on the classpath is inert." It is not.
`koin-core` alone is enough to implement `KoinComponent` or call `GlobalContext.get()` — full
service-locator DI, no `koin-compose` required — and because `checkModuleGraph` inspects *declared
coordinates* and has to skip that one, declaring it explicitly passes the gate. Verified by doing it:
`implementation(libs.koin.core)` added to `:core:component` produced zero violations.

A declared-coordinate gate is structurally blind to this, because the thing that matters is *usage*,
not declaration. So the hole is closed where it is visible — at the import:

```yaml
# config/detekt/detekt.yml
style:
  ForbiddenImport:
    active: true
    includes: ['**/core/component/**']
    imports:
      - 'org.koin.**'
      - 'cafe.adriel.voyager.**'
      - 'com.apollographql.apollo.**'
      - 'nl.rhaydus.softcover.core.domain.**'
```

The two gates are complementary: `checkModuleGraph` keeps the dependency graph honest,
`ForbiddenImport` keeps the source honest. **Both verified to fire and to be correctly scoped:** a
probe file importing `KoinComponent` in `:core:component` was rejected, while `:core:connectivity` and
`:core:designsystem` — which both import `org.koin.*` legitimately — pass clean. Scoped to
`:core:component` only, because `:core:uibinding` imports domain models by design.

Coverage gap worth knowing: the Gradle wiring runs detekt over commonMain + mobileMain + androidMain +
jvmMain, so an `iosMain`-only file is not scanned. Components live in commonMain, so this is
acceptable — but do not put a component in `iosMain` and assume the gate saw it.

Three smaller fixes, also applied: a comment recording that the banned-groups check deliberately
covers test configurations too (the sibling api-visibility check deliberately does not, and the
asymmetry read as an oversight); a refreshed `checkModuleGraph` task description, which had gone stale
two rules ago; and `docs/reference/module-structure.md` updated with the three new modules, since
`settings.gradle.kts` now includes them and that file's stated job is the concrete module roster.

### 5d. S2 outcome — the gesture spec, and three notes

**The easter-egg gesture is specified**, closing Appendix A's one open decision: **seven taps on
`VersionFooter`, each within two seconds of the last, a `milestone` haptic on the seventh**, then a
push of `ComponentGalleryScreen`. The counting lives in `SecretTapCounter`
(`feature/settings/.../presentation/util/`) as a plain class rather than Compose state, because the
timing edges — a tap exactly at the window boundary, a broken run restarting at 1 rather than 0 — are
worth unit tests and this repo has no Compose UI test harness. The footer is wrapped in
`noRippleClickable`: no ripple, no hand cursor, no press scale. It renders exactly as it did before.

**1. `previews` is an interface, not a naming convention.** R5 as prose ("every UI model ships
preview fixtures") is a review note. It is now `UiModelPreviews<M>` in `:core:component/gallery/`,
implemented by the model's `companion object`, so a missing fixture set is a compile error. The typed
`galleryEntry(...)` factory beside `GalleryEntry` takes that companion and erases `M` on the way in —
which is what lets one `ImmutableList<GalleryEntry>` hold every component's fixtures without a
generic wildcard at the registry.

**2. `kotlin.time.Instant`, not `kotlinx.datetime.Instant`.** The plan assumed the latter, since
kotlinx-datetime is already on the classpath. The repo has moved on: every `Instant` in
`core/domain`, `core/book`, `core/profile` and `core/personal` is `kotlin.time.Instant`, and the
kotlinx typealias is deprecated. `SecretTapCounter` uses the stdlib type and so needs no dependency
at all.

**3. `:core:component` declares exactly one dependency: `api(libs.kotlinx.collections.immutable)`.**
`api`, because `ImmutableList` is in the public API of `UiModelPreviews`, `GalleryEntry`, and every UI
model to come. It still declares **no project dependency** — nothing in the gallery scaffold needs a
token yet, and per § 5b finding 2 an unused declaration fails the health gate. The
`:core:component -> :core:designsystem` edge arrives with the first real component in S4.

**The gallery screen has no nav destination and no sidebar row.** § 5a called for "its nav
destination"; it does not need one. The push is within `feature:settings` itself (About → gallery), so
no `ScreenDestination` entry is involved, and a visible desktop `SettingsCategory` row would defeat
the easter egg. Desktop reaches it through the same footer gesture on the master–detail pane's About
category, via a `navigateToComponentGallery` callback threaded onto `SettingsScreenLayout` — unused in
the mobile `actual`, exactly as `navigateToAbout` / `navigateToRoadmap` are unused on desktop.

**Gates re-verified at this boundary:** `checkModuleGraph` (233 edges), `ktlintCheck`,
`detektJvmMain` + `detektAndroidMain` on both touched modules, and `projectHealth` on both are green;
`:feature:settings` compiles for JVM, Android **and** iOS.

### 5e. S3 outcome — five decisions, a correction, and a doc hazard to watch

**`:core:presentation` does not depend on `:core:designsystem`.** Nothing in the evicted set imports a
token, a component, or a theme value — checked before the move, not assumed. The two modules sit side
by side rather than stacking, which is why S3 could be a pure move with no adapter layer.

**The Koin module was renamed and moved whole, not split.** `DesignSystemModule.kt` ->
`core/presentation/di/PresentationModule.kt`, `designSystemModule` -> `presentationModule`, body
unchanged, symbol renamed at all 10 include sites. The alternative considered — leave a
`designSystemModule = module { includes(bookModule) }` behind, since `EditionImage` still
`koinInject`s `PersistEditionImageUseCase` — was rejected: that is a `:core:designsystem` -> `:core:book`
DI edge G2 forces us to delete in S4 anyway, and a Koin module is a non-component by S3's own
definition. `EditionImage` keeps resolving because `presentationModule` is in the aggregate graph;
`orchestration`'s Koin `verify()` test (`SoftcoverModulesVerificationTest`) passes unchanged, which is
the proof.

Two things made this risk-free, both verified rather than assumed: `bookModule` itself
`includes(dispatcherModule)` (`core/book/di/BookModule.kt:46`), and all 10 includers already
`includes(dispatcherModule)` directly. So no consumer's transitive graph shrank.

**`presentation/model/` split three ways, and only two thirds of it moved.** `BookInitialCover` (a nav
payload carried on `ScreenDestination.BookDetail`) and `LibraryTab` moved. `ProgressSheetTab`,
`ProgressSheetTabMapping` and `VerdictSheetContext` **stayed**, because `UpdateProgressBottomSheet` and
`VerdictSheet` consume them and do not move until S4 — moving them now would have forced a
`:core:designsystem` -> `:core:presentation` edge, inverting the direction the split exists to
establish. Their real homes are S4's call: the two pure enums to `:core:component`, the domain mapping
to `:core:uibinding`, each travelling with its component.

**`:core:book` stayed `implementation`, so S3 added ZERO allowlist rows.** § 3a anticipated needing
`":core:presentation" to ":core:book"` because `BookDetailPrefetcher`'s constructor takes
`FetchBookByIdUseCase`. It turned out not to: the constructor is only ever called by
`rememberBookDetailPrefetcher()` in the same file, so it is now `internal` — correct encapsulation on
its own merits — and `:core:presentation:projectHealth` accepts `implementation`. The pre-approval in
§ 3a stands unused; do not write the row speculatively. One row **was** needed elsewhere:
`:feature:book_detail` re-exports presentation types, so its edge is `api` — no allowlist entry, since
`:core:presentation` is an infra/contract module, not a data-area one.

**`:core:designsystem` lost `api(libs.voyager.tabNavigator)`.** `Tab` appeared only in `AppNavigator`,
which moved. The unused-dependency check is `severity("fail")`, so this had to land in the same change.
All five remaining tab users declare the artifact themselves. `voyager.navigator` stays — the three
androidMain debug screens still use it.

**S3's description said "TOAD wiring" moves. There was never any TOAD in `:core:designsystem`.**
`nl.rhaydus:toad` is declared only by the 9 feature modules; the module had no TOAD-shaped code and no
TOAD dependency. `docs/reference/module-structure.md` claimed otherwise and has been corrected. Nothing
was missed — there was nothing there.

**A documentation hazard S3 creates.** The design-system docs used `core/presentation/component` as
shorthand for "a shared component in core" (module name elided). Now that `core/presentation/` is a
real module that is *banned* from holding components, that path reads as a live instruction to do the
one thing the split forbids. The four occurrences in `docs/reference/design-system/` were rewritten to
`core/designsystem/presentation/component`. **`CLAUDE.md:32` carried the same shorthand and was
corrected too** (in a follow-up commit, since it is a project instruction file rather than a doc this
stage owns): it now names today's path, points at `:core:component` as the destination, and says
explicitly that `:core:presentation` is not a component home. **S12 should trim that parenthetical**
once the migration is done and the path is simply `core/component/`.

**Review outcome.** `rhaydus-kotlin:code-reviewer` verified the move independently (not by re-reading
the claims above) and found no mis-pointed import, mis-scoped model, wrong source set, or doc-sync gap.
Its one substantive note is now fixed: `EditionImage` stays in `:core:designsystem`, which has no
build-time edge to `:core:presentation`, so its `koinInject<PersistEditionImageUseCase>()` is satisfied
by a module it does not compile against — action at a distance that was written down only here and in
`PresentationModule.kt`. The call site now carries a comment naming `presentationModule`, the
verification test that guards it, and the fact that the seam disappears in S4.

**Gates at this boundary:** `checkModuleGraph` (259 edges), `ktlintCheck`, `projectHealth` on
`:core:presentation`, `:core:designsystem` and all 11 consumers, JVM compilation across every module
including `:desktopApp`, iOS (`iosSimulatorArm64`) on the touched KMP modules, type-resolved detekt
under JDK 21 on `:core:presentation` / `:core:designsystem` / `:orchestration`, and the full
`androidHostTest` suites of `:core:presentation`, `:orchestration`, `:feature:library` and
`:feature:book_detail` — all green.

### 5f. S4-1 outcome — the theme was domain-typed, and four other things the plan missed

**G2 was unreachable as written, and not for the reason § 2 gives.** § 2 indicts the *components* for
taking domain types. The **theme package itself** did too: `Theme.kt`, `PaletteColors.kt` and
`LocalThemeConfiguration.kt` — none of which ever leave the token module — imported `ColorPalette`,
`ThemeMode` and `ThemeConfiguration`. Independently, `ThemePreviewTile` and `ColorPalettePreviewTile`
are components, so R4 forbids them the domain enums, yet their whole job is selecting a palette's
hexes — and they cannot map, because the mapper lives in `:core:uibinding`, which depends *on*
`:core:designsystem`. Both pressures forced the same answer: **the design system owns a palette token
of its own.**

**`SpinePalette` is the token; `ColorPalette` is now a bare persisted identifier.** The token carries
the hex table, the palette's `label` and its `gloss`; the domain enum carries five names and
`DEFAULT`. `ColorPalette.toSpinePalette()` in `:core:uibinding` is the only bridge, and its `when` is
exhaustive, so a sixth palette that has no hex table is a compile error rather than a
`getValue`-missing-key crash at runtime. Moving the copy off the domain enum was a decision taken
before starting, not a side effect: `label`/`gloss` are user-facing strings, and they now sit beside
the hexes they describe.

**`SoftcoverTheme` takes `darkTheme: Boolean`, not `ThemeMode`.** Resolving `SYSTEM` needs the
reader's stored preference, so it belongs above the token layer — `ThemeMode.isDark()` is now in
`:core:presentation`. The parameter defaults to `isSystemInDarkTheme()`, which keeps a bare
`SoftcoverTheme { }` (the shape every `@Preview` uses) behaving exactly as before; the four
`SoftcoverTheme(themeMode = ThemeMode.LIGHT/DARK)` preview call sites became
`SoftcoverTheme(darkTheme = false/true)`.

**`ThemePreviewTile` takes a `ThemeTilePainting`, not a mode.** `LIGHT` / `DARK` / `SPLIT`, plus a
plain `label` — one enum rather than a `darkTheme` + `split` boolean pair, because "dark" and "split
down the diagonal" are mutually exclusive and a pair would let a caller ask for both. Deciding that a
`SYSTEM` choice reads as `SPLIT` is the Appearance screen's job, and its mapping stays feature-local
per R6 (one consumer). The enum sits beside the component and travels with it to `:core:component`
in S4-3, where it becomes a field of the tile's UI model.

**`:core:presentation` still does not depend on `:core:designsystem`.** § 5e's finding survives S4-1:
`LocalThemeConfiguration` holds a domain type and `isDark()` reads a Compose foundation API, so
neither needs a token. The two modules still sit side by side rather than stacking.

**The pre-existing `styleCheck` red is cleared.** The header's caveat named
`SoftcoverColorSchemeTest.kt:171`'s `UnusedParameter` as out of reach, since nothing in the migration
touched that file. S4-1 re-points that very file onto `SpinePalette`, so the on-touch compliance
policy now reaches it: `assertPairPasses`'s `scheme` parameter (passed nine times, read never) is
gone. `:core:designsystem:detektAndroidHostTest` is green.

**Review outcome.** `rhaydus-kotlin:code-reviewer` verified behavioural equivalence independently —
the new `darkTheme: Boolean = isSystemInDarkTheme()` default is exactly the old
`ThemeMode.DEFAULT` -> `SYSTEM` -> `isDark()` path, the palette->scheme maps are re-keyed and not
rebuilt, and `ComponentGalleryContent`'s override fallback is untouched. It confirmed the two-enum
split is the right call, that `ThemeTilePainting` is correctly component-adjacent, and that the
`ThemeMode` -> `ThemeTilePainting` reading correctly stays feature-local under R6.

**It also caught a claim in this stage's own KDoc that was false in one direction, and that matters
beyond the wording.** `SpinePalette.kt` said the mapper's exhaustive `when` makes forgetting a
palette a compile error. It does — *one way only*. The `when` is exhaustive over `ColorPalette`, its
receiver, so a new **preference** entry cannot compile until the mapper (and therefore the token) has
one too. A new **token** entry added first compiles happily and becomes a look nothing can ever
select, with no build signal at all. No `when` can see that direction, so the guarantee is now stated
honestly and backed by `SpinePaletteMapperTest`'s bijection assertion — the only thing that actually
catches it. Three smaller fixes also applied: `Theme.kt`'s KDoc named `[lightScheme]`/`[darkScheme]`,
neither of which has ever existed in that file; `Color.kt` and `LocalDarkTheme.kt` carried the theme
package's last two references to domain types (in KDoc, so invisible to both gates, and directly
against the boundary this sub-commit exists to draw); and a stray comma in `patterns.md`.

**Gates at this boundary:** `checkModuleGraph` (267 edges), `ktlintCheck`, `projectHealth` on
`:core:{domain, designsystem, uibinding, presentation}` + `:feature:{settings, profile}` +
`:orchestration`, `compileKotlinJvm` across **every** module plus `:desktopApp:compileKotlin`,
`compileKotlinIosSimulatorArm64` on all seven touched KMP modules, type-resolved detekt under JDK 21
(`detektJvmMain` / `detektAndroidMain` / `detektAndroidHostTest` as applicable on all seven), and the
`androidHostTest` suites of `:core:designsystem`, `:core:domain`, `:core:uibinding` and
`:feature:settings` — all green. `:core:uibinding` had no tests before this sub-commit; it now has
`SpinePaletteMapperTest` (3 tests, passing), whose bijection assertion is the drift guard the `when`
cannot be.

**One unrelated red, verified pre-existing.** `:core:preferences:testAndroidHostTest` fails 5 of 192 —
every one in `AndroidLegacySecureApiKeyStorageTest`, with
`java.security.KeyStoreException: NoSuchAlgorithmException` (no Android Keystore provider on the JVM
host). Reproduced 5/5 with the branch stashed. Nothing in S4-1 touches that module's sources.

### 5g. S4-2a outcome — the direction rule, and why S4's ordering was impossible

**This is the finding that re-cut S4.** `:core:component` -> `:core:designsystem` is the allowed
direction, so the reverse is a dependency *cycle*, not merely a smell. That gives one rule with
teeth:

> **A mover may depend on a stayer. A stayer may never depend on a mover.**
> A component can leave `:core:designsystem` only once nothing left in `:core:designsystem` uses it.

S4's original sub-commits ran the other way — rich text and the primitives (the shared *leaves*)
before the sheets and share cards that consume them. Every one of those sub-commits would have failed
at Gradle configuration time. The correct order is **consumer-first, leaves last**, which is the
inverse of how the plan was written.

**What the graph actually says.** Layering `:core:designsystem`'s ~50 moving files by "whose
consumers have already left" gives six layers, and the leaves — `ShareContent`, `ReviewMarkType`, the
`*ShareContent` models — sit in the *last* one. Two things make it worse than a pure reordering:

- **`internal` visibility.** The editor helpers (`ReviewMark`, `ReviewMarkType`, `ReviewEditorBuffer`
  and all of `ReviewRichText.kt`) are `internal`. A consumer that moves ahead of them cannot see them
  at all, so the choice is to widen a dozen symbols to public and move them a commit later, or move
  the family whole. Whole is right.
- **The closure is transitive.** Moving the rich-text cluster drags in `VerdictBlock` (renders
  `ReviewDocumentText`), `VerdictSheet` (uses every editor helper), `ShareCard` (its quote body renders
  `ReviewDocumentText`), and then `ShareCardDebugScreen` + `DebugRoutesSection` (they render
  `ShareCard`) — **11 files, 2,857 lines**, versus the ~800 the plan assumed.

**S4-2a exists to shrink that closure.** Relocating the three debug screens is a pure move with no
UI-model work, and it takes 381 lines out of S4-2b while doing three other things worth having:

**The debug screens now ship in no release binary.** They were in `:core:designsystem`'s `androidMain`
— a KMP Android library, which produces a **single variant** — so only their *binding* was
build-type-stripped; the screens themselves shipped to every user. `app/src/debug/` on
`debugImplementation` is the first time that is actually true. `:app` is the only module with build
types, and the tier rule (`tierOf` / `tierAllowances` in the root build) forbids `:app` -> `:feature:settings`, so the shell
is the only home available — the Settings surface that reveals them cannot host them.

**`:core:designsystem` dropped Voyager.** `voyager-navigator` was `api`-exposed for these three screens
alone; `voyager-koin` was already unused and only survived because it sits on the dependency-analysis
exclude list. Both are gone. `:app` declares `voyager-navigator` on `debugImplementation` itself rather
than leaning on `:core:presentation` to re-export it.

**Three `*ShareContent` models became public** — `QuoteShareContent`, `StatShareContent`,
`YearRecapShareContent`. They were `internal` because the only cross-file consumer was the debug screen
inside the same module. This is not churn: they are library models that S4-2b makes public anyway, and
a public `sealed interface ShareContent` whose members were internal could never be `when`-ed
exhaustively from outside the module in the first place.

**A gate that would have gone quietly blind, now closed and proved.** `:app`'s detekt tasks read
`src/main` only, so moving Compose out of `androidMain` into `app/src/debug/java` would have dropped
381 lines out of type-resolved detekt entirely. `detektDebug` / `detektRelease` now read `src/main`
plus that variant's own source set — each variant only its own, because `src/debug` and `src/release`
both declare `nl.rhaydus.softcover.di.debugRoutesModule` and one scope holding both hands type
resolution two conflicting declarations of one symbol. **Verified against a deliberate violation:** an
empty private function in `app/src/debug/.../DebugRoutesSection.kt` was rejected
(`EmptyFunctionBlock`, `UnusedPrivateMember`), and the file passes clean once reverted.

**`:app:projectHealth` is unreachable on this machine**, because it depends on
`:app:compileDebugJavaWithJavac` — the pre-existing `JdkImageTransform` failure in the header's
caveats. So `:app`'s new declarations were checked by hand instead: `designsystem-image` was in the
first draft and is **not** used (`nl.rhaydus.designsystem.share`, which the share-card debug screen
needs, is in `designsystem-**core**` — confirmed by listing the AAR's packages, not by guessing), so it
was removed. Both material3 coordinates are already on the dependency-analysis exclude list, so the
pinned `compose-material3-expressive` cannot be misreported. `:app:compileDebugKotlin` and
`:app:compileReleaseKotlin` both pass — only the *Java* step is broken, so the Kotlin half of `:app`
is genuinely verified.

**Review outcome.** `rhaydus-kotlin:code-reviewer` re-derived the dependency reasoning by hand rather
than trusting § 5g, and confirmed the `internal` -> public widening is required (not a leak — the
family was already public in the contract doc), that `implementation(project(":core:presentation"))`
is correctly not debug-only, and that the per-variant detekt split never puts the two
`debugRoutesModule` declarations in one scope. It also confirmed the "381 lines" figure is precise
rather than directional: `MotionDebugScreen` moved too, but was never in S4-2b's closure, so only
`ShareCardDebugScreen` (270) + `DebugRoutesSection` (111) come off it.

Three fixes applied. **One redundant dependency it caught that no gate could:**
`debugImplementation(libs.rhaydus.designsystemCore)` was pointless — `:app` already sees
`designsystem-core` in every variant through `:core:designsystem`'s `api` edge, and the
used-transitive check is `severity("ignore")` repo-wide, so even a reachable `:app:projectHealth`
would not have flagged it. Dropped, and the remaining three are now commented with why each is
needed. Also: four **brace-glomming** sites in `ShareCardDebugScreen.kt` (pre-existing, carried in by
the `git mv`, invisible to ktlint because the rule exempts trailing-lambda calls) unwrapped under the
on-touch policy; and the release `DebugRoutesModule`'s KDoc still said the tooling was "compiled into
the design system", which this sub-commit is precisely what stops being true.

**Gates at this boundary:** `checkModuleGraph` (270 edges), `ktlintCheck`, repo-wide
`compileKotlinJvm` + `:desktopApp:compileKotlin` + `:app:compileDebugKotlin` +
`:app:compileReleaseKotlin`, `compileKotlinIosSimulatorArm64` on the four touched KMP modules,
`projectHealth` on `:core:designsystem` / `:core:presentation` / `:orchestration` /
`:feature:settings`, and type-resolved detekt under JDK 21 including `:app:detektMain` (which fans out
to `detektDebug` + `detektRelease`) — all green. `:app:projectHealth` unreachable, as above.

### 5h. S4-2b outcome — the first real components in the library, and one deferral recorded

25 files, the first components to actually land in `:core:component`. What it settled:

**`RichTextUiModel` is named for what it holds, not where it came from.** The R4 conversion § 7.0
called for is a rename in shape but not in meaning: the library now owns `RichTextUiModel` /
`RichTextParagraph` / `RichTextRun` plus the editor's `RichTextMark` / `RichTextMarkType` /
`RichTextEditorBuffer`, all `ImmutableList`-typed per R3. Nothing about formatted prose is specific to
a review — the quote share card renders one with no review in sight — so `Review*` would have been the
wrong name to carry across the boundary.

**The mapper is a pair, and that is the interesting part.** Most UI models are write-once: a feature
maps domain -> UI and the component renders. The verdict sheet is an **editor**, so the edited model
must travel back out through `onSave` to be persisted. A one-way mapper would have forced the sheet to
keep emitting `ReviewDocument` — exactly the domain type R4 exists to keep out. Hence
`toRichTextUiModel()` **and** `toReviewDocument()` in `:core:uibinding`, promoted straight past R6's
usual feature-local start because they landed with two consumers already.

**`VerdictSheet`'s last domain type became a slot, not a model.** It took a `BookEdition` purely to
render `EditionImage`. Resolving a book's cover needs the reader's chosen edition, the book's default,
a fallback URL *and* a locally persisted file — none of which a component may know. So it takes
`cover: @Composable () -> Unit`, owns only the slot's width, and each caller passes the cover surface
it already uses. This is the § 7.1 escape hatch ("a component that needs a slot takes a trailing
`content`") used for its actual purpose, and it means the sheet needs **no rework** when `Cover` lands
in S4-4 — the caller swaps what it puts in the slot.

**A deferral, recorded as a decision rather than left as a silent gap.** `VerdictBlock`,
`VerdictSheet` and `RichTextFormattingToolbar` landed **R4-clean and R8-named, but not R1/R2-shaped**:
they still take loose parameters and, in the sheet's case, three separate callbacks rather than one
model plus one sealed event lambda. That is deliberate. What forced them into this sub-commit was the
direction rule, not their own readiness, and § 7.2 puts the verdict family's chrome consolidation in
S6 — restructuring them now would collide with that and mean doing it twice. The consequence to accept
knowingly: **they cannot be registered in the Component Gallery yet**, because `GalleryRegistry` is
keyed on a UI model's `previews` and they have no model. `GalleryFamily.VERDICT` exists (its package
does), holds no entry, and `GalleryRegistry.families` filters empty families out, so the gallery simply
does not show it until S6.

> **Updated by S4-3.** These three are now the library's **only** R1 holdouts, and the reasoning above
> does not generalise: what forced them here was the direction rule, whereas S4-3's two sheets were in
> their own scheduled slot and took the full contract. § 5j has the correction. S6 owes R1 to
> `VerdictBlock`, `VerdictSheet` and `RichTextFormattingToolbar`, and nothing else.

**The gallery renders its first two families.** `RICHTEXT` (5 fixtures) and `SHARE` (8, one per variant
plus the two anatomy outliers — a minimal book card and the in-progress reading update). The share
fixtures were **lifted from `ShareCard.kt`'s nine existing `@Preview` literals**, and those `@Preview`
functions now render *from* `ShareCardUiModel.previews` rather than re-declaring the same values — which
is R5's stated point: the preview set and the gallery set cannot drift because they are one list.

**A share card cannot be handed the gallery's width, and that is a lesson worth keeping.** It sets its
own width (`ShareCardDimensions`, 300–420dp) because it is an export artefact at fixed dimensions, so
on a phone it is wider than the fixture tile. The gallery pans it (`horizontalScroll`) rather than the
card learning about the gallery. No "gallery mode" parameter was added, and `component-contract.md`
§ 7.3 now records the rule: when a component genuinely owns its metrics, let the surrounding surface
adapt.

**Four visibility widenings, all of them the end state arriving on time.** `spoilerCover` (a
`:core:designsystem` colour role) went public because its renderer now lives in `:core:component`; the
editor helpers in `RichTextEditing.kt` went from `internal` to public because their consumer crossed a
module boundary. Neither is a leak — a public `sealed interface ShareCardUiModel` whose members were
`internal` could never have been `when`-ed exhaustively from outside the module anyway.

**The dependency gate earned its keep again.** `:core:designsystem:projectHealth` failed on
`implementation(libs.rhaydus.designsystemImage)` the moment `ShareCard` left — `RhaydusShimmerImage`
was its only user in that module. Dropped, and `:core:component` declares it instead, along with
`designsystem-editorial` (which `:core:designsystem` holds on `implementation`, so it is not
transitively visible).

**Gates at this boundary:** `checkModuleGraph` (287 edges), `ktlintCheck`, repo-wide `compileKotlinJvm`
+ `:desktopApp:compileKotlin` + both `:app` variants' Kotlin compilation,
`compileKotlinIosSimulatorArm64` on `:core:{component, uibinding, designsystem}` and
`:feature:{book_detail, reading, profile}`, `projectHealth` on all six touched modules, type-resolved
detekt under JDK 21 across the six plus `:app:detektMain`, and the `androidHostTest` suites of
`:core:{component, uibinding, designsystem}`, `:feature:{book_detail, reading, profile}` and
`:orchestration` — all green.

**Not verified:** no visual pass. `RichText`'s spoiler-reveal tap, the verdict sheet's cover slot, and
the share cards' capture path are all render-time behaviour that compilation cannot speak to, and this
repo has no Compose UI tests by decision (§ 6 "Test posture"). The gallery is now non-empty, so a
desktop run is finally worth something — it is the next thing to do, ahead of S4-3.

### 5i. The R9 correction — mapping had leaked into the render, and the contract had no rule against it

**Raised by the user against S4-2b, before it was committed.** The R4 conversions in S4-1 and S4-2b
each replaced a domain-typed component parameter with a UI model — and then called the mapper *at the
call site*, inside the composable. Fourteen sites across six files. That is a layering violation the
contract did not forbid, because nobody had written the rule down: R1–R8 govern a component's
*signature* and its *model*, and say nothing about where the mapping runs.

**`component-contract.md` now carries R9**, and `architecture.md` points at it: *mapping happens in
the ScreenModel; a composable never calls a mapper.* The `UiState` carries the result. Three reasons,
in the order they bite: it is a **layering** rule (deciding *what* to show does not belong in the code
that decides *how*), a **correctness** rule for the reverse direction (an editor's output has one
place to be mapped, not one per call site), and a **performance** rule — a mapper in composition
re-runs every recomposition and allocates a fresh model, which then breaks the equality check that
would have let Compose skip the component it was built for.

**The independent confirmation is worth recording.** `rhaydus-kotlin:code-reviewer`, reviewing the
same diff without knowing R9 was coming, flagged the identical call sites from the performance angle
alone — and found that in `VerdictSheet`'s case the mapped value was consumed by an **unkeyed**
`remember`, so only the first computed value was ever used and every recomputation after it was
provably wasted. `remember` at the call site would have hidden that; moving the call removed it.

**What moved.** `UiState` gained the mapped fields, populated where the source already lands:
`verdictReview` (book_detail, reading) in the actions/collectors that resolve the book;
`reviews: ImmutableList<BookReviewUiModel>` — a new feature-local presentation model — replacing the
domain list outright; `shareBookCard` / `shareUpdateCard` (book_detail) via a new `ShareCardsCollector`;
`readingLifeShareCard` (profile) combined across two collectors; and `themeChoices` / `paletteChoices` /
`paletteGloss` / the gallery's `paletteChipOptions` (settings) built in the theme collectors. Feature
mappers moved out of `presentation/component/` into `presentation/mapper/` and were renamed for their
R8 target types. The reverse direction went into the actions: `OnSaveVerdictAction` maps
`RichTextUiModel -> ReviewDocument` itself, in both features.

**A feature-local presentation model MAY hold a domain enum as its event payload.** R4 governs
`:core:component`, not a feature's own `presentation/model/`. So `PaletteChoice` carries both the
`SpinePalette` the tile renders and the `ColorPalette` its tap dispatches. R9 is about *where the
mapping runs*, and that is satisfied: the render neither maps nor labels.

**The carve-out, written into R9 so the rule is not read too widely.** `ThemeMode.isDark()` stays in
composition. "Follow the device" resolves through `isSystemInDarkTheme()`, which exists only in
composition — reading a *platform* signal is not mapping, and the same goes for `CompositionLocal`
reads and window size classes. Trying to move it would have been a regression dressed as compliance.

**One silent bug this correction introduced and the verification caught.** The agent refactoring
settings/orchestration stalled mid-task. It had declared `MainActivityViewModel._spinePalette` as a
second `MutableStateFlow` and never wired its update — which **compiles perfectly** and would have
pinned the app to the default palette forever while the preference read correctly everywhere else.
It is now *derived* from `themeState` with `map` + `stateIn`: two flows that must be updated in
lockstep are one forgotten `update` away from disagreeing, and `map` cannot forget. **The lesson for
the remaining stages: a state field added but never populated passes every gate this repo has.** After
an agent-assisted state change, grep that each new field is both declared and assigned.

**Also closed here, from the same review:** the `ShareCard` R3 violation (four collection fields came
over from `:core:designsystem` as plain `List`, where R3 did not apply, and nothing converted them —
`ShareCard` was recomposing needlessly for three variants); the **phantom test** (`RichTextMapper`'s
KDoc claimed `RichTextMapperTest` covered round-tripping and the tracker's "with tests" box was ticked
— neither was true; there are now 10 round-trip / fidelity / ordering tests, verified from the results
XML); the verdict cover slot's treatment, which the two callers had begun to duplicate asymmetrically,
centralised as `VerdictSheetCoverDefaults` in `:core:component/verdict/`; and a KDoc claiming
`RichTextUiModel` used to live in `:core:domain`, which it never did.

**Review outcome, and the two things it caught that were mine.** `rhaydus-kotlin:code-reviewer`
re-reviewed the combined S4-2b + R9 state and confirmed R9 is satisfied everywhere it traced —
including the two paths flagged as least-trusted: profile's two-collector combine is order-independent
(each collector reads the other half off current state inside the same synchronous `setState`), and
`BookReviewUiModel.id` is a stable key with nothing dropped from the domain model.

It found two errors of mine worth recording, because both are the *kind* that recurs:

- **A global `replace()` caught a second call site.** Centralising the verdict cover treatment into
  `VerdictSheetCoverDefaults` also rewrote the book page's unrelated **hero** jacket, which merely
  happened to share `16.dp`. That coupled the hero to a verdict-sheet constant, so tuning the sheet
  would silently move the hero with it. The hero has its own constant again, with a comment saying why
  it deliberately is not the shared one. **Scripted renames need their match count checked, not just
  their result compiled.**
- **An `api` edge with no justification, on a gate that cannot see it.** `:feature:book_detail`
  declared `api(project(":core:component"))` on the reasoning that `BookDetailUiState` exposes
  `VerdictSheetContext` publicly. `BookDetailUiState` is `internal`. The two features doing the
  identical integration used `implementation`. And `checkModuleGraph`'s api-visibility rule only covers
  `dataAreaModules`, which `:core:component` is not in — so this was a silent leak of exactly the shape
  that rule's own comment warns about. Now `implementation`.

Also fixed from that review: `canDelete` still read the domain `reviewDocument` rather than the
`state.verdictReview` this change consolidated; brace glomming in two reading actions; a stray
blank-line run.

**The test gap it found was the real one, and it is now closed.** The six reading action tests had
been patched only far enough to keep their mocks compiling — **zero** assertions on `verdictReview` —
and the two new `book_detail` collectors plus profile's combine had no tests at all. That is precisely
the gap the `MainActivityViewModel` bug above exploited. Added: `verdictReview` assertions across all
six reading actions, `VerdictReviewCollectorTest` (5) and `ShareCardsCollectorTest` (6 — covering
staggered input arrival and the spoiler-tag exclusion), `ReadingLifeCollectorTest` (5) and
`UserInformationCollectorTest` (6 — both arrival orders, single-half-null, recombination). No
production bug surfaced. Counts verified from the results XML, not from an agent's report.

**Explicitly NOT done.** `UiState` still holds domain models — `BookDetailUiState` alone carries
`Book`, `BookEdition`, `BookList`, `UserTag`, `DeadlineProgress`. R9 stops the *render* from mapping;
it does not stop the state from carrying domain types. That is S7 (`BookCard`) and S10 (shelf
teardown), and pulling it into S4 would mean re-planning the remaining stages.

### 5j. S4-3 outcome — the first R1 components, and a cost argument that was wrong

Two sheets, 1,987 lines, six new library types, two promoted mappers, four new collectors. What it
settled:

**They landed R1-shaped, and the reasoning that nearly deferred that was wrong.** The first plan for
S4-3 kept today's callback parameters and left R1/R2 to S6, on the stated ground that § 7.2 re-cuts
these sheets in S6 anyway, so the call sites would be rewritten twice. **That conflated two different
things.** S6's work is *chrome extraction* — pulling `SheetScaffold` / `SheetHeader` / `SheetRow` /
`SheetFooter` out of the sheets' internals. It does not touch their outer signature. An `onEvent`
surface survives S6 intact, so the double-work cost did not exist. The user pushed back on the
lambda soup, the cost was re-derived, and both sheets took the full contract instead:

```kotlin
fun UpdateProgressBottomSheet(model: ProgressSheetUiModel, onEvent: (ProgressSheetEvent) -> Unit, modifier: Modifier = Modifier)
fun ChooseListsBottomSheet(model: ChooseListsUiModel, onEvent: (ChooseListsEvent) -> Unit, modifier: Modifier = Modifier, jacket: @Composable (ChooseListsJacket, Modifier) -> Unit)
```

**The generalisable lesson: § 5h's deferral was not a precedent.** `VerdictSheet` and friends are
R1-less because the direction rule dragged them into S4-2b before they were ready. S4-3's sheets were
in their own scheduled slot. Same-shaped exception, entirely different justification — and reading the
first as licence for the second is how one recorded deferral becomes a house style. § 5h now says so,
and names the three components S6 still owes R1 to.

**R1 had no shipped instance before this.** Worth stating plainly, because the contract reads as
though it did: `ShareCard` satisfies R1 by being inert (no callbacks at all), `RichText` mirrors
`Text`'s own signature, and § 7.2's illustration is `BookCard`, which does not exist yet. The closest
thing was `RichTextFormattingToolbar`'s `onToggle: (RichTextMarkType) -> Unit` — the pattern in
miniature. So these two sheets are where the pattern gets exercised, on four call sites, *before* S7
bets `BookCard`'s 21 on it. `component-contract.md` § 7.2 now cites `ProgressSheetEvent` rather than a
hypothetical.

**Where a component's own screen state goes — decided.** The progress sheet's selected tab was a
second parameter beside the model, which § 7.1 forbids. It moved onto `ProgressSheetUiModel`. Its
*stored* home stays `UiState.selectedProgressSheetTab`; the model field is derived from it inside
`ProgressSheetCollector`, so there is exactly one writer and § 5i's "two fields one forgotten update
apart" hazard does not apply. **The cost, accepted knowingly:** a tab tap now travels state ->
collector -> state, so the segmented switcher updates on the second emission rather than the first.
Same shape `ShareCardsCollector` already has. If it ever reads laggy, the fix is for
`OnProgressTabClickAction` to write both the stored tab and the derived model in its one `setState` —
a second writer, and worth it only against a real symptom, not pre-emptively.

**The jacket slot, and why the treatment travels *into* it.** `ChooseListsBottomSheet` draws up to
three rotated covers through `EditionImage`, which takes a `BookEdition` — a domain type G1 forbids
the library from naming. So it became a slot, the same § 7.1 escape hatch as `VerdictSheet`'s `cover`.
The design choice worth recording is the *direction*: the sheet hands the caller a
`ChooseListsJacket` (index, corner radius, elevation, shadow) plus a `Modifier` already carrying the
52/56dp width and the −8°/+4°/0° rotations. It does **not** publish a `ChooseListsJacketDefaults`
object for the caller to read. That is § 5i's `VerdictSheetCoverDefaults` lesson applied before the
fact — two callers left to apply the same treatment themselves drift apart. Here neither caller has
anything to decide. When `Cover` lands in S4-4 the carrier is deleted and the slot becomes
`covers: ImmutableList<CoverImageUiModel>`.

**A sealed subject killed a boolean that was being re-derived six times.** `ChooseListsBottomSheet`
signalled "this is the bulk case" with `bookTitle == null`, immediately re-derived as
`val isBulk = bookTitle == null` and then threaded through six private composables, any of which could
be handed the wrong one. It is now `ChooseListsVariant.SingleBook | ManyBooks` — the branch is the
type. This is R2 doing what R2 is for, on a component nobody had thought of as having variants.

**Collectors, not actions — and the reason is § 5i, not taste.** `book` is written by
`UserBooksFlowCollector` plus eight actions in book_detail; `bookToUpdate` by five actions in reading.
A derived field patched into each write site is one forgotten `copy` from a stale sheet, and that bug
passes every gate this repo has. Four collectors derive the models off `scope.state` instead
(`ProgressSheetCollector` ×2, `ChooseListsCollector` ×2), each with a `*Snapshot` data class and
`distinctUntilChanged`, copying `ShareCardsCollector` exactly. `progressSheet` is now null precisely
when its source book is.

**R9 bought a real performance fix, not just a layering one.** `LibraryUiState.resolveSelectedBooks()`
scans every book in every collected shelf, and **both** Library layouts called it inside composition,
on every frame the bulk sheet was open, to resolve three cover images. It now runs once per state
change in `ChooseListsCollector`, which writes `chooseListsSheet` and `chooseListsJacketEditions` in
one `setState` so the model's jacket count and the editions the render indexes into can never describe
different selections. The collector also skips the whole per-list membership pass while
`selectedBookIds` is empty — outside selection mode there is no sheet to feed.

**Two resolution asymmetries preserved on purpose, and now locked by tests.** `toProgressSheetUiModel`
falls back to `defaultEdition.pages` for the page total but **not** to `defaultEdition.audioSeconds`
for the audio total, and it picks the medium off `currentEdition` alone — so a print edition shelved
against a book whose default is an audiobook still logs pages. Both look like bugs and are today's
behaviour; "tidying" either would silently change which tabs a book offers. The mapper's KDoc says so
and `ProgressSheetMapperTest` asserts both directions.

**One pre-existing dead KDoc link, surfaced by the move.** `SoftcoverIcon`'s KDoc pointed at
`nl.rhaydus.softcover.core.designsystem.presentation.model.RhaydusIconResource.Drawable`. That type
has always lived in the foundation (`nl.rhaydus.designsystem.icon`), so the reference was wrong from
the start — and invisible to both G1's gates for exactly the reason § 6a records: a fully-qualified
name in KDoc is neither an import nor a declared dependency. Deleting the `presentation/model/`
package made it unambiguously dangle. It now names the sibling `drawableIconResource` helper, which
is what a caller actually reaches for.

**Deferred, and recorded rather than left as a gap:** neither sheet is in the Component Gallery. A
fixture tile cannot render a modal inline, and a "tap to open" fixture is a gallery *feature* that
wants designing once for all eighteen sheets in S6, not invented twice against a sample of two. Both
models ship compile-checked `previews` regardless (R5), and the components' own `@Preview`s render
*from* that list — including two cases the pre-migration sheet never previewed at all: the audiobook
time layout and the no-page-count layout.

**Gates at this boundary:** `checkModuleGraph` (291 edges), `ktlintCheck`, repo-wide `compileKotlinJvm`
+ `:desktopApp:compileKotlin` + both `:app` variants' Kotlin compilation,
`compileKotlinIosSimulatorArm64` on `:core:{component, uibinding, designsystem, domain}` and
`:feature:{book_detail, reading, library}`, `projectHealth` on all seven touched modules, and
type-resolved detekt under JDK 21 (`styleCheck`, whole repo) — all green. **1,085 unit tests across
the five touched modules, 0 failures**, counts read off the results XML rather than an agent's report:
48 new (12 `ProgressSheetMapperTest`, 20 `ChooseListsMapperTest`, 16 across the four collector tests)
plus the 9-test `ProgressSheetTabMappingTest` carried into `:core:uibinding`. `:core:component` needed no
new external declaration: `kotlinx-datetime` reaches it through `:core:designsystem`'s `api` edge.
`feature:library` gained `:core:component` and `:core:uibinding`, both on `implementation` — § 5i's
`api` mistake not repeated.

**Review outcome, and the one real regression it caught.** `rhaydus-kotlin:code-reviewer` ran three
independent passes and cross-checked each of eight specific claims against the pre-migration file via
`git show HEAD:`. It confirmed the `selectedTab` single-writer claim, the `when`-exhaustiveness of the
tab dispatch, byte-identical behaviour in all three tab bodies, both mapper asymmetries, and
byte-identical caption/pill strings. No blockers. It found one genuine regression and two things worth
fixing:

- **A coverless bulk jacket was being tilted.** `ChooseListsMapper` clamped the header's cover count
  to a *minimum of one*, on the reasoning that the header shows a placeholder rather than a gap. But
  `StackedJackets` then rotated that lone placeholder by −8°, where the pre-migration code
  special-cased `covers.isEmpty()` and drew it upright. So a bulk selection none of whose books
  resolve a cover would have rendered a single tilted jacket, which reads as a rendering fault rather
  than as a stack. The field is now `coverCount`, honestly clamped to **0..3**, and the component
  branches on zero. **The lesson is about the clamp, not the rotation:** `coerceIn(1, 3)` looked like
  defensive hygiene and was actually the bug — it erased the distinction the render needed. A clamp
  that narrows a domain is a decision, not a safety net.
- **`ChooseListsSubject` became `ChooseListsVariant`.** R8 names the variant type `*Variant`, and this
  is the type the header's `when` dispatches on. "Subject" was the more descriptive name and the wrong
  one — R8 exists so that this is not a judgement call per component.
- **An action imported a composable for a KDoc link.** `OnBulkToggleListMembershipAction` imported
  `ChooseListsBottomSheet` purely to make `[ChooseListsBottomSheet]` resolve — a presentation-action →
  UI edge that existed only for a doc link. Pre-existing, carried in by the path rename, now a plain
  code-span reference with the import gone.

**One efficiency caveat left in place deliberately, and documented rather than papered over.**
`resolveSelectedBooks()` sits inside the collector's `map`, so it runs per state emission, *ahead* of
`distinctUntilChanged` — while selection mode is active an unrelated change (a search keystroke, a
sort) still re-runs the scan even though the snapshot is about to be deduped. Moving it behind the
dedupe means either carrying `booksByTab`/`bookByBookId` into the snapshot (whose own structural
comparison is the cost being avoided) or reimplementing the lookup in the collector, away from the
state it belongs to. Per-frame was the problem worth fixing; per-emission-during-a-transient-mode is
not. `ChooseListsSnapshot`'s KDoc now says exactly that, because the first version of it overclaimed.

**Partly verified by a desktop launch, and it is worth being precise about what that proved.** The
app was started (`:desktopApp:run`) and reached seven GraphQL requests with **no Koin failure**, which
does establish that the DI graph resolves and that all four new collectors are bound — a missing
`bind …Collector::class` would have thrown at ScreenModel creation, and that is the one silent-wiring
failure mode this stage could plausibly have introduced. It proves nothing about pixels: the run could
not get past the network layer on this machine (a JDK cert-path failure reaching the API, unrelated to
this branch and reproducible on `main`).

**So still not verified:** the tab switcher's response to a tap (the one thing the derived
`selectedTab` could plausibly regress), the jacket slot in both its single and three-jacket forms, and
the audiobook time layout are all render-time behaviour compilation cannot speak to.

### 5k. S4-4 outcome — the `:core:book` edge is dead, and R9 finally reached the lists

The last component out of `:core:designsystem` that mattered. What it settled:

**G3 landed here, not in S4-6, and the tracker was wrong to schedule it later.** § 6 said G3 waits
for S4-6. It cannot: `onUnusedDependencies` is `severity("fail")`, so the moment `EditionImage.kt`
is deleted, `api(project(":core:book"))` and `api(libs.coil3)` are unused and the build is red until
they go — and the allowlist row goes with them. S4-4's own checklist line ("Kills the `:core:book`
edge") was the correct one. **`:core:designsystem` now has exactly one project dependency left**
(`:core:domain`, for the `Deadline*` trio), so G2 genuinely is S4-6's.

**`CoverVariant` is R2's per-variant metrics table at scale, and the taxonomy *is* the audit.**
`EditionImage` took `elevation` / `cornerRadius` / `shadowColor` / `maxDecodePx` as loose parameters;
§ 7.1 admits no parameter beside the model, so they had to move. Auditing all 29 call sites turned up
**14 distinct metric tuples**, expressed as surface-named entries resolved through
`CoverDimensions.forVariant`. Every value is reproduced exactly — this commit changes no pixels.
Several entries share a tuple and are **kept separate anyway**, because the point of the table is
that tuning one surface cannot move another.

**It surfaced real drift, which was recorded rather than fixed.** `HiddenSeriesStack` uses a 3dp
corner radius where every other flat cover uses 4dp, and Reading's three thumbnails sit at 6/8/10dp.
Collapsing those is a design decision for `docs/reference/design-system/`, with a deliberate choice
of value — not something to smuggle into a structural move. The table makes the drift readable in one
file for the first time.

**The Koin seam § 5e complained about is closed.** `EditionImage` reached
`PersistEditionImageUseCase` through the aggregate Koin graph from a module it did not compile
against — action at a distance written down only in a KDoc. It is now a `CoverImagePersister` fun
interface behind `LocalCoverImagePersister`, provided by `:core:presentation`'s
`ProvideCoverImagePersister` and mounted once in `App.kt`. The `null` default also deleted the
`LocalInspectionMode` guard the old code needed to keep previews working.

**Resolution and rendering split cleanly, and the awkward consumer proved the split was right.**
`ReadingSessionService` is an Android foreground service with no composition at all, and it needs the
same cover the UI shows for its notification. Because the ladder lives in `:core:uibinding` as a plain
`resolveCoverSource(...)`, it kept working with a one-line unwrap. Had the resolution followed the
component into `:core:component`, that consumer would have had nowhere to go.

**`rememberCoverImageRequest` is public, and that was not the first plan.** Two surfaces cannot route
through `Cover`: the full-screen viewer fills the screen with a zoom/pan `graphicsLayer`, and the
Reading hero's backdrop uses `ContentScale.Crop` + `blur(64.dp)` with no shimmer and no coverless
rung. Forcing either through the component would have changed pixels. Exposing the request builder
costs `:core:component` an `api(libs.coil3)` and keeps both surfaces honest.

**`FullScreenCoverScreen` stopped carrying domain objects.** It held two `@TransientNavArg
BookEdition?`s and leaked `coil3.request.ImageRequest` through its own `expect`/`actual`. It now
carries one `CoverUiModel` — a small immutable value, so no `TransientNavArg` — and `:feature:book_detail`
no longer imports Coil anywhere.

**The choose-lists jacket slot is gone, and the covers went on the *variant*, not the model.** § 5j
said the slot would become `covers: ImmutableList<CoverUiModel>` on `ChooseListsUiModel`. That would
have been R2-wrong: a `SingleBook` holding three covers is a state the type should not admit. It is
`SingleBook(name, cover)` / `ManyBooks(bookCount, covers)`, and **`coverCount` is deleted rather than
kept alongside** — `covers.size` is the count, and two fields one forgotten update apart is the § 5i
hazard on the exact field whose clamp the S4-3 review already caught once. The empty-stack
placeholder is built by the component from library data, so `covers` stays the single source of truth.

**A behaviour change to know about: covers now land one state emission after their books.** R9 moves
the mapping into collectors, so a list renders its items before their cover models exist. Three
different answers to that got written concurrently — a blank placeholder, skipping the item, and
drawing nothing — and **skipping was the dangerous one**: the whole shelf or rail renders empty for a
frame and then pops in, a visible layout jump, and library's collector hops to `defaultDispatcher` so
its lag is certain rather than theoretical.

**The first fix was itself the wrong shape, and the user caught it.** Unifying the three answers on
"blank placeholder" left *four features each holding a private `CoverOrPlaceholder`* plus seven inline
`if (cover != null)` guards — ten copies of one rule, inside the migration whose entire purpose is to
delete duplication like that. **`Cover(model: CoverUiModel?)` now takes the nullable model and owns
the placeholder itself**, so all ten sites collapsed to a plain `Cover(model = covers[id], …)`. The
subtlety worth keeping: the null branch must apply the same 2:3 aspect the loaded branch does, or the
placeholder takes the caller's width and no height and collapses the layout it exists to hold open.
If the lag ever reads badly, the fix is for the source writer to set both fields in one `setState` —
the same escape hatch § 5j recorded for the progress sheet's tab.

**Two pre-existing oddities preserved on purpose.** Reading's verdict sheet passes the *same* edition
as both `edition` and `defaultEdition`, disabling the fallback rung every other Reading cover uses;
book_detail's verdict cover does the same. Both are today's behaviour, both are now commented at the
mapping site. "Tidying" either would silently change which cover a verdict sheet shows.

**One design wrinkle the plan missed.** The hero's shared-element key needs `bookId` and
`transitionSurface`, which were screen constructor arguments — invisible to a collector. Stamping the
key in composition would allocate a fresh model per recomposition, which is exactly what R9's third
reason forbids, so both are now seeded into `BookDetailUiState` from the ScreenModel the same way
`initialCover` already was.

**Gates at this boundary:** `checkModuleGraph`, `ktlintCheck`, repo-wide `compileKotlinJvm` +
`:desktopApp:compileKotlin` + both `:app` variants, **repo-wide `compileKotlinIosSimulatorArm64`**
(the gate that matters most here — `:core:uibinding` gained its first platform source sets), and
`projectHealth` on all ten touched modules. Tests: `ChooseListsMapperTest` reworked onto `covers`
(19), `ChooseListsCollectorTest` re-pointed (4), the new `CoverSourceResolverTest` (8 — **the
resolution ladder had never had a single test before this**), and `MonogramCoverMetricsTest` extended
from 6 to 15 to cover `showFullTitle` / `titleFontSize` / `titleMaxLines`.

**Not verified:** no visual pass. The cover is the most-rendered component in the app and the gallery
now has a `COVER` family, so a desktop run is worth more here than at any previous boundary.

### 5l. S4-5a outcome — the library's copy, its own resources, and the rule R10 came out of

The first of S4-5's two commits: **13 files out of `:core:designsystem`, every component in them
either already domain- and DI-free or made so here**, leaving only the `Deadline*` trio and the `Unreleased*` pair
for S4-5b. What it settled:

**Every mover took the full contract, and that was the user's call rather than the cheap one.** Three
options were put up: a gate-clean move keeping today's signatures (smallest diff, ~15 new entries in
`component-contract.md` § 7.4a), a hybrid, or the full contract now with each model **named for the
family it will become** — `ChipUiModel`, `TopBarUiModel`, `BadgeUiModel`, `StatNumberUiModel`,
`BannerUiModel`, `EmptyStateUiModel` — so S5–S9 extend them with variants rather than replace them.
The last was chosen. The consequence worth stating: **§ 7.4a still lists exactly three exceptions**,
the same three S4-2b left there, and the gallery went from three families to ten in one commit.

**A text component was given the `Text`-shaped parameters after its model, and that was the wrong
call — corrected, by the user, into R11.** The plan called for a surface-named `StatNumberVariant`
metrics table on the `CoverVariant` precedent. I dropped it, reasoning that `RichText` — shipped in
S4-2b — already takes `model` first and then `style` / `color` / `maxLines` / `overflow`, so a table
over five editorial type styles would be typography masquerading as anatomy. `StatNumber` and
`ClickableText` followed `RichText` instead. **That read an existing violation as a precedent**: a
loose render parameter is drift with a default value, invisible to the model, untestable, and free to
diverge between two surfaces that should match — the exact failure `CoverVariant` was created to end.
`component-contract.md` now carries **R11** (a component takes only its model, its event lambda and a
modifier), the plan's variant table is the right answer after all, and `RichText`, `StatNumber`,
`ClickableText`, `MarkAsReadBurst` and `CoverlessTitleCover` are named holdouts in the S11 retrofit.
The lesson generalises past this family: **the library is young enough that "X already does it" is as
likely to be an unswept violation as a pattern** — check the rule, not the neighbour.

**`StatNumberFormat` is the real R3 lesson here, and it is a different one from R3's usual.**
`AnimatedStatNumber` took `formatter: (Int) -> String`. R3 is normally about collections, but a
**lambda field is worse than an unstable list**: it allocates fresh on every recomposition and is never
equal to itself, so it defeats the skipping the model exists to enable. The replacement is a
descriptor — `Grouped` / `Plain` / `Decimal(digits)` — resolved inside the component through the
foundation's own `formatGroupedNumber` / `formatDecimalNumber`. Three entries because three is what
the five call sites actually asked for. The Int/Float overload pair collapsed into one `Double`, since
the tween always ran on a float anyway and the format is what decides whether a fraction is ever seen.

**`TopBarSurface` collapsed three parallel derivations of one flag into one field.** The book page
derived a container colour (`animateColorAsState` to `Transparent`), a back-button `IconButtonColors`
pair, and an overflow-menu colour pair — all three from `shouldBeExpanded`, all three at the call site,
any one of which could have been forgotten or updated alone. `TopBarUiModel.surface` is now
`OPAQUE | OVER_MEDIA` and the component derives all three. The trailing `actions` slot **receives the
resolved `IconButtonColors`**, so the feature's own overflow menu — which owns its own dropdown state
and therefore cannot be a model entry — takes the same treatment without re-deriving it.

**Two dead symbols were deleted rather than migrated.** `SoftcoverLoadingDialog` was `internal` with
no call site anywhere, including inside its own module; `SoftcoverTopBarAction` and the `actions:
List<SoftcoverTopBarAction>` parameter it fed had **no call site either** — every consumer used the
`additionalActions` slot. `titleAlignment` and `colors` were likewise unused at all eleven call sites
and did not cross. Carrying dead API across a module boundary is how a library starts its life with
debt.

**`:core:component` owns its own copy now, and the module's whole string table moved.**
`:core:designsystem`'s `composeResources/values/strings.xml` held **four strings, all connectivity**,
owned entirely by the two components leaving. Three moved; `connectivity_offline_action_blocked` had
**no reader anywhere** — `publicResClass = false` means only its own module could read it, and nothing
did — so it was deleted rather than carried. The library's `compose.resources` block mirrors the
design system's, `publicResClass` included. The principle written into the doc: **copy that belongs to
a component rather than to a feature lives in the library**, because every surface reporting "you're
offline" reports it identically, and `offlineBannerUiModel()` / `offlineEmptyStateUiModel()` resolve
it. A resource read is a composition-scoped platform read, so it sits in R9's existing carve-out.

**The connectivity pair split cleanly across the two modules.** `rememberIsOnline()` — the half that
resolves a `NetworkAvailabilityProvider` out of Koin — moved to `:core:presentation`
(`connectivity/`), and the render halves became `Banner` and `EmptyState` taking the resolved flag.
`RootScreen` had been resolving the provider itself *and* rendering a component that resolved it
again; it now reads the state once. `:core:presentation` gained `api(libs.rhaydus.corePlatform)` — the
gate caught that `implementation` was wrong, because the injection seam names the provider type in its
own signature.

**`:core:designsystem` lost Koin and `core-platform` here, and nothing else could have made it.**
`onUnusedDependencies` is `severity("fail")`, so both dropped in the same commit that emptied them —
the § 5k pattern repeating. `api(project(":core:domain"))` and `kotlinx-datetime` survive into S4-5b,
which is where G2 lands.

**`:feature:onboarding` and `:feature:scan` got an explicit `:core:component` edge they did not
strictly need.** Both compile without one, because `:core:presentation` `api`-exposes the library
(§ 6a's row, added in S4-4 for `ActiveSession`). Leaning on that would have made two features' use of
the component library invisible in their own build files and dependent on an edge that exists for an
unrelated reason. Both declare it, both on `implementation`, and `projectHealth` is green on each.

**One deliberate deviation from the collector precedent, and it is the interesting one.** Every other
UI model in this commit is derived off `scope.state` in a collector. The **Library filter sheet's chips
cannot be**: the sheet holds a composition-local `LibraryFilters` draft and commits only on "Show N
titles", so which chip reads selected depends on state that deliberately never reaches `LibraryUiState`
— that draft/commit behaviour is the documented pattern (§ 3.5 in the design system), not an oversight.
The collector therefore writes the chips *without* `selected`, plus a `filterValueByChipKey` lookup,
and the render combines the two. Recorded here because it looks like a gap in the R9 story and is not.

**R10 was written because of this commit, and deliberately not applied in it.** Converting thirteen
components surfaced the same shape in five features: the model reaches the contract-shaped signature,
and then the *call site* builds it — `StatNumberUiModel(...)` inline, `remember { TopBarUiModel(...) }`,
a file-level constant model, `ClickableTextUiModel(text = ...)` at the point of use. That satisfies
R1–R9 and still puts the "what does this component show" decision back inside the render, which is
what the model was introduced to prevent. **R10 now says a UI model is never built in composition; it
arrives on the `UiState`.** The user's instruction was to document it and schedule it rather than widen
this commit, so it is normative for new work, `docs/reference/architecture.md` points at it, and the
new **S11 — contract retrofit** stage sweeps the existing call sites *before* the migration's final
verification. **R11 joined it in the same review round** (§ 7.1 / R11), from the same user reading of
this commit. The three edges R10 leaves open — library-owned copy resolved from `composeResources`,
the gallery's own fixtures, and a model depending on a value only composition has (book detail's
scroll-derived top-bar surface) — are S11's to settle, not this commit's to pre-decide.

**Gallery.** Eight new entries and one new family (`CELEBRATION`) — `TopBar`, `SearchTopBar`, `Chip`,
`StatNumber`, `ThemePreviewTile`, `ColorPalettePreviewTile`, `ClickableText`, `MarkAsReadBurst`,
`Banner`, `EmptyState`. Two carry the same caveat S4-3's sheets do: `MarkAsReadBurst` is an animation a
still fixture cannot show, and `LoadingSheet` is a modal, so neither reads honestly inline. `EmptyState`
fills whatever it is given, so the fixture tile lends it a height rather than the component learning
about the gallery (§ 7.3's rule, applied in the other direction from `ShareCard`).

**The `:core:component` -> `:core:designsystem` edge became `api`, and the gate is right that it
should.** The Appearance picker tiles' models name a token in their own public surface
(`ThemePreviewTileUiModel.palette: SpinePalette`), so a consumer holding one needs the type.
`projectHealth` caught it. Worth recording because it looks like the kind of `api` edge § 6a exists to
stop, and is not: the root build deliberately keeps `:core:designsystem` out of `apiSignOffModules`
precisely because it is a leaf once G2 lands, so re-exporting it republishes nothing a consumer could
not already reach. The library's *own* surface stays sign-off-gated.

**Gates at this boundary:** `checkModuleGraph`, `ktlintCheck`, repo-wide `compileKotlinJvm` +
`:desktopApp:compileKotlin` + both `:app` variants' Kotlin compilation, **repo-wide
`compileKotlinIosSimulatorArm64`**, `projectHealth` on all thirteen touched modules, and type-resolved
detekt under JDK 21 (`styleCheck`, whole repo) — all green. **3,346 unit tests, 5 failures**, counts
read off the results XML: all five are the pre-existing `AndroidLegacySecureApiKeyStorageTest` cases in
the untouched `:core:preferences` (no Android Keystore on the JVM host), reproduced on `main` and
recorded in this file's header. 46 tests are new — `SearchTopBarCollectorTest` (7),
`ThemeChoiceMapperTest` + `PaletteChoiceMapperTest` (11, on two mappers that had **no** coverage at all
before this commit changed their signatures), `LibraryFilterChipsBuilderTest` +
`FilterChipModelsCollectorTest` (15), and `TagChipModelsCollectorTest` +
`TagEditorChipModelsCollectorTest` (13).

**Not verified: no visual pass.** The top bar's scrim over the book-detail hero, the Explore search
chrome's focus contract, the two Appearance picker rows and the offline banner are all render-time
behaviour compilation cannot speak to, and this repo has no Compose UI tests by decision (§ 6 "Test
posture"). The gallery now carries ten families, so a desktop run is worth more than at any previous
boundary.

**Review outcome, and the one real regression it caught.** `rhaydus-kotlin:code-reviewer` re-derived
every moved component against `git show HEAD:<old-path>` rather than trusting this section, and ran
two independent passes that converged on the same defect. It confirmed the hard parts — `TopBar`'s
main-path colour and scrim derivation, `SearchTopBar`'s focus contract line-for-line, `Chip`'s
read-only branch (no `onClick` argument at all, not `enabled = false`), the filter sheet's
draft/commit split and its ownership/format gating, all five collectors' fields actually written and
Koin-registered, and all three deleted-as-dead claims.

**The regression: the book page's offline placeholder lost its opaque bar.** One `TopBarUiModel` was
built from `shouldBeExpanded` and reused on both branches — but the offline branch returns before the
`LazyColumn` that owns `lazyListState` ever composes, so `shouldBeExpanded` is pinned to `false`
there, resolving to `OVER_MEDIA`: a transparent bar with scrimmed white controls over a plain empty
state. Pre-migration that branch passed no `colors` at all and therefore always got the opaque
default. **The lesson is about the collapse itself:** folding three call-site derivations into one
model field is right, and it makes the model's *inputs* the thing to check — a value derived from
scroll state is meaningless on a branch that never scrolls. The offline branch now builds its own
`TopBarUiModel`, with a comment saying why. Three smaller fixes also applied: two stale doc entries
(`ClickableText`'s path in `patterns.md`, a `badge/` family in `module-structure.md` that S4-5b has
not created yet), 24 `) }` trailing-lambda glomming sites across the four new test files and one
production line this commit had touched, and a comment on `FilterChipModelsSnapshot`'s cross-tab merge
recording that it is safe only because chip keys are facet-namespaced.

### 5m. S4-5b outcome — G2, and the three dependencies that had to leave together

The last components out of `:core:designsystem`. **The module is now tokens only — six directories
(`icon`, `illustration`, `layout`, `modifier`, `theme`, `transition`) and zero project
dependencies.** `presentation/component/` does not exist any more. What it settled:

**G2 landed here rather than in S4-6, for the third time in this stage's history.** § 6 scheduled it
for S4-6; § 5k had already recorded the same forcing function catching G3 out. `onUnusedDependencies`
is `severity("fail")`, so the moment the five files were deleted the module's whole dependency block
was unused and the build was red until it emptied. What the plan missed is that it was **three**
dependencies, not two: `api(project(":core:domain"))` and `kotlinx-datetime` were expected, but
`core-common` went too — `secondsToHm` (the pace line) and `currentLocalDate` (the release-date
formatters) were its only users in the module, and both left with the components. That one was a
transitive trap rather than a clean drop: `:core:component` and `:core:uibinding` both call into
`nl.rhaydus.common` and neither declared the coordinate, inheriting it through the `api` edge being
removed. Both declare it now. The same shape had already caught `kotlinx-datetime` one step earlier —
`UpdateProgressBottomSheet` has used it since S4-3 without declaring it, invisibly, because
`onUsedTransitiveDependencies` is `severity("ignore")`. **An `api` edge is load-bearing for consumers
you never listed; deleting one means auditing its users, not just its owner.**

**Both halves of G2 were verified by deliberately reintroducing a violation, and the first attempt at
verifying the source half proved nothing.** Adding a `core.domain` import to `Color.kt` failed at
*compile*, not at detekt — with the project dependency gone, the import cannot resolve at all. That
is a stronger failure but it does not exercise the rule, and a rule that never fires reads exactly
like one that passes (§ 6a). The honest test is the scenario the source gate actually exists for: a
stray import that still resolves because something else on the compile classpath supplies the type.
Restoring `implementation(project(":core:domain"))` alongside the import produced the real finding —
`ForbiddenImport` on `Color.kt:3`. The dependency half was verified the same way and reports
`:core:designsystem → :core:domain (tokens only — this module must declare NO project dependency at
all)`.

**detekt 1.23.8 has no multiple-rule-instance support, so the two scopes share one rule.**
`ForbiddenImport`'s `includes` is now `['**/core/component/**', '**/core/designsystem/**']` against a
single import list. That makes the ban a **superset** for the design system — Koin, Voyager and
Apollo imports are forbidden there too, where G2 only asked for domain. That is correct rather than
incidental, and is written into the config comment: a token module has no more business with DI,
navigation or the network client than the component library does.

**The hand-off's `DeadlineSummaryUiModel(dateText, paceText, foreground: Color?)` was wrong, and R10
is why.** The default ink is `MaterialTheme.colorScheme.onSurfaceVariant` — a theme lookup that only
resolves *in composition*. A model built in a collector therefore cannot hold it, so the nullable
`Color` could never have been populated for the common case. It is a two-entry
`DeadlineSummaryTone` (`OnSurface` / `OnHeroBackdrop`) resolved inside the component, which is also
what R2 wants — a named variant rather than a flat nullable beside the model. **Worth generalising:
when a loose render parameter resists becoming a model field, check whether the value is
composition-scoped before reaching for a nullable. If it is, the field was never the answer; a
variant is.**

**The `Deadline*` compute was in composition in two features, and the hand-off's "R9 wiring" line
undersold it.** Only book detail had a collector; `LibraryShelf.kt:870` and
`ReadingShelf.kt:1548`'s `Book.deadlineProgressFrom(state)` both ran `DeadlineProgress.compute(...)`
during layout, the second one from four call sites across two layout actuals. So this commit moved
the *domain computation* off the composition as well as the mapping — six collectors in total
(`DeadlineModelsCollector` in library and reading, `UnreleasedBadgeModelsCollector` in explore,
`UnreleasedBadgeCollector` in book detail, plus writes folded into the existing `BookDeadlineCollector`
and `RoadmapDocumentCollector`). `deadlineProgressByBook` stays a **domain-typed** field on library
and reading state on purpose: Library's own countdown badge and the expired-cover grayscale branch
read `status`/`daysRemaining`, and neither is a `:core:component` component until S5.

**One map, not five — the cover precedent does not generalise.** `CoverModelsCollector` keeps five
separate maps because a cover carries a per-rail `sharedTransitionKey` surface, so the same book
yields a different model per rail. A badge carries no such thing, so explore's `unreleasedBadges` is
one id-keyed map across all four source lists and five would have been five ways to disagree. **The
question to ask of a new model map is whether the model varies by surface, not whether the previous
one did.**

**Two call sites the hand-off's "verified" table missed**, both consumers of the moving date
formatters rather than of the components: `ExploreShelf.kt:243` and `RoadmapContent.kt:180`. The
first was a **hand-rolled duplicate** of `UnreleasedBadge` — same `Surface` + `Text` anatomy, same
colours, different copy ("Arriving …") and an `8dp`/`4dp` pad against everything else's `6dp`/`2dp`.
It is now `Badge` with `BadgeVariant.FeaturedRelease`, reproducing the pad exactly, and the
`UnreleasedBadgeStyle` enum gained a third entry to resolve its copy. Two entries in a metrics table
from a sample of one deviation is thin, but it is the § 5k rule applied honestly: the drift is now
readable in one file instead of invisible in two. The second was using `formatLongRelease()` to
render **"Last updated …"**, which is not a release date at all — so the formatters moved as
`formatCompactDate` / `formatLongDate`, named for the shape they render, with the release copy living
in the mapper where it belongs.

**Gates at this boundary:** `checkModuleGraph` (303 edges), `ktlintCheck`, repo-wide `compileKotlinJvm`
+ `:desktopApp:compileKotlin` + both `:app` variants, repo-wide `compileKotlinIosSimulatorArm64`,
`projectHealth` on all eight touched modules, and type-resolved detekt under JDK 21 (`styleCheck`,
whole repo) — all green. One deprecation warning was introduced and fixed rather than left: the
roadmap collector reached for `kotlinx.datetime.Instant`, where the code it replaced used
`kotlin.time.Instant`.

**Tests: 3,519 completed, 5 failed** — the same five pre-existing
`AndroidLegacySecureApiKeyStorageTest` cases in the untouched `:core:preferences` (no Android Keystore
on the JVM host) this file's header already records. **56 are new**: `DateFormatsTest` (3),
`UnreleasedMapperTest` (3) and `DeadlineMapperTest` (16) in `:core:uibinding`; `DeadlineModelsCollectorTest`
in library (6) and reading (8); `UnreleasedBadgeModelsCollectorTest` (11); `UnreleasedBadgeCollectorTest`
(4); plus extensions to `BookDeadlineCollectorTest` (3) and `RoadmapDocumentCollectorTest` (2).

> **The recorded baseline of "3,346 tests" is wrong, and future stages should not compare against
> it.** 3,519 − 56 is 3,463, not 3,346. Counts here are read off the results XML after
> `./gradlew testAndroidHostTest`, and **without `--continue` that run halts at `:core:preferences`** —
> the module whose five failures are permanent on this machine — so every module Gradle had not yet
> reached contributes nothing to the total. The S4-5a figure was almost certainly taken from such a
> halted run. Counts recorded from here on use `--continue`, and the number that actually matters is
> the failure list, not the total: five, all pre-existing, unchanged.

**The device run found a latent S4-5a crash, and it reads as a correction to § 5l.** That section
records that `:core:component` "owns its own copy now" and that the library's `compose.resources` block
"mirrors the design system's, `publicResClass` included". It mirrored everything except the line that
makes the resources exist on Android: `:core:designsystem`'s `androidLibrary` block carries
`androidResources.enable = true` and the library's did not. CMP resources ship as Android *assets* and the
KMP Android library plugin keeps those off by default, so the module generated its `Res` accessor,
compiled clean, and packaged **nothing** — `MissingResourceException` on the first read. The first reader
is `offlineBannerUiModel()`, so the app crashed on launch on a device with no network, three commits after
the omission landed. S4-5a's own "not verified: no visual pass" is exactly why it survived that long.

Two consequences beyond the one-line fix. **A sweep of every resource-owning module found a second
instance**: `:feature:settings`' bundled `ROADMAP.md` fallback had never worked on Android at all — it is
read only before the first live fetch lands, so a warm cache never touches it, and it would have surfaced
on a fresh install after a release. **And it is now a gate**, `checkResourcePackaging`, wired into `check`
and verified by flipping a real module's flag: a module declaring `packageOfResClass` with
`androidResources.enable` off fails the build. Documented in `module-structure.md` § Build wiring
conventions and in `CLAUDE.md`, and filed for the foundation as F28, since the trap is generic to any
nl.rhaydus app that gives a module its own `composeResources`. **The lesson for the stages still to come:
a module gaining its own resources is a build-configuration change, not only a source change, and
compilation cannot speak to whether it worked.**

**Visual pass: partial.** Every change here is a re-point that should be pixel-identical, and the one
place that claim is load-bearing is Explore's featured hero, where a hand-rolled badge became a shared
component at a reproduced pad. **That one is confirmed on-device** — the hero renders "Arriving Dec 8"
through `Badge` at the `FeaturedRelease` pad — along with the Reading screen, during the crash
investigation above. The rest is still unseen: no deadline-carrying book was on screen, so the badge /
cover-overlay / summary-line trio has not been watched rendering from its new models. The gallery now
carries a `BADGE` family with three entries, which is the cheapest way to close that.

### 5n. S4-6 outcome — a stage that kept shrinking, and what "pure file surgery" still costs

S4-6 closed S4. It was a third of the size this file advertised for it, and the reason is worth
writing down because it happened three times in a row.

**The stage kept being overtaken by its own gates.** § 5 scheduled S4-6 as "zero project deps, G2,
G3, the doc rewrite". By the time it ran, all four were done. `onUnusedDependencies` is
`severity("fail")`, so a commit that deletes the last user of a dependency *cannot* leave the
dependency declared — the build is red until it goes. S4-4 deleted `EditionImage.kt` and was forced
to drop `:core:book` (G3); S4-5b deleted the `Deadline*` trio and was forced to empty the block
entirely (G2). § 6a even predicted the opposite in prose — it said G2 "lands in S4-6" — and was
wrong for the same reason twice over. **The generalisable rule: a gate that asserts the absence of
something cannot be scheduled independently of the commit that removes the last of it.** Where a
gate and a deletion describe the same fact, the deletion sets the date. Plan the gate's *wording*
early; do not plan its *commit*.

The doc sweep went the other way, and this is the finding worth carrying forward. S4-5b's close-out
had already brought the *reference* docs current — `components.md` and `module-structure.md` needed
nothing — so the "doc rewrite" looked like four lines of stale scheduling prose plus one future-tense
comment in the root build. An audit of **this file** against the tree found far more, because nothing
had ever checked it:

- **§ 4 carried a second copy of the component contract.** It never gained R9, R10 or R11; its R3
  claimed `kotlinx-collections-immutable` still had to be added to the version catalog, which S1 had
  done and which this file's own § 5 bullet says twenty lines below it; its R5 predated
  `UiModelPreviews<T>`; and its § 4.3 worked example — the part billed as *"read it before writing any
  new component"* — pointed at a directory deleted in S4-2b, named two types renamed in that same
  commit, and listed two "gaps to close" that were both closed. **Deleted, not re-synced**, and § 4
  now says why.
- **§ 3a's cost analysis was inverted.** It said the migration's `api` edges needed "no allowlist row
  at all". Two rows exist, and the rule they answer to was renamed `apiSignOffModules` and *widened to
  include the migration's own three modules* by § 6a's audit. The prediction was about a rule the
  migration then changed.
- **Seven § 7 boxes were unticked for work that had landed**, mostly in S4-5a: `PillChip`→`Chip`,
  `SoftcoverTopBar`→`TopBar`, `AnimatedStatNumber`→`StatNumber`, `ConnectivityBanner`→`Banner`,
  `OfflineScreenContent`→`EmptyState`, `SoftcoverLoadingSheet`→`LoadingSheet`, and
  `SoftcoverTopBarAction`, which was deleted rather than migrated and had no box for that outcome.
  Two more (`ConcealableTagChip`, `Cover`) were half-done and are now split so the remainder is
  visible.
- **Roughly a dozen open items pointed at `core/designsystem/presentation/component/`**, a directory
  that no longer exists. § 7's preamble licenses *line* drift; it does not license a dead module path,
  which sends a reader looking for a file rather than a line.
- **§ 7.6 still excluded `EditionImage`'s platform bodies as out of scope.** They went into
  `:core:uibinding` in S4-4. That is the *second* "it stays put" exclusion in that section to be
  wrong, after the debug screens.

**The pattern behind all five: a working tracker rots in exactly the places nothing reads back.**
Checkboxes get ticked by whoever does the work; prose written once at the top, and checklist entries
for stages still years away, are read by nobody until someone needs them — which is the moment they
mislead. Two cheap habits fall out of it, and both are now this file's practice: **cite symbols, not
line numbers** (every `build.gradle.kts:NNN` in here was wrong, so they are gone), and **never keep a
second copy of a normative rule** — `CLAUDE.md`'s Roadmap section already retired four planning docs
over that exact failure, and § 4 was the same bug wearing a contract.

**So what actually landed was the `ShareCard.kt` split.** 1,061 lines became eight files — the
§ 2 baseline's 1,161 had already lost about a hundred to S4-2b's rename, and the baseline figures in
§ 2, § 7.0 and Appendix A are left alone on purpose, because a baseline that gets edited forward
stops being one. The dispatch and `ShareCardSignOff` stayed in `ShareCard.kt` (~100 lines), each of
the six card bodies took its own file with its private parts and its `@Preview`s, and `TABULAR_NUMS`
took a `ShareCardNumerals.kt` of its own. Every declaration moved byte-for-byte; the split was
verified by sorting the old file's non-import lines against the new files' and diffing, which left
exactly three intended differences and nothing else.

Review then found a fourth, which the diff could not: a dead
`@OptIn(ExperimentalLayoutApi::class)` on `ReadingLifeShareCardBody`, whose only remaining reference
was the annotation's own import. It has been dead since before the split — the body uses no
`FlowRow` — and it survived precisely because a sorted-line diff proves *equality*, not *necessity*.
Removed, and it is the one change here that is not byte-for-byte.

**"Pure file surgery" is not visibility-neutral, and that is the one thing to expect next time.** A
top-level `private` in Kotlin is *file*-scoped, so the moment `ShareCard`'s dispatch `when` calls a
body across a file boundary, that body cannot stay private. The six bodies are now `internal` —
still invisible outside `:core:component`, but no longer invisible outside their own file. Nothing
else widened: every file-local helper (`buildBookStatsLine`, `ReadingUpdateReaderIdentity`, the seven
`ReadingLife*` parts, the fixture `val`s, all nine previews) stayed `private` in its new home. Worth
knowing before the next split is proposed as cost-free: **splitting a file always widens something,
and the question is only how much.** Here the answer was six symbols and no public surface, which is
why it was still the right call.

**Two things left standing on purpose, so neither reads as an oversight:**

- **`ShareCard`'s two parallel colour `when`s.** The dispatcher resolves `surfaceColor` and
  `contentColor` with two separate six-arm `when (content)` blocks — precisely the shape R2 says to
  replace with a per-variant lookup, and `ShareCardDimensions.forContent` is sitting in the same
  package as the pattern to copy. Folding them into a `ShareCardPalette` is a *type* change, and this
  commit was scoped as file surgery; mixing the two would have made the diff unreadable against the
  original, which is the same reason S4-2b did not split the file in the first place. It is a
  candidate for S11, which is already opening these call sites.
- **`TABULAR_NUMS` is now the family's fourth copy.** Splitting the file forced the constant out of
  `ShareCard.kt`, and doing so surfaced that `"tnum"` already exists as three identical `private
  const`s (`EditorialTypography.kt`, `statistic/StatNumber.kt`, and here) plus roughly fourteen inline
  literals across six feature modules. The honest fix is one token in `:core:designsystem`, ~17 call
  sites, and a `foundations.md` bullet — a real cross-cutting change that does not belong inside a
  file move. Recorded here rather than done, which is the same call § 5k made about `CoverVariant`'s
  3dp/4dp drift.

**One box was stale rather than open.** § 7.0's `ReviewCard` entry had never been ticked, but the
work landed in S4-2b: it takes `BookReviewUiModel`, whose `body` is a `RichTextUiModel`, so no domain
type reaches it. The component itself stays in `feature:book_detail` — it never lived in
`:core:designsystem`, so only the R4 conversion was ever in S4's scope. Checking an unticked box
against the code before believing it is cheaper than the alternative, and this file has now been
wrong in both directions.

**Not attempted, and named so it is not mistaken for an omission:** `progress/UpdateProgressBottomSheet.kt`
is 1,200 lines — larger than `ShareCard.kt` was — and is on no checklist. It is one component with
one public symbol, so it breaks no rule; but if the per-body split was worth doing here, that file is
the next place to ask the question. S10 or S12, not S5.

**Gates:** `checkModuleGraph` (303 edges), `ktlintCheck`, repo-wide `compileKotlinJvm` +
`:desktopApp:compileKotlin` + both `:app` variants, repo-wide `compileKotlinIosSimulatorArm64`,
`:core:component:projectHealth`, and whole-repo `styleCheck` under JDK 21 — all green.

**Tests: 3,519 completed, 0 failed — and the standing "5 pre-existing failures" caveat is retired.**
§ 5m recorded five `AndroidLegacySecureApiKeyStorageTest` failures in the untouched
`:core:preferences` as an unavoidable baseline ("no Android Keystore on the JVM host"). They do not
reproduce: run under the same JDK 21 that detekt already needs, all five pass and the suite is
wholly green. They were a JDK 26 artefact, like the two toolchain problems in this file's header,
not a property of the tests. Two practical consequences: **use
`JAVA_HOME=…/jbr-21.0.11/… ./gradlew testAndroidHostTest --continue`**, and do not compare a future
run against "3,519 / 5". Note also that the plain `test` lifecycle task reaches none of this — it
runs `:app` and `:desktopApp` only, and on JDK 26 it dies in `:app:compileDebugJavaWithJavac` before
reaching even those; `testAndroidHostTest` is the task that runs the 850 KMP suites.

**A note on the import sets, since the method generalises.** Neither ktlint nor the Kotlin compiler
reports an unused import in this build, so a split file can silently carry imports it does not need
and nothing complains. Hand-auditing them by grep does not work either: `height = dimensions.height`
is a named argument, not the `Modifier.height` extension, and the two are indistinguishable by
pattern. What does work is deleting every import and letting the compiler name what it cannot
resolve, round by round until it converges — each round exposes the next layer, because an
unresolved receiver hides its own extensions. Five rounds here, and the resulting set is minimal by
construction: every import in these files was demanded by the compiler.

### 5a. The Component Gallery — decided: shipped easter egg

Not debug-only. Consequences to build for, rather than discover late:

- **It must be `commonMain`.** `MotionDebugScreen` / `ShareCardDebugScreen` / `DebugRoutesSection`
  are Android-only (and, as of S4-2a, live in `app/src/debug/` — § 5g). A shipped gallery has to
  render on iOS and desktop too.
- **Split across two modules.** Registry (`GalleryRegistry`, `GalleryEntry` — every component paired
  with its `previews` fixtures) in `:core:component/gallery/`. Screen (`ComponentGalleryScreen`, its
  TOAD wiring, its nav destination) in `feature:settings`, because G1 bans `:core:component` from
  Voyager and screens belong to features.
- **Trigger:** seven taps on `VersionFooter`
  (`feature/settings/presentation/screen/SettingsShelf.kt:1303`), which already renders on the About
  screen — each within two seconds of the last, `milestone` haptic on the seventh. **Specified in S2;
  see § 5d.**
- **Preview fixtures ship in the release binary.** They are data classes and strings, so the size
  cost is small — but it is no longer zero, and every component added later adds to it. Worth a
  measurement at S12, not a blocker.
- **It is now a user-visible surface**, so it needs a design pass and a
  `docs/reference/design-system/` entry. G5 applies to it like any other screen.

**Why the contract precedes the migrations:** S5 is where the convention gets stress-tested on cheap
components, and S7 is where a stability or shared-element regression costs a visible frame drop
rather than a compile error. Doing S7 before S5–S6 would mean settling the model conventions on the
hardest family.

---

## 6. Gates

Convention does not hold a boundary — `:core:designsystem` is the proof, and the root build's
module-graph comment already said so before this migration started. Every rule below is a build failure.

- [x] **G1 — `:core:component` ban list.** DONE in S1. `checkModuleGraph` now carries
      `componentLibraryAllowedProjects`, `componentLibraryBannedGroups`, and
      `conventionProvidedCoordinates` (§ 5b finding 1 explains why the last one exists), with both
      checks in the task body. 231 edges validated; rejection verified against a deliberate
      violation. **Paired with a scoped detekt `ForbiddenImport` rule** that closes the
      usage-level hole this check cannot see — see § 5c. Shape as landed:

      ```kotlin
      val componentLibraryAllowedProjects = mapOf(
          ":core:component" to setOf(":core:designsystem"),
      )
      val componentLibraryBannedGroups = setOf(
          "io.insert-koin", "cafe.adriel.voyager", "com.apollographql.apollo",
      )
      ```
- [x] **G2 — `:core:designsystem` has zero project dependencies.** DONE in **S4-5b**, not S4-6 —
      the same forcing function as G3 (`onUnusedDependencies` is `severity("fail")`, so the edges had
      to go in the commit that emptied them). Asserted in `checkModuleGraph` alongside
      `bannedReverseEdges` and the `apiSignOffModules` check (§ 6a), via a
      `zeroProjectDependencyModules` set. **Both halves landed together, and both were verified by
      deliberately reintroducing a violation** (§ 6a's standing rule):
      - [x] the **dependency** gate — `checkModuleGraph` fails if `:core:designsystem` declares any
            `project(...)` dependency at all
      - [x] the **source** gate — a detekt `ForbiddenImport` on
            `nl.rhaydus.softcover.core.domain.**` scoped to `**/core/designsystem/**`, pairing with
            G2 exactly as § 5c pairs one with G1. **Not belt-and-braces: the two catch different
            things, verified the hard way.** S4-1 left fully-qualified
            `nl.rhaydus.softcover.core.domain.model.*` references in `Color.kt` and
            `LocalDarkTheme.kt`'s KDoc, and *both* gates were blind to them — they are neither
            imports nor declared dependencies. A reviewer caught them, not a gate. The import-level
            rule at least closes the case where a stray `import` survives a dependency removal
            (possible while another module on the compile classpath still `api`-exposes the type).
- [x] **G3 — DONE in S4-4. The `:core:designsystem` -> `:core:book` api allowlist row is gone** from
      `allowedApiDataEdges` — note the row list has since grown two rows and the set that drives it
      was renamed `apiSignOffModules`; see § 6a.
- [ ] **G4 — Composable budget ratchet.** A `checkComponentBudget` task counting `@Composable`
      declarations outside `:core:component`, excluding only (a) functions whose name ends in
      `Preview` and (b) platform `expect`/`actual` composables (`BarcodeScanner`). Ceiling set to
      the post-migration measured value; a rise fails the build. Wire into `check`.
- [ ] **G5 — Doc rule.** `docs/reference/design-system/` updated. Already enforced by
      `rhaydus-kotlin:code-reviewer`, which treats a design-system change without a doc update as a
      blocker. `components.md` was 83KB when this was written and is **96KB** after S4 — it grows
      with every family the library absorbs, so it will need splitting per family, mirroring the
      § 3 package layout.
- [ ] **G6 — `./gradlew check` green**, including `styleCheck` (type-resolved detekt across every
      module) and `ktlintCheck`.

### 6a. Gate audit — what the restructure outgrew, and what is still pending

Prompted by the `api(project(":core:component"))` mistake in § 5i slipping past every gate. The
question worth asking was not "is `checkModuleGraph` broken" — it does exactly what it says — but
"has the restructure outgrown what it was told to check". It had, in two places. Both are now closed
and **both were verified by deliberately reintroducing the violation and watching the build fail**,
because an ungated rule reads exactly like a passing one.

**1. The api-visibility rule was scoped to data modules only.** Its own rationale — an `api` edge
republishes the target's whole surface downstream, "exactly how `:core:designsystem` became a
god-module" — applies verbatim to a module whose entire surface is a component library. `dataAreaModules`
is now `apiSignOffModules`, adding `:core:{component, presentation, uibinding}`. Widening it surfaced
exactly two edges, both deliberate and both now allowlisted with their reason: `:core:uibinding ->
api(:core:component)` (§ 3a — seeing both sides of a mapping *is* the module) and
`:feature:book_detail -> api(:core:presentation)` (§ 5e). `:core:domain` and `:core:designsystem` are
deliberately left out: domain is a dependency-free contract module (§ 3a settled that it may
`api`-expose freely, and gating it would mean allowlisting ~15 legitimate edges), and designsystem is a
leaf now that G2 has landed.

**2. The direction rule (§ 5g) had no gate at all** — the rule that re-cut this entire stage. A
reverse edge inside the UI stack is usually a Gradle *cycle*, so it did fail, but with a task-graph
trace that says nothing about why it is wrong. `bannedReverseEdges` now names them and points at § 5g.
It also catches the non-cyclic case: `:core:designsystem -> :core:presentation` is not a cycle and is
still forbidden, since establishing that those two sit side by side rather than stacking was the whole
point of S3.

**Pending, and already scheduled — not blind spots:** G4 (`checkComponentBudget`) is S12's, since
the count is still falling. **Neither G2 nor G3 waited for S4-6, and neither could have:**
`onUnusedDependencies` is `severity("fail")`, so the commit that empties a dependency block is
forced to drop it. Deleting `EditionImage.kt` made `api(project(":core:book"))` and `api(libs.coil3)`
unused in S4-4, taking the allowlist row with them (G3); deleting the `Deadline*` trio emptied the
rest in S4-5b (G2). Both were required for their stage to be green rather than optional cleanup.
This paragraph said the opposite until S4-6 corrected it — the audit predicted a schedule the build
would not permit, which is § 5n's point.

**New follow-up this audit surfaced**, and it is now tracked as work rather than prose: G2 is a
**pair** of gates, not one — the dependency assertion plus a source-level `ForbiddenImport` scoped to
`**/core/designsystem/**`. Both halves are checkboxes under G2 above, with the reason they are not
redundant.

**Known and accepted gaps, recorded so they are decisions:**

- **R9 has no mechanical gate.** A detekt rule on mapper calls inside a `@Composable` was offered and
  declined in favour of the contract doc plus review; the pattern is awkward to express precisely and
  risks false positives. Worth knowing that it is the one contract rule with zero enforcement, and
  that a violation of it did reach the tree silently before a human caught it.
- **detekt does not scan `iosMain`** (no type resolution for native targets — § 5c). `:core:component`
  has no `iosMain` at all today, so the `ForbiddenImport` gap is theoretical; it stops being
  theoretical the first time a component needs a platform actual.
- **`:app:projectHealth` is unreachable on this machine** (the pre-existing `JdkImageTransform`
  failure), so `:app`'s dependency declarations are ungated locally — which matters more since S4-2a
  gave it a `debugImplementation` block. CI on a working toolchain would cover it.
- **A `UiState` field declared but never populated passes every gate here** (§ 5i). No gate is
  proposed; the mitigation is the unit tests added in S4-2b and the habit of grepping that each new
  field is assigned, not just declared.

### Test posture — a decision, not an omission

There are **no Compose UI tests anywhere in the repo today**; only the
`androidx-compose-ui-test-manifest` artifact is wired (`AndroidComposeConventionPlugin.kt:60`), and
nothing uses `createComposeRule`. Existing "component" tests (`ReviewRichTextTest`,
`MonogramCoverMetricsTest`) are pure-JVM logic tests on JUnit5 + kotest.

So for this migration:

- **Automated coverage** = unit tests on every UI model and every mapper (pure Kotlin, no Compose).
  Per `CLAUDE.md`, these are written by the `unit-test-writer` agent — never in the main
  conversation — with tightly scoped briefs and narrow `--tests` filters.
- **Visual acceptance** = the Component Gallery, rendering every `previews` fixture across both
  themes and every palette.
- **Introducing Compose UI tests is explicitly out of scope.** Recorded here so it is a decision
  rather than a silent gap. Revisit after the library exists.

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

> **DONE in S4-6.** S4-2b moved `share/` and did the R8 rename and the R4 fix but left
> `ShareCard.kt` one file, because splitting it satisfied no gate and would have made an already-large
> diff harder to read against the original. S4-6 split it into eight files, with every declaration
> carried across byte-for-byte. The one thing it could not keep was visibility — see § 5n.
>
> The heading's 1,161 is the § 2 baseline figure and stays that way, per this section's preamble.
> The file was **1,061** lines when S4-6 opened it: S4-2b's `*ShareContent` -> `*ShareCardUiModel`
> rename and R4 fix had already taken about a hundred lines out of it.

The dispatch and the `*ShareContent` types are already correct (`component-contract.md` § 7.3) — this is a rename plus a
mechanical split plus one real piece of work (rich text, below).

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

This is the only place in the migration where R4 is a design problem rather than a rename. Do it
early in S4 — `QuoteShareCardBody` and `VerdictSheet` both block on it.

- [x] `RichTextUiModel` / `RichTextRun` / `RichTextMark` in `:core:component`
- [x] `ReviewDocument` -> `RichTextUiModel` mapper in `:core:uibinding`, with tests
- [x] `ReviewRichText` — `core/designsystem/presentation/component/ReviewRichText.kt`
- [x] `ReviewDocumentText` — `core/designsystem/presentation/component/ReviewDocumentText.kt`
- [x] `ReviewMark` / `ReviewMarkType` — `core/designsystem/presentation/component/ReviewMark.kt`, `ReviewMarkType.kt`
- [x] `ReviewEditorBuffer` — `core/designsystem/presentation/component/ReviewEditorBuffer.kt`
- [x] `VerdictBlock`, `VerdictScoreAndCaption` — `core/designsystem/presentation/component/VerdictBlock.kt`
- [x] `ReviewCard` — `feature/book_detail/presentation/screen/BookDetailShelf.kt`. Done in S4-2b and the box was simply never ticked: it takes `BookReviewUiModel`, whose `body` is a `RichTextUiModel`, so no domain type reaches it. The *component* stays feature-local — it never lived in `:core:designsystem`, so only the R4 conversion was ever in this stage's scope; pulling the card itself into the library is S6's row work
- [x] Existing `ReviewRichTextTest` re-pointed at the new model (it currently asserts on `ReviewDocument`)

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
- [ ] `AddPill`, `TagChip`, `TagChipName` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:510,621,709`
- [ ] `TrackingNowChip` — `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:437`
- [ ] `RecentSearchChip`, `SortChip`, `FlowRowMoodChips` — `feature/explore/presentation/screen/ExploreShelf.kt:1260,1426,1405`
- [ ] `SetProgressChip` — `feature/reading/presentation/screen/ReadingShelf.kt:1136`
- [ ] `UpdatePillButton` — `feature/settings/presentation/screen/SettingsShelf.kt:1273`
- [ ] `SortLabelControl` — `feature/library/presentation/component/LibraryControlLine.kt:107`

#### Headers & labels — 17 -> `SectionHeader` + `PageMasthead` + `SidebarLabel`

Kills all three cross-module name collisions.

- [ ] `SectionLabel` × 4 — `app/src/debug/.../MotionDebugScreen.kt`, `feature/book_detail/presentation/screen/BookDetailShelf.kt:2535`, `feature/profile/presentation/screen/ProfileShelf.kt:136`, `feature/reading/presentation/screen/ReadingShelf.kt:1178`
- [ ] `EditorialHeader` × 2 — `core/component/progress/UpdateProgressBottomSheet.kt` (moved in S4-3; now takes a `title: String`), `feature/reading/presentation/screen/ReadingScreenLayout.mobile.kt:158`
- [ ] `SidebarSectionLabel` × 2 — `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:416`, `feature/settings/presentation/screen/SettingsScreenLayout.jvm.kt:260`
- [ ] `SmallSectionLabel`, `InlineAccentLabel` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:1128,1100`
- [ ] `SectionIntro` — `feature/profile/presentation/screen/ProfileShelf.kt:402`
- [ ] `SectionHeaderBar` — `feature/explore/presentation/screen/ExploreScreenLayout.jvm.kt:557`
- [ ] `AlsoReadingSectionHeader` — `feature/reading/presentation/screen/ReadingShelf.kt:1205`
- [ ] `SearchResultsHeader`, `HiddenSuggestionsGroupHeader` — `feature/explore/presentation/screen/ExploreShelf.kt:1472`, `feature/explore/presentation/screen/HiddenSuggestionsShelf.kt:177`
- [ ] `LibraryTabsGroupHeader`, `RowLabel`, `SettingsPageHeader`, `SidebarHeader`, `DesktopPaneHeader` — `feature/settings/presentation/screen/SettingsShelf.kt:625,968`, `SettingsScreenLayout.mobile.kt:222`, `SettingsScreenLayout.jvm.kt:241,583`
- [ ] `MastheadHeader` — `feature/library/presentation/screen/LibraryScreenLayout.mobile.kt:485`
- [ ] `ProfileHeader` — `feature/profile/presentation/screen/ProfileScreenLayout.mobile.kt:196`
- [ ] `DesktopExploreHeader`, `DesktopLibraryHeader`, `DesktopReadingHeader` — `feature/explore/presentation/screen/ExploreScreenLayout.jvm.kt:172`, `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:481`, `feature/reading/presentation/screen/ReadingScreenLayout.jvm.kt:220`
- [ ] `ArrangeSubLabel` — `feature/library/presentation/component/LibraryArrangeSheet.kt:191`
- [ ] `ChangeEditionHeader` — `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:208`
- [ ] `ChooseListsHeader` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3; now takes a `ChooseListsVariant` + the jacket slot)
- [ ] `ShelvesSheetHeader` — `feature/library/presentation/component/LibraryShelvesSheet.kt:90`
- [ ] `TagEditorHeader` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:281`
- [ ] `SelectionHeader` — `feature/library/presentation/screen/LibraryShelf.kt:1793`

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
- [ ] `SpoilerToggleIcon` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:677`

#### Skeletons — 9 -> `Skeleton` + `SkeletonUiModel`

- [ ] `EditorialSectionHeaderSkeleton`, `FeaturedCardSkeleton`, `RailCardSkeleton`, `TrendingCardSkeleton`, `BecauseYouReadCardSkeleton`, `SeriesCardSkeleton`, `MoodTileSkeleton` — `feature/explore/presentation/screen/ExploreShelf.kt:132,326,499,579,605,689,1185`
- [ ] `RoadmapSkeleton`, `RoadmapSkeletonLine` — `feature/settings/presentation/screen/RoadmapContent.kt:449,487`

#### Dividers & rules — 5 -> `Divider` + `DividerUiModel`

- [ ] `DebugRowDivider` — `app/src/debug/.../DebugRoutesSection.kt:106`
- [ ] `ReadingLifeDivider` — `core/component/share/ReadingLifeShareCardBody.kt` (S4-6 split)
- [ ] `HorizontalBreak` — `feature/settings/presentation/screen/RoadmapContent.kt:379`
- [ ] `QuoteRule` — `feature/lists/presentation/screen/CreateListSheetContent.kt:176`
- [ ] `OrTypeItDivider` — `feature/onboarding/presentation/screen/OnboardingShelf.kt:237`

### 7.2 Rows & sheet chrome (S6)

#### List rows — 22 -> `ListRow` + `ListRowUiModel`

- [ ] `ChooseListsRow`, `NewListRow` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3; `ChooseListsRow` now takes one `ChooseListsRowUiModel`)
- [ ] `WhenReadRow` — `core/component/progress/UpdateProgressBottomSheet.kt` (moved in S4-3, unchanged)
- [ ] `DebugNavigationRow` — `app/src/debug/.../DebugRoutesSection.kt:68`
- [ ] `HapticRow` — `app/src/debug/.../MotionDebugScreen.kt:137`
- [ ] `AboutLinkRow`, `AboutNavigationRow`, `AboutUsernameRow`, `AboutRow` — `feature/settings/presentation/screen/AboutContent.kt:192,232,275,307`
- [ ] `SettingsToggleRow`, `SettingsSelectableRow`, `ReorderableRow` — `feature/settings/presentation/screen/SettingsShelf.kt:445,517,853`
- [ ] `SettingsMenuRow` — `feature/settings/presentation/screen/SettingsScreenLayout.mobile.kt:248`
- [ ] `SettingsSidebarRow` — `feature/settings/presentation/screen/SettingsScreenLayout.jvm.kt:274`
- [ ] `ShelfSidebarRow` — `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:427`
- [ ] `ShelvesSheetRow`, `ShowTitlesToggleRow` — `feature/library/presentation/component/LibraryShelvesSheet.kt:128`, `LibraryArrangeSheet.kt:227`
- [ ] `ShelveRow`, `DeadlineRow` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:959,1547`
- [ ] `StreakStripSheetRow` — `feature/reading/presentation/component/StreakStrip.kt:203`
- [ ] `BecauseYouReadGenreSheetRow`, `DismissSheetOption`, `SearchFocusRecentRow` — `feature/explore/presentation/screen/ExploreShelf.kt:1811,965,1348`
- [ ] `ShareEntryRow` — `feature/profile/presentation/screen/ProfileShelf.kt:426`
- [ ] `ExplainerStepRow`, `PasteFromClipboardRow` — `feature/onboarding/presentation/screen/OnboardingShelf.kt:404,178`

#### Sheet chrome — extract from 18 sheets -> `SheetScaffold` + `SheetHeader` + `SheetRow` + `SheetFooter`

Chrome only; each sheet's **body** stays a feature composable (`component-contract.md` § 7.6).

- [ ] `ChooseListsBottomSheet` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3; already R1/R2-shaped, so S6 owes it chrome extraction only)
- [ ] `UpdateProgressBottomSheet`, `ProgressBottomSheetContent`, `TabSwitcher` — `core/component/progress/UpdateProgressBottomSheet.kt` (moved in S4-3; already R1/R2-shaped, so S6 owes it chrome extraction only)
- [ ] `VerdictSheet` — `core/component/verdict/VerdictSheet.kt` — **also owes R1** (§ 5h, § 5j)
- [x] `SoftcoverLoadingDialog`, `SoftcoverLoadingSheet` — **DONE in S4-5a.** The sheet became `LoadingSheet` (`core/component/sheet/`, + model + event, consumed by both onboarding layouts); the dialog was deleted as dead. Sheet *chrome* extraction is still owed on `LoadingSheet` — that is this section's S6 work, not this box
- [ ] `LibraryFilterSheet`, `FilterSheetFooter`, `EmptyFacetMessage`, `TagSearchField` — `feature/library/presentation/component/LibraryFilterSheet.kt:76,394,442,309`
- [ ] `LibraryArrangeSheet` — `feature/library/presentation/component/LibraryArrangeSheet.kt:81`
- [ ] `LibraryShelvesSheet` — `feature/library/presentation/component/LibraryShelvesSheet.kt:50`
- [ ] `BulkRemoveConfirmationDialog` — `feature/library/presentation/screen/LibraryShelf.kt:1981`
- [ ] `TagEditorBottomSheet` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:149`
- [ ] `EditionBottomSheetSelector`, `EditionBottomSheetContent` — `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:60,87`
- [ ] `ShareBookBottomSheet` — `feature/book_detail/presentation/component/ShareBookBottomSheet.kt:57`
- [ ] `DeadlinePickerDialog` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:2586`
- [ ] `BecauseYouReadGenreSheet`, `ContinueSeriesDismissSheet`, `ContinueSeriesMenuSheet` — `feature/explore/presentation/screen/ExploreShelf.kt:1756,911,861`
- [ ] `StreakStripSheet`, `StreakStripSheetContent` — `feature/reading/presentation/component/StreakStrip.kt:161,171`
- [ ] `ProfileShareBottomSheet`, `LogOutConfirmBottomSheet` — `feature/profile/presentation/screen/ProfileShelf.kt:1890,2074`
- [ ] `CreateListSheet`, `CreateListSheetContent` — `feature/lists/presentation/screen/CreateListSheet.kt:15`, `CreateListSheetContent.kt:83`
- [ ] `UnknownIsbnSheet` — `feature/scan/presentation/component/UnknownIsbnSheet.kt:28`

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

### 7.4 Screen states (S8)

#### Empty states — 8 -> `EmptyState` + `EmptyStateUiModel`

- [ ] `ChooseListsEmptyState` — `core/component/lists/ChooseListsBottomSheet.kt` (moved in S4-3; hand-rolled `Column` + two `Text`s, does not use `EmptyState`)
- [x] `OfflineScreenContent` — **DONE in S4-5a.** `OfflineGuard.kt` was deleted whole; the content became `EmptyState` + `EmptyStateUiModel` (`core/component/state/`) behind an `offlineEmptyStateUiModel()` factory, called from four screen layouts. `rememberIsOnline` went to `core/presentation/connectivity/OnlineState.kt`
- [ ] `EmptyListScreen` — `feature/library/presentation/screen/LibraryShelf.kt:1696`
- [ ] `EmptyCurrentlyReadingScreen` — `feature/reading/presentation/screen/ReadingShelf.kt:1238`
- [ ] `HiddenSuggestionsEmptyState` — `feature/explore/presentation/screen/HiddenSuggestionsShelf.kt:497`
- [ ] `TagEditorEmptyState` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:761`
- [ ] `EmptyEntriesCard` — `feature/settings/presentation/screen/SettingsShelf.kt:1100`
- [ ] `EmptyDetailPane` — `orchestration/presentation/BookDetailPaneHost.kt:65`

#### Callouts & banners — 8 -> `Callout` + `Banner`

The four `*Callout`s are one component with a tone variant.

- [ ] `StatusCallout`, `ReadInfoCallout`, `WantToReadInfoCallout`, `DnfInfoCallout` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:1663,1599,1649,1627`
- [ ] `ScanEditionUpdateBanner` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:1762`
- [x] `ConnectivityBanner` — **DONE in S4-5a.** Became `Banner` + `BannerUiModel` + `BannerTone` (`core/component/callout/`), called once from `RootScreen.kt`. The `Callout` half of this family's target is still open
- [ ] `RoadmapErrorBanner` — `feature/settings/presentation/screen/RoadmapContent.kt:123`
- [ ] `PaceNudgeRibbon` — `feature/reading/presentation/screen/ReadingShelf.kt:1520`

#### Top bars — 9 -> `TopBar` + `SearchTopBar` + `BackBar`

`TopBar` and `SearchTopBar` stay separate (`component-contract.md` § 7.6).

- [x] `SoftcoverTopBar`, `SoftcoverSearchTopBar`, `SearchChromeBarcodeButton`, `SearchChromeInputArea` — **DONE in S4-5a.** `TopBar` and `SearchTopBar` in `core/component/topbar/` (with `TopBarUiModel`/`Event`/`Navigation`/`Surface` and `SearchTopBarUiModel`/`Event`); the two chrome helpers stayed private inside `SearchTopBar.kt`. ~12 screen layouts consume them. Only `BackBar` is left of this family
- [x] `SoftcoverTopBarAction` — **GONE, deleted in S4-5a** rather than migrated. No successor type: `TopBarUiModel` carries `title` / `subtitle` / `navigation` / `surface`, and a screen's own actions go in the trailing slot
- [ ] `TagEditorTopBar` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:248`
- [ ] `DesktopBookDetailTopBar` — `feature/book_detail/presentation/screen/BookDetailScreenLayout.jvm.kt:135`
- [ ] `OnboardingTopBar` — `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:158`
- [ ] `DesktopSettingsBackBar` — `feature/settings/presentation/screen/SettingsScreenLayout.jvm.kt:622`
- [ ] `HiddenSuggestionsDesktopBackBar` — `feature/explore/presentation/screen/HiddenSuggestionsScreenLayout.jvm.kt:82`

#### Controls & fields — 14 -> `Toggle` + `SegmentedControl` + `TextField`

- [ ] `TimeField` — `core/component/progress/UpdateProgressBottomSheet.kt` (moved in S4-3)
- [ ] `RichTextFormattingToolbar` — `core/component/control/RichTextFormattingToolbar.kt` (was `ReviewFormattingToolbar`; moved + renamed in S4-2b. In the library, but not yet the `Toggle`/`SegmentedControl`/`TextField` consolidation this group is about)
- [ ] `LensToggle`, `LensSegment` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:632,686`
- [ ] `ShareCardVariantToggle` — `feature/book_detail/presentation/component/ShareBookBottomSheet.kt:226`
- [ ] `TagNamingField` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:424`
- [ ] `SelectCircleControl` — `feature/library/presentation/component/LibraryControlLine.kt:264`
- [ ] `BookmarkToggle`, `BecauseYouReadGenreControl` — `feature/explore/presentation/screen/ExploreShelf.kt:1642,1694`
- [ ] `YearMetricToggle`, `HideUntaggedAuthorsToggle` — `feature/profile/presentation/screen/ProfileShelf.kt:579,1069`
- [ ] `EyeToggle` — `feature/settings/presentation/screen/SettingsShelf.kt:1020`
- [ ] `KeyField` — `feature/onboarding/presentation/screen/OnboardingShelf.kt:266`
- [ ] `NameHeroField`, `PrivacyProseToggle` — `feature/lists/presentation/screen/CreateListSheetContent.kt:214,335`

### 7.5 Statistics & progress (S9)

The `/dataviz` skill conventions apply to everything in the chart group.

#### Stat tiles — 7 -> `StatTile` + `StatTileUiModel`

- [x] `AnimatedStatNumber` (×2 overloads), `StatPulseText` — **DONE in S4-5a.** Became `StatNumber` + `StatNumberUiModel` + `StatNumberFormat` (`core/component/statistic/`); `StatPulseText` stayed its private helper. Note this migrated the *number primitive* only — `StatTile` below is untouched
- [ ] `ReadingLifeFooterStat` — `core/component/share/ReadingLifeShareCardBody.kt` (S4-6 split)
- [ ] `HeroStatCard`, `StatTile`, `SmallStatTile` — `feature/profile/presentation/screen/ProfileShelf.kt:230,292,341`
- [ ] `FeaturedProgressStat` — `feature/reading/presentation/screen/ReadingShelf.kt:765`

#### Charts & legends — 11 -> `Chart` family + `Legend`

- [ ] `MiniBar` — `core/component/control/PreviewTile.kt` (moved in S4-5a)
- [ ] `ReadingLifeRidgeline`, `ReadingLifeGenreRanking`, `ReadingLifeGenreRow` — `core/component/share/ReadingLifeShareCardBody.kt` (S4-6 split)
- [ ] `GenreRankedBars`, `GenreRankedBar`, `GenreBarTrack` — `feature/profile/presentation/screen/ProfileShelf.kt:832,865,907`
- [ ] `YearColumnChart` — `feature/profile/presentation/screen/ProfileShelf.kt:617`
- [ ] `GenderProportionBar`, `GenderLegend` — `feature/profile/presentation/screen/ProfileShelf.kt:1115,1142`
- [ ] `DemographicProportionBar`, `DemographicLegend`, `DemographicLegendRow` — `feature/profile/presentation/screen/ProfileShelf.kt:1307,1362,1401`
- [ ] `RatingsHistogramChart`, `RatingsAverageRow` — `feature/profile/presentation/screen/ProfileShelf.kt:1646,1577`

#### Progress — 6 -> `ProgressIndicator` + `ProgressUiModel`

- [ ] `EditorialProgressIndicator` — `core/component/progress/UpdateProgressBottomSheet.kt` (moved in S4-3)
- [ ] `LibraryWaveProgressRow` — `feature/library/presentation/screen/LibraryShelf.kt:1141`
- [ ] `ProgressBlock` — `feature/reading/presentation/screen/ReadingShelf.kt:1100`
- [ ] `FocusProgressBar` — `feature/session/presentation/screen/FocusModeShelf.kt:307`
- [ ] `WavyConnector`, `WavySineLine` — `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:360,390`

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
  `Theme.{android,ios,jvm}`, `TransientNavArg.{android,jvm}`. **`EditionImage`'s platform bodies were
  on this list and should not have been** — S4-4 deleted `EditionImage` outright and the seam it owned
  became `core/uibinding/cover/LocalImageSource.{android,ios,jvm}.kt` plus
  `core/presentation/cover/CoverImagePersisterProvider.kt`, with the composable itself becoming `Cover`
  in the library. That is the second time an "it stays put" exclusion here was wrong (see the debug
  screens below): **an `expect`/`actual` is a reason a declaration needs platform bodies, not a reason
  it stays out of the library** — the bodies can move too, and here they moved to a different module
  than the composable did.
- **Debug screens** — `MotionDebugScreen`, `ShareCardDebugScreen`, `DebugRoutesSection`. Not
  components, so they are not converted to UI models — but they did **not** stay where they were, as
  this bullet originally claimed. S4-2a relocated all three to `app/src/debug/` (§ 5g): they consume
  Voyager and the components that are moving out, and `:core:designsystem` cannot depend on
  `:core:component`. `DebugRoutesContent`, the seam that binds them per build type, moved to
  `:core:presentation`.
- **Share cards are IN scope** — see § 7.0. (Earlier draft deferred them; `component-contract.md` § 7.3 explains why that
  was wrong.) They keep their own family — do not fold them into `BookCard` — but they migrate in S4
  with everything else in `:core:designsystem`.
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
| **`docs/reference/design-system/components.md` keeps growing** — 83KB when this table was written, **96KB after S4** | S12 | Split per family mirroring the § 3 package layout. G5 blocks merge without it. Re-measure at S12 rather than trusting either figure here. |
| **Reviewer load.** A single PR of this size is not reviewable in the normal way. | merge | Commit-per-stage discipline; run `rhaydus-kotlin:code-reviewer` per stage, not once at the end. |

---

## 9. How to resume in a new session

1. Read this file top to bottom. The checkboxes are the state.
2. `git log --oneline main..HEAD` on the migration branch to confirm which stage last landed.
3. `./gradlew check` — confirm the branch is green before adding to it.
4. Re-run the § 2 baseline count and compare with the last recorded value.
5. Pick up the first unchecked item in the lowest unchecked stage. Do not skip ahead: § 5 explains
   why the ordering matters.
6. Per `CLAUDE.md`: feature logic -> `rhaydus-kotlin:rhaydus-logic`, Compose render ->
   `rhaydus-kotlin:rhaydus-ui`, tests -> `unit-test-writer` (never inline), review ->
   `rhaydus-kotlin:code-reviewer`, style -> the `style-check` skill.

---

## Appendix A — Decisions taken

All three opening questions are resolved. Recorded here so they are not re-opened mid-migration.

| Question | Decision | Where it lands |
|---|---|---|
| Gallery reachability | **Shipped easter egg**, not debug-only. N taps on `VersionFooter`. Registry in `:core:component`, screen in `feature:settings`, `commonMain` so it works on all three platforms. | § 5a, S2 |
| `:core:uibinding` dependency visibility | **`api`.** Costs nothing at the gate — all 44 domain models live in `:core:domain`, which is not a data-area module, so no allowlist row is required. A `:core:uibinding -> :core:<data>` row is **pre-approved** if a mapper ever needs one; write it when the edge exists, not speculatively. | § 3a, S1 |
| `ShareCard.kt` (1,161 lines) | **S4, split per body while renaming** — not deferred to S12. It is already the contract's reference implementation; one pass, not two. | `component-contract.md` § 7.3, § 7.0 |

### Still open

- [x] **Easter-egg gesture spec** — **decided in S2:** seven taps, a two-second window between
      consecutive taps, `milestone` haptic on unlock, `noRippleClickable` so the footer looks
      untouched. Counting logic in `SecretTapCounter`, unit-tested. See § 5d.
- [ ] **Fixture size in the release binary.** Measure at S12; not expected to block.

## Appendix B — GitHub issue

**Created: [#275](https://github.com/CinqueIzumi/Softcover/issues/275).** Single `kind:tech` issue, no
milestone (engineering work outside the release cadence, per `CLAUDE.md` § Roadmap). Labels
`area:cross-cutting`, `kind:tech`, `scope:L`. No sub-issues — this is one rollout.

**Tag is `E.1`, not `D.1`.** The draft below said `D.1`; that namespace was already taken (20 issues
use `D.*`, including a `D.1`). Letters A, B, C, D and T are all in use, so the migration claimed the
free `E` series, leaving `E.2+` for follow-ups.

The issue body as filed differs from the draft below in three ways, all reflecting what S1 learned:
it carries the Stages list as a checklist (S1 ticked), it names both isolation gates rather than only
`checkModuleGraph`, and it records the empirically-verified reason the second gate is necessary. When
the two disagree, **the issue is current** — this appendix is kept as the drafting record.

```markdown
<!-- sc-tag: D.1 -->
## Goal

Move every component the app renders into a first-class component library at `:core:component` —
reused or not — each driven by a UI model, with near-duplicate families collapsed to one component
that renders by variant.

Rolled out as **one branch, one PR**. The stage-by-stage plan, per-component checklists with file
paths, and progress state live in `docs/working/component-library-migration.md`.

## Why

- ~650 `@Composable` declarations; only 132 are in `core:designsystem`. ~500 are feature-local,
  and 12,182 lines sit inside six `*Shelf.kt` files.
- `core:designsystem` `api`-depends on `:core:domain` and `:core:book`, and houses navigation, DI,
  a use-case-calling prefetcher, and error mapping. It cannot be the library until it is split.
  `build.gradle.kts` already carries the leak as an allowlist row:
  `":core:designsystem" to ":core:book"`.
- 21 separate book-card implementations across 5 features. `SectionLabel` is declared 4 times;
  `EditorialHeader` and `SidebarSectionLabel` twice each.
- The pattern is already proven here but applied exactly once: `core/designsystem/.../share/`
  has a sealed `ShareContent`, presentation-ready `*ShareContent` data classes, and a single
  `ShareCard(content:)` that `when`-dispatches to private per-variant bodies. It becomes the
  reference implementation; everything else has to be brought up to it.

## Target

    :core:designsystem  tokens only — zero project dependencies
    :core:component     the library; depends on designsystem only; domain / data / Koin /
                        Voyager / Apollo banned at the gate
    :core:uibinding     domain -> UI model adapters shared by 2+ features; `api`-exposes
                        :core:component + :core:domain (no allowlist row needed — all 44
                        domain models live in :core:domain, not a data-area module)
    :core:presentation  nav contracts, TOAD wiring, session, error mapping, DI
                        (evicted from designsystem)

## Component contract

    @Composable
    fun BookCard(
        model: BookCardUiModel,
        onEvent: (BookCardEvent) -> Unit,
        modifier: Modifier = Modifier,
    )

- One sealed event lambda, not N callbacks; events carry the model's key. Per-item callback lambdas
  break skippability in large grids.
- Sealed `variant` over flat enum + nullables, so illegal combinations are unrepresentable.
- UI models are `data class` with `ImmutableList` collections. Stability is a hard requirement —
  there is no stability config file today, so `kotlinx-collections-immutable` is added.
- Models carry presentation-ready values only: no domain types, no `Instant`.
- Mappers start feature-local; promoted to `:core:uibinding` on the second consumer.
- Every UI model ships `previews` fixtures — the gallery's data and the mappers' expected outputs.
- Suffix is `*UiModel`, not `*Content` — `Content` is already overloaded as a composable name in
  every feature. The `share/` types are renamed accordingly.

## Scope judgement

Families consolidate on shared anatomy, not shared category name. The four `*InfoCallout`s become
one `Callout`. `SoftcoverTopBar` / `SoftcoverSearchTopBar` stay separate — the search bar's
documented focus contract makes a merged component two disjoint parameter sets. For sheets,
consolidate the chrome (`SheetScaffold` / `SheetHeader` / `SheetRow` / `SheetFooter`) and leave each
body feature-local.

## Acceptance

- [ ] `checkModuleGraph` fails if `:core:component` gains a domain, data, Koin, Voyager, or Apollo
      dependency
- [ ] `share/` migrated: bodies split one-per-file, `*ShareContent` renamed to `*ShareCardUiModel`,
      and its `ReviewDocument` dependency replaced by a library-owned `RichTextUiModel`
- [ ] `:core:designsystem` has zero project dependencies; the `-> :core:book` api allowlist row is
      deleted
- [ ] `checkComponentBudget` ratchet: `@Composable` count outside `:core:component` cannot rise
      (previews and platform `expect`/`actual` excluded)
- [ ] A Component Gallery reachable as a **shipped easter egg** (N taps on the version footer),
      rendering every UI model's `previews` fixtures across both themes and all palettes; registry in
      `:core:component`, screen in `feature:settings`, `commonMain` so it renders on all three
      platforms
- [ ] Unit tests on every UI model and mapper (Compose UI tests are explicitly out of scope — none
      exist in the repo today)
- [ ] `docs/reference/design-system/` rewritten per family, mirroring the `:core:component` package
      layout
- [ ] `./gradlew check` green
- [ ] `docs/working/component-library-migration.md` deleted

## Risk

`BookCard` concentrates it: 21 call sites in 5 features, all participating in shared-element
transitions via `bookCoverTransitionKey`, several inside selection modes and lazy grids where a
stability regression is a dropped frame rather than a compile error.
```
