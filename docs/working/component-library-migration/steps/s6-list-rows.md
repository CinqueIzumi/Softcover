# S6 — List rows

**Stage:** S6 — Rows & sheet chrome. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

22 declarations collapse onto `ListRow` + `ListRowUiModel`.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S6-1 | `ListRow` design, plus the core, debug, settings and about rows | [ ] |
| S6-2 | The remaining features | [ ] |

## S6-1 — design + core, debug, settings, about

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `ChooseListsRow` | `core/component/lists/ChooseListsBottomSheet.kt:290` | takes one `ChooseListsRowUiModel` |
| `NewListRow` | `core/component/lists/ChooseListsBottomSheet.kt:511` | takes one `ChooseListsRowUiModel` |
| `WhenReadRow` | `core/component/progress/WhenReadRow.kt:46` | |
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

**Phase 1 questions:** `ChooseListsRow` / `NewListRow` already take a resolved `ChooseListsRowUiModel`
— decide whether `ListRowUiModel` generalizes that shape (leading slot, title, trailing slot, event) or
whether `ChooseListsRowUiModel` becomes a specialisation. `ReorderableRow` carries drag affordance —
decide whether that is a `ListRowUiModel` variant or a decorator around `ListRow`.

## S6-2 — the remaining features

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
