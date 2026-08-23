# Component Library Migration — Implementation Tracker

> **Lifecycle.** This is a *working* tracker for a single rollout, not a roadmap document. The
> roadmap lives in GitHub Issues (see `CLAUDE.md` § Roadmap). **Delete this file in the same PR that
> completes the migration.** While it exists it is the source of truth for migration progress —
> update the checkboxes as work lands, in the same commit as the work.

**Rollout model:** one branch, one PR, merged all at once. Stages below are *commit* boundaries on
that branch, not separate pull requests. Every stage boundary must leave the branch compiling.

**Status:** `S2 COMPLETE, REVIEWED` — next up S3 (evict non-components from `:core:designsystem`)
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
> Also note: **`styleCheck` is already red at this branch point** for a reason unrelated to the
> migration — `SoftcoverColorSchemeTest.kt:171` has an `UnusedParameter` finding. Reproduced with the
> branch stashed. Not fixed here (nothing in the migration touches that file, so the on-touch
> compliance policy does not reach it), but it means `styleCheck` is not a clean signal until someone
> clears it. Per-module detekt is: all three new modules pass.
>
> Reliable per-change gates: `checkModuleGraph`, `<module>:projectHealth`,
> `<module>:compileKotlinJvm`, `ktlintCheck`, and `<module>:detektJvmMain` under JDK 21.

---

## 1. Goal

Move every component the app renders into a first-class component library, whether it is currently
reused or not. Each component is driven by a **UI model**; families of near-duplicate components
collapse into **one** component that renders differently based on the model passed in.

## 2. Baseline (measured 2026-08-21, `release/3.2.0` @ `ada51050`)

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
| Working instances of the pattern under another name | **1** — the `share/` package (§ 4.3) |

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
`":core:designsystem" to ":core:book"` in `allowedApiDataEdges` (`build.gradle.kts:281`). **That row
disappearing is a measurable outcome of this migration.**

### Confirmed duplication

Same-named composables declared in multiple modules today:

- `SectionLabel` × 4 — `core/designsystem/.../debug/MotionDebugScreen.kt:349`,
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

:core:presentation     The non-component residents evicted from designsystem: TOAD wiring,
                       AppNavigator, ScreenDestination, TabDestination, TransientNavArg,
                       BookDetailPresenter, CreateListPresenter, ActiveSessionController,
                       SessionAuthenticator, ApiErrorMessage, SplashState, ReAuthState,
                       BookDetailPrefetcher, LocalAppUpdate, the Koin module.
```

All four are tier `core`, so `tierOf()` (`build.gradle.kts:255`) classifies them automatically from
their path. No tier-map change is needed; the new **ban list** is (see § 6).

### 3a. `:core:uibinding` dependency visibility — decided: `api`

`:core:uibinding` re-exposes its edges with `api`, so a feature depending on it sees the domain type,
the UI model, and the tokens without re-declaring all three.

**What that costs at the gate: nothing.** The api-visibility rule (`build.gradle.kts:281`) only
covers `dataAreaModules` — `:core:{book, lists, deadlines, personal, profile, identity, preferences}`.
All **44** domain models live in `:core:domain`, which is an *infra/contract* module and may
`api`-expose freely. `:core:book` owns only two of its own models (`IsbnEditionMatch`,
`CreatedBook`) alongside its repository and use cases. So the mappers this migration needs —
`Book`, `BookEdition`, `UserBook`, `BookList`, `DeadlineStatus`, `ColorPalette`, `ReviewDocument`,
all in `:core:domain` — require **no allowlist row at all**.

Policy recorded so nothing blocks mid-migration:

- `api(project(":core:domain"))`, `api(project(":core:component"))`,
  `api(project(":core:designsystem"))` from `:core:uibinding` — no row needed, none to be written.
- Should a shared mapper genuinely need a *data-area* type (realistically only `:core:book`'s
  `IsbnEditionMatch` / `CreatedBook`), **this decision pre-approves adding the
  `":core:uibinding" to ":core:<data>"` row** to `allowedApiDataEdges` without further sign-off.
  Write the row when the edge exists — not speculatively, or the allowlist rots into noise the way
  the designsystem row did.
- The row that **does** need writing is for `:core:presentation`: the evicted `BookDetailPrefetcher`
  consumes `FetchBookByIdUseCase` and `PersistEditionImageUseCase` from `:core:book`. That is the
  real reason `":core:designsystem" to ":core:book"` exists today. Prefer `implementation` there; add
  `":core:presentation" to ":core:book"` only if the use-case types surface in its public API.

### Package layout inside `:core:component`

One directory per family. Model, component, and preview fixtures live together.

```
component/
  bookcard/    BookCard.kt        BookCardUiModel.kt        BookCardPreviews.kt
  cover/       Cover.kt           CoverUiModel.kt           CoverPreviews.kt
  chip/        Chip.kt            ChipUiModel.kt            ChipPreviews.kt
  row/         ListRow.kt         ListRowUiModel.kt         ListRowPreviews.kt
  header/      SectionHeader.kt   PageMasthead.kt           SidebarLabel.kt
  badge/       Badge.kt           CoverOverlay.kt
  callout/     Callout.kt         Banner.kt
  state/       EmptyState.kt      Skeleton.kt               ErrorState.kt
  statistic/   StatTile.kt        Chart.kt                  Legend.kt
  progress/    ProgressIndicator.kt
  sheet/       SheetScaffold.kt   SheetHeader.kt            SheetRow.kt   SheetFooter.kt
  topbar/      TopBar.kt          SearchTopBar.kt           BackBar.kt
  control/     Toggle.kt          SegmentedControl.kt       TextField.kt  Divider.kt
  share/       ShareCard.kt       + one file per card body  (§ 7.0)
  gallery/     GalleryRegistry.kt GalleryEntry.kt
```

**The gallery splits across two modules.** The *registry* — the list of every component paired with
its `previews` fixtures — is pure data and lives in `:core:component/gallery/`. The *screen* lives in
`feature:settings`, because it is a shipped, navigable screen and G1 bans `:core:component` from
Voyager. See § 5a.

---

## 4. The UI model contract

This is the decision that determines whether the library is any good. It must be written down
**before** the first component moves, or the 21 book cards each invent their own convention.

> **Canonical as of S2:** this contract now lives in
> [`docs/reference/design-system/component-contract.md`](../reference/design-system/component-contract.md)
> as § 7 of the design system, and that file is the one to read and to keep current. What follows is
> the drafting record — where the two disagree, the design-system doc is right.

### 4.1 Signature

```kotlin
@Composable
fun BookCard(
    model: BookCardUiModel,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
)
```

### 4.2 Rules

**R1 — One sealed event lambda, never N callbacks.**
`onEvent: (BookCardEvent) -> Unit` with `BookCardEvent` sealed (`Click`, `LongClick`,
`ToggleSelected`, `Bookmark`, `Overflow`), each event carrying the model's key. A per-item
`onClick = { ... }` lambda allocates fresh on every recomposition and kills skippability across a
500-item library grid; one hoisted lambda plus a key does not.

**R2 — Sealed variants, not a flat enum beside nullable soup.**

```kotlin
data class BookCardUiModel(
    val key: BookCardKey,                  // identity + shared-element transition key
    val content: BookCardContent,          // cover, title, subtitle, badges — always present
    val variant: BookCardVariant,          // sealed: Grid | CoverOnly | Row(density) |
                                           //         Rail | Featured(backdrop) | Tile
    val decorations: BookCardDecorations,   // progress?, selection?, trailing?
)
```

`BookCard` does `when (model.variant)` and dispatches to private per-variant layout composables. The
public surface stays one symbol. This makes illegal states unrepresentable — `CoverOnly` cannot carry
a subtitle, `Featured` cannot carry a selection circle — where the flat-enum shape would allow both
and every caller would have to remember not to.

**R3 — Stability is a hard requirement.**
There is no `stabilityConfigurationFile` in the build and no `kotlinx-collections-immutable` in
`gradle/libs.versions.toml` today. A UI model holding `List<Badge>` is unstable, so every card in
every grid recomposes every frame. **Add `kotlinx-collections-immutable`** and type every collection
in a UI model as `ImmutableList` — compiler-checked, versus a stability config file that drifts
silently.

**R4 — Presentation-ready values only.**
Formatted strings, resolved icon tokens, computed fractions. No `Instant`, no `DeadlineStatus`, no
`UserBook`. `DeadlineBadge(status: DeadlineStatus)` becomes `Badge(model: BadgeUiModel)` where the
*feature* already decided the label and the tone.

**R5 — Every UI model ships preview fixtures.**
`BookCardUiModel.Companion.previews: ImmutableList<BookCardUiModel>` in `:core:component`. These are
the Component Gallery's data **and** the mappers' expected outputs, so a component's preview set and
its test set cannot diverge.

**R6 — Mapper placement.**
A mapper starts in the consuming feature's `presentation/mapper/`. It is promoted to
`:core:uibinding` **on its second consumer**. Two features independently writing the identical
mapper is the promotion signal; two features needing *different* mappings onto the same UI model is
the system working as intended.

**R8 — Suffix is `*UiModel`, not `*Content`.**
The `share/` package already names these types `*ShareContent` (§ 4.3). Standardize on `*UiModel`
and rename them during S4. Reason: `Content` is heavily overloaded here as a *composable* name —
every feature has a `Content`, plus `AboutContent`, `RoadmapContent`, `EditionBottomSheetContent`,
`ProgressBottomSheetContent`, `SessionPeekBarContent`, `ReadingLifeContent`, `ProfileContent`. Using
it as a data-type suffix as well makes the two concepts unreadable in imports. `*UiModel` is
unambiguous.

**R7 — Shared-element keys travel in the model.**
`bookCoverTransitionKey(editionId, bookId, surface)` lives in
`core/designsystem/.../transition/SharedElementScopes.kt` and stays in the token module. The
resolved key string is a field on `BookCardKey`; the component never computes it.

### 4.3 Reference implementation — `ShareCard` already does this

**Correction to an earlier reading of this codebase:** the contract above is *not* net-new. No type
is named `*UiModel`, but `core/designsystem/presentation/share/` is a working, shipped instance of
R2 under a different name. Read it before writing any new component — it is the model to copy.

```kotlin
// share/ShareContent.kt
sealed interface ShareContent

// share/BookShareContent.kt — presentation-ready primitives only, zero domain types
data class BookShareContent(
    val coverUrl: String?,
    val title: String,
    val author: String,
    val communityRating: Double?,
    val userRating: Int?,
    val releaseYear: Int?,
    val pageCount: Int?,
    val description: String?,
    val quote: String?,
) : ShareContent

// share/ShareCard.kt:68 — ONE public symbol, `when` dispatch to private per-variant bodies
@Composable
fun ShareCard(content: ShareContent, modifier: Modifier = Modifier) { ... }
```

It satisfies R2 (sealed variant, single public symbol, private per-variant bodies) and R4 for five of
its six variants, and it even carries a per-variant sizing table
(`ShareCardDimensions.forContent(content)`) — the pattern to reuse wherever a variant needs its own
metrics.

Two gaps to close, not two reasons to distrust it:

- **R4 gap:** `ShareCard.kt` still imports `ReviewDocument`, `ReviewParagraph`, `ReviewRun`, and
  `ThemeMode` from `:core:domain`. See the rich-text item in § 7.0.
- **R8 gap:** the `*ShareContent` naming. Renamed in S4.

### 4.4 Where consolidation is the wrong call

Families consolidate on **shared anatomy, not shared category name.** Record the judgement calls
here so they are not re-litigated mid-migration:

| Do collapse | Do **not** collapse | Why |
|---|---|---|
| The four `*InfoCallout`s -> one `Callout` with a tone variant | `SoftcoverTopBar` and `SoftcoverSearchTopBar` | The design-system doc already specifies the search bar's bespoke focus contract (`focused` driven by the caller, `onSearchActivated`/`onSearchDismissed`/`onClearSearch` as intents). Merged, it is one component with two disjoint parameter sets. |
| The 21 book cards -> one `BookCard` | The 18 sheet **bodies** | Consolidate sheet *chrome* (`SheetScaffold`/`SheetHeader`/`SheetRow`/`SheetFooter`); leave each body a feature composable built from those parts. `LibraryFilterSheet` and `TagEditorBottomSheet` share no anatomy — a variant enum over them would be a component in name only. |

---

## 5. Stages

One branch. One commit (or a small run of commits) per stage. **Each stage boundary compiles.**

- [x] **S1 — Module scaffolding.** DONE. Created `:core:component`, `:core:uibinding`,
      `:core:presentation`; wired `settings.gradle.kts`; three module build files
      (`softcover.kmp.library` + `softcover.kmp.compose`); added `kotlinx-collections-immutable`
      (0.4.0) to `gradle/libs.versions.toml`; extended `checkModuleGraph` with the § 6 ban list.
      Nothing moved yet. **See § 5b for two findings that changed the design.**
- [x] **S2 — Contract & gallery scaffold.** DONE. § 4 is written into
      `docs/reference/design-system/component-contract.md` as § 7 of the design system, citing
      `ShareCard` as the reference implementation (§ 4.3) — **that doc is now canonical for the
      contract; § 4 here is the drafting record.** Landed the `previews` fixture pattern as a
      compile-checked interface (`UiModelPreviews<M>`), the `GalleryRegistry` / `GalleryEntry` /
      `GalleryFixture` / `GalleryFamily` scaffold in `:core:component`, and the
      `ComponentGalleryScreen` + its TOAD wiring + the easter-egg tap gesture in `feature:settings`.
      **See § 5d for the gesture spec (previously open) and three implementation notes.**
- [ ] **S3 — Evict non-components.** Move nav / TOAD / session / error / DI / prefetch out of
      `:core:designsystem` into `:core:presentation`; re-point every importing module. This is the
      largest mechanical churn (nav types are imported nearly everywhere) and carries no UI change.
- [ ] **S4 — Tokens-only designsystem.** Move the 132 existing `core:designsystem` components into
      `:core:component`, converting each to a UI model as it moves (they cannot land domain-typed —
      the gate rejects it). Write their mappers per R6. Includes the `share/` package: split the six
      card bodies one-per-file, rename `*ShareContent` -> `*ShareCardUiModel` per R8, and close its
      R4 gap via the rich-text item in § 7.0. `:core:designsystem` ends at zero project
      dependencies; delete the `":core:designsystem" to ":core:book"` allowlist row.
- [ ] **S5 — Primitives** (§ 7.1): chips/pills, badges/overlays, headers/labels, dividers, skeletons.
- [ ] **S6 — Rows & sheet chrome** (§ 7.2).
- [ ] **S7 — `BookCard`** (§ 7.3). The main event, and the risk concentration point.
- [ ] **S8 — Screen states** (§ 7.4): empty states, callouts, banners, offline/error, top bars.
- [ ] **S9 — Statistics** (§ 7.5): stat tiles, charts, legends, progress indicators.
- [ ] **S10 — Shelf teardown.** The six `*Shelf.kt` files shrink to layout + composition.
- [ ] **S11 — Close out.** Final gate values, `docs/reference/design-system/` rewrite, gallery
      completeness pass, delete this file.

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
The unused-dependency check is `severity("fail")` (root build file, ~line 311), so a
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

### 5a. The Component Gallery — decided: shipped easter egg

Not debug-only. Consequences to build for, rather than discover late:

- **It must be `commonMain`.** `MotionDebugScreen` / `ShareCardDebugScreen` /
  `DebugRoutesSection` live in `core/designsystem/src/androidMain/.../debug/` and are Android-only.
  A shipped gallery has to render on iOS and desktop too.
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
  measurement at S11, not a blocker.
- **It is now a user-visible surface**, so it needs a design pass and a
  `docs/reference/design-system/` entry. G5 applies to it like any other screen.

**Why the contract precedes the migrations:** S5 is where the convention gets stress-tested on cheap
components, and S7 is where a stability or shared-element regression costs a visible frame drop
rather than a compile error. Doing S7 before S5–S6 would mean settling the model conventions on the
hardest family.

---

## 6. Gates

Convention does not hold a boundary — `:core:designsystem` is the proof, and `build.gradle.kts:264`
already says so in a comment. Every rule below is a build failure.

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
- [ ] **G2 — `:core:designsystem` has zero project dependencies.** Assert in the same task.
- [ ] **G3 — The `:core:designsystem` -> `:core:book` api allowlist row is gone** from
      `allowedApiDataEdges` (`build.gradle.kts:281`).
- [ ] **G4 — Composable budget ratchet.** A `checkComponentBudget` task counting `@Composable`
      declarations outside `:core:component`, excluding only (a) functions whose name ends in
      `Preview` and (b) platform `expect`/`actual` composables (`BarcodeScanner`). Ceiling set to
      the post-migration measured value; a rise fails the build. Wire into `check`.
- [ ] **G5 — Doc rule.** `docs/reference/design-system/` updated. Already enforced by
      `rhaydus-kotlin:code-reviewer`, which treats a design-system change without a doc update as a
      blocker. `components.md` is 83KB today and will need splitting per family, mirroring the
      § 3 package layout.
- [ ] **G6 — `./gradlew check` green**, including `styleCheck` (type-resolved detekt across every
      module) and `ktlintCheck`.

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

These two land in S4, before every family below. The share cards are the reference implementation (§ 4.3); rich text blocks `QuoteShareCardBody` and `VerdictSheet`.

#### Share cards — 1,161 lines -> `share/` in `:core:component`, one file per body

**Recommendation: do this in S4, not S11, and split the bodies while renaming.** S4 already touches
every `:core:designsystem` component; leaving this file whole means either it does not get UI models
in S4 (and then fails G1, since it imports `:core:domain`) or it does and stays a 1,161-line
monolith that S11 has to re-open. One pass, not two.

The dispatch and the `*ShareContent` types are already correct (§ 4.3) — this is a rename plus a
mechanical split plus one real piece of work (rich text, below).

- [ ] Keep `ShareCard(content:)` dispatch + `ShareCardSignOff` + `ShareCardDimensions` in `ShareCard.kt` (`share/ShareCard.kt:68,959`)
- [ ] `BookShareCardBody` -> own file (`share/ShareCard.kt:132`, + `buildBookStatsLine:226`)
- [ ] `ReadingUpdateShareCardBody`, `ReadingUpdateReaderIdentity` -> own file (`:243,377`)
- [ ] `StatShareCardBody` -> own file (`:408`)
- [ ] `QuoteShareCardBody` -> own file (`:433`) — carries the R4 rich-text gap
- [ ] `YearRecapShareCardBody` -> own file (`:472`)
- [ ] `ReadingLifeShareCardBody` + its parts (`MiniScallopPortrait:677`, `ReadingLifeRidgeline:721`, `ReadingLifeGenreRanking:824`, `ReadingLifeGenreRow:851`, `ReadingLifeFooterStat:887`, `ReadingLifeDivider:924`, `normalizedReadingLifeMonths:933`, `readingLifeInitials:943`) -> own file
- [ ] Rename per R8: `ShareContent` -> `ShareCardUiModel`; `BookShareContent`, `QuoteShareContent`, `StatShareContent`, `YearRecapShareContent`, `ReadingLifeShareContent`, `ReadingUpdateShareContent` -> `*ShareCardUiModel`
- [ ] Update the two mappers that build them: `feature/book_detail/presentation/component/ReadingUpdateShareContentMapper.kt` (and its existing test) and the `ProfileShareBottomSheet` / `ReadingLifeSharePreview` construction sites in `feature/profile`

#### Rich text — the one non-mechanical R4 conversion

Six components take `ReviewDocument` / `ReviewParagraph` / `ReviewRun` / `ReviewMark` straight from
`:core:domain`. R4 forbids that in `:core:component`, so the library needs its own
`RichTextUiModel` + `RichTextRun` + `RichTextMark`, with the domain -> UI mapping in
`:core:uibinding` (two consumers: book_detail and the quote share card, so R6 promotes it
immediately).

This is the only place in the migration where R4 is a design problem rather than a rename. Do it
early in S4 — `QuoteShareCardBody` and `VerdictSheet` both block on it.

- [ ] `RichTextUiModel` / `RichTextRun` / `RichTextMark` in `:core:component`
- [ ] `ReviewDocument` -> `RichTextUiModel` mapper in `:core:uibinding`, with tests
- [ ] `ReviewRichText` — `core/designsystem/presentation/component/ReviewRichText.kt`
- [ ] `ReviewDocumentText` — `core/designsystem/presentation/component/ReviewDocumentText.kt`
- [ ] `ReviewMark` / `ReviewMarkType` — `core/designsystem/presentation/component/ReviewMark.kt`, `ReviewMarkType.kt`
- [ ] `ReviewEditorBuffer` — `core/designsystem/presentation/component/ReviewEditorBuffer.kt`
- [ ] `VerdictBlock`, `VerdictScoreAndCaption` — `core/designsystem/presentation/component/VerdictBlock.kt`
- [ ] `ReviewCard` — `feature/book_detail/presentation/screen/BookDetailShelf.kt`
- [ ] Existing `ReviewRichTextTest` re-pointed at the new model (it currently asserts on `ReviewDocument`)

### 7.1 Primitives (S5)

#### Chips & pills — 29 -> `Chip` + `ChipUiModel`

- [ ] `PillChip`, `PillChipLabel` — `core/designsystem/presentation/component/PillChip.kt:31,81`
- [ ] `AddFilledPill`, `AddOutlinePill`, `MembershipPill`, `OnListChip` — `core/designsystem/presentation/component/ChooseListsBottomSheet.kt:540,559,480,502`
- [ ] `FormatChip` — `core/designsystem/presentation/component/ReviewFormattingToolbar.kt:65`
- [ ] `SearchChromePill` — `core/designsystem/presentation/component/SoftcoverTopBar.kt:126`
- [ ] `ActiveFilterChip`, `ClearAllChip`, `LibraryFilterChipRow` — `feature/library/presentation/component/LibraryFilterChipRow.kt:96,136,40`
- [ ] `ArrangeChip`, `LayoutChipRow`, `SortChipRow` — `feature/library/presentation/component/LibraryArrangeSheet.kt:304,201,251`
- [ ] `FilterPillControl`, `RearrangeHintChip` — `feature/library/presentation/component/LibraryControlLine.kt:209,154`
- [ ] `SelectionActionPill` — `feature/library/presentation/screen/LibraryShelf.kt:1905`
- [ ] `ConcealableTagChip`, `DashedTagOpenerChip`, `ExternalLinkPill` — `feature/book_detail/presentation/screen/BookDetailShelf.kt:2099,1946,2209`
- [ ] `AddPill`, `TagChip`, `TagChipName` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:510,621,709`
- [ ] `TrackingNowChip` — `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:437`
- [ ] `RecentSearchChip`, `SortChip`, `FlowRowMoodChips` — `feature/explore/presentation/screen/ExploreShelf.kt:1260,1426,1405`
- [ ] `SetProgressChip` — `feature/reading/presentation/screen/ReadingShelf.kt:1136`
- [ ] `UpdatePillButton` — `feature/settings/presentation/screen/SettingsShelf.kt:1273`
- [ ] `SortLabelControl` — `feature/library/presentation/component/LibraryControlLine.kt:107`

#### Headers & labels — 17 -> `SectionHeader` + `PageMasthead` + `SidebarLabel`

Kills all three cross-module name collisions.

- [ ] `SectionLabel` × 4 — `core/designsystem/presentation/debug/MotionDebugScreen.kt:349`, `feature/book_detail/presentation/screen/BookDetailShelf.kt:2535`, `feature/profile/presentation/screen/ProfileShelf.kt:136`, `feature/reading/presentation/screen/ReadingShelf.kt:1178`
- [ ] `EditorialHeader` × 2 — `core/designsystem/presentation/component/UpdateProgressBottomSheet.kt:250`, `feature/reading/presentation/screen/ReadingScreenLayout.mobile.kt:158`
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
- [ ] `ChooseListsHeader` — `core/designsystem/presentation/component/ChooseListsBottomSheet.kt:171`
- [ ] `ShelvesSheetHeader` — `feature/library/presentation/component/LibraryShelvesSheet.kt:90`
- [ ] `TagEditorHeader` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:281`
- [ ] `SelectionHeader` — `feature/library/presentation/screen/LibraryShelf.kt:1793`

#### Badges & cover overlays — 12 -> `Badge` + `CoverOverlay`

- [ ] `DeadlineBadge` — `core/designsystem/presentation/component/DeadlineBadge.kt:15` (drop the `DeadlineStatus` parameter per R4)
- [ ] `DeadlineCoverOverlay` — `core/designsystem/presentation/component/DeadlineCoverOverlay.kt:15`
- [ ] `UnreleasedBadge` (+ `UnreleasedBadgeStyle`) — `core/designsystem/presentation/component/UnreleasedBadge.kt:45`
- [ ] `BookmarkGlyph` — `core/designsystem/presentation/component/ChooseListsBottomSheet.kt:418`
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

- [ ] `DebugRowDivider` — `core/designsystem/presentation/debug/DebugRoutesSection.kt:106`
- [ ] `ReadingLifeDivider` — `core/designsystem/presentation/share/ShareCard.kt:924`
- [ ] `HorizontalBreak` — `feature/settings/presentation/screen/RoadmapContent.kt:379`
- [ ] `QuoteRule` — `feature/lists/presentation/screen/CreateListSheetContent.kt:176`
- [ ] `OrTypeItDivider` — `feature/onboarding/presentation/screen/OnboardingShelf.kt:237`

### 7.2 Rows & sheet chrome (S6)

#### List rows — 22 -> `ListRow` + `ListRowUiModel`

- [ ] `ChooseListsRow`, `NewListRow` — `core/designsystem/presentation/component/ChooseListsBottomSheet.kt:330,594`
- [ ] `WhenReadRow` — `core/designsystem/presentation/component/UpdateProgressBottomSheet.kt:372`
- [ ] `DebugNavigationRow` — `core/designsystem/presentation/debug/DebugRoutesSection.kt:68`
- [ ] `HapticRow` — `core/designsystem/presentation/debug/MotionDebugScreen.kt:137`
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

Chrome only; each sheet's **body** stays a feature composable (§ 4.4).

- [ ] `ChooseListsBottomSheet` — `core/designsystem/presentation/component/ChooseListsBottomSheet.kt:66`
- [ ] `UpdateProgressBottomSheet`, `ProgressBottomSheetContent`, `TabSwitcher` — `core/designsystem/presentation/component/UpdateProgressBottomSheet.kt:100,124,283`
- [ ] `VerdictSheet` — `core/designsystem/presentation/component/VerdictSheet.kt:99`
- [ ] `SoftcoverLoadingDialog`, `SoftcoverLoadingSheet` — `core/designsystem/presentation/component/SoftcoverLoadingDialog.kt:25,35`
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
| `StackedJackets` — `core/designsystem/presentation/component/ChooseListsBottomSheet.kt:264` | -> `Cover(stacked)` |

Checklist:

- [ ] `BookCardUiModel` / `BookCardVariant` / `BookCardContent` / `BookCardDecorations` / `BookCardEvent` / `BookCardKey`
- [ ] `Cover` + `CoverUiModel` (incl. stacked, coverless-monogram, selection, deadline overlay)
- [ ] `BookCard` with per-variant private layouts
- [ ] Preview fixtures covering every variant × decoration combination
- [ ] Mappers: `feature:library`, `feature:explore`, `feature:reading`, `feature:profile`, `feature:book_detail` (promote to `:core:uibinding` per R6 where two features converge)
- [ ] Mapper unit tests (via `unit-test-writer`)
- [ ] Shared-element transition keys verified on library -> book detail and explore -> book detail
- [ ] Skippability verified: no per-item lambda allocation in the library grid

### 7.4 Screen states (S8)

#### Empty states — 8 -> `EmptyState` + `EmptyStateUiModel`

- [ ] `ChooseListsEmptyState` — `core/designsystem/presentation/component/ChooseListsBottomSheet.kt:309`
- [ ] `OfflineScreenContent` — `core/designsystem/presentation/component/OfflineGuard.kt:30`
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
- [ ] `ConnectivityBanner` — `core/designsystem/presentation/component/ConnectivityBanner.kt:24`
- [ ] `RoadmapErrorBanner` — `feature/settings/presentation/screen/RoadmapContent.kt:123`
- [ ] `PaceNudgeRibbon` — `feature/reading/presentation/screen/ReadingShelf.kt:1520`

#### Top bars — 9 -> `TopBar` + `SearchTopBar` + `BackBar`

`SoftcoverTopBar` and `SoftcoverSearchTopBar` stay separate (§ 4.4).

- [ ] `SoftcoverTopBar`, `SoftcoverSearchTopBar`, `SearchChromeBarcodeButton`, `SearchChromeInputArea` — `core/designsystem/presentation/component/SoftcoverTopBar.kt:349,82,319`
- [ ] `SoftcoverTopBarAction` — `core/designsystem/presentation/component/SoftcoverTopBarAction.kt`
- [ ] `TagEditorTopBar` — `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:248`
- [ ] `DesktopBookDetailTopBar` — `feature/book_detail/presentation/screen/BookDetailScreenLayout.jvm.kt:135`
- [ ] `OnboardingTopBar` — `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:158`
- [ ] `DesktopSettingsBackBar` — `feature/settings/presentation/screen/SettingsScreenLayout.jvm.kt:622`
- [ ] `HiddenSuggestionsDesktopBackBar` — `feature/explore/presentation/screen/HiddenSuggestionsScreenLayout.jvm.kt:82`

#### Controls & fields — 14 -> `Toggle` + `SegmentedControl` + `TextField`

- [ ] `TimeField` — `core/designsystem/presentation/component/UpdateProgressBottomSheet.kt:1105`
- [ ] `ReviewFormattingToolbar` — `core/designsystem/presentation/component/ReviewFormattingToolbar.kt:27`
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

- [ ] `AnimatedStatNumber` (×2 overloads), `StatPulseText` — `core/designsystem/presentation/component/AnimatedStatNumber.kt:41,81,115`
- [ ] `ReadingLifeFooterStat` — `core/designsystem/presentation/share/ShareCard.kt:887`
- [ ] `HeroStatCard`, `StatTile`, `SmallStatTile` — `feature/profile/presentation/screen/ProfileShelf.kt:230,292,341`
- [ ] `FeaturedProgressStat` — `feature/reading/presentation/screen/ReadingShelf.kt:765`

#### Charts & legends — 11 -> `Chart` family + `Legend`

- [ ] `MiniBar` — `core/designsystem/presentation/component/PreviewTile.kt:107`
- [ ] `ReadingLifeRidgeline`, `ReadingLifeGenreRanking`, `ReadingLifeGenreRow` — `core/designsystem/presentation/share/ShareCard.kt:721,851`
- [ ] `GenreRankedBars`, `GenreRankedBar`, `GenreBarTrack` — `feature/profile/presentation/screen/ProfileShelf.kt:832,865,907`
- [ ] `YearColumnChart` — `feature/profile/presentation/screen/ProfileShelf.kt:617`
- [ ] `GenderProportionBar`, `GenderLegend` — `feature/profile/presentation/screen/ProfileShelf.kt:1115,1142`
- [ ] `DemographicProportionBar`, `DemographicLegend`, `DemographicLegendRow` — `feature/profile/presentation/screen/ProfileShelf.kt:1307,1362,1401`
- [ ] `RatingsHistogramChart`, `RatingsAverageRow` — `feature/profile/presentation/screen/ProfileShelf.kt:1646,1577`

#### Progress — 6 -> `ProgressIndicator` + `ProgressUiModel`

- [ ] `EditorialProgressIndicator` — `core/designsystem/presentation/component/UpdateProgressBottomSheet.kt:310`
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
  `EditionImage` platform bodies, `Theme.{android,ios,jvm}`, `TransientNavArg.{android,jvm}`.
- **Debug screens** — `MotionDebugScreen`, `ShareCardDebugScreen`, `DebugRoutesSection`. These
  become gallery *consumers*, and `ComponentGalleryScreen` joins them.
- **Share cards are IN scope** — see § 7.0. (Earlier draft deferred them; § 4.3 explains why that
  was wrong.) They keep their own family — do not fold them into `BookCard` — but they migrate in S4
  with everything else in `:core:designsystem`.
- **`@Preview` functions.** Excluded from the G4 budget count.

---

## 8. Risks

| Risk | Where | Mitigation |
|---|---|---|
| **Compose stability regression** — a `List` in a UI model makes every grid item recompose per frame | S7, library grid + explore rails | R3: `kotlinx-collections-immutable`, `ImmutableList` everywhere. Verify with the compiler metrics report before S11. |
| **Lambda-allocation regression** — per-item `onClick` defeats skipping | S7 | R1: one hoisted `onEvent`, key on the model. |
| **Shared-element transitions break** | S7 | R7: key resolved by the mapper, carried on `BookCardKey`. Manually verify library -> detail and explore -> detail. |
| **Long-lived red branch.** All-at-once means the module split's compile breakage is resolved inside the branch. | S3, S4 | Stage boundaries must compile. Commit per stage so the PR is reviewable commit-by-commit even though it merges once. |
| **Session loss mid-migration** | any | This file. Update checkboxes in the same commit as the work, and record the branch name in the header. |
| **`docs/reference/design-system/components.md` is 83KB** and will be substantially rewritten | S11 | Split per family mirroring the § 3 package layout. G5 blocks merge without it. |
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
| `ShareCard.kt` (1,161 lines) | **S4, split per body while renaming** — not deferred to S11. It is already the contract's reference implementation; one pass, not two. | § 4.3, § 7.0 |

### Still open

- [x] **Easter-egg gesture spec** — **decided in S2:** seven taps, a two-second window between
      consecutive taps, `milestone` haptic on unlock, `noRippleClickable` so the footer looks
      untouched. Counting logic in `SecretTapCounter`, unit-tested. See § 5d.
- [ ] **Fixture size in the release binary.** Measure at S11; not expected to block.

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
