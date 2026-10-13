# Patterns — Settings

### Settings rows

- `feature/settings`, internal. `SettingsRow` is the anatomy behind `SettingsMenuRow` and
  `SettingsSelectableRow`: an optional 42dp `surfaceContainerHigh` leading tile, a `titleMedium` title
  (`primary` when selected) over an optional italic `bodySmall` secondary that wraps, an optional trailing
  slot. Interactive rows press through `tactileClickable` / `tactileSelectable`; a row with no interaction
  wraps its title in `SelectionContainer`.
- `SettingsMenuRowTarget`: `SubPage` (chevron), `ExternalLink` (`OpenInNew`), `Copyable` (no interaction).
  `SettingsSelectableRow` reserves its check slot when unselected.
- `SettingsRowGroup` draws a hairline `Divider` above every row (first included), never a card; a
  single-choice run uses `selectableGroup()`. Switches are the shared `ToggleRow`.

### Desktop settings master-detail

- With `settingsUsesMasterDetail` (desktop), Settings is a category source-list sidebar beside a detail
  pane that swaps inline; Account → Your profile still pushes. Appearance, Library tabs and About render
  in the pane under a `DesktopVerticalScrollbar`, the Library-tabs save bar docked at the pane's foot.
- Sub-screen bodies are shared composables: mobile wraps them in a pushed `TopBar` page.
- Desktop Library and Explore never take the book-detail pane (`wideGridTabsUseDetailPane`); Reading
  keeps it on every platform.

### Theme picker (preview tiles)

- Leads the Appearance body on every platform: a `SectionHeader` (**Theme** / "The coat it wears"), one
  `ThemePreviewTile` per `ThemeMode` (Light, Dark, System), weighted but capped at 128dp, then one italic
  gloss line explaining System.
- A pick writes straight through; the whole app repaints.
- Brightness decisions read `LocalDarkTheme`, never `isSystemInDarkTheme()`; platform chrome follows the
  resolved mode through `ApplyPlatformThemeAppearance`.

### Spine-colour picker (the featured personalisation)

- Under the theme picker: a `SectionHeader` (**Spine colour** / "The paper and the ink"), one
  `ColorPalettePreviewTile` per `ColorPalette`, house palette first, in a 3 + 2 `FlowRow` of equal
  measured widths (capped at 128dp), then a gloss line naming the selected palette (`SpinePalette.gloss`).
- Dynamic colour is the section's tail `ToggleRow` (only where `supportsDynamicColor()`); a tile tap
  turns it off (`SetColorPaletteUseCase`). Writes straight through.

### Appearance settings rows (flat, hairline-separated)

- `AppearanceSettingsContent` (mobile page and desktop pane): flat rows on the page background split by
  hairlines, never cards. An italic intro, then four `SectionHeader`s: Theme, Spine colour, **Display**
  ("The look") and **Date notation** ("How dates read").
- Display's switches are `ToggleRow`s from `SettingsScreenUiState.displayToggleRows`. Date notation is a
  run of `SettingsSelectableRow`s from `dateStyleRows` (`SettingsChoiceRowUiModel`), each showing today's
  date in that style.

### Desktop display scale

- Desktop only (`showUiScaleControl`): a "Display scale" section after the two pickers, one
  `SettingsSelectableRow` per `UiScale` (100–200%). `UiScale.factor` scales `LocalDensity` at the desktop
  composition root, applying at once.

### About screen body

- `AboutContent` (mobile page, desktop fallback and pane): an unheaded italic intro, then three
  `SectionHeader` groups of `SettingsMenuRow`s: **Credits** (Hardcover.app link plus the non-affiliation
  disclaimer in prose), **Source** (the GitHub repository and Roadmap), **Contact** (report an issue, the
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
