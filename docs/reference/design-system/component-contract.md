# Design System — Component contract

## 7. Component contract

Every component in `:core:component` is driven by a **UI model**. This contract fixes what a component's
signature looks like, what its model may hold, and where the domain → UI mapping lives.

It is normative. A component that does not satisfy R1–R12 does not belong in `:core:component`. The build
gates R10, R4 and the ban on DI, navigation and the network (§ 7.4); the standing exceptions are in § 7.4a.

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
    data class PagesSubmitted(val page: String) : ProgressSheetEvent
    …
    data object MarkAsReadRequested : ProgressSheetEvent
    data object Dismissed : ProgressSheetEvent
}
```

- **One hoisted handler per list.** `BookCard` events carry a `BookCardKey`. A surface holds its cards as a
  `BookCardSet<P>` on `UiState`: the cards paired with the payload each key resolves to, built in one pass by
  a collector or mapper (`toBookCardSet()`). It builds one handler with
  `rememberBookCardEventHandler(set, onEvent)`, where `onEvent` receives the event plus the set's payload for
  its key. An event whose key the set does not hold is dropped. `rememberPrefetchingBookCardEventHandler`
  (`:core:presentation`) adds the book-detail prefetch on press. A lazy-list key comes from
  `BookCardKey.lazyKey` (`"book:12"`, `"edition:3"`, `"series:7"`): Bundle-saveable and unique across kinds.
- **Screen state the component renders belongs on the model**, not in a second parameter (the progress
  sheet's selected tab is `ProgressSheetUiModel.selectedTab`). Its stored home stays in the feature's
  `UiState`, and the model field is derived from it in one collector, so there is one writer.
- **Handle every branch.** An exhaustive `when` makes a missing branch a compile error, but not an empty
  one: a branch left blank to "wire later" passes every gate, `EmptyFunctionBlock` included, because detekt
  does not see `when` arms.

**R2 — Sealed variants, not a flat enum beside nullable fields.**

```kotlin
data class BookCardUiModel(
    val key: BookCardKey,                 // identity
    val variant: BookCardVariant,         // sealed: CoverOnly | Column | Row(size); each carries its own text
    val decorations: BookCardDecorations,  // progress? (Reserved | Value), selection? (Multiple | Single), press behaviour
    val isLoading: Boolean,               // R12
)
```

The component does `when (model.variant)` and dispatches to private per-variant layout composables; the
public surface stays one symbol. Variant-specific data rides on the variant, so illegal states are
unrepresentable (`CoverOnly` cannot carry a subtitle). There is one variant per distinct treatment, named
after the look, not the surface: surfaces that share a look share a variant (`CoverVariant.Flat`). A
variant's metrics are a private `when` beside the component, never a public table or a branch inside the
layout.

**R3 — Stability is a hard requirement.** Every collection in a UI model is `ImmutableList` /
`ImmutableSet` / `ImmutableMap` (`kotlinx-collections-immutable`). There is no
`stabilityConfigurationFile` in this build and none is to be added. A lambda field
(`formatter: (Int) -> String`) is forbidden too: replace it with a sealed descriptor the component resolves
internally, sized to what call sites need (`StatNumberFormat`'s `Grouped` / `Plain` / `Decimal`).

**R4 — Presentation-ready values only.** A UI model holds formatted strings, resolved icon tokens and
computed fractions — no domain type, no `Instant`, no `Duration`, no enum from `:core:domain`. A deadline
status renders as `Badge(model: BadgeUiModel)`, with the label and tone decided by the feature's mapper.
When a component seems to need a domain type, give it a library-owned model of the same shape
(`RichTextUiModel` in place of `ReviewDocument`). A component that echoes a caller's identity back in its
events takes a generic key: `ColorPalettePreviewTileUiModel<K>` is built by `feature:settings` as
`ColorPalettePreviewTileUiModel<ColorPalette>`, and the component only forwards the key (R9).
`kotlinx.datetime.LocalDate` is an allowed value type: a plain calendar day carries no domain vocabulary
(`DatePickerUiModel.initialDate`).

**R5 — A UI model that is rendered or tested ships preview fixtures.**

```kotlin
data class ChipUiModel(…) {
    companion object : UiModelPreviews<ChipUiModel> {
        override val previews: ImmutableMap<String, ChipUiModel> = persistentMapOf("Idle" to …)
    }
}
```

The companion implements `UiModelPreviews<T>` (`gallery/`), keyed by the label the Component Gallery shows;
`@Preview`s read a fixture by that label. It is required wherever the model is rendered in the gallery or a
`@Preview`, or its fixtures are read by a test — a model nothing reads carries no companion. The fixtures
are both the Component Gallery's data and the mappers' expected outputs. Cover the variants, not the
permutations: one fixture per `variant` branch, plus one per decoration that changes the anatomy (a
truncating title, a missing cover, a selected state). A fixture that differs only in string content is
noise.

**R6 — Mapper placement: feature-local first, promoted on the second consumer.** A
`Book -> BookCardUiModel` mapper starts in the consuming feature's `presentation/mapper/` and moves to
`:core:uibinding` when a second feature needs the **identical** mapping. Two features mapping differently
onto the same UI model (`explore` → `Column`, `library` → `Row`) keep two mappers, permanently.

**R7 — Shared-element keys travel in the model.** `bookTransitionKey(element, bookId, surface)`
returns a `BookTransitionKey`, built only by that builder in `:core:component` (the type's constructor is
internal). A key is built from the book id, never an edition id, plus a `surface` when the same book can
appear in several visible carousels on one screen. The mapper resolves a `BookTransitionKey` and stores it on the
model of the component that runs the transition (`CoverUiModel.sharedTransitionKey`,
`BookCardContent.titleTransitionKey`, `BookCardLinePart.Rating.transitionKey`). A component never computes a
transition key; it does not know which surface it is on.

**R8 — The suffix is `*UiModel`.** Not `*Content` (already the name of a composable in every feature),
`*Model` or `*State`. The event type is `*Event`, the identity type `*Key`, the variant type `*Variant`.
Sub-models that only group fields of one model take its prefix (`BookCardContent`, `BookCardDecorations`).

**R9 — Mapping happens in the ScreenModel. A composable never calls a mapper.** A domain → UI mapping runs
once, off the composition: in the ScreenModel, an `Action`, a `Collector`, or a dependency injected into one
of those. The `UiState` carries the result; a component never receives a domain value to convert.

```kotlin
// WRONG — the render maps
RichText(model = state.book?.userBook?.reviewDocument?.toRichTextUiModel())

// RIGHT — the collector mapped; the render forwards
VerdictBlock(model = state.verdictBlock, onEvent = onEvent)
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
  component resolves internally (`BookCardEyebrow.Tone`'s `Accent` / `Error`, not a nullable
  `Color`).
- **One model per identity unless it varies by surface.** A collector building per-item models keeps one map
  keyed by identity; a cover carries a per-rail shared-element key and needs one map per rail, a badge with
  no surface-scoped data needs exactly one.
- **A model chosen by a value only composition has arrives once per outcome.** For scroll position, a window
  size class or a `CompositionLocal`, the `UiState` carries one model per outcome and the render picks one; it
  never builds or `.copy`s a model from the signal. Book detail's mobile bar holds `topBar` (`Opaque`, titled)
  and `overMediaTopBar` (`OverMedia`, untitled) and picks by whether the hero has scrolled away.
- **Library-owned copy arrives through a seam.** Copy a component owns and resolves from its own
  `composeResources` (the offline banner and offline screen) is exposed as the `OfflineCopy` interface
  (`state/`), whose methods are `suspend` and return the models. `ResourceOfflineCopy` is the default
  implementation, bound once in `presentationModule` (`:core:component` itself uses no DI). A collector
  reads it from its `Dependencies` and puts the result on the `UiState`, so the render never builds the
  model and a collector test can substitute the copy.
- **Gated by `softcover:UiModelBuiltInRender`.** Preview fixtures, the gallery and debug screens are excluded
  by path. A `*UiState` member may look up models the collectors stored but never builds one; a constructor,
  `.copy` or model-returning call in a member or getter that returns a model is reported. A reducer that
  returns the `UiState` itself (`withName`) is not. A recorded § 7.4a exception carries
  `@ModelBuiltInComposition(reason)`, never `@Suppress`.

**R11 — The signature is the model, the event lambda and the modifier. Nothing else.** Anything a call site
would otherwise pass — a `TextStyle`, a `Color`, `maxLines`, an autosize spec, a particle count, a
duration — is a property of the UI model. Where the treatment differs per surface, name the looks in a
variant and resolve each in a private per-variant `when` (as R2); where it does not, the component decides.

```kotlin
// WRONG — the surface hands the component its treatment
StatNumber(model = state.pagesRead, style = editorialTypography.statHero, color = onSurface)
RichText(model = state.review, style = editorialTypography.body, maxLines = 8)

// RIGHT — the treatment rides on the model
StatNumber(model = state.pagesRead)     // model.variant / model.tone decide the face and the ink
RichText(model = state.review)          // model.variant decides the face and the line cap
```

- **Allowed: a trailing `content` slot** (§ 7.1). It is a composition hole the caller fills with a
  composable, which a data model cannot hold.
- **Allowed: Compose plumbing that cannot live in an immutable value** — `modifier`, a platform
  `WindowInsets` the component pads inside its own container (`Banner(windowInsets = …)`), or a hoisted
  mutable state object shared with the scaffold (`TopAppBarScrollBehavior`). The test: could it be serialised with
  the rest of the model? A `TextStyle` could, a `ScrollState` could not.

**R12 — Loading is the component's own state, never a separate skeleton.** A component that renders data
the screen may not have yet takes `isLoading` (on the model, or as a parameter on a data-free primitive)
and keeps its real layout while loading. Unknown text renders through `SoftcoverText` with a stand-in;
other unknown leaves sit under `Modifier.skeletonMask(isLoading)` at their real size. A list of unknown
length renders a fixed count of its real items, each loading: the UiState carries that list of loading
models, built by the feature's mapper / collector, and the render iterates it like the loaded list.
No per-surface skeleton composable, no skeleton variant, no `SkeletonCrossfade` from a skeleton layout to
the real one (`Cover`'s crossfade from its own masked slot to the image is the real layout). A model's
`previews` include a loading fixture; a data-free primitive's gallery fixtures do.

### 7.3 Reference implementation — `ShareCard`

`core/component/share/` is the contract's worked example. Read it before writing a new component.

```kotlin
// A sealed model whose members are the variants
sealed interface ShareCardUiModel

// Presentation-ready primitives only — no domain types
data class BookShareCardUiModel(
    val cover: CoverUiModel,
    val title: String,
    val author: String,
    val statsLine: String?,
    val description: String?,
) : ShareCardUiModel

// ONE public symbol; `when` dispatch to private per-variant bodies
@Composable
fun ShareCard(model: ShareCardUiModel, modifier: Modifier = Modifier) { … }
```

It shows R1 (no callbacks — a share card is inert), R2 (sealed variant, one public symbol, private bodies,
private per-variant sizing `when`), R3, R4 (`RichTextUiModel` on the reading-update card), R5 (its
`UiModelPreviews` companion feeds both the gallery and its `@Preview` functions) and R8. A share card sets
its own width, because it is an export artefact at fixed dimensions. When a component genuinely owns its
metrics, resolve them in a private per-variant `when` and let the surrounding surface adapt (the gallery
pans it); never add a "gallery mode" parameter.

### 7.4 What the build enforces

- **detekt `softcover:UiModelBuiltInRender`** (R10) fails on a UI model built in composition.
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
- **R9 has no mechanical gate.** It is enforced by review only.

### 7.4a Known exceptions

- **A data-free primitive (`Divider`) takes defaulted parameters and has no UI model.** The call site passes
  only the overrides.
- **`SoftcoverText` takes `style` and `color` (R11) and has no model.** It is the text leaf other components
  style, so it is a data-free primitive like `Text`: the caller passes its own model's string inline. Styling
  goes through `style` alone; `Text`'s loose font parameters are not exposed. The exemption covers this
  primitive only; a component that renders data still carries its treatment on its model.
- **Debug screens (`app/src/debug/`) are exempt from R10.** They may build UI models in composition or hold
  file-level `*UiModel` constants.
- **A modal host's gallery entry renders its `internal` body inline** inside `GallerySheetFrame`, without the
  modal container; a dialog's entry draws its content (the date picker draws its calendar through
  `DatePicker`). A modal host's model ships `previews` where R5 requires it, and its own `@Preview`
  functions render from that map.

### 7.5 The Component Gallery

The gallery is the library's **visual acceptance surface**: every UI model's `previews`, rendered across
both brightnesses and all five spine colours (§ 2.1). There are no Compose UI tests; the gallery is the
visual check. Unit tests cover model logic and fixture coverage, never copied style
values or defaults (`.claude/rules/tests.md`).

It is a **shipped easter egg**, rendered on Android, iOS and desktop from `commonMain`:

- **Seven taps on the About screen's version footer**, each within two seconds of the last (a longer pause
  resets the count). The seventh tap confirms with a haptic and pushes the gallery. The footer gives no
  hint.
- **The registry is data in the library.** `GalleryRegistry` (`:core:component/gallery/`) pairs each
  component with its family and fixtures. A new component adds its entry in the same change.
- **The screen lives in `feature:settings`**, which pushes it itself from the tap gesture. It has no nav
  destination and no sidebar row.

**Anatomy.** An intro line, then a "Preview controls" region opened by a `SectionHeader` (Section register) with two
override chip rows, **Brightness** (`ThemeMode`) and **Spine colour** (`ColorPalette`), each a wrapping row
of `Chip`s under a bar-less `eyebrowSmall` sub-label. Tapping the selected chip clears the override back to
the app's own setting (tap-to-toggle-off), and a short gloss line under the rows says so. A third chip row
filters the sections to one family, with the same toggle-off rule. All three rows stay in the app's own
look, outside the overridden theme.

Below sits the **themed region**: a `SoftcoverTheme` resolved from the overrides (each falling back to
`LocalThemeConfiguration` when `null`; dynamic colour forced off whenever a palette override is set),
wrapping a hairline-bordered, rounded surface filled with that theme's `background`. Inside, each visible
family gets a `SectionHeader` (Section register), then per component its name and blurb, then each fixture as a small
label over a tonal tile that constrains the fixture's width and lets it size its own height. The registry is
never empty, so the frame has no empty state of its own.

### 7.6 Where consolidation is the wrong call

Families consolidate on **shared anatomy, not shared category name.**

| Collapse | Keep separate | Why |
|---|---|---|
| Every in-flow condition card is one `Callout` with a tone variant | `TopBar`, `EditorialTopBar` and `SearchTopBar` | The search bar's focus contract (`SearchTopBar` KDoc) — caller-driven `focused`, intents for activate/dismiss/clear — has no counterpart on the plain bar. `EditorialTopBar` is a plain row with a start-aligned editorial headline, not a Material app bar: no scroll behaviour, subtitle, surface or autosizing. Merged, each is one component with disjoint parameter sets. |
| Every book and series card is one `BookCard` with a sealed variant | Sheet **bodies** | Sheet *chrome* is shared (`SheetScaffold`, `SheetRowGroup`, `SheetActionRow`); each body is a feature composable built from those parts. `LibraryFilterSheet` and `TagEditorBottomSheet` share no anatomy. |

The test: **can the two share a layout, with the variant choosing only which parts appear?** If yes, one
component. If the variant would choose *which parameters mean anything*, two components.

### 7.6a Over-engineering

Every component, mapper, collector and state field takes the simplest shape that fits this contract. These
are rejected at design time, in briefs and in review:

- A style or metrics table, or an internal type, whose fields only one variant sets off a shared value, or
  that copies what the framework already supplies (`Surface`'s content colour, `LocalContentColor`).
- One layout split between a table and `when` branches. Variants whose anatomies differ get private
  per-variant bodies; one scaffold plus a small style only when the bodies are near-identical.
- Surface-named variants that mostly share one style tuple; a data-free primitive still behind a model
  (§ 7.4a).
- A model field no call site sets off its default, a sub-model wrapping one field, a variant no call site
  uses.
- A mapper, helper, handler factory or seam with one caller that adds a layer without removing repetition.
- State fields that duplicate each other, or one derived value written in more than one place.
