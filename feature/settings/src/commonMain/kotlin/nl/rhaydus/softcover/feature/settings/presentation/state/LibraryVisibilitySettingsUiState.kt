package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderTone
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.settings.presentation.model.LibraryTabEntry
import nl.rhaydus.softcover.feature.settings.presentation.model.buildLibraryTabEntries
import nl.rhaydus.toad.UiState

private const val LIBRARY_TABS_TITLE = "Library tabs"

internal data class LibraryVisibilitySettingsUiState(
    val libraryTabsPageHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Library tabs",
        headline = "Arrange your shelves.",
        description = "Choose which shelves ride along the top of your library — and the order they sit in.",
    ),
    val libraryTabsHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(
        eyebrow = "Your tabs",
        tone = SectionHeaderTone.Accent,
    ),
    val persistedEnabledStatusCodes: Set<Int> = emptySet(),
    val persistedEnabledListIds: Set<Int> = emptySet(),
    val persistedTabOrder: List<String> = emptyList(),
    val draftEnabledStatusCodes: Set<Int> = emptySet(),
    val draftEnabledListIds: Set<Int> = emptySet(),
    val draftTabOrder: List<String> = emptyList(),
    val initialized: Boolean = false,
    val isSaving: Boolean = false,
    val discardSheet: SheetSession<SheetScaffoldUiModel, LeaveScreen> = SheetSession.Closed,
    val availableLists: List<BookList> = emptyList(),
    val statusCounts: Map<Int, Int> = emptyMap(),
    val totalCount: Int = 0,
    val topBar: TopBarUiModel = TopBarUiModel(
        title = LIBRARY_TABS_TITLE,
    ),
    /** Derived from the count, list and draft fields, but only refreshed by [withOrderedEntries]. */
    val orderedEntries: List<LibraryTabEntry> = buildLibraryTabEntries(
        statusCounts = emptyMap(),
        totalCount = 0,
        availableLists = emptyList(),
        draftEnabledStatusCodes = emptySet(),
        draftEnabledListIds = emptySet(),
        draftTabOrder = emptyList(),
    ),
) : UiState {
    val isDirty: Boolean
        get() = initialized && (
            draftEnabledStatusCodes != persistedEnabledStatusCodes ||
                draftEnabledListIds != persistedEnabledListIds ||
                draftTabOrder != persistedTabOrder
            )
}

internal fun LibraryVisibilitySettingsUiState.withOrderedEntries(): LibraryVisibilitySettingsUiState = copy(
    orderedEntries = buildLibraryTabEntries(
        statusCounts = statusCounts,
        totalCount = totalCount,
        availableLists = availableLists,
        draftEnabledStatusCodes = draftEnabledStatusCodes,
        draftEnabledListIds = draftEnabledListIds,
        draftTabOrder = draftTabOrder,
    ),
)
