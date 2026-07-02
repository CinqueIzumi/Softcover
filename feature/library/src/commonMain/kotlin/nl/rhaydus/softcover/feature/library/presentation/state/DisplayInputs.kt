package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.util.availableFinishedYears
import nl.rhaydus.softcover.feature.library.presentation.util.totalPages

/**
 * The slice of [LibraryUiState] that the per-tab display lists derive from — the source lists plus
 * every search/year/filter/sort input. [DisplayListsCollector] maps state to this bundle and gates
 * recomputation with `distinctUntilChanged`, so the output fields are deliberately absent (a
 * self-write can't retrigger the derivation).
 */
internal data class DisplayInputs(
    val booksByTab: Map<String, List<Book>>,
    val editionsByTab: Map<String, List<BookEdition>>,
    val searchQuery: String,
    val selectedReadYear: Int?,
    val filtersByTab: Map<String, LibraryFilters>,
    val sortModeByTab: Map<String, LibrarySortMode>,
    val sortDirectionByTab: Map<String, SortDirection>,
    val addedAtByTab: Map<String, Map<Int, String?>>,
    val bookByBookId: Map<Int, Book>,
) {
    /**
     * Runs the search + year + filter (+ edition sort) derivation for every tab. [prevBooksByTab] /
     * [prevEditionsByTab] carry the previous emission so a tab whose recomputed list is structurally
     * equal reuses the prior instance — keeping downstream `remember(list)` blocks from invalidating
     * when nothing about that tab actually changed.
     */
    fun compute(
        prevBooksByTab: Map<String, List<Book>>,
        prevEditionsByTab: Map<String, List<BookEdition>>,
    ): DisplayResult {
        val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id
        val tabStats = mutableMapOf<String, LibraryTabStats>()

        val newBooksByTab = booksByTab.mapValues { (tabId, raw) ->
            val computed = computeDisplayBooks(
                raw = raw,
                query = searchQuery,
                isReadTab = tabId == readTabId,
                selectedReadYear = selectedReadYear,
                filters = filtersByTab[tabId] ?: LibraryFilters(),
            )
            val prev = prevBooksByTab[tabId]
            val stable = if (computed == prev) prev else computed

            tabStats[tabId] = LibraryTabStats(
                itemCount = stable.size,
                totalPages = stable.totalPages(),
            )

            stable
        }

        val newEditionsByTab = editionsByTab.mapValues { (tabId, raw) ->
            val mode = sortModeByTab[tabId] ?: LibraryTab.defaultSortMode(tabId = tabId)
            val computed = computeDisplayEditions(
                raw = raw,
                query = searchQuery,
                mode = mode,
                direction = sortDirectionByTab[tabId] ?: mode.defaultDirection,
                addedAtByEditionId = addedAtByTab[tabId].orEmpty(),
                filters = filtersByTab[tabId] ?: LibraryFilters(),
                bookByBookId = bookByBookId,
            )
            val prev = prevEditionsByTab[tabId]
            val stable = if (computed == prev) prev else computed

            tabStats[tabId] = LibraryTabStats(
                itemCount = stable.size,
                totalPages = 0,
            )

            stable
        }

        return DisplayResult(
            displayBooksByTab = newBooksByTab,
            displayEditionsByTab = newEditionsByTab,
            tabStatsByTab = tabStats,
            availableReadYearsCached = booksByTab[readTabId]?.availableFinishedYears().orEmpty(),
        )
    }
}
