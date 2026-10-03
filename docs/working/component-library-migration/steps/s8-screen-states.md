# S8 — Screen states

**Stage:** S8 — Screen states. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S8-1 | Empty states | [x] |
| S8-2 | Callouts & banners | [ ] |
| S8-3 | Top bars | [ ] |
| S8-C-states | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## Approved shape

S8-1:
- `EmptyState(model, onEvent: (EmptyStateEvent) -> Unit = {}, modifier)`; model `title`, `body: String?`, `button: EmptyStateButtonUiModel?` (label + optional icon), `variant: EmptyStateVariant` (`Centered` default, `Inline`); event `ButtonClicked`.
- `Centered`: swaying quote glyph (`onSurfaceVariant` 0.15 dark / 0.3 light, standard `quoteGlyph`), editorial headlineMedium, bodyLarge, gaps 8 / 12, optional `RhaydusButton`; all centred.
- `Inline`: start-aligned italic editorial titleLarge + editorial body, gap 4, no glyph, no button.
- `EmptyState` no longer fills; full-surface callers pass `fillMaxSize()`.
- Folds to `Centered`: Library, Reading's top block (glyph/copy/"Find a book"), Hidden suggestions, Tag editor, `EmptyDetailPane`, offline (gains the glyph). `Inline`: ChooseLists.
- `GalleryEmptyState` deleted (registry never empty). `EmptyEntriesCard` stays feature-local (card notice, not this anatomy).
- Library's model built by the library collector per tab, on `UiState` (feature-local mapper); static copy is a file-level constant in its feature (§ 7.4a).
- No loading state; fixtures: Centered + button, Centered title-only, Inline.

## S8-1 — Empty states (8 → `EmptyState` + `EmptyStateUiModel`)

`OfflineScreenContent` already migrated in S4-5a (`offlineEmptyStateUiModel()`, four screen layouts).

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `ChooseListsEmptyState` | `core/component/lists/ChooseListsBottomSheet.kt:269` | hand-rolled `Column` + two `Text`s, does not use `EmptyState` |
| `EmptyListScreen` | `feature/library/presentation/screen/section/LibrarySectionShared.kt:170` | |
| `EmptyCurrentlyReadingScreen` | `feature/reading/presentation/screen/section/EmptyCurrentlyReadingScreen.kt:39` | |
| `HiddenSuggestionsEmptyState` | `feature/explore/presentation/screen/HiddenSuggestionsShelf.kt:494` | |
| `TagEditorEmptyState` | `feature/book_detail/presentation/component/TagEditorCollection.kt:109` | |
| `EmptyEntriesCard` | `feature/settings/presentation/screen/section/EmptyEntriesCard.kt:15` | |
| `EmptyDetailPane` | `orchestration/presentation/BookDetailPaneHost.kt:65` | `:orchestration` module |

## S8-2 — Callouts & banners (8 → `Callout` + `Banner`)

The four `*Callout`s are one component with a tone variant. `ConnectivityBanner` already migrated in
S4-5a as `Banner` + `BannerUiModel` + `BannerTone` (`core/component/callout/`), called once from
`RootScreen.kt`. Carried from S5-C-headers: `AppUpdateSection`'s hand-rolled accent bar + eyebrow on
`primaryContainer` (container ink, strings built in composition) is a callout and lands here.

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `StatusCallout` | `feature/book_detail/presentation/screen/section/YoursLensContent.kt:96` | |
| `ReadInfoCallout` | `feature/book_detail/presentation/screen/section/YoursLensContent.kt:32` | |
| `WantToReadInfoCallout` | `feature/book_detail/presentation/screen/section/YoursLensContent.kt:82` | |
| `DnfInfoCallout` | `feature/book_detail/presentation/screen/section/YoursLensContent.kt:60` | |
| `ScanEditionUpdateBanner` | `feature/book_detail/presentation/screen/section/ScanEditionUpdateBanner.kt:35` | |
| `RoadmapErrorBanner` | `feature/settings/presentation/screen/RoadmapContent.kt:122` | |
| `PaceNudgeRibbon` | `feature/reading/presentation/screen/section/PaceNudgeRibbon.kt:30` | |

## S8-3 — Top bars (9 → `TopBar` + `SearchTopBar` + `BackBar`)

`TopBar` and `SearchTopBar` stay separate (`component-contract.md` § 7.6); already migrated in S4-5a
with `TopBarUiModel`/`Event`/`Navigation`/`Surface` and `SearchTopBarUiModel`/`Event`, consumed by
~12 screen layouts. `SoftcoverTopBarAction` was deleted rather than migrated — `TopBarUiModel` carries
`title`/`subtitle`/`navigation`/`surface`, and a screen's own actions go in the trailing slot. Only
`BackBar` is left of this family.

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `TagEditorTopBar` | `feature/book_detail/presentation/component/TagEditorTopBar.kt:17` | |
| `DesktopBookDetailTopBar` | `feature/book_detail/presentation/screen/BookDetailScreenLayout.jvm.kt:138` | |
| `OnboardingTopBar` | `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:173` | |
| `DesktopSettingsBackBar` | `feature/settings/presentation/screen/DesktopSettingsBackBar.kt:28` | |
| `HiddenSuggestionsDesktopBackBar` | `feature/explore/presentation/screen/HiddenSuggestionsScreenLayout.jvm.kt:82` | |

Settle book detail's scroll-derived `TopBarSurface` in `steps/s11-contract-retrofit.md` S11-3, not here.
