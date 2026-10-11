package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetSelection
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.buildLibraryFilterSheetSelection
import nl.rhaydus.softcover.feature.library.presentation.state.libraryPreviewCount

/**
 * The state slice the Filter sheet's resolved chip models are derived from. [compute] gates on
 * `distinctUntilChanged` upstream in `FilterDraftChipsCollector`, so its output
 * (`filterSheet.selection`) is deliberately absent from this bundle. Carries [libraryPreviewCount]'s
 * wider inputs (search, sort, editions) so [resultCount] never drifts from what committing the draft
 * would actually show.
 */
internal data class FilterDraftChipsSnapshot(
    val draft: LibraryFilterDraft?,
    val filterChipsByTab: ImmutableMap<String, LibraryFilterChips>,
    val booksByTab: ImmutableMap<String, ImmutableList<Book>>,
    val editionsByTab: ImmutableMap<String, ImmutableList<BookEdition>>,
    val addedAtByTab: ImmutableMap<String, ImmutableMap<Int, String?>>,
    val bookByBookId: ImmutableMap<Int, Book>,
    val sortModeByTab: ImmutableMap<String, LibrarySortMode>,
    val sortDirectionByTab: ImmutableMap<String, SortDirection>,
    val searchQuery: String,
) {
    fun compute(): LibraryFilterSheetSelection? {
        val draft = draft ?: return null
        val chips = filterChipsByTab[draft.tabId] ?: LibraryFilterChips()

        val previewState = LibraryUiState(
            booksByTab = booksByTab,
            editionsByTab = editionsByTab,
            addedAtByTab = addedAtByTab,
            bookByBookId = bookByBookId,
            sortModeByTab = sortModeByTab,
            sortDirectionByTab = sortDirectionByTab,
            searchQuery = searchQuery,
        )

        val resultCount = libraryPreviewCount(
            state = previewState,
            tabId = draft.tabId,
            draftFilters = draft.filters,
        )

        return buildLibraryFilterSheetSelection(
            chips = chips,
            draft = draft,
            resultCount = resultCount,
        )
    }
}
