package nl.rhaydus.softcover.feature.settings.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
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
    val persistedEnabledStatusCodes: ImmutableSet<Int> = persistentSetOf(),
    val persistedEnabledListIds: ImmutableSet<Int> = persistentSetOf(),
    val persistedTabOrder: ImmutableList<String> = persistentListOf(),
    val draftEnabledStatusCodes: ImmutableSet<Int> = persistentSetOf(),
    val draftEnabledListIds: ImmutableSet<Int> = persistentSetOf(),
    val draftTabOrder: ImmutableList<String> = persistentListOf(),
    val initialized: Boolean = false,
    val isSaving: Boolean = false,
    val discardSheet: SheetSession<SheetScaffoldUiModel, LeaveScreen> = SheetSession.Closed,
    val availableLists: ImmutableList<BookList> = persistentListOf(),
    val statusCounts: ImmutableMap<Int, Int> = persistentMapOf(),
    val totalCount: Int = 0,
    val topBar: TopBarUiModel = TopBarUiModel(
        title = LIBRARY_TABS_TITLE,
    ),
    /** Derived from the count, list and draft fields, but only refreshed by [withOrderedEntries]. */
    val orderedEntries: ImmutableList<LibraryTabEntry> = buildLibraryTabEntries(
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
