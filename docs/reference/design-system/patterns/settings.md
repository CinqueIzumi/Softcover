# Patterns — Settings

### Main Settings menu (mobile)

- A `PageMasthead` "Settings" / "Set Softcover up the way you like it.", then three groups, each a
  `SectionHeader`, `space-md`, a flat `SettingsRowGroup`, and `space-section` before the next.
- **Account** / "Your account": Your profile ("Your shelves, stats and reading year").
- **Personalisation** / "Look and feel": Appearance ("Theme, accent and text size"), Library tabs ("Which shelves
  show, and their order"), Widgets ("Your shelves, on your home screen", only where the platform has widgets),
  Hidden suggestions ("Books you've asked us to stop recommending").
- **About** / "The fine print": About Softcover ("Credits, source, and how to reach us"), What's new ("What
  changed in each version."), Roadmap ("What's coming next, in the order it's planned").
- Every row is a `SubPage` row. The groups sit flat on the page background, never on cards.

### Settings rows

- `feature/settings`, internal. `SettingsMenuRow` is the row: a 42dp `Corners.md` `surfaceContainerHigh` tile
  holding a 22dp `primary` glyph, a `titleMedium` title over an italic `bodySmall` gloss that wraps, and a
  trailing 20dp `onSurfaceVariant` glyph. `SettingsRow` is the anatomy underneath, with a `leading` slot and no
  horizontal padding of its own; `SettingsSelectableRow` reuses it with a check slot reserved when unselected.
- `SettingsMenuRowTarget` sets the trailing glyph and the interaction: `SubPage` (chevron), `ExternalLink`
  (`OpenInNew`), `Edit` (pencil), `Add` (plus), `Copyable` (selectable text, not clickable) and `Static`
  (nothing to do). Interactive rows press through `tactileClickable` / `tactileSelectable`.
- `SettingsRowGroup` draws a hairline `Divider` above every row (first included) and none after the last, never a
  card; a single-choice run uses `selectableGroup()`. Switches are the shared `ToggleRow`.

### Desktop settings master-detail

- With `settingsUsesMasterDetail` (desktop), Settings is a category source-list sidebar beside a detail
  pane that swaps inline; Account → Your profile still pushes. Appearance, Library tabs and About render
  in the pane under a `DesktopVerticalScrollbar`, the Library-tabs save bar docked at the pane's foot.
- Sub-screen bodies are shared composables: mobile wraps them in a pushed `TopBar` page.
- Desktop Library and Explore never take the book-detail pane (`wideGridTabsUseDetailPane`); Reading
  keeps it on every platform.

### Theme picker (preview tiles)

- Leads the Appearance body on every platform: a `SectionHeader` (**Theme** / "Light, dark or dim"), one
  `ThemePreviewTile` per `ThemeMode` (Light, Dark, Dim, System) in a 2 x 2 `FlowRow` of equal measured
  widths (capped at 128dp), then one italic gloss line explaining Dim and System.
- A pick writes straight through; the whole app repaints.
- Brightness decisions read `LocalSchemeBrightness.current.isDark`, never `isSystemInDarkTheme()`;
  platform chrome follows the resolved mode through `ApplyPlatformThemeAppearance`.

### Spine-colour picker (the featured personalisation)

- Under the theme picker: a `SectionHeader` (**Spine colour** / "Paper and ink"), one
  `ColorPalettePreviewTile` per `ColorPalette`, house palette first, in a 3 + 2 `FlowRow` of equal
  measured widths (capped at 128dp), then a gloss line naming the selected palette (`SpinePalette.gloss`).
- Dynamic colour is the section's tail (`SettingsScreenUiState.dynamicColorSlot`): a `ToggleRow` where the
  platform supports it, nothing elsewhere; a tile tap turns it off (`SetColorPaletteUseCase`). Writes
  straight through.
- While Dim is the theme the section steps back (`spineColourSection`): its tiles render muted and inert
  (`ColorPalettePreviewTileUiModel.enabled = false`), the gloss says spine colours return with Light, Dark
  or System, and the dynamic-colour tail becomes a gloss (empty where the platform has none). Nothing is
  written; the stored palette and dynamic-colour choices wait for the next theme.

### Appearance settings rows (flat, hairline-separated)

- `AppearanceSettingsContent` (mobile page and desktop pane): flat rows on the page background split by
  hairlines, never cards. An italic intro, "How Softcover looks, and how it shows dates.", then up to five
  `SectionHeader`s in order: Theme, Spine colour, Display scale (desktop only), **Display** ("Display") and
  **Date notation** ("How dates are written").
- Display's switches are `ToggleRow`s from `SettingsScreenUiState.displayToggleRows`. Date notation is a
  run of `SettingsSelectableRow`s from `dateStyleRows` (`SettingsChoiceRowUiModel`), each showing today's
  date in that style.

### Desktop display scale

- Desktop only (`showUiScaleControl`): a **Display scale** / "Text & interface size" section after the two
  pickers, with a description ("Scales all text and controls. Try a larger size if the app looks too small
  on your display."), one `SettingsSelectableRow` per `UiScale` (100–200%) and the line "Changes apply
  right away." `UiScale.factor` scales `LocalDensity` at the desktop composition root.

### About screen body

- `AboutContent` (mobile page, desktop fallback and pane): an unheaded italic intro, then three
  `SectionHeader` groups of `SettingsMenuRow`s: **Credits** (Hardcover.app link plus the non-affiliation
  disclaimer in prose), **Source** (the GitHub repository, What's new and Roadmap), **Contact** (report an issue, the
  developer's Discord username as `Copyable`, the Hardcover Discord invite).
- It closes with `VersionFooter`, the app's only version line. No license text.

### Library tabs reorderable row

- Settings → Library tabs: one flat column of `ReorderableRow`s, no card, no dividers, under one
  `SectionHeader` and a bar-less group eyebrow + italic subhead.
- A row: a grip (or a dimmed pin on the fixed "All" row), the name (a leading `primary` dot for a custom
  list) over an italic count ("Hidden from tabs" when hidden), and a trailing eye toggle on hideable
  rows. A hidden row dims.
- The grip drags immediately (`Modifier.draggable`, `lift` / `drop`); nothing drops above "All". A
  "Create a new list" text button ends the list; Save docks below.
