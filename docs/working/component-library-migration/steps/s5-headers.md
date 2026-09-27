# S5 — Headers & labels

**Stage:** S5 — Primitives. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

17 declarations collapse onto `SectionHeader` + `PageMasthead` + `SidebarLabel`, killing three
cross-module name collisions (`SectionLabel` × 4, `EditorialHeader` × 2, `SidebarSectionLabel` × 2).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S5-4 | `SectionHeader` / `SidebarLabel` design, plus core and the sidebar labels | [ ] |
| S5-5 | Section labels and headers across the features | [ ] |
| S5-6 | Mastheads, the desktop headers, the sheet headers | [ ] |
| S5-C-headers | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## S5-4 — design + core + sidebar labels

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `EditorialHeader` | `core/component/progress/EditorialHeader.kt:19` | takes `title: String`; a second `EditorialHeader` lives in `feature/reading` (S5-5) |
| `SidebarSectionLabel` | `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:424` | × 2, see S5-5 counterpart |
| `SidebarHeader` | `feature/settings/presentation/screen/section/SettingsCategorySidebar.kt:110` | |
| `DesktopPaneHeader` | `feature/settings/presentation/screen/section/DesktopPaneHeader.kt:15` | |

**Phase 1 questions:** does `SidebarLabel` cover both `SidebarSectionLabel` occurrences and
`SidebarHeader`, or is `SidebarHeader` a distinct role (page-level vs. section-level)? Settle the
`SectionHeader` vs. `PageMasthead` boundary here before S5-5 assigns every feature-local header to one
of the two.

## S5-5 — section labels and headers across the features

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `SectionLabel` | `app/src/debug/.../MotionDebugScreen.kt:364`, `feature/book_detail/presentation/screen/section/BookDetailSectionShared.kt:42`, `feature/profile/presentation/screen/section/ProfileSectionShared.kt:22`, `feature/reading/presentation/screen/section/ReadingSectionShared.kt:18` | × 4 name collision |
| `EditorialHeader` | `feature/reading/presentation/screen/section/EditorialHeader.kt:27` | × 2, see S5-4 counterpart |
| `SmallSectionLabel` | `feature/book_detail/presentation/screen/section/BookDetailSectionShared.kt:33` | |
| `InlineAccentLabel` | `feature/book_detail/presentation/screen/section/ShelveControlCard.kt:384` | |
| `SectionIntro` | `feature/profile/presentation/screen/section/ProfileSectionShared.kt:47` | |
| `SectionHeaderBar` | `feature/explore/presentation/screen/section/DesktopExploreSectionShared.kt:15` | |
| `AlsoReadingSectionHeader` | `feature/reading/presentation/screen/section/AlsoReadingSectionHeader.kt:26` | |
| `SearchResultsHeader` | `feature/explore/presentation/screen/section/SearchResultsHeader.kt:20` | |
| `HiddenSuggestionsGroupHeader` | `feature/explore/presentation/screen/HiddenSuggestionsShelf.kt:188` | |
| `LibraryTabsGroupHeader` | `feature/settings/presentation/screen/section/LibraryTabsGroupHeader.kt:37` | |
| `RowLabel` | `feature/settings/presentation/screen/section/ReorderableRow.kt:163` | |
| `SettingsPageHeader` | `feature/settings/presentation/screen/SettingsScreenLayout.mobile.kt:228` | |
| `ArrangeSubLabel` | `feature/library/presentation/component/LibraryArrangeSheet.kt:191` | |
| `ChangeEditionHeader` | `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:213` | |
| `ChooseListsHeader` | `core/component/lists/ChooseListsBottomSheet.kt:141` | takes `ChooseListsVariant` + jacket slot |
| `ShelvesSheetHeader` | `feature/library/presentation/component/LibraryShelvesSheet.kt:90` | |
| `TagEditorHeader` | `feature/book_detail/presentation/component/TagEditorHeader.kt:37` | |
| `SelectionHeader` | `feature/library/presentation/screen/section/SelectionHeader.kt:41` | |

## S5-6 — mastheads, desktop headers, sheet headers

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `MastheadHeader` | `feature/library/presentation/screen/section/MastheadHeader.kt:37` | |
| `ProfileHeader` | `feature/profile/presentation/screen/ProfileScreenLayout.mobile.kt:219` | |
| `DesktopExploreHeader` | `feature/explore/presentation/screen/section/DesktopExploreHeader.kt:27` | |
| `DesktopLibraryHeader` | `feature/library/presentation/screen/LibraryScreenLayout.jvm.kt:489` | |
| `DesktopReadingHeader` | `feature/reading/presentation/screen/ReadingScreenLayout.jvm.kt:240` | |

**Phase 1 questions:** this sub-step overlaps `SheetHeader` (S6-3, sheet chrome) — decide here whether
sheet-context headers (`ChooseListsHeader`, `ShelvesSheetHeader`, `TagEditorHeader` from S5-5) fold into
`SheetHeader` instead of `SectionHeader`/`PageMasthead`, and record the split before S6-3 starts.
