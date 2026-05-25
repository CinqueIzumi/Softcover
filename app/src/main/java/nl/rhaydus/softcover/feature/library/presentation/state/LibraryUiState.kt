package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.sort.applyEditionSort
import nl.rhaydus.softcover.feature.library.presentation.util.availableFinishedYears
import nl.rhaydus.softcover.feature.library.presentation.util.finishedYear
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.model.SortDirection

data class LibraryUiState(
    val visibleTabs: List<LibraryTab> = listOf(
        LibraryTab.All,
        LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING),
    ),
    val tabsLoaded: Boolean = false,
    val selectedTabId: String = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id,
    val booksByTab: Map<String, List<Book>> = emptyMap(),
    val editionsByTab: Map<String, List<BookEdition>> = emptyMap(),

    /**
     * Parent [Book] lookup for editions rendered in custom-list tabs. Populated by
     * [BookListsCollector] so book-level facets (tags, rating, release year) can resolve
     * even when the surface only renders [BookEdition]s.
     */
    val bookByBookId: Map<Int, Book> = emptyMap(),

    val isLoading: Boolean = true,
    val gridLayout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
    val isLayoutMenuExpanded: Boolean = false,
    val sortModeByTab: Map<String, LibrarySortMode> = emptyMap(),
    val sortDirectionByTab: Map<String, SortDirection> = emptyMap(),
    val isSortMenuExpanded: Boolean = false,

    val filtersByTab: Map<String, LibraryFilters> = emptyMap(),
    val isFilterSheetExpanded: Boolean = false,

    /**
     * Filter picker options per tab, precomputed off the main thread by `FilterOptionsCollector`
     * so opening the sheet is O(1) on the UI thread. Tabs whose books haven't been collected yet
     * are absent from this map; the lookup falls back to an empty [LibraryFilterOptions].
     */
    val filterOptionsByTab: Map<String, LibraryFilterOptions> = emptyMap(),

    val deadlines: Map<Int, BookDeadline> = emptyMap(),
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,

    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val selectedReadYear: Int? = null,

    /**
     * Bulk-select mode. Entered by long-pressing a cover on a built-in shelf tab (All or Status);
     * exits when the user taps the close button, presses back, switches tabs, or deselects the
     * last book. Custom-list tabs do not support selection — their entries are editions, which
     * don't have shelf semantics.
     */
    val selectionMode: Boolean = false,
    val selectedBookIds: Set<Int> = emptySet(),
    val bulkActionInProgress: Boolean = false,
    val isBulkMoveMenuExpanded: Boolean = false,
    val isBulkRemoveDialogShown: Boolean = false,
    val isBulkAddToListSheetShown: Boolean = false,

    /**
     * The user's full set of custom lists (unfiltered by the enabled-tabs setting), populated by
     * [BookListsCollector] so the bulk "Add to list" sheet can offer every list the user owns —
     * not just the ones surfaced as Library tabs.
     */
    val customLists: List<BookList> = emptyList(),

    /** List ids whose bulk add/remove mutation is in flight; rows render a spinner instead of the indicator. */
    val listsBeingMutated: Set<Int> = emptySet(),
) : UiState {
    fun sortModeFor(tabId: String): LibrarySortMode =
        sortModeByTab[tabId] ?: LibraryTab.defaultSortMode(tabId = tabId)

    fun sortDirectionFor(tabId: String): SortDirection =
        sortDirectionByTab[tabId] ?: sortModeFor(tabId = tabId).defaultDirection

    fun filtersFor(tabId: String): LibraryFilters =
        filtersByTab[tabId] ?: LibraryFilters()

    /**
     * Filter picker options for [tabId]. Reads from the precomputed [filterOptionsByTab] map —
     * if the collector has not produced options for this tab yet (cold start, or the tab's books
     * just landed and the worker hasn't caught up), returns an empty [LibraryFilterOptions] so the
     * picker can show its empty-facet message instead of stalling the UI thread.
     */
    fun availableFilterOptionsFor(tabId: String): LibraryFilterOptions =
        filterOptionsByTab[tabId] ?: LibraryFilterOptions()

    /**
     * Books to render for [tabId]. The list in [booksByTab] is already sorted by the DAO via
     * SQL `ORDER BY` (see `BooksLocalDataSource.getSortedAllUserBooks` and friends), so this
     * pass only needs to apply the in-memory search, Read-tab year filter, and the filter chips.
     *
     * Returns `null` when the source list hasn't been collected yet (so callers can distinguish
     * "loading" from "loaded but empty"); returns an empty list when filters narrowed it to nothing.
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

        val filters = filtersFor(tabId = tabId)

        return if (filters.isEmpty) yearFiltered else yearFiltered.filter { filters.matchesBook(book = it) }
    }

    /**
     * Editions to render for [tabId] (custom lists only), with search + sort + filters applied
     * in memory. Custom-list edition counts are small enough that the in-memory sort is fine —
     * the SQL sort path is books-only. Returns `null` when the source list hasn't been collected yet.
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

        val sorted = searchFiltered.applyEditionSort(
            mode = sortModeFor(tabId = tabId),
            direction = sortDirectionFor(tabId = tabId),
        )

        val filters = filtersFor(tabId = tabId)

        return if (filters.isEmpty) {
            sorted
        } else {
            sorted.filter { edition ->
                filters.matchesEdition(
                    edition = edition,
                    book = bookByBookId[edition.bookId],
                )
            }
        }
    }

    /**
     * Resolves [selectedBookIds] to the underlying [Book] domain models so action handlers can hand
     * them to use cases that operate on [Book] rather than just id. Looks across every collected
     * shelf list plus the custom-list parent lookup so a selection survives a tab being
     * recomputed mid-flight.
     */
    fun resolveSelectedBooks(): List<Book> {
        if (selectedBookIds.isEmpty()) return emptyList()

        val byId: Map<Int, Book> = booksByTab.values.asSequence()
            .flatten()
            .associateBy { it.id } + bookByBookId

        return selectedBookIds.mapNotNull { byId[it] }
    }

    /** Years that the Read tab can be filtered to, computed from raw (unfiltered) Read books. */
    val availableReadYears: List<Int>
        get() {
            val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id

            return booksByTab[readTabId]?.availableFinishedYears().orEmpty()
        }
}
