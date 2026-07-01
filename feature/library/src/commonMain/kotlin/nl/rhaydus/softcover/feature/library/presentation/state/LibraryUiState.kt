package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.toad.UiState

internal data class LibraryUiState(
    val visibleTabs: List<LibraryTab> = listOf(
        LibraryTab.All,
        LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING),
    ),
    val tabsLoaded: Boolean = false,
    val selectedTabId: String = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id,
    val booksByTab: Map<String, List<Book>> = emptyMap(),
    val editionsByTab: Map<String, List<BookEdition>> = emptyMap(),

    /**
     * Custom-list `list_books.created_at` keyed by tab id, then edition id. Populated by
     * [BookListsCollector] so the DATE_ADDED edition sort can resolve a real timestamp instead
     * of leaning on the data layer's input order — which only happens to be date-added DESC for
     * non-ranked lists and gets shuffled by position-first ordering on ranked lists.
     */
    val addedAtByTab: Map<String, Map<Int, String?>> = emptyMap(),

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

    /**
     * Search + year + filter-chip results per tab for book shelves, precomputed off the main
     * thread by [DisplayListsCollector] so composition just does a map lookup on every frame.
     */
    val displayBooksByTab: Map<String, List<Book>> = emptyMap(),

    /**
     * Search + sort + filter results per tab for custom-list (edition) shelves, precomputed off
     * the main thread by [DisplayListsCollector].
     */
    val displayEditionsByTab: Map<String, List<BookEdition>> = emptyMap(),

    /**
     * Tab-level stats (count + pages) keyed by tab id, precomputed off the main thread by
     * [DisplayListsCollector].
     */
    val tabStatsByTab: Map<String, LibraryTabStats> = emptyMap(),

    /**
     * Available Read-tab years, precomputed off the main thread by [DisplayListsCollector] from
     * the raw (unfiltered) Read books. Empty until the collector emits its first result.
     */
    val availableReadYearsCached: List<Int> = emptyList(),

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

    /**
     * Rearrange mode. A transient editing state entered from the drag-handle icon-button in the
     * view-control strip's trailing cluster, and only meaningful while the current tab is on a
     * positional sort (MANUAL on a built-in shelf, or
     * ORDER on a ranked custom list). Outside this mode, positional sorts render display-only — the
     * saved order shows with normal tap/long-press intact and no drag handles — so scrolling can't
     * nudge the order. Mutually exclusive with [selectionMode]; cleared on tab switch, sort change,
     * and back.
     */
    val isRearranging: Boolean = false,
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
     * Books to render for [tabId], precomputed by [DisplayListsCollector] and served as an O(1)
     * map lookup. Returns `null` when the source list hasn't been collected yet **or** the
     * collector hasn't produced the display list for it yet (so callers can distinguish "still
     * preparing" from "loaded but empty"); returns an empty list only when filters narrowed a
     * computed list to nothing. Callers that already hold the raw source list fall back to it for
     * the one-frame window between raw data landing and the display list being computed.
     */
    fun displayBooksFor(tabId: String): List<Book>? =
        if (tabId in booksByTab) displayBooksByTab[tabId] else null

    /**
     * Editions to render for [tabId] (custom lists only), precomputed by [DisplayListsCollector]
     * and served as an O(1) map lookup. Returns `null` when the source list hasn't been collected
     * yet **or** the collector hasn't produced the display list for it yet; returns an empty list
     * only when filters narrowed a computed list to nothing.
     */
    fun displayEditionsFor(tabId: String): List<BookEdition>? =
        if (tabId in editionsByTab) displayEditionsByTab[tabId] else null

    fun tabStatsFor(tabId: String): LibraryTabStats =
        tabStatsByTab[tabId] ?: LibraryTabStats(
            itemCount = 0,
            totalPages = 0,
        )

    /**
     * Resolves [bookIds] (defaulting to the current [selectedBookIds]) to the underlying [Book]
     * domain models so action handlers can hand them to use cases that operate on [Book] rather than
     * just id. Looks across every collected shelf list plus the custom-list parent lookup so a
     * selection survives a tab being recomputed mid-flight. A non-default [bookIds] backs the desktop
     * per-book context menu, which acts on a single book without first entering selection mode.
     */
    fun resolveSelectedBooks(bookIds: Set<Int> = selectedBookIds): List<Book> {
        if (bookIds.isEmpty()) return emptyList()

        val byId: Map<Int, Book> = booksByTab.values.asSequence()
            .flatten()
            .associateBy { it.id } + bookByBookId

        return bookIds.mapNotNull { byId[it] }
    }

    /** Years that the Read tab can be filtered to. Reads from the precomputed [availableReadYearsCached]. */
    val availableReadYears: List<Int>
        get() = availableReadYearsCached
}
