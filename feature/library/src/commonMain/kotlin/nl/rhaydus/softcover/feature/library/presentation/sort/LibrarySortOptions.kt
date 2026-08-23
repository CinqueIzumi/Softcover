package nl.rhaydus.softcover.feature.library.presentation.sort

import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * The sort modes the Arrange sheet should offer for [tab], given [state]. Two layers of gating
 * apply, in order:
 *
 * 1. **Shelf-shape rules** (unchanged from the old `SortPill` dropdown): custom lists only offer
 *    the edition-sortable modes (plus ORDER once the list is ranked); the All and Did Not Finish
 *    shelves have no single position to attach MANUAL to; ORDER is custom-list only everywhere else.
 * 2. **Content gating** (new): PROGRESS and DEADLINE_URGENCY only appear when the shelf actually has
 *    in-progress books or tracked deadlines, and DATE_FINISHED only appears where finished books
 *    exist — offering a sort with nothing to differentiate by is a dead end for the user.
 *
 * Reads the tab's *raw* source list (not the search/filter-narrowed display list) so the offered
 * sorts don't flicker as the user types into search.
 */
internal fun librarySortOptions(
    tab: LibraryTab,
    state: LibraryUiState,
): List<LibrarySortMode> {
    if (tab is LibraryTab.CustomList) {
        val ordered = listOf(
            LibrarySortMode.DATE_ADDED,
            LibrarySortMode.TITLE,
            LibrarySortMode.AUTHOR,
            LibrarySortMode.PAGE_COUNT,
            LibrarySortMode.RELEASE_DATE,
        )

        val isRanked = state.customLists.firstOrNull { it.id == tab.listId }?.ranked == true

        return if (isRanked) listOf(LibrarySortMode.ORDER) + ordered else ordered
    }

    val isAllTab = tab.id == LibraryTab.All.id
    val isDidNotFinishTab = tab.id == LibraryTab.Status.of(UserBookStatus.DID_NOT_FINISH).id

    val shelfShaped = if (isAllTab || isDidNotFinishTab) {
        LibrarySortMode.entries.filter { it != LibrarySortMode.MANUAL && it != LibrarySortMode.ORDER }
    } else {
        LibrarySortMode.entries.filter { it != LibrarySortMode.ORDER }
    }

    val books = state.booksByTab[tab.id].orEmpty()
    val hasInProgress = books.any { it.status == BookStatus.Reading }
    val hasFinished = books.any { it.status == BookStatus.Read }
    val hasDeadlines = books.any { it.id in state.deadlines }

    return shelfShaped.filter { mode ->
        when (mode) {
            LibrarySortMode.PROGRESS -> hasInProgress
            LibrarySortMode.DEADLINE_URGENCY -> hasDeadlines
            LibrarySortMode.DATE_FINISHED -> hasFinished
            else -> true
        }
    }
}
