# S5 — Headers & labels

**Stage:** S5 — Primitives. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

The feature-local headers and the foundation `EditorialSectionHeader` collapse onto `SectionHeader` +
`PageMasthead`, killing three cross-module name collisions (`SectionLabel` × 4, `EditorialHeader` × 2,
`SidebarSectionLabel` × 2).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S5-4 | `SectionHeader` / `PageMasthead` design, plus the sidebar labels and the settings page headers | [x] |
| S5-5 | Section labels and headers across the features | [ ] |
| S5-5b | Every `EditorialSectionHeader` call site onto `SectionHeader` | [ ] |
| S5-6 | Mastheads and the desktop headers | [ ] |
| S5-C-headers | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## Approved shape

- **`SectionHeader(model, modifier)`** in `:core:component` `header/`; no events. `SectionHeaderUiModel` is
  sealed over the three accent-bar registers of `foundations.md`: `Section(eyebrow, headline?,
  description?, pulseKey = 0)` (32×4 bar + `eyebrow`, primary), `Inline(eyebrow)` (20×1 hairline +
  `eyebrowSmall`), `Label(eyebrow)` (no bar, `eyebrowSmall`, `onSurfaceVariant`).
- One rounded bar, one 12dp bar → eyebrow gap, one `headline` style; the 8dp gap, square bars and
  `headlineSmall` / `headlineMedium` headlines are accepted visual shifts. `SectionLabel`'s unused `color`
  parameter is dropped; `pulseKey` keeps book detail's bar pulse.
- No mapper: the strings are presentation-ready. Every header model, static text included, is built in
  the feature's collector and arrives as a `UiState` field (R10); no constant models in render files.
- **No `SidebarLabel`:** both `SidebarSectionLabel`s are `SectionHeaderUiModel.Label`; the sidebar owns its
  26dp inset through the modifier.
- **`PageMasthead(model, modifier)`** in `header/`: optional eyebrow (no bar), page title, optional
  subtitle, `size` = `Regular` / `Compact`. Absorbs `DesktopPaneHeader` (Regular, eyebrow),
  `SettingsPageHeader` (Regular, pulled forward from S5-5) and `SidebarHeader` (Compact).
- **Boundary:** a `SectionHeader` opens a region within a page or sheet and always has an eyebrow; a
  `PageMasthead` names the page, once per page. S5-6 decides whether the stateful mastheads compose
  `PageMasthead` or stay feature-local.
- **Sheet headers:** `SheetHeader` (S6-3) is a `SectionHeader.Section` plus a trailing jacket slot; the
  sheet headers and the core `EditorialHeader` move to S6-3.
- **S5-5b:** every `EditorialSectionHeader` call site moves onto `SectionHeader.Section`; the app stops
  consuming the foundation component. `EditorialSectionHeaderSkeleton` (S5-8) mirrors `SectionHeader`.

## S5-4 — design + sidebar labels + settings page headers

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `SidebarSectionLabel` | `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:425` | × 2, the other in `SettingsCategorySidebar.kt` |
| `SidebarHeader` | `feature/settings/presentation/screen/section/SettingsCategorySidebar.kt:110` | |
| `DesktopPaneHeader` | `feature/settings/presentation/screen/section/DesktopPaneHeader.kt:15` | |
| `SettingsPageHeader` | `feature/settings/presentation/screen/SettingsScreenLayout.mobile.kt:228` | |

## S5-5 — section labels and headers across the features

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `SectionLabel` | `app/src/debug/.../MotionDebugScreen.kt:364`, `feature/book_detail/presentation/screen/section/BookDetailSectionShared.kt:42`, `feature/profile/presentation/screen/section/ProfileSectionShared.kt:22`, `feature/reading/presentation/screen/section/ReadingSectionShared.kt:18` | × 4 name collision |
| `EditorialHeader` | `feature/reading/presentation/screen/section/EditorialHeader.kt:27` | × 2, the core one is in S6-3 |
| `SmallSectionLabel` | `feature/book_detail/presentation/screen/section/BookDetailSectionShared.kt:33` | |
| `InlineAccentLabel` | `feature/book_detail/presentation/screen/section/ShelveControlCard.kt:384` | |
| `SectionIntro` | `feature/profile/presentation/screen/section/ProfileSectionShared.kt:47` | |
| `SectionHeaderBar` | `feature/explore/presentation/screen/section/DesktopExploreSectionShared.kt:15` | |
| `AlsoReadingSectionHeader` | `feature/reading/presentation/screen/section/AlsoReadingSectionHeader.kt:26` | |
| `SearchResultsHeader` | `feature/explore/presentation/screen/section/SearchResultsHeader.kt:20` | |
| `HiddenSuggestionsGroupHeader` | `feature/explore/presentation/screen/HiddenSuggestionsShelf.kt:188` | |
| `LibraryTabsGroupHeader` | `feature/settings/presentation/screen/section/LibraryTabsGroupHeader.kt:37` | |
| `RowLabel` | `feature/settings/presentation/screen/section/ReorderableRow.kt:163` | |
| `ArrangeSubLabel` | `feature/library/presentation/component/LibraryArrangeSheet.kt:191` | |
| `SelectionHeader` | `feature/library/presentation/screen/section/SelectionHeader.kt:41` | |

## S5-5b — `EditorialSectionHeader` call sites

Every `EditorialSectionHeader(` call in `feature/`, `core/` and `app/src/debug` (`git grep`), about 40.

## S5-6 — mastheads and desktop headers

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `MastheadHeader` | `feature/library/presentation/screen/section/MastheadHeader.kt:37` | |
| `ProfileHeader` | `feature/profile/presentation/screen/ProfileScreenLayout.mobile.kt:219` | |
| `DesktopExploreHeader` | `feature/explore/presentation/screen/section/DesktopExploreHeader.kt:27` | |
| `DesktopLibraryHeader` | `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:489` | |
| `DesktopReadingHeader` | `feature/reading/presentation/screen/ReadingScreenLayout.jvm.kt:240` | |
