# Design System — Foundations

## 2. Foundations

### 2.1 Color

The role model is [fd §3](../../rhaydus/0.3.1/design-system-foundations.md#3-color-roles). This app's deltas:

- **Primary-filled hero.** The one primary-filled card per scrollable region is a stat hero (the profile's pages-read hero). Inside it demoted text is `onPrimary` at ~0.75 alpha and the divider `onPrimary` at ~0.25. Cards built around imagery (covers, photos) keep a `surfaceContainer*` shade.
- **Inverse-primary** comes from the chosen palette (below) like the rest of the primary family.
- **Rating gold (`RatingGold`).** Rating-star glyphs only, read-only and interactive. It sits outside the Material scheme and the dynamic-colour override.
- **Fixed inks.** `MoodInkCosyBackground` / `MoodInkCosyForeground`, `MoodInkDreadBackground` / `MoodInkDreadForeground` / `MoodInkDreadEyebrow`, `MoodInkHeartWrenchBackground` / `MoodInkHeartWrenchForeground` (Explore's mood tiles) and `MonogramCoverInk` / `MonogramCoverForeground` (the coverless-title cover). They read identically in light and dark and in every palette. All live with `RatingGold` in `core/designsystem`'s `Color.kt`; never re-declare one of these hexes at a call site.
- **Spoiler redaction.** `ColorScheme.spoilerEditorHighlight` (a translucent wash in the review editor) and `ColorScheme.spoilerCover` (the near-solid block over a spoiler run on display, until tapped), both derived from `onSurfaceVariant`. Only for the per-run spoiler mark; the [whole-review gate](patterns/book-detail.md#spoiler-reveal) is not a colour role.

**Spine colour: the palette.** `SpinePalette` names five looks — **Softcover** (default: warm clay, old gold, warm-white paper), **Vellum** (house gold, sage, cream page), **Ink** (slate blue, oxblood, cool blue-grey page), **Foxed** (walnut brown, dusty blue, tanned paper), **Sea** (sea teal, warm sand, pale blue-green page). The reader picks one in the [Appearance screen](patterns/settings.md#spine-colour-picker-the-featured-personalisation).

- `SpinePalette` (`core/designsystem`, `presentation/theme/`) is the token: hex table, name, gloss. `ColorPalette` (`:core:domain`) is the persisted identifier only. `:core:uibinding`'s `ColorPalette.toSpinePalette()` is the only bridge. A new palette lands in three places: the `SpinePalette` entry, its hex table in `Color.kt`, and the `ColorPalette` entry.
- A palette is a whole look: the three accent families (with on-, container-, on-container- partners and `inversePrimary`), its own error family, and the paper (`background`, `surface`, every `surfaceContainer*`, `surfaceVariant`, both outlines, the inverse pair). Only the black scrim and the fixed inks stay put.
- Each palette's neutral ramp is the house ramp re-tinted at identical lightness, so contrast pairs hold in every palette.
- A palette is authored in `Color.kt` as one `PaletteColors`: three `AccentFamily` values (tones 20/30/40/80/90), a `NeutralFamily` (sixteen steps) and a `NeutralVariantFamily` (five). `Theme.kt` maps tones to light and dark slots with one map for all palettes. Never spell a role out per mode; add tones to the table and keep the ~6:1 contrast band (pairs listed in each family's KDoc).
- **Never hard-code a scheme hex at a call site**, and never assume the app is warm. A colour that must survive any palette joins the fixed inks.
- A composable that paints a palette or brightness the reader is not in (the preview tiles) calls `softcoverColorScheme(brightness, palette)`, never `MaterialTheme.colorScheme`. A feature holding a `ColorPalette` maps it first.

Dynamic colour (Android 12+) is off by default. While on, the wallpaper scheme replaces the palette; the chosen palette stays persisted underneath. Picking a spine colour switches dynamic colour off.

**Light, dark, or the device's own.** `ThemeMode` (Light / Dark / Dim / System, default System; System resolves to Light or Dark only) is picked in the Appearance screen's [theme picker](patterns/settings.md#theme-picker-preview-tiles). `SoftcoverTheme` takes a resolved `SchemeBrightness` (Light / Dark / Dim; default the host's light or dark); `:core:uibinding`'s `ThemeMode.toSchemeBrightness(systemInDark)` resolves the mode. Dim is one standalone warm scheme: it ignores the spine-colour palette and dynamic colour, and picking it turns dynamic colour off. A surface choosing an alpha, ink or asset by brightness reads `LocalSchemeBrightness.current.isDark`, never `isSystemInDarkTheme()`. The app shell's `ApplyPlatformThemeAppearance` points system-bar and status-bar glyphs at the chosen mode.

### 2.2 Typography

Plumbing is [fd §2](../../rhaydus/0.3.1/design-system-foundations.md#2-typography-plumbing). Both families are bundled as Compose Multiplatform font resources:

- **Fraunces** — the editorial serif with true italics: page titles, display, headlines, body prose, stats, the quote glyph. Tabular figures come from `TextStyle.tabularFigures()`, never a raw OpenType string.
- **Inter** — the chrome sans, roman only: eyebrows, card and list titles, Material component internals, utility labels. A chrome surface that needs italic belongs in the editorial scale.

| Role            | Where it goes                                                                 | Character                          |
| --------------- | ----------------------------------------------------------------------------- | ---------------------------------- |
| `eyebrow`       | All-caps section labels, kicker over a headline, accent over a hero stat     | Wide letter-spacing, semibold      |
| `eyebrowSmall`  | Same role, when packed inside cards/rows                                     | As above, tighter                  |
| `pageTitle`     | The screen's primary title *inside* the scrolling page (the top-bar slot uses Material `titleLarge`) | Roman, semibold, large |
| `display`       | Section headlines that follow an eyebrow                                     | Italic, semibold, expressive       |
| `headlineMedium` / `headlineSmall` | Subsection or modal headlines                                  | Italic, semibold                   |
| `titleLarge` / `titleMedium` / `titleSmall` | Card titles, list item titles, grouped row headers       | Roman, semibold                    |
| `bodyLarge` / `body` / `bodySmall` | Running prose, descriptions, captions                            | Italic                             |
| `review`        | Community review prose                                                        | Roman `bodyLarge`                  |
| `statHero`      | The single largest number on a screen                                         | Italic, semibold, oversize         |
| `statLarge`     | Editable hero numbers (input fields styled as stats)                          | Italic, semibold                   |
| `quoteGlyph`    | Oversized quotation mark behind a quote or as an empty-state flourish, `onSurfaceVariant` at low alpha | Display face, very large, low alpha |

**`quoteGlyph` tint exception.** Where the glyph interrupts a rule line (the [title-page sheet](patterns/explore-profile.md#title-page-sheet-create-list)) it is `primary` at 0.28 alpha.

Italic is reserved for display, headlines, body prose and stats; roman for page titles, card/list titles and chrome.

### 2.3 Shape & elevation

- **Corners.** The `Corners` scale (`xxs` 2, `xs` 4, `sm` 8, `md` 12, `lg` 16, `xl` 20, `xxl` 28 dp, plus `pill`); true circles keep `CircleShape`. Cover art uses a subtle radius.
- **Elevation.** The one shadow exception is book covers, which carry a slight shadow.
- **Accent bar.** Two sizes, per [fd §4](../../rhaydus/0.3.1/design-system-foundations.md#4-shape-elevation-and-the-accent-bar):
  - **Section bar — 32×4 dp.** Sits in the page gutter before a full `eyebrow` + display/headline pair: sheet headers, settings section headers, top-level regions.
  - **Inline bar — 20×1 dp hairline.** Always with `eyebrowSmall`, inside a card, hero or subcomponent: featured cards (Reading), page-title accents (Library), Book detail's "Shelve this book" opener.
  - **No bar.** A compact in-flow sub-label (`eyebrowSmall`, `onSurfaceVariant`) inside a section already introduced: Book detail's `YOUR RATING` / `YOUR TAGS` / `YOUR REVIEW` / `TAGS` / `FIND IT`.
  A full `eyebrow` always gets the 32×4 bar. The one exception is the [title-page composition](patterns/explore-profile.md#title-page-sheet-create-list), whose quote-glyph-broken rule plays the bar's role.

### 2.4 Spacing

Spacing is rhythmic, not formulaic. The recurring values are `Spacing` tokens:

- **2** dp (`xxxs`) — the hairline gap.
- **4 / 8** dp (`xxs` / `xs`) — tight inline gaps (icon-to-label, badge offsets).
- **12 / 16** dp (`sm` / `md`) — within-component padding, label-to-control gaps.
- **24** dp (`xl`, `Spacing.gutter`) — the page horizontal gutter on every page. **20** dp (`lg`) — the gap between a hero card's image and its text block.
- **28 / 32** dp (`xxl` / `xxxl`) — the gap between blocks inside a hero region, and masthead to first section (32). Sheet blocks are separated by 24; a section header to its body is 16.
- **40 / 48** dp (`x4l` / `x5l`) — gap between major page sections; the 40 step is `Spacing.section`.

Spacing calls read a token; sizes and strokes stay literal `.dp`.

Sections always breathe. Two adjacent eyebrow/headline pairs without a generous gap between them is a layout bug.

### 2.5 Motion

Structure is [fd §7](../../rhaydus/0.3.1/design-system-foundations.md#7-motion-structure); desktop affordances are [fd §11](../../rhaydus/0.3.1/design-system-foundations.md#11-desktop-affordances-pointer-and-keyboard). This app's specifics:

- **App motion specs.** `MaterialTheme.motion.valueChange` (critically damped, `StiffnessVeryLow`) springs every shown value: `StatNumber` numbers (except `Roll`, for counts the user drives) and chart bar, fill and segment sizes. `MaterialTheme.motion.celebration(durationMillis)` drives the burst; `MaterialTheme.motion.ambientLoop(durationMillis)` drives the shimmer and the quote-glyph sway. Never hand-author another.
- **Reduced motion** is resolved at the theme root: `SoftcoverTheme` provides a snapping `MotionScheme` and `Motion`, so call sites never branch. The foundation `playDecorativeMotion()` is only for skipping an effect outright (burst, shimmer, glyph sway, shake, pulse).
- **Press feedback** goes through `:core:component` `modifier/Tactile.kt` (`tactileClickable` / `tactileSelectable` / `tactileToggleable`: press scale, desktop hover wash clipped to `hoverShape`, hand cursor). The foundation `pressScale` family is banned outside the foundation (detekt `ForbiddenImport`). A primitive whose visible shape is smaller than its touch box passes `hoverShape = null` and its own `interactionSource`, then applies `hoverHighlight` on the visible shape. Cover-variant book cards lift on hover instead of washing.
- **Press haptics.** Outcome haptics follow the result, not the press. Never on navigation taps; press scale never implies a haptic.
- **Desktop chrome.** Every icon-only desktop control is wrapped in the foundation `DesktopTooltip` (`nl.rhaydus.designsystem.component`). A pushed desktop screen carries `EditorialTopBar` (`:core:component`), never `DesktopBackStrip` or a hand-rolled back row; its model is a defaulted field on the screen's `UiState`.
- **Desktop Esc.** The foundation `Modifier.dismissOnEscape` (`nl.rhaydus.designsystem.modifier`) closes Focus Mode, the full-screen cover viewer, Library bulk-select / Rearrange mode and the desktop Explore search. Gate `enabled` on a mode entered by a gesture; when the condition flips while typing, keep it enabled and guard inside `onDismiss`, because enabling it takes focus.
- **Desktop context menu.** Right-click on a library book opens `DesktopContextMenu` with Open, Select (Add to / Remove from selection in selection mode), Mark as read (unread books), Add to list… and Remove from library. The per-book items reuse the bulk actions with a one-book id set.
- **Progress** uses the wavy indicator and animates between values.
- **Tab-root swap.** A 200ms cross-fade with a 12dp upward drift on the incoming tab; never a hard cut or horizontal slide. Per-tab `rememberSaveable` state survives through a shared `SaveableStateHolder` keyed by tab. Instant under reduced motion.
- **Root push/pop.** A push fades the incoming screen in while scaling up from 0.92; a pop fades the outgoing screen out while scaling down to 0.92, drawn above the revealed screen. On Android the back gesture seeks this transition and the cover, title and rating morphs; iOS and desktop pop plainly. The two-pane detail pane does not use it.
- **Mutation-rejection shake.** A rolled-back optimistic mutation shakes its card/row (6dp, 3 cycles), fires the `reject` haptic, and swaps the eyebrow to the error role with "Couldn't save — tap to retry". Use `:core:component`'s `Modifier.shakeOnError` (with `rememberShakeTrigger()` when the shake must outlive its error).
- **List mutation and staggered entry** use the foundation APIs. Hoist `rememberStaggeredEntryCoordinator` above whatever gates the loading/loaded swap, so the stagger plays only when content is on hand at first paint.
- **Cover-to-detail morph.** A key is applied only through `Modifier.navSharedBounds`; the mapper resolves it with `bookTransitionKey(element, bookId, surface)` and a component never computes one. Never key a decorative cover (blurred backdrops).

### 2.6 Iconography

Rules are [fd §6](../../rhaydus/0.3.1/design-system-foundations.md#6-iconography). Icons are addressed through design-system types, never the platform resource system:

- `SoftcoverIcon` (`core/designsystem/presentation/icon/`) is the typed catalog of bundled vectors; its `Res` is module-internal.
- Components take and draw a `RhaydusIconResource` (foundation, `nl.rhaydus.designsystem.icon`), built from a `SoftcoverIcon` with `drawableIconResource(contentDescription, icon)`. Draw with `Icon(painter = token.getIconPainter(), contentDescription = token.contentDescription, …)`, never a raw `painterResource`.
- The Android notification icons (`ic_reading` / `ic_play` / `ic_pause` / `ic_stop` / `ic_edit` / `ic_bookmark`) stay `R.drawable` resources in `core:designsystem`'s `androidMain`.

**Illustrations.** `SoftcoverIllustration` (`core/designsystem/presentation/illustration/`) is the typed catalog of bundled illustrations. Draw with `Image(painter = illustration.painter(), contentDescription = …)`.

### 2.7 Window size & breakpoints

Breakpoints and `rememberWindowSizeClass()` are [fd §5.7](../../rhaydus/0.3.1/design-system-foundations.md#57-window-size-and-breakpoints). Navigation chrome per class: **bottom bar (compact) → rail (medium) → sidebar (expanded)**. Expanded root list surfaces gain the [list-detail two-pane](patterns/cross-cutting.md#list-detail-two-pane). The docked/floating bottom-bar preference applies only in compact.
