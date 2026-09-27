# Design System — Component contract

## 7. Component contract

Every component in `:core:component` is driven by a **UI model**. This contract fixes what a component's
signature looks like, what its model may hold, and where the domain → UI mapping lives.

It is normative. A component that does not satisfy R1–R11 does not belong in `:core:component`; two of the
rules are build gates (§ 7.4), and the standing exceptions are in § 7.4a. R10 and R11 are not yet satisfied
by the whole library; both say what that means for new code. Why the rules exist is in § 7.7, which is
optional reading.

> **Read `share/` before writing a new component.** `ShareCard` is the reference implementation (§ 7.3).

---

### 7.1 The signature

```kotlin
@Composable
fun BookCard(
    model: BookCardUiModel,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
)
```

Three parameters, in that order. A component that renders nothing interactive drops `onEvent`; a component
that needs a slot takes a trailing `content: @Composable () -> Unit`. Nothing else is added, at either end of
the list — no loose `title: String` beside the model, no second callback, and no trailing
`style: TextStyle = …` or `color: Color = …` (R11).

### 7.2 The rules

**R1 — One sealed event lambda, never N callbacks.** `onEvent: (XEvent) -> Unit`, with `XEvent` a sealed
interface whose members carry the model's key. The caller hoists one lambda and reads the key off the event;
it never builds a fresh `onClick = { … }` per item. The shipped example is `ProgressSheetEvent`
(`progress/`):

```kotlin
sealed interface ProgressSheetEvent {
    data class TabSelected(val tab: ProgressSheetTab) : ProgressSheetEvent
    data class PagesSubmitted(val page: String, val actionAt: String?) : ProgressSheetEvent
    …
    data class MarkAsReadRequested(val actionAt: String?) : ProgressSheetEvent
    data object Dismissed : ProgressSheetEvent
}
```

- **Screen state the component renders belongs on the model**, not in a second parameter (the progress
  sheet's selected tab is `ProgressSheetUiModel.selectedTab`). Its stored home stays in the feature's
  `UiState`, and the model field is derived from it in one collector, so there is one writer.
- **Handle every branch.** An exhaustive `when` makes a missing branch a compile error, but not an empty
  one: a branch left blank to "wire later" passes every gate, `EmptyFunctionBlock` included, because detekt
  does not see `when` arms.

**R2 — Sealed variants, not a flat enum beside nullable fields.**

```kotlin
data class BookCardUiModel(
    val key: BookCardKey,                 // identity + shared-element transition key
    val content: BookCardContent,         // cover, title, subtitle, badges — always present
    val variant: BookCardVariant,         // sealed: Grid | CoverOnly | Row(density) |
                                          //         Rail | Featured(backdrop) | Tile
    val decorations: BookCardDecorations,  // progress?, selection?, trailing?
)
```

The component does `when (model.variant)` and dispatches to private per-variant layout composables; the
public surface stays one symbol. Variant-specific data rides on the variant, so illegal states are
unrepresentable (`CoverOnly` cannot carry a subtitle). A variant that needs its own metrics gets a lookup
keyed off the variant, never a branch inside the layout: `ShareCardDimensions.forContent(content)` and
`CoverDimensions.forVariant` (one surface-named `CoverVariant` entry per surface). Entries that share a
metric tuple **stay separate**, so tuning one surface cannot move another.

**R3 — Stability is a hard requirement.** Every collection in a UI model is `ImmutableList` /
`ImmutableSet` / `ImmutableMap` (`kotlinx-collections-immutable`). There is no
`stabilityConfigurationFile` in this build and none is to be added. A lambda field
(`formatter: (Int) -> String`) is forbidden too: replace it with a sealed descriptor the component resolves
internally, sized to what call sites need (`StatNumberFormat`'s `Grouped` / `Plain` / `Decimal(digits)`).

**R4 — Presentation-ready values only.** A UI model holds formatted strings, resolved icon tokens and
computed fractions — no domain type, no `Instant`, no `Duration`, no enum from `:core:domain`.
`DeadlineBadge(status: DeadlineStatus)` becomes `Badge(model: BadgeUiModel)`, with the label and tone
decided by the feature. When a component seems to need a domain type, give it a library-owned model of the
same shape (`RichTextUiModel` in place of `ReviewDocument`). R4 governs `:core:component`, not a feature's
own presentation models: a feature-local model may carry a domain enum as an event payload (`PaletteChoice`
carries a `ColorPalette`) as long as R9 holds — the render neither maps nor labels it, only forwards it.

**R5 — Every UI model ships preview fixtures.**

```kotlin
data class ChipUiModel(…) {
    companion object : UiModelPreviews<ChipUiModel> {
        override val previews: ImmutableList<ChipUiModel> = persistentListOf(…)
    }
}
```

The companion implements `UiModelPreviews<T>` (`gallery/`). The fixtures are both the Component Gallery's
data and the mappers' expected outputs. Cover the variants, not the permutations: one fixture per `variant`
branch, plus one per decoration that changes the anatomy (a truncating title, a missing cover, a selected
state). A fixture that differs only in string content is noise.

**R6 — Mapper placement: feature-local first, promoted on the second consumer.** A
`Book -> BookCardUiModel` mapper starts in the consuming feature's `presentation/mapper/` and moves to
`:core:uibinding` when a second feature needs the **identical** mapping. Two features mapping differently
onto the same UI model (`explore` → `Rail`, `library` → `Grid`) keep two mappers, permanently.

**R7 — Shared-element keys travel in the model.** `bookCoverTransitionKey(editionId, bookId, surface)` stays
in `:core:designsystem` (`transition/SharedElementScopes.kt`) as a token. The resolved key string is a field
on the model's key (`BookCardKey`), computed by the mapper. A component never computes a transition key; it
does not know which surface it is on.

**R8 — The suffix is `*UiModel`.** Not `*Content` (already the name of a composable in every feature),
`*Model` or `*State`. The event type is `*Event`, the identity type `*Key`, the variant type `*Variant`.
Sub-models that only group fields of one model take its prefix (`BookCardContent`, `BookCardDecorations`).

**R9 — Mapping happens in the ScreenModel. A composable never calls a mapper.** A domain → UI mapping runs
once, off the composition: in the ScreenModel, an `Action`, a `Collector`, or a dependency injected into one
of those. The `UiState` carries the result; a component never receives a domain value to convert.

```kotlin
// WRONG — the render maps
VerdictBlock(review = state.book?.userBook?.reviewDocument?.toRichTextUiModel())

// RIGHT — the collector mapped; the render forwards
VerdictBlock(review = state.verdictReview)
```

Carve-out: reading a **platform** signal in composition is not mapping. `ThemeMode.isDark()` (which resolves
through `isSystemInDarkTheme()`), resolving a `CompositionLocal`, measuring a window size class and reading
the platform brightness stay where they are. The rule is about domain data.

**R10 — A UI model is never built in composition. It arrives on the `UiState`.** A composable never
constructs a UI model — not from domain data, feature state or literals, and not inside a `remember`. The
model is assembled where the state is assembled and reaches the render as a `UiState` field.

```kotlin
// WRONG — the render assembles the model
StatNumber(model = StatNumberUiModel(value = state.pagesRead.toDouble()))
TopBar(model = remember(title) { TopBarUiModel(title = title) })

// RIGHT — the collector assembled it; the render forwards
StatNumber(model = state.pagesReadStat)
TopBar(model = state.topBar)
```

- **A composition-scoped value is a variant, not a nullable field.** When a loose parameter resolves from
  something only composition has (a theme lookup, a platform read), name the possibilities as a variant the
  component resolves internally (`DeadlineSummaryTone`'s `OnSurface` / `OnHeroBackdrop`, not a nullable
  `Color`).
- **One model per identity unless it varies by surface.** A collector building per-item models keeps one map
  keyed by identity; a cover carries a per-rail shared-element key and needs one map per rail, a badge with
  no surface-scoped data needs exactly one.
- **Not yet satisfied by the whole library.** The retrofit is a dedicated stage in
  `docs/working/component-library-migration/steps/s11-contract-retrofit.md`. Write new components to R10 and do not take an
  existing call site as precedent. That stage, not this rule, settles three edges: copy the library owns and
  resolves from its own `composeResources` (`offlineBannerUiModel()`); preview fixtures and the gallery, which
  construct models by definition; and a model that depends on a value only composition has (scroll
  position, window size class, a `CompositionLocal`).

**R11 — The signature is the model, the event lambda and the modifier. Nothing else.** Anything a call site
would otherwise pass — a `TextStyle`, a `Color`, `maxLines`, an autosize spec, a particle count, a
duration — is a property of the UI model. Where the treatment differs per surface, name the surfaces in a
variant and resolve them in a table (as R2); where it does not, the component decides.

```kotlin
// WRONG — the surface hands the component its treatment
StatNumber(model = state.pagesRead, style = editorialTypography.statHero, color = onSurface)
RichText(model = state.review, style = editorialTypography.body, maxLines = 8)

// RIGHT — the treatment rides on the model
StatNumber(model = state.pagesRead)     // model.variant / model.tone decide the face and the ink
RichText(model = state.review)          // model.variant decides the face and the line cap
```

- **Allowed: a trailing `content` slot** (§ 7.1). It is a composition hole the caller fills with a
  composable, which a data model cannot hold (`VerdictSheet`'s `cover` slot).
- **Allowed: Compose plumbing that cannot live in an immutable value** — `modifier`, or a hoisted mutable
  state object shared with the scaffold (`TopAppBarScrollBehavior`). The test: could it be serialised with
  the rest of the model? A `TextStyle` could, a `ScrollState` could not.
- **Not yet satisfied by the whole library.** Fixed in the same S11 retrofit as R10. Current holdouts:
  `RichText` (`style`, `color`, `maxLines`, `overflow`, `onClick`, `onTextLayout`), `StatNumber` (`style`,
  `color`, `autoSize`, `maxLines`), `ClickableText` (`style`, `inlineContent`), `MarkAsReadBurst` (`color`,
  `secondaryColor`), `CoverlessTitleCover` (`title`), and the R1 holdouts `VerdictBlock` / `VerdictSheet`.
  Write new components to R11; an existing signature is not licence.

### 7.3 Reference implementation — `ShareCard`

`core/component/share/` is the contract's worked example. Read it before writing a new component.

```kotlin
// A sealed model whose members are the variants
sealed interface ShareCardUiModel

// Presentation-ready primitives only — no domain types
data class BookShareCardUiModel(
    val coverUrl: String?,
    val title: String,
    val author: String,
    val communityRating: Double?,
    …
) : ShareCardUiModel

// ONE public symbol; `when` dispatch to private per-variant bodies
@Composable
fun ShareCard(content: ShareCardUiModel, modifier: Modifier = Modifier) { … }
```

It shows R1 (no callbacks — a share card is inert), R2 (sealed variant, one public symbol, private bodies,
per-variant sizing table), R3, R4 (`RichTextUiModel` on the reading-update card), R5 (its
`UiModelPreviews` companion feeds both the gallery and its `@Preview` functions) and R8. A share card sets
its own width, because it is an export artefact at fixed dimensions. When a component genuinely owns its
metrics, put them in a per-variant table and let the surrounding surface adapt (the gallery pans it); never
add a "gallery mode" parameter.

### 7.4 What the build enforces

- **`checkModuleGraph`** fails if `:core:component` declares a dependency on anything but
  `:core:designsystem`, or on Koin / Voyager / Apollo coordinates.
- **detekt `ForbiddenImport`**, scoped to `**/core/component/**`, fails on an import of `org.koin.**`,
  `cafe.adriel.voyager.**`, `com.apollographql.apollo.**` or `nl.rhaydus.softcover.core.domain.**`. It
  checks usage, which the coordinate check cannot: the convention plugin puts `koin-core` on every module.

Consequences:

- **A component cannot reach for DI, navigation or the network.** Everything it needs arrives in its model
  or its event lambda.
- **A component cannot take a domain type**, so R4 is a compile-time fact in `:core:component` and a review
  matter for components still in a feature.
- **detekt does not scan `iosMain`.** Components live in `commonMain`; one in `iosMain` is not gated.
- **R9 has no mechanical gate.** It is enforced by review only (§ 7.7).
- **A `UiState` field can be declared and never populated with every gate passing.** After a state change,
  grep that every new field is both declared and assigned; prefer deriving a field with `map` over writing
  it from a second place.

### 7.4a Known exceptions to R1

- **`VerdictSheet`, `VerdictBlock` and `RichTextFormattingToolbar`** still take loose parameters and N
  callbacks; they are fixed with the verdict family's sheet-chrome consolidation. Do not copy their shape.
  `VerdictSheet`'s `cover` slot stays a slot on purpose (R11).
- **Neither migrated sheet has a gallery entry**, because a fixture tile cannot render a modal inline. Their
  models still ship `previews` (R5), and the components' own `@Preview` functions render from that list. A
  "tap to open" fixture is a gallery feature to design once for all sheets, not per sheet.

### 7.5 The Component Gallery

The gallery is the library's **visual acceptance surface**: every UI model's `previews`, rendered across
both brightnesses and all five spine colours (§ 2.1). There are no Compose UI tests and none are planned —
the gallery plus per-model unit tests are the coverage.

It is a **shipped easter egg**, rendered on Android, iOS and desktop from `commonMain`:

- **Seven taps on the About screen's version footer**, each within two seconds of the last (a longer pause
  resets the count). The seventh tap confirms with a haptic and pushes the gallery. The footer gives no
  hint.
- **The registry is data in the library.** `GalleryRegistry` (`:core:component/gallery/`) pairs each
  component with its family and fixtures. A new component adds its entry in the same change. An empty
  family is filtered out.
- **The screen lives in `feature:settings`**, which pushes it itself from the tap gesture. It has no nav
  destination and no sidebar row.
- **It is a user-visible surface**, so it gets a design pass like any other screen.

**Anatomy.** An intro line, then a "Preview controls" region opened by an `EditorialSectionHeader` with two
override chip rows, **Brightness** (`ThemeMode`) and **Spine colour** (`ColorPalette`), each a wrapping row
of `Chip`s under a bar-less `eyebrowSmall` sub-label. Both rows always render, even with an empty registry.
Tapping the selected chip clears the override back to the app's own setting (tap-to-toggle-off), and a
short gloss line under the rows says so. Once the registry has a family, a third chip row filters the
sections to one family, with the same toggle-off rule. All three rows stay in the app's own look, outside
the overridden theme.

Below sits the **themed region**: a `SoftcoverTheme` resolved from the overrides (each falling back to
`LocalThemeConfiguration` when `null`; dynamic colour forced off whenever a palette override is set),
wrapping a hairline-bordered, rounded surface filled with that theme's `background`. Inside, each visible
family gets an `EditorialSectionHeader`, then per component its name and blurb, then each fixture as a small
label over a tonal tile that constrains the fixture's width and lets it size its own height.

With the registry empty, the frame shows the empty-state variant of the
[editorial quote](patterns/editorial.md#editorial-quote) pattern (the swaying low-alpha quote glyph, as on
the empty Reading and Hidden-suggestions screens), with copy explaining that the library fills one
migration stage at a time. It renders inside the themed region, so it still shows the chosen brightness and
palette.

### 7.6 Where consolidation is the wrong call

Families consolidate on **shared anatomy, not shared category name.**

| Collapse | Keep separate | Why |
|---|---|---|
| The four `*InfoCallout`s → one `Callout` with a tone variant | `TopBar` and `SearchTopBar` | The search bar's focus contract (§ 3.1) — caller-driven `focused`, intents for activate/dismiss/clear — has no counterpart on the plain bar. Merged, it is one component with two disjoint parameter sets. |
| The 21 book cards → one `BookCard` with a sealed variant | The 18 sheet **bodies** | Consolidate sheet *chrome* (`SheetScaffold` / `SheetHeader` / `SheetRow` / `SheetFooter`) and leave each body a feature composable built from those parts. `LibraryFilterSheet` and `TagEditorBottomSheet` share no anatomy. |

The test: **can the two share a layout, with the variant choosing only which parts appear?** If yes, one
component. If the variant would choose *which parameters mean anything*, two components.

### 7.7 Rationale (optional reading)

Read this only when a rule looks arbitrary; each note prevents a wrong turn that has been taken before.

- **R1 is a performance rule.** A per-item lambda allocates on every recomposition and defeats skipping
  across a 500-item grid; one hoisted lambda plus a key does not.
- **R2's per-surface table is also the audit.** Loose metric parameters drift apart across call sites
  unseen; as entries in one table, every surface's metrics are one readable list.
- **R3 is compiler-checked on purpose.** A stability config file drifts silently; an `ImmutableList`
  parameter does not. A `List` field makes the whole model unstable, so every card in a grid recomposes
  every frame; a lambda field is never equal to itself and does the same.
- **R9 has three reasons.** Layering: a mapper in a composable puts *what* to show inside the code that
  decides *how*. The reverse direction: an editor's output travels back out to be persisted, and if the
  `Action` maps there is one place for that. Performance: a mapper in composition re-runs and allocates a
  fresh model on every recomposition, which breaks the equality check that lets Compose skip; `remember`
  hides the symptom. A detekt rule for R9 was considered and declined as hard to express precisely without
  false positives — do not add one.
- **R10 goes further than R9** because the model exists to be the single, testable description of what a
  component shows. One assembled at the call site cannot be asserted in a unit test or reused by a second
  surface, which leaves the model decorative.
- **R11: a loose render parameter is drift with a default value.** It is invisible to the model, so nothing
  can enumerate or test it, and surfaces that should look alike diverge.
