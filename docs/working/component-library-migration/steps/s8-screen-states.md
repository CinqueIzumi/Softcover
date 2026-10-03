# S8 — Screen states

**Stage:** S8 — Screen states. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S8-1 | Empty states | [x] |
| S8-2 | Callouts & banners | [x] |
| S8-3 | Top bars | [x] |
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

S8-2:
- `Callout(model, onEvent: (CalloutEvent) -> Unit = {}, modifier)` in `core/component/callout/`; model `tone: CalloutTone` (`Neutral` surfaceContainerHigh / onSurface, `Highlight` primaryContainer / onPrimaryContainer), `eyebrow`, `lead: CalloutLead?` (`Icon(resource)` | `AccentBar`), `title: String?`, `body`, `action: CalloutActionUiModel?` (`Button(label)` | `Busy(label)` spinner + label | `Progress` indeterminate linear bar), `dismissible: Boolean`; events `ActionClicked`, `DismissClicked`.
- One 16 dp radius; eyebrow `eyebrowSmall` in primary; body editorial `bodySmall`; title editorial `headlineMedium`; action button is a `RhaydusButton` (tonal on Neutral, filled on Highlight). The component adds no outer gutter — callers keep 16 / 24 dp.
- No loading flag: no call site renders it while loading; in-flight is `action = Busy`.
- Folds to `Callout`: the three status callouts (one mapper, `Icon` lead), `ScanEditionUpdateBanner` (`Button`/`Busy`, dismissible), `AppUpdateSection` (`Highlight`, `AccentBar`, title, `Button`/`Progress`; pill button replaced).
- Book detail: status-callout and edition-banner models built in the state layer by feature-local mappers, on `BookDetailUiState`; copy is file-level constants (§ 7.4a).
- Settings: a collector observes `ObserveAppUpdateStateUseCase` and puts the `CalloutUiModel?` on settings UiState, replacing the `LocalAppUpdateState` read in composition; cross-feature wiring checked against `module-structure.md` (use case moves to `core:domain` if the rules require).
- `PaceNudgeRibbon` folds into `Banner`: new `BannerTone.Highlight` (primaryContainer, editorial italic bodySmall; `Warning` keeps Material bodySmall centred), optional leading icon, `dismissible` + `onEvent` (`BannerEvent.DismissClicked`); the reading mapper builds the `BannerUiModel`; dismissal animates.
- `RoadmapErrorBanner` stays: wrapper over foundation `InlineErrorState`, not a callout.
- `foundations.md` "inline bar" line corrected: status callouts lead with an icon.
- Fixtures: Callout status, edition idle + busy, app-update button + progress; Banner warning + highlight dismissible.

S8-3:
- `EditorialTopBar(model, onEvent: (EditorialTopBarEvent) -> Unit, modifier, actions: @Composable RowScope.() -> Unit = {})` in `core/component/topbar/`; model `title: String? = null`; event `BackClicked`. Separate from `TopBar` per § 7.6 (Material app bar vs plain row with start-aligned editorial headline).
- Anatomy: back `IconButton` (pointer-hand cursor, "Back" `DesktopTooltip`, "Navigate back" description), 4 dp gap, editorial `headlineMedium` onSurface title, trailing `actions`; one padding set start 8 / end 8 / top 12 / bottom 4 (book detail gains 8 dp on top — log in `visual-checks.md`).
- No loading state (static copy); fixtures titled + untitled; gallery entry in the top-bar family.
- Models live on each screen's `UiState` as defaulted fields (as `SettingsScreenUiState.aboutCreditsHeader`), passed through by the render; book detail's untitled model carries `BookOverflowMenu` in `actions`. No mapper.
- Same move for the older render-built models: the 9 mobile `*_TOP_BAR`s (settings ×5, hidden suggestions, profile, scan) and `EditionBottomSheetSelector`'s count label. Loading stand-ins (Roadmap, Explore headers and cards) stay for the R10 sweep.
- Folds: `DesktopSettingsBackBar` (file deleted; About, Appearance, Component gallery, Library tabs, Roadmap), `HiddenSuggestionsDesktopBackBar`, `DesktopBookDetailTopBar`.
- `OnboardingTopBar` stays feature-local (folio, pager-linked cross-fade); its hand-rolled accent bar + eyebrow becomes `SectionHeader` `Section(eyebrow)`, one model per page on `OnboardingUiState`, inside the existing cross-fade (bar fades with the text; accepted).
- `components.md` gains an "Editorial top bar" index line; `module-structure.md` marks it built.

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

## S8-3 — Top bars (9 → `TopBar` + `SearchTopBar` + `EditorialTopBar`)

`TopBar` and `SearchTopBar` stay separate (`component-contract.md` § 7.6); already migrated in S4-5a
with `TopBarUiModel`/`Event`/`Navigation`/`Surface` and `SearchTopBarUiModel`/`Event`, consumed by
~12 screen layouts. `SoftcoverTopBarAction` was deleted rather than migrated — `TopBarUiModel` carries
`title`/`subtitle`/`navigation`/`surface`, and a screen's own actions go in the trailing slot. Only
the desktop back bars and onboarding's bar are left of this family; `TagEditorTopBar` went with the
sheet-chrome move.

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `DesktopBookDetailTopBar` | `feature/book_detail/presentation/screen/BookDetailScreenLayout.jvm.kt:138` | |
| `OnboardingTopBar` | `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:173` | |
| `DesktopSettingsBackBar` | `feature/settings/presentation/screen/DesktopSettingsBackBar.kt:28` | |
| `HiddenSuggestionsDesktopBackBar` | `feature/explore/presentation/screen/HiddenSuggestionsScreenLayout.jvm.kt:82` | |

Settle book detail's scroll-derived `TopBarSurface` in `steps/s11-contract-retrofit.md` S11-3, not here.
