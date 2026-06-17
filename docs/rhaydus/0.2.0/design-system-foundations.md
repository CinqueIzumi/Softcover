# Design System Foundations

The shared, brand-agnostic skeleton for `nl.rhaydus` apps (Jetpack Compose, Material 3 Expressive, the
home-grown TOAD presentation architecture). This document captures only the structure that is genuinely
reusable across apps: the typography plumbing, the color-role model, the Material 3 Expressive setup, the
layout primitives, the component and TOAD conventions, and the maintenance rule.

It is deliberately empty of brand identity. Each app keeps its own `design-system.md` (or
`DESIGN_SYSTEM.md`) that names its palette, fonts, tone, motion choreography, illustrations, and concrete
component catalog. When a rule below says "each app decides," that decision belongs in the app's own doc,
not here.

> **Maintenance rule.** Any change that introduces, retires, or alters a foundation, component, or pattern
> in this shared skeleton must update this file in the same change. If the doc and the code disagree, the
> code is wrong, the doc is wrong, or both, and none of those is acceptable to merge. The same rule binds
> each app's own design-system doc for its brand-specific surfaces.

## 1. Theme setup (Material 3 Expressive)

The theme is built on **Material 3 Expressive**, not the stable Material 3 baseline.

- The root theme composable wraps content in **`MaterialExpressiveTheme`** (opt-in via
  `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`), passing a `colorScheme`, a `typography`, and
  `motionScheme = MotionScheme.expressive()`. Screens consume the expressive motion scheme rather than
  hand-authoring durations or easings.
- The Compose BOM ships a stable `material3`, but the Expressive components and APIs (the expressive theme
  entry point, the expressive motion scheme, the floating/expressive widgets) live in an **alpha
  `androidx.compose.material3:material3` artifact that is pinned in `libs.versions.toml` to override the
  BOM's version**. The pin carries a one-line comment explaining why (Expressive components). When the BOM
  catches up, the override is removed. Treat the pin as load-bearing, not stray.
- **Dynamic color (Android 12+ Material You) is wired through the theme but off by default.** The theme
  composable takes a `dynamicColor: Boolean = false` parameter. Each app's canonical scheme is the design;
  dynamic color is an opt-in personalization, never the default look.
- Light and dark `ColorScheme`s are full `lightColorScheme(...)` / `darkColorScheme(...)` declarations
  mapping every Material slot to a named token. The token *values* (hex) are brand-specific and live in the
  app.

## 2. Typography plumbing

Two parallel type scales coexist, and both are reachable from `MaterialTheme`.

- **Material typography.** A standard display/headline/title/body/label scale that off-the-shelf Material 3
  components consume implicitly, passed as the `typography` argument to the expressive theme. Do not
  override it for component internals.
- **App-specific typography scale.** A project-defined `@Immutable data class` of named `TextStyle` roles,
  used for any text a *screen itself* composes (as opposed to text rendered inside a stock Material
  component). It is exposed through the standard CompositionLocal-plus-extension pattern:
  - An internal `staticCompositionLocalOf { DefaultXTypography }` holds the default instance.
  - A `MaterialTheme.<x>Typography` extension property (`@Composable @ReadOnlyComposable`) reads `.current`,
    so call sites reach the scale the same way they reach `MaterialTheme.typography` or
    `MaterialTheme.colorScheme`.
  - The root theme composable provides the local via
    `CompositionLocalProvider(LocalXTypography provides DefaultXTypography) { MaterialExpressiveTheme(...) }`.
- **Font families are isolated in one file.** Each family resolves through a single named `FontFamily`
  declaration (e.g. a reading face and an accent face). Whether those resolve to system generic families
  (`SansSerif`, `Monospace`, zero font assets, works offline) or to bundled/multiplatform font resources is
  an **app-level choice documented in the app's own doc**. The structural rule is the isolation: swapping
  the pairing later changes only those declarations and nothing else in the system moves.
- **No ad-hoc `TextStyle` at a call site.** Pick the closest app-scale or Material role and `.copy(...)`
  only when a documented exception requires it. Case transforms (e.g. `.uppercase()` on an all-caps label)
  are applied at the call site, not baked into the style.
- **Shared editorial role contract (opt-in layer).** Editorial components need a shared type vocabulary,
  which a design-agnostic core cannot define. It therefore lives in the **opt-in `designsystem-editorial`
  module**, not in core: a small fixed `EditorialTypography` role set (eyebrow, pageTitle, headline, title,
  body, statLarge / statHero, ...), provided by an app via `EditorialTheme(editorialTypography = ...)` (nested inside
  `RhaydusTheme`) and read by editorial components through `MaterialTheme.editorialTypography`. That module
  owns the role *names*; the role *values* stay a brand decision (`buildEditorialTypography(typography)` is a
  neutral starting point to `.copy(...)`, or build from scratch). An app that does not use editorial
  components never depends on the module, and the neutral core neither defines nor requires it.

The *set of role names*, their sizes/weights, and which family each role uses are brand decisions and belong
in the app's own typography table. This doc only fixes the plumbing.

## 3. Color roles

Color is sourced from the app's Material 3 Expressive scheme. The reusable structure is the **mapping of
semantic roles onto Material slots**, not any palette.

- **Primary.** The single accent. Used for text/chrome emphasis (section marks, active toggle/filter
  states, progress indicators, the filled primary button). It is also, by deliberate convention, the
  surface fill of **at most one hero/stat card per scrollable region**. Inside such a card content is
  `onPrimary`, demoted text drops to `onPrimary` at low alpha, and any call to action steps down a tier
  (tonal/elevated) so it does not vanish into the fill. **Beyond that one hero, primary is not a background
  color**; ordinary cards, rows, and grouped regions use the page background or a `surfaceContainer*` shade.
- **on-surface / on-background.** Default readable copy.
- **on-surface-variant.** Demoted copy: metadata, captions, secondary lines, inactive labels.
- **surface / background.** The plain page canvas.
- **surfaceContainer, -High, -Highest.** Elevated tiles, cards, grouped sections. Step the container shade
  *up* to express grouping or focus. **Elevation is tonal, not shadowed**; there are no drop-shadow cards in
  the system (an app may carve a narrow brand exception, e.g. a physical-object motif, and must document it
  in its own doc).
- **surfaceContainerLowest.** Reserved for modal sheets and overlays sitting *above* the page.
- **outline / outlineVariant.** Hairline dividers and the rare bordered control. Prefer whitespace and a
  tone change to a dense rule.
- **secondaryContainer / onSecondaryContainer.** The selected state of pill chips and similar facet
  toggles; the idle state sits on `surfaceContainerHigh / onSurface`.
- **inversePrimary.** Small attention markers on chrome (notification dots).
- **error / onError.** Destructive and validation states only.

Roles that sit **outside** the Material scheme (a fixed brand accent that must read identically in light and
dark, an app-specific redaction treatment, syntax tinting, and so on) are app-specific extensions and belong
in the app's own doc, never here.

## 4. Shape, elevation, and the accent bar

- **Corners.** Small radii on cards/tiles, medium on buttons (Material defaults via the expressive theme),
  fully-rounded (`RoundedCornerShape(percent = 50)`) for pill chips and active toggle states. Exact radii
  are app-tunable.
- **Elevation is tonal** (see section 3): step the container shade rather than adding pixels of elevation.
- **Accent bar.** A short primary-colored bar leads a section label, in **two sizes picked by the label
  role**: a larger "section" bar that sits in the page gutter ahead of a full-size label-plus-headline pair,
  and a smaller inline hairline for a compact label living *inside* a card or hero. The two are never mixed:
  a full label gets the section bar, a compact label gets the hairline or no bar. The exact dimensions are
  an app choice; the two-tier structure is shared.

## 5. Layout primitives

### 5.1 Spacing scale

Spacing is rhythmic, drawn from a small recurring set of dp steps rather than computed: tight inline gaps,
within-component padding, the page horizontal gutter, gaps between blocks inside a region, and gaps between
major page sections, each step roughly double the last. The page gutter is a single value held consistent
for a whole page. Sections always breathe; two adjacent section headers without a generous gap between them
is a layout bug. The concrete values are app-tunable but follow this doubling rhythm.

### 5.2 Page scaffold and bottom-bar padding

A page is, top to bottom: an optional top app bar, scrolling content with a consistent horizontal gutter,
and bottom navigation when on a tab root. **The bottom of scrolling content reserves padding equal to the
bottom bar's footprint** so the last item is never occluded.

This is wired through a CompositionLocal, not passed by hand down every screen:

- A `LocalBottomBarPadding` CompositionLocal (default `0.dp`) carries the current bottom-bar footprint.
- The bottom-bar host screen measures the bar's height, adds spacing and the `navigationBars` window inset,
  and **provides** that total through `CompositionLocalProvider(LocalBottomBarPadding provides ...)`.
- Screens read it through a `rememberBottomBarPadding()` helper and apply it as trailing content padding.
- A companion `BottomNavigationSpacer()` helper emits a `Spacer` sized to the bottom navigation-bar inset
  for screens that need the raw inset.

**Root tabs prefer an in-page title over a chrome title.** On a root tab, the screen's identity renders
*inside* the scroll (an in-page `pageTitle`-style role), so it shares the editorial scale with the section
headers below. Reach for a Material top-bar title only on sub-screens pushed into the stack, where back
navigation or trailing actions are needed.

### 5.3 Section rhythm

The canonical way to introduce any region: an optional accent bar, a short label (the orienting
eyebrow/kicker), a human-readable headline, then the body (a list, a carousel/grid, or a single block), then
a generous gap before the next section. `EditorialSectionHeader` (in `designsystem-editorial`) is the shared
implementation of the accent-bar + eyebrow + headline + optional-description opener.

### 5.4 Hero region

A hero opens a screen with one dominant element. Hero regions sit on the page surface, not on a container;
they are part of the page, not a tile within it. The single primary-filled stat card (section 3) is the one
exception that carries a fill.

### 5.5 Cards and rows / reserved-row rule

Collections render as cards (fixed-size in a horizontal carousel) or rows (the same anatomy laid
horizontally). **Cards reserve the height of every optional row they may show, so the list never reflows as
data streams in.** Horizontal carousels paint no explicit overflow affordance: the rightmost card clipped
against the gutter is itself the "there is more" cue, never an edge fade, chevron, or page indicator.

### 5.6 Modal sheets

Modal bottom sheets open fully expanded on `surfaceContainerLowest`. The first element inside is always the
canonical section header (accent bar, label, headline, optional description), followed by the sheet body.
Sheets handle their own dismissal and never need a custom close button. **No exemptions:** every modal sheet
opens with this header. A stock Material `titleLarge`/`bodyMedium` sheet header reads as system chrome and is
a regression. If a sheet's content is so transient that this header feels theatrical, it should be a
`Dialog`, not a sheet.

### 5.7 Window size and breakpoints

The app adapts to the **measured size of the surface it renders into**, never to a hardcoded device
type. The breakpoints live in exactly one place so no screen invents its own threshold:

- `rememberWindowSizeClass()` reads the window size and buckets its width into a `WindowWidthClass`:
  **`COMPACT` (`< 600dp`)**, **`MEDIUM` (`600-840dp`)**, **`EXPANDED` (`>= 840dp`)**. The two bounds
  (`COMPACT_MAX_WIDTH`, `MEDIUM_MAX_WIDTH`) are constants on `WindowSizeClass`; `widthClassFor(...)` is
  the single function that applies them, so layout code and tests bucket identically.
- **Branch on `widthClass`, not on raw dp.** Reach for the measured `widthDp` / `heightDp` only for the
  rare continuous decision (a hero whose height tracks the viewport).
- The class recomputes whenever the container size changes, so it tracks desktop window resizes and
  foldable posture changes for free. The breakpoint values are tunable per app, but the three-bucket
  structure and the single-source rule are shared.

### 5.8 Adaptive content width and the two-pane split

A list surface that is a single full-width column on a phone should not stretch a single column across a
desktop window. Two shared moves cover this:

- **`TwoPaneScaffold`** is a pure-layout fixed-leading / flexible-trailing split: a fixed-width `list`
  pane beside a flexible `detail` pane with a divider. It knows nothing about navigation or screen
  models. Passing a `null` `detail` **collapses to a single pane** with no divider, and `list` stays the
  first child either way, so toggling `detail` between a value and `null` preserves the list subtree's
  state (and any `movableContentOf` it hosts) instead of tearing it down.
- The canonical wiring: on **`EXPANDED`** a list-detail screen shows both panes side by side; on
  **`COMPACT` / `MEDIUM`** the detail is pushed as a full screen by ordinary navigation. The list pane
  picks its own column width budget; an app caps overall content width rather than letting text lines run
  the full monitor.
- `Modifier.cappedContentWidth(max)` centers a scrolling content column and caps its width; the two
  sanctioned caps live on `ContentMaxWidth` (`Reading` for single-column copy/forms, `Pane` for a
  list/grid spread). Pick the role rather than inventing a per-screen number, and leave full-bleed media
  (covers, hero backdrops) outside the capped column.

### 5.9 Adaptive modal sheet (sheet to panel)

A bottom sheet welded to the bottom edge under a tall scrim reads as a phone layout on a desktop window.
**`AdaptiveModalSheet`** resolves a modal to the window width: a `ModalBottomSheet` on `COMPACT` /
`MEDIUM`, and a centered, bordered, large-radius panel (a `Dialog`) on `EXPANDED`.

- Its `content` slot is **identical to `ModalBottomSheet`'s** (a `ColumnScope` body owning its own
  padding and scroll), so a call site swaps one for the other without touching the body.
- Both forms open on `surfaceContainerLowest` and lead with the canonical section header (5.6); neither
  carries a close button.
- A body that needs an in-content dismiss (e.g. an "are you sure?" choice) calls `LocalModalSheetDismiss`
  rather than driving sheet state itself, so it closes correctly in both forms (the sheet animates down;
  the panel closes immediately). A body that needs to adjust its density to the surface it landed in
  reads `LocalModalSheetForm` (`SHEET` vs `PANEL`) instead of re-deriving the width class.

## 6. Iconography

- Material **outline** icons, consistent stroke weight across the app. Never mix filled and outlined icon
  families on the same surface (an app may sanction a single filled glyph as a deliberate state pair, e.g. an
  active toggle, and must document it).
- Icon-only controls always carry a content description.
- Icon size scales with the control's size token; an icon embedded in body type sits on the type baseline at
  a size proportional to the surrounding text.

How icons are addressed (raw Material `Icons`, or a typed in-house icon/illustration catalog backed by
multiplatform resources) is an app-level decision and belongs in the app's own doc.

## 7. Motion (structure)

- Use the Material **expressive motion scheme** (wired into the theme, section 1); do not hand-author
  durations or easings unless animating a custom property the scheme cannot express.
- **Press feedback on ripple-less surfaces.** Tappable cards and ripple-less surfaces scale to about 0.97
  while pressed and ease back on release, riding an `InteractionSource` so the scale lines up with the
  platform's pressed-state ticks. Press scale is the *only* indication on a ripple-less surface: never strip
  a card's ripple without adding the scale. Material buttons keep their ripple and pressed-shape morph and do
  not stack scale on top.
- **Wavy progress.** Progress and indeterminate waits use the wavy/expressive progress indicator; flat bars
  are chrome only and never carry semantic progress. (Pull-to-refresh is the common documented exception,
  since the wavy circle fights the pull arc; an app names its own exception.)
- **User-triggered list mutations animate; ambient changes do not.** Only user-driven add/remove animates
  (removal fades and neighbors close the gap); initial load, background refresh, and pagination tail jump in
  without animation. `rememberLazyItemMutationAnimator(keys)` + `Modifier.mutationAnimated(scope, animator,
  key)` provide this: they snapshot the first non-empty key set as the un-animated baseline and animate only
  what appears afterward (with a brief accent-bar pulse on the inserted item). The animator distinguishes
  only *first snapshot* from *appeared later*, not *user-driven* from *ambient* - so apply it to surfaces
  whose changes are user-driven; a background refetch or pagination of a list it wraps will animate too.
- **Staggered entry is a one-shot welcome moment**, played only by items composed during the initial
  screen-entry window, never an animate-on-scroll effect. `rememberStaggeredEntryCoordinator(key)` +
  `Modifier.staggeredEntry(coordinator, index)` provide it; "first entry" is tracked per key for the
  process lifetime, so a return visit to the same screen renders statically.
- **All motion is suppressed under reduced motion** (system animations disabled): the reduced path is an
  instant, un-animated swap. This gate is mandatory for every animation.

The *choreography* of commits (haptic textures, celebration bursts, shared-element morphs, shake-on-error,
and so on) is brand-specific and belongs in each app's own doc.

## 8. Component API conventions

A short, shared shape for how components are written; the concrete catalog (what each app actually ships)
lives in the app's own doc. The design-agnostic primitives that every app shares (layout, modifiers, the
button family) live in `designsystem-core`; the opt-in editorial language lives in `designsystem-editorial`
(`EditorialSectionHeader`, `HeroStatNumberField` + `EditorialSuffix`, `DropCapText`, `EditorialSearchField`,
`PullToRefreshEyebrow` - all reading the shared editorial type roles, section 2). Async images live in the
opt-in `designsystem-image` module (it brings Coil): `RhaydusImage` (plain), `RhaydusPlaceholderImage` (a
caller-supplied placeholder slot while loading), and `RhaydusShimmerImage` (the placeholder is a shimmer).
The network fetcher stays an app choice, configured on the app's Coil `ImageLoader`.

- **The button family is shared.** `RhaydusButton`, `RhaydusToggleButton`, `RhaydusIconToggleButton`, and
  `RhaydusSplitButton` (in `designsystem-core`) are the canonical buttons, parameterized by the `ButtonSize`
  / `ButtonStyle` / `ToggleButtonStyle` / `IconToggleButtonStyle` / `SplitButtonStyle` enums and taking icons
  as `RhaydusIconResource`. Reach for these rather than calling Material `Button` directly; the filled style
  carries the press-scale feedback (section 7). They use Material typography, so they carry no editorial
  dependency.
- **Reusable building blocks live in `core/presentation/widget/` (or the app's design-system module);
  theming lives in `core/presentation/theme/`.** Shell-tier components that enumerate concrete app tabs
  (bottom navigation, the tab host) live at the shell/orchestration tier, not in generic `core`, because they
  name concrete features; the reusable pieces they lean on (icon toggles, color roles) stay in `core`.
- **One declaration per file**, named after the declaration. One action per file in TOAD features (section 9).
- **Slot APIs over flags where the content varies.** A pill chip takes an optional `onClick` (interactive
  when present, a read-only tag when omitted) rather than a boolean mode. Components prefer the canonical
  shared primitive over a hand-rolled `Surface`-and-`Text`.
- **Visibility is deliberate.** Reserve `public` for the cross-module surface (domain contracts, use cases,
  `*Screen`/`*Tab`, the aggregated `*Module`, shared design-system components). Everything else is
  module-private. For a screen, only `override fun Content()` and the stateless `XScreen(state, runAction)`
  render function are public; every sub-component composable is `private`.
- **Compose formatting** (enforced by the style gates): a blank line between sibling composables in any
  layout scope (including `Spacer`); any call with two or more arguments breaks one-argument-per-line with a
  trailing comma; single-argument forms stay inline; property/parameter annotations go on their own line
  above the declaration.
- **Preview convention.** The stateless `XScreen(state, runAction)` render function is public specifically so
  `@Preview` can drive it without a screen model. Previews drive the stateless render function, never the
  `Content()` entry point.

## 9. TOAD to Compose glue

TOAD (the presentation architecture; see [`toad-architecture.md`](toad-architecture.md)) connects to Compose
at exactly one seam.

- A screen is a Voyager `Screen` object. In `Content()` it obtains its screen model via
  `koinScreenModel<XScreenModel>()`, collects state with `collectAsState()`, and hands `state` plus
  `screenModel::runAction` to a **stateless** `XScreen(state, runAction)` render function. The render
  function and every sub-component are otherwise side-effect-free.
- **The screen model exposes exactly one public entry point, `runAction(action)`.** UI never touches the
  dispatcher, repositories, or the action scope directly; it only dispatches actions.
- **`UiState` is rendered; `LocalVariables` is not.** Transient state actions need between dispatches
  (in-flight ids, scratch values) lives in `LocalVariables` so it does not trigger recomposition.
- **One-off effects go through `UiEvent`, collected once at the screen's top level.** Off-app handoffs (open
  a URL, share, launch the browser) dispatch a `UiEvent` that the top-level `Content()` collects and hands to
  the platform handler there. **Never call a `LocalUriHandler`, share sheet, or other platform handler from
  inside a leaf composable**; the handoff always leaves through the screen's top, never a leaf.
- Collectors push external state (a repository `StateFlow`) into the same action scope from the side, so
  external changes flow into the UI without a user action.

## 10. Decision rules (shared core)

Walk this before reaching for novelty. Brand-specific rules extend this list in each app's own doc.

- **Need a primary action?** Filled button. One per region.
- **Need to introduce a region?** The section header (accent bar, label, headline).
- **Need to mark a region as elevated relative to its neighbor?** Step the container shade. Do not add a
  shadow.
- **Need a divider?** First try a vertical gap or a tone change. Reach for a hairline only when neither will
  do.
- **Need a custom font weight, size, or family?** Pick a different role from the app or Material scale. Never
  inline a `TextStyle`.
- **Need to show progress?** The wavy/expressive progress indicator (bar and number move as one), except the
  documented pull-to-refresh exception.
- **Need a modal interaction?** Bottom sheet first (with its canonical header), full screen second, dialog
  only for an unavoidable blocking moment.
- **Need an off-app handoff?** Dispatch a `UiEvent` collected at the screen top, never call the platform
  handler from a leaf.
- **Need to adapt to window width?** `rememberWindowSizeClass()` and branch on `widthClass` (5.7); never
  read a raw dp threshold at the call site.
- **Need a modal that should not read as a phone sheet on a wide window?** `AdaptiveModalSheet` (5.9), not
  a bare `ModalBottomSheet`.
- **Building a desktop (jvm) layout?** Reach for the section 11 affordances before hand-rolling pointer or
  keyboard behaviour.

## 11. Desktop affordances (pointer and keyboard)

Compose Multiplatform runs the same shared composables on a desktop (jvm) window, where a pointer and a
physical keyboard exist. These affordances make a shared surface feel native there while staying inert on
touch, so a shared composable can carry them unconditionally. The desktop-only ones live in `jvmMain`; the
two modifiers below are in `commonMain` and no-op on touch.

- **Hand cursor.** `Modifier.pointerHandCursor()` shows the platform "clickable" hand cursor while the
  pointer hovers a clickable surface that lacks an obvious built-in cursor affordance. A no-op on touch
  (no pointer device), so it is safe to apply in shared layouts.
- **Hover highlight.** `Modifier.hoverHighlight(interactionSource)` paints a subtle translucent wash while
  the pointer hovers, cross-fading in and out. It is the hover sibling of `pressScale` (7); pair it with a
  `clickable` / `hoverable` that **shares the same `InteractionSource`**. Inert on touch, and suppressed
  under reduced motion like every other animation.
- **Tooltip.** `DesktopTooltip(text) { control }` wraps a pointer-only control in a hover tooltip on
  desktop and is a pure pass-through on touch (no extra layout node). Reach for it on **every icon-only
  desktop control**: under a pointer, an icon that carries only a `contentDescription` is a dead end, and
  the hover state otherwise promises information no label delivers. The `contentDescription` stays as the
  accessibility label; `text` is the human-readable hover label.
- **Back strip.** `DesktopBackStrip(...)` is the static leading-gutter back button a pushed desktop screen
  carries instead of a scroll-collapsing top bar. The app supplies its own arrow glyph (wrapped in
  `RhaydusIconResource`); no icon asset is baked into the shared module.
- **Esc dismisses in-app overlays.** `Modifier.dismissOnEscape(enabled) { onDismiss() }` closes an in-app
  surface that sits over the page (a full-screen viewer, a transient selection mode) on the Esc key. It is
  only for surfaces that are **not** a `Dialog` / `Popup`: a `Dialog` (including `AdaptiveModalSheet`'s
  expanded panel) already maps `dismissOnBackPress` to Esc. It runs in the key **preview** phase so Esc is
  caught even when a descendant text field holds focus, and it never grabs focus while disabled, so it can
  gate on a transient mode without disturbing normal focus.
- **Selection on desktop.** Where touch enters a multi-select mode by long-press, desktop also honors the
  pointer idioms (right-click to open the selection, modifier-click to extend it). The concrete gestures
  are an app decision; the rule is that a desktop layout does not leave long-press as the only entry.
- **Right-click menu.** `DesktopContextMenu(items) { content }` wraps a surface in a desktop secondary-click
  menu (the pointer counterpart to a touch long-press) and is a pure pass-through on touch and for an empty
  `items` list, so a shared composable can wrap unconditionally and populate items only where a menu applies.

**Tap-to-dismiss scrims use `detectTapGestures`, not `clickable`.** A full-screen scrim that dismisses an
overlay on tap must take its tap through `Modifier.pointerInput { detectTapGestures { ... } }`. `clickable`
also binds keyboard activation (Space / Enter) and makes the node focusable and semantically a button, so a
scrim built with `clickable` becomes a focus stop that swallows Space / Enter on desktop. `detectTapGestures`
is pointer-only and carries none of that, which is exactly what a scrim wants.
