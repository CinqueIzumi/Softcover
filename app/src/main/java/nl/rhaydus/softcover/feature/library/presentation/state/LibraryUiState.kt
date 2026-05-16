package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.sort.applyEditionSort
import nl.rhaydus.softcover.feature.library.presentation.sort.applySort
import nl.rhaydus.softcover.feature.library.presentation.util.availableFinishedYears
import nl.rhaydus.softcover.feature.library.presentation.util.finishedYear
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode

data class LibraryUiState(
    val visibleTabs: List<LibraryTab> = listOf(
        LibraryTab.All,
        LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING),
    ),
    val selectedTabId: String = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id,
    val booksByTab: Map<String, List<Book>> = emptyMap(),
    val editionsByTab: Map<String, List<BookEdition>> = emptyMap(),

    val isLoading: Boolean = true,
    val gridLayout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
    val isLayoutMenuExpanded: Boolean = false,
    val sortModeByTab: Map<String, LibrarySortMode> = emptyMap(),
    val isSortMenuExpanded: Boolean = false,

    val deadlines: Map<Int, BookDeadline> = emptyMap(),
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,

    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val selectedReadYear: Int? = null,
) : UiState {
    fun sortModeFor(tabId: String): LibrarySortMode =
        sortModeByTab[tabId] ?: LibrarySortMode.Default

    /**
     * Books to render for [tabId], with the active search query, the Read-tab year chip, and the
     * per-tab sort already applied. Returns `null` when the source list hasn't been collected yet
     * (use this to distinguish "loading" from "empty"); returns an empty list when the source is
     * loaded but filters narrowed it to nothing.
     */
    fun displayBooksFor(tabId: String): List<Book>? {
        val raw = booksByTab[tabId] ?: return null

        val query = searchQuery.trim()

        val searchFiltered = if (query.isEmpty()) {
            raw
        } else {
            raw.filter { book ->
                book.title.contains(query, ignoreCase = true) ||
                    book.authors.any { it.name.contains(query, ignoreCase = true) }
            }
        }

        val isReadTab = tabId == LibraryTab.Status.of(UserBookStatus.READ).id

        val yearFiltered = if (isReadTab && selectedReadYear != null) {
            searchFiltered.filter { it.finishedYear() == selectedReadYear }
        } else {
            searchFiltered
        }

        return yearFiltered.applySort(
            mode = sortModeFor(tabId = tabId),
            deadlines = deadlines,
        )
    }

    /**
     * Editions to render for [tabId] (custom lists only), with search + sort applied. Returns
     * `null` when the source list hasn't been collected yet.
     */
    fun displayEditionsFor(tabId: String): List<BookEdition>? {
        val raw = editionsByTab[tabId] ?: return null

        val query = searchQuery.trim()

        val searchFiltered = if (query.isEmpty()) {
            raw
        } else {
            raw.filter { edition ->
                edition.title.orEmpty().contains(query, ignoreCase = true) ||
                    edition.authors.any { it.name.contains(query, ignoreCase = true) }
            }
        }

        return searchFiltered.applyEditionSort(mode = sortModeFor(tabId = tabId))
    }

    /** Years that the Read tab can be filtered to, computed from raw (unfiltered) Read books. */
    val availableReadYears: List<Int>
        get() {
            val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id

            return booksByTab[readTabId]?.availableFinishedYears().orEmpty()
        }
}
