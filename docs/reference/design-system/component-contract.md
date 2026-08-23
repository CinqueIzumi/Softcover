# Design System — Component contract

## 7. Component contract

Every component in `:core:component` is driven by a **UI model**. This section is the contract that
makes the library one system rather than 132 individually-reasonable components: what a component's
signature looks like, what its model may hold, and where the domain → UI mapping lives.

It is normative. A component that does not satisfy R1–R8 does not belong in `:core:component`, and
two of the rules are enforced by the build rather than by review (see § 7.4).

> **Read `share/` before writing a new component.** `ShareCard` is the reference implementation —
> shipped, working, and already the shape described below. § 7.3 walks it.

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

Three parameters, in that order. A component that renders nothing interactive drops `onEvent`; a
component that needs a slot takes a trailing `content: @Composable () -> Unit`. Nothing else is
added to the front of the list — no loose `title: String` beside the model, no second callback.

### 7.2 The rules

**R1 — One sealed event lambda, never N callbacks.**

`onEvent: (BookCardEvent) -> Unit`, with `BookCardEvent` a sealed interface (`Click`, `LongClick`,
`ToggleSelected`, `Bookmark`, `Overflow`) whose members carry the model's key. The caller hoists one
lambda and reads the key off the event; it does not build a fresh `onClick = { … }` per item.

This is a performance rule, not a taste rule: a per-item lambda allocates on every recomposition and
defeats skippability across a 500-item library grid. One hoisted lambda plus a key does not.

**R2 — Sealed variants, not a flat enum beside nullable soup.**

```kotlin
data class BookCardUiModel(
    val key: BookCardKey,                 // identity + shared-element transition key
    val content: BookCardContent,         // cover, title, subtitle, badges — always present
    val variant: BookCardVariant,         // sealed: Grid | CoverOnly | Row(density) |
                                          //         Rail | Featured(backdrop) | Tile
    val decorations: BookCardDecorations,  // progress?, selection?, trailing?
)
```

The component does `when (model.variant)` and dispatches to private per-variant layout composables.
**The public surface stays one symbol.** Variant-specific data rides on the variant, so illegal
states are unrepresentable — `CoverOnly` cannot carry a subtitle, `Featured` cannot carry a selection
circle. A flat enum plus a bag of nullables would allow both, and every caller would have to remember
which combinations are real.

Where a variant needs its own metrics, give it a lookup keyed off the variant rather than branching
inside the layout — `ShareCardDimensions.forContent(content)` is the pattern to copy.

**R3 — Stability is a hard requirement.**

Every collection in a UI model is typed as `ImmutableList` / `ImmutableSet` / `ImmutableMap`
(`kotlinx-collections-immutable`). A `List` field makes the whole model unstable, which makes every
card in every grid recompose every frame.

This is deliberately a *compiler-checked* rule rather than a `stabilityConfigurationFile` entry: the
config file drifts silently, an `ImmutableList` parameter does not. There is no stability config in
this build and none is to be added.

**R4 — Presentation-ready values only.**

A UI model holds formatted strings, resolved icon tokens, and computed fractions. It holds no domain
type, no `Instant`, no `Duration`, no enum from `:core:domain`. `DeadlineBadge(status: DeadlineStatus)`
becomes `Badge(model: BadgeUiModel)`, where the *feature* already decided the label and the tone.

The library's job is to render; deciding *what* to render is the feature's. When a component appears
to need a domain type, what it actually needs is a library-owned model of the same shape — see
`RichTextUiModel`, which exists precisely because six components were taking `ReviewDocument` straight
from `:core:domain`.

**R5 — Every UI model ships preview fixtures.**

```kotlin
data class ChipUiModel(…) {
    companion object : UiModelPreviews<ChipUiModel> {
        override val previews: ImmutableList<ChipUiModel> = persistentListOf(…)
    }
}
```

The companion implements `UiModelPreviews<T>` (`:core:component`, `gallery/`), so R5 is a compile
error to forget rather than a review note. The fixtures are the Component Gallery's data **and** the
mappers' expected outputs, so a component's preview set and its test set cannot diverge.

Cover the variants, not the permutations: one fixture per `variant` branch, plus one for each
decoration that changes the anatomy (a long title that must truncate, a missing cover, a selected
state). A fixture that only differs by string content is noise in the gallery.

**R6 — Mapper placement: feature-local first, promoted on the second consumer.**

A `Book -> BookCardUiModel` mapper starts in the consuming feature's `presentation/mapper/`. It moves
to `:core:uibinding` **when a second feature needs the identical mapping** — that duplication is the
promotion signal.

Two features needing *different* mappings onto the same UI model is the system working as intended,
not a case for promotion: `explore` mapping a trending book to a `Rail` card and `library` mapping a
shelved book to a `Grid` card are two mappers, permanently.

**R7 — Shared-element keys travel in the model.**

`bookCoverTransitionKey(editionId, bookId, surface)` stays in `:core:designsystem`
(`transition/SharedElementScopes.kt`) — it is a token, not a component. The *resolved* key string is a
field on `BookCardKey`, computed by the mapper. A component never computes a transition key, because
a component does not know which surface it is on.

**R8 — The suffix is `*UiModel`.**

Not `*Content`, not `*Model`, not `*State`. `Content` is already the name of a composable in every
feature (`AboutContent`, `RoadmapContent`, `ProfileContent`, `EditionBottomSheetContent`, …), and
using it for a data type as well makes an import list unreadable.

The event type is `*Event`; the identity type is `*Key`; the variant type is `*Variant`. Sub-models
that exist only to group fields of one model take that model's prefix (`BookCardContent`,
`BookCardDecorations`).

### 7.3 Reference implementation — `ShareCard`

`core/designsystem/presentation/share/` is a shipped instance of this contract under an older name.
Read it before writing a new component.

```kotlin
// A sealed model whose members are the variants
sealed interface ShareContent

// Presentation-ready primitives only — no domain types
data class BookShareContent(
    val coverUrl: String?,
    val title: String,
    val author: String,
    val communityRating: Double?,
    …
) : ShareContent

// ONE public symbol; `when` dispatch to private per-variant bodies
@Composable
fun ShareCard(content: ShareContent, modifier: Modifier = Modifier) { … }
```

It satisfies R1 (no callbacks at all — a share card is inert), R2 (sealed variant, single public
symbol, private bodies, per-variant sizing table), and R4 for five of its six variants. Its two gaps
are being closed as it moves into `:core:component`: the `*ShareContent` naming (R8) and the
`ReviewDocument` import in the quote card (R4).

### 7.4 What the build enforces

Two rules are gates, not conventions, because convention did not hold this boundary before:

- **`checkModuleGraph`** fails if `:core:component` declares a dependency on anything but
  `:core:designsystem`, or on Koin / Voyager / Apollo coordinates. It validates declared
  *coordinates*.
- **detekt `ForbiddenImport`**, scoped to `**/core/component/**`, fails on an import of
  `org.koin.**`, `cafe.adriel.voyager.**`, `com.apollographql.apollo.**`, or
  `nl.rhaydus.softcover.core.domain.**`. It validates *usage*, which the coordinate check cannot
  see — `koin-core` is injected into every module by the convention plugin, and is on its own enough
  to implement `KoinComponent`.

Consequences worth internalising before designing a component:

- **A component cannot reach for DI, navigation, or the network.** Anything it needs arrives in its
  model or its event lambda.
- **A component cannot take a domain type**, so R4 is a compile-time fact for `:core:component` and
  a review matter only for components still living in a feature.
- **detekt does not scan `iosMain`** (no type resolution for native targets). Components live in
  `commonMain`; do not put one in `iosMain` and assume the gate saw it.

### 7.5 The Component Gallery

The gallery is the library's **visual acceptance surface**: every UI model's `previews` fixtures,
rendered across both brightnesses and all five spine colours (§2.1). There are no Compose UI tests in
this repo and none are planned — the gallery plus per-model unit tests are the coverage.

It is a **shipped easter egg**, not a debug build feature, so it renders on Android, iOS and desktop
alike from `commonMain`:

- **Reached by seven taps on the version footer** on the About screen, within two seconds of each
  other; the count resets on any longer pause. The seventh tap confirms with a haptic and pushes the
  gallery. Nothing about the footer hints at it — that is the point.
- **The registry is data and lives in the library.** `GalleryRegistry` in `:core:component/gallery/`
  pairs each component with its family and its fixtures. Adding a component to the gallery is one
  entry in that list, in the same change as the component.
- **The screen lives in `feature:settings`.** Screens belong to features, and `:core:component` is
  banned from Voyager.
- **It is a user-visible surface**, so it gets a design pass and an entry in this doc like any other
  screen.

**Anatomy, as built.** An intro line names what the surface is, then two override chip rows —
**Brightness** (`ThemeMode`) and **Spine colour** (`ColorPalette`) — each a wrapping row of `PillChip`s
under a bar-less `eyebrowSmall` sub-label, sitting inside one `EditorialSectionHeader`-opened "Preview
controls" region. Both rows render unconditionally, even with an empty registry, because they retint
the framed region below on their own and that retinting *is* the point of the override. Selecting an
already-selected chip clears the override back to the app's own current setting (tap-to-toggle-off,
per the action contract) rather than needing a separate "app default" chip; a short gloss line under
the rows spells this out, since the affordance itself is invisible until tried. Once the registry has
at least one family, a third chip row filters the sections below to one family at a time, following the
same tap-to-toggle-off rule. All three rows are chrome and stay in the app's own current look — they
are never inside the overridden theme.

Below the controls sits the **themed region**: a `SoftcoverTheme` resolved from the override state
(each override falling back to `LocalThemeConfiguration` — the app's own setting — when `null`, and
dynamic colour forced off whenever a palette override is in effect, since dynamic colour would
otherwise silently replace it right back), wrapping a hairline-bordered, rounded surface filled with
that theme's own `background`. The border and independent fill are what let the surface read as a
framed spread of paper distinct from the page above it, rather than as a continuation of the chrome —
which is also why the controls above are deliberately outside this boundary: only the previewed
material steps into the chosen brightness and palette. Inside the frame, each visible family gets an
`EditorialSectionHeader`, then per component its name and blurb, then each preview fixture as a small
label over a tonal tile that constrains the fixture's width and lets it size its own height — a chip
sizes to a line, a card sizes to its own body, and neither is stretched or clipped to match a
neighbour.

With the registry empty (its state as of the S2 contract/gallery scaffold), the frame shows the
documented empty-state variant of the editorial quote pattern (§5) — the same low-alpha, swaying quote
glyph the empty Reading and Hidden-suggestions screens use — with copy that explains the library is
filling one migration stage at a time, so the frame reads as a deliberate moment rather than an
unfinished placeholder. Because the empty state renders inside the themed region too, it still shows the
chosen brightness and palette even with nothing yet to preview.

### 7.6 Where consolidation is the wrong call

Families consolidate on **shared anatomy, not shared category name.** Two components with the same
noun in their name but disjoint parameter sets are two components.

| Collapse | Keep separate | Why |
|---|---|---|
| The four `*InfoCallout`s → one `Callout` with a tone variant | `SoftcoverTopBar` and `SoftcoverSearchTopBar` | The search bar's focus contract (§3.1) — caller-driven `focused`, intents for activate/dismiss/clear — has no counterpart on the plain bar. Merged, it is one component with two disjoint parameter sets. |
| The 21 book cards → one `BookCard` with a sealed variant | The 18 sheet **bodies** | Consolidate sheet *chrome* (`SheetScaffold` / `SheetHeader` / `SheetRow` / `SheetFooter`) and leave each body a feature composable built from those parts. `LibraryFilterSheet` and `TagEditorBottomSheet` share no anatomy; a variant enum over them would be a component in name only. |

The test to apply: **can the two share a layout, with the variant choosing only which parts appear?**
If yes, one component. If the variant would choose *which parameters mean anything*, two components.
