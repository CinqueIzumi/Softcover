# Components — Buttons & controls

### Button

Five styles (filled, tonal, elevated, outlined, text) across five sizes (XS → XL). Filled is the page's primary action; tonal is a secondary action that still wants weight; outlined and text are tertiary. One filled action per region — never two competing primaries.

### Toggle buttons

Same size scale as Button; the active state expresses itself via shape morph and fill, not via a separate badge.

### Split button

A single primary action paired with an attached chevron that opens a menu of related variants (e.g. "Mark as reading" + alternative shelves). Reach for it when an action has one obvious default and a small set of equivalents.

### Bookmark toggle

A 44×44dp, 12dp-radius add/remove-from-library control: unset is `surfaceContainerHigh` fill with a `primary` glyph (`SoftcoverIcon.BookmarkAdd`), set is `primary` fill with an `onPrimary` glyph (`SoftcoverIcon.BookmarkAdded`). Used on Explore's search-result rows; reach for it on any other row-style book listing that offers an inline add/remove-from-library action rather than hand-rolling an `IconToggleButton`.

### Swipe row actions

`SwipeRowActions` wraps a list-layout row in a Material 3 `SwipeToDismissBox` and reveals a coloured background as the user drags. Swipe-right (start → end) is the *mark-as-read* action — tertiary container with `ic_bookmark_check` and "Mark as read" copy; suppressed via `allowMarkAsRead = false` when the book is already in Read. Swipe-left (end → start) is the *remove* action — error container with `ic_delete` and "Remove" copy. Background opacity tracks the swipe progress (`progress * 1.2f`, capped at 1) so the affordance reveals as the user pulls. The dismiss state is reset immediately on commit so the row snaps back; the calling surface is responsible for the data write and for shaking the row on failure (pair with `Modifier.shakeOnError`). Reach for it on the `LIST_COMPACT` and `LIST_LARGE` library layouts; never apply it to grid cards.

## Parked for KDoc (Step 07d)

### Theme preview tile

`ThemePreviewTile(model: ThemePreviewTileUiModel, onEvent)` (`core:component`, `control/`) is the choice-shaped-like-the-thing control behind the Appearance screen's [theme picker](../patterns/settings.md#theme-picker-preview-tiles). Each tile paints **the app's own page in miniature** — the 32×4 section accent bar shrunk to a hairline, a headline rule, two body rules, and one `surfaceContainerHigh` card carrying a `primary` progress line — so the reader picks by looking rather than by reading three words. Aspect 0.78, 14dp radius. **It cannot read colours off `MaterialTheme`**, because its whole job is to show the theme the reader is *not* in: it resolves both sides of the pair itself through `softcoverColorScheme(darkTheme, palette)` — in the reader's chosen spine colour (§2.1), so the two Appearance pickers agree — and, when `dynamicColor` is on, through `dynamicColorSchemeOrNull` — so the tiles re-render in the wallpaper palette the moment dynamic colour is switched on, and what a tile promises is what picking it delivers. It takes a `ThemeTilePainting` (`LIGHT` / `DARK` / `SPLIT`) and a plain `label` rather than the reader's `ThemeMode`, so the tile never learns the preference vocabulary — the Appearance screen is what decides a `SYSTEM` choice reads as `SPLIT`; one enum rather than a `darkTheme` + `split` pair, because "dark" and "split" are mutually exclusive. `SPLIT` is drawn as **one tile split on a bottom-left-to-top-right diagonal** (light above the seam, dark below), never as a third flat swatch: "whichever your device is" has no single colour. Selection, press feedback, label, and semantics come from the shared **preview tile frame** below, not from this tile. It lives in the component library rather than in `feature/settings` because the onboarding theme step is a second consumer in waiting; the schemes it paints come from the tokens the library depends on. The Appearance screen builds the model off the composition in its theme collector and keeps the reader's `ThemeMode` on its own `ThemeChoice` as the tap payload (§7 R9). Reach for this anatomy for any "pick a whole-app look" choice; do not render a theme as a bare colour swatch or a radio row.

### Preview tile frame

`PreviewTileFrame(label, selected, onClick, modifier) { miniature }` plus `MiniBar(widthFraction, height, color)` (`core:component`, `control/PreviewTile.kt`, both **internal**) are the two primitives the Appearance pickers' tiles are built from: the 0.78-aspect, 14dp-radius clipped frame with its label beneath, and the rounded rule a miniature stands its runs of type up from. Selection is a 2dp `primary` ring (1dp `outlineVariant` at rest) plus the `primary` label beneath — **no check badge**, since the ring already says it once — over `Role.RadioButton` semantics so the state is announced to a reader who sees neither. Press feedback is `pressScale` on the tile's own `InteractionSource` (§2.5) with `pointerHandCursor` for desktop. Both picker tiles compose it so the two rows read as one object rather than as lookalikes that drift apart; a **feature composes the finished tiles, never the frame**. Any future "pick a whole-app look" choice (the onboarding theme step, a future density or cover-shape picker) goes through it too, rather than growing a third tile from scratch.

### Colour palette preview tile

`ColorPalettePreviewTile(model: ColorPalettePreviewTileUiModel, onEvent)` (`core:component`, `control/`) is one choice in the [spine-colour picker](../patterns/settings.md#spine-colour-picker-the-featured-personalisation): the same page miniature the theme tile shows, painted in that `SpinePalette` — its paper *and* its accents (§2.1). It takes the design-system token, not the reader's `ColorPalette`, so the caller maps first (§2.1). Its miniature leans **harder on the accent** than the theme tile's — the card is a `primary`-**filled** hero (the app's real hero-stat treatment, §2.1) and a `tertiaryContainer` badge sits beneath it — so that one tile carries the whole look: the page tint, the lead colour at full strength, and the second note it is paired with. Two palettes that share a page tint are then still told apart at a glance. It paints in the brightness the reader is **in** (`LocalDarkTheme`) rather than resolving both, since that half of the choice belongs to the theme picker above it, and it **always paints its own palette even while dynamic colour is overriding the scheme app-wide** — five identical wallpaper-coloured tiles would say nothing; the section's gloss line carries that state instead. Never render a palette as a bare colour swatch, a paint chip, or a radio row.
