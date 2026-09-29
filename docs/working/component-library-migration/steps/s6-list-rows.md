# S6 — List rows

**Stage:** S6 — Rows & sheet chrome. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

There is no generic `ListRow`. "Row" is a category name, not shared anatomy (contract § 7.6); each
declaration is audited for a real counterpart before anything is extracted.

## Approved shape

S6-1 (approved 2026-09-30):

- No `ListRow` / `ListRowUiModel`.
- `SidebarRow` + `SidebarRowUiModel` in `:core:component`. It replaces `SettingsSidebarRow` and library's
  `ShelfSidebarRow`, which share one `Surface` anatomy: h12/v2 outer, 10dp shape, `secondaryContainer`
  when selected, `hoverHighlight`, `labelLarge` SemiBold/Medium, h14/v11 inner. The model chooses only
  the optional parts: a leading icon, a trailing arrow and a long-press. The row adds selected semantics
  and absorbs the sidebar's 26dp `SectionHeader.Label` inset. The app shell's `SidebarItem`
  (`orchestration/.../EditorialSidebar.kt`) is not a third copy and stays: a different surface, with its
  own radius, icon, type role, badge and pulse.
- `SettingsMenuRow` and `AboutRow` (with `AboutLinkRow`, `AboutNavigationRow`, `AboutUsernameRow`)
  merge into one settings-local row in `commonMain`, not into `:core:component`: no other feature uses
  the anatomy. Press treatment (decided 2026-09-30): `SettingsMenuRow`'s press-scale plus the animated
  `surfaceContainer` wash (gated on `playDecorativeMotion()`) on both surfaces, with `pointerHandCursor`.
  The gloss wraps rather than truncating. Built as `SettingsMenuRow` + `SettingsRowAccessory`.
- `SettingsToggleRow` / `SettingsSelectableRow` stay separate. They get toggle and selection semantics
  (a `Role.Switch` whole-row toggle, and `Role.RadioButton` `selectable`).
- Stay as they are: `ChooseListsRow` (already model-driven and private to its sheet), `NewListRow` (its
  "+" text glyph becomes an icon), `DebugNavigationRow` / `HapticRow` (debug only), `ReorderableRow`
  (one caller, its own anatomy).
- `WhenReadRow` is not a row. Its R10 lift leaves S6 for the S11 retrofit: its model depends on a
  composition-read `now`, one of R10's unsettled edges.

S6-2 (approved 2026-09-30), audited, not built:

- `ShelvesSheetRow` (library) and `BecauseYouReadGenreSheetRow` (explore) are the one cross-feature
  pair. They are sheet rows, so they move to S6-3's `SheetRow` inventory (`s6-sheet-chrome.md`).
- Every other S6-2 row stays feature-local: its container, ink, type role or interaction differs, so a
  shared model would be all variants. This covers `ShareEntryRow`, `PasteFromClipboardRow`,
  `DismissSheetOption`, `ShowTitlesToggleRow`, `ShelveRow`, `DeadlineRow`, `StreakStripSheetRow`,
  `SearchFocusRecentRow` and `ExplainerStepRow`.
- Folded into S6-1 as local fixes: `SearchFocusRecentRow`'s 15dp remove target, which is nested in a
  clickable row, and selected semantics on `SidebarItem`.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S6-1 | `SidebarRow`, the settings menu-row merge, the row semantics fixes | [x] |
| S6-2 | Audit of the remaining features; nothing to build (see `## Approved shape`) | [x] |
| S6-C-rows | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## S6-1 — audit inventory: core, debug, settings, about

Carried from S5-C-headers: the sidebar label inset `padding(start = 26.dp, top = 8.dp, bottom = 6.dp)`,
repeated on every sidebar `SectionHeader.Label` in library and settings, belongs to the sidebar's grid;
absorb it with the sidebar rows here.

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `ChooseListsRow` | `core/component/lists/ChooseListsBottomSheet.kt:290` | takes one `ChooseListsRowUiModel` |
| `NewListRow` | `core/component/lists/ChooseListsBottomSheet.kt:511` | takes one `ChooseListsRowUiModel` |
| `WhenReadRow` | `core/component/progress/WhenReadRow.kt:46` | its date pill moves to `Chip` in S5-1; lift `whenReadChipModel` onto `UiState` here (R10) |
| `DebugNavigationRow` | `app/src/debug/.../DebugRoutesSection.kt:68` | |
| `HapticRow` | `app/src/debug/.../MotionDebugScreen.kt:152` | |
| `AboutLinkRow` | `feature/settings/presentation/screen/AboutContent.kt:197` | |
| `AboutNavigationRow` | `feature/settings/presentation/screen/AboutContent.kt:237` | |
| `AboutUsernameRow` | `feature/settings/presentation/screen/AboutContent.kt:280` | |
| `AboutRow` | `feature/settings/presentation/screen/AboutContent.kt:312` | |
| `SettingsToggleRow` | `feature/settings/presentation/screen/section/SettingsSectionShared.kt:40` | |
| `SettingsSelectableRow` | `feature/settings/presentation/screen/section/SettingsSectionShared.kt:83` | |
| `ReorderableRow` | `feature/settings/presentation/screen/section/ReorderableRow.kt:48` | |
| `SettingsMenuRow` | `feature/settings/presentation/screen/SettingsScreenLayout.mobile.kt:254` | |
| `SettingsSidebarRow` | `feature/settings/presentation/screen/section/SettingsSidebarRow.kt:30` | |


## S6-2 — audit inventory: the remaining features

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `ShelfSidebarRow` | `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:435` | |
| `ShelvesSheetRow` | `feature/library/presentation/component/LibraryShelvesSheet.kt:128` | |
| `ShowTitlesToggleRow` | `feature/library/presentation/component/LibraryArrangeSheet.kt:227` | |
| `ShelveRow` | `feature/book_detail/presentation/screen/section/ShelveControlCard.kt:243` | |
| `DeadlineRow` | `feature/book_detail/presentation/screen/section/InProgressSection.kt:257` | |
| `StreakStripSheetRow` | `feature/reading/presentation/component/StreakStrip.kt:203` | |
| `BecauseYouReadGenreSheetRow` | `feature/explore/presentation/screen/section/BecauseYouReadGenreControl.kt:166` | |
| `DismissSheetOption` | `feature/explore/presentation/screen/section/ContinueSeriesMenuSheet.kt:148` | |
| `SearchFocusRecentRow` | `feature/explore/presentation/screen/section/SearchFocusContent.kt:106` | |
| `ShareEntryRow` | `feature/profile/presentation/screen/section/ShareEntryRow.kt:36` | |
| `ExplainerStepRow` | `feature/onboarding/presentation/screen/OnboardingShelf.kt:410` | |
| `PasteFromClipboardRow` | `feature/onboarding/presentation/screen/OnboardingShelf.kt:180` | |
