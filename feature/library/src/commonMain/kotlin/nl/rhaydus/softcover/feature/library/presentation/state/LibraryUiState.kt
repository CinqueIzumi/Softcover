package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sidebar.SidebarUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.uibinding.masthead.mastheadRefreshFor
import nl.rhaydus.softcover.core.uibinding.session.ConfirmedPause
import nl.rhaydus.softcover.feature.library.presentation.collector.MastheadSubtitleStandIn
import nl.rhaydus.softcover.feature.library.presentation.collector.emptyStatesFor
import nl.rhaydus.softcover.feature.library.presentation.collector.shelfSidebarFor
import nl.rhaydus.softcover.feature.library.presentation.mapper.standInCards
import nl.rhaydus.toad.UiState

internal data class LibraryUiState(
    val visibleTabs: ImmutableList<LibraryTab> = persistentListOf(
        LibraryTab.All,
        LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING),
    ),
    val tabsLoaded: Boolean = false,
    val selectedTabId: String = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id,

    /** Static text, so the default below is its own source of truth — no collector sets it. */
    val shelvesSidebarLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Shelves"),
    val listsSidebarLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Lists"),

    val shelfSidebar: SidebarUiModel<String> = shelfSidebarFor(
        visibleTabs = visibleTabs,
        selectedTabId = selectedTabId,
        shelvesSidebarLabel = shelvesSidebarLabel,
        listsSidebarLabel = listsSidebarLabel,
    ),

    val emptyStatesByTab: ImmutableMap<String, EmptyStateUiModel> = emptyStatesFor(visibleTabs),
    val booksByTab: ImmutableMap<String, ImmutableList<Book>> = persistentMapOf(),
    val editionsByTab: ImmutableMap<String, ImmutableList<BookEdition>> = persistentMapOf(),

    /**
     * Custom-list `list_books.created_at` keyed by tab id, then edition id. Populated by
     * [BookListsCollector] so the DATE_ADDED edition sort can resolve a real timestamp instead
     * of leaning on the data layer's input order — which only happens to be date-added DESC for
     * non-ranked lists and gets shuffled by position-first ordering on ranked lists.
     */
    val addedAtByTab: ImmutableMap<String, ImmutableMap<Int, String?>> = persistentMapOf(),

    /**
     * Parent [Book] lookup for editions rendered in custom-list tabs. Populated by
     * [BookListsCollector] so book-level facets (tags, rating, release year) can resolve
     * even when the surface only renders [BookEdition]s.
     */
    val bookByBookId: ImmutableMap<Int, Book> = persistentMapOf(),

    val isLoading: Boolean = true,
    val gridLayout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,

    /**
     * Whether the mobile shelf pager accepts a horizontal swipe, and with it whether the neighbour
     * rail is shown. Backed by the "Swipe between shelves" preference (Settings → Appearance →
     * Display), which is **opt-in**: the default leaves the Shelves sheet as the only switcher, so
     * the screen matches the 3.1.0 redesign until the reader asks for the gesture. Desktop ignores
     * it — its Library switches shelves from a permanent sidebar.
     */
    val shelfSwipeEnabled: Boolean = false,
    val sortModeByTab: ImmutableMap<String, LibrarySortMode> = persistentMapOf(),
    val sortDirectionByTab: ImmutableMap<String, SortDirection> = persistentMapOf(),

    /** The Arrange sheet (layout + titles toggle + sort) shown on both mobile and desktop. */
    val isArrangeSheetExpanded: Boolean = false,

    /**
     * The Arrange sheet's uncommitted draft — seeded by `OnArrangeSheetExpandedChangeAction` on
     * open, mutated by draft-edit actions, cleared on close, committed by `OnApplyArrangeAction`.
     * Null whenever the sheet is closed.
     */
    val arrangeDraft: LibraryArrangeDraft? = null,

    val arrangeSheet: SheetScaffoldUiModel = arrangeSheetScaffoldFor(resultCount = 0),

    val arrangeLayoutHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Layout"),

    /** The Arrange sheet's layout row, mapped by `ArrangeDraftChipsCollector` off [arrangeDraft]. */
    val arrangeLayoutChips: ChipSet<LibraryLayoutChip> = ChipSet(),

    /** The Arrange sheet's sort row, mapped by `ArrangeDraftChipsCollector` off [arrangeDraft]. */
    val arrangeSortChips: ChipSet<LibrarySortMode> = ChipSet(),

    /** The Arrange sheet's sort-row header, mapped by `ArrangeDraftChipsCollector` off [arrangeDraft]. */
    val arrangeSortHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Sort"),

    /** The Arrange sheet's titles toggle; null when the layout is a list. */
    val arrangeTitlesToggle: ToggleRowUiModel<String>? = null,

    /** The unranked custom list the Arrange sheet offers to make ordered; null on any other shelf. */
    val arrangeMakeOrderedListId: Int? = null,

    /** The Shelves sheet — the tab switcher on mobile; desktop keeps its permanent sidebar. */
    val shelvesSheet: SheetSession<ShelvesSheetUiModel, ManageShelves> = SheetSession.Closed,

    val isShelvesSheetRequested: Boolean = false,

    /** Set when a shelf is picked in the Shelves sheet; the layout clears it once the pager has shown that shelf. */
    val isShelvesSheetPickPending: Boolean = false,

    val filtersByTab: ImmutableMap<String, LibraryFilters> = persistentMapOf(),
    val isFilterSheetExpanded: Boolean = false,

    /**
     * The Filter sheet's uncommitted draft — seeded by `OnFilterSheetExpandedChangeAction` on open,
     * mutated by draft-edit actions, cleared on close, committed by `OnApplyFiltersAction`. Null
     * whenever the sheet is closed.
     */
    val filterDraft: LibraryFilterDraft? = null,

    /** The Filter sheet; its `selection` is `null` whenever the sheet is closed. */
    val filterSheet: LibraryFilterSheetUiModel = LibraryFilterSheetUiModel(
        scaffold = filterSheetScaffoldFor(
            resultCount = 0,
            filtersActive = false,
        ),
        selection = null,
        tagSearchField = tagSearchFieldFor(query = ""),
    ),

    /**
     * Filter picker options per tab, precomputed off the main thread by `FilterOptionsCollector`
     * so opening the sheet is O(1) on the UI thread. Tabs whose books haven't been collected yet
     * are absent from this map; the lookup falls back to an empty [LibraryFilterOptions].
     */
    val filterOptionsByTab: ImmutableMap<String, LibraryFilterOptions> = persistentMapOf(),

    val filterChipsByTab: ImmutableMap<String, LibraryFilterChips> = persistentMapOf(),

    val activeFilterChipsByTab: ImmutableMap<String, LibraryActiveFilterChips> = persistentMapOf(),
    val rearrangeChipByTab: ImmutableMap<String, ChipUiModel> = persistentMapOf(),

    /** The control line's Filter pill per tab, mapped by `PillButtonsCollector`; the dot marks active filters. */
    val filterPillByTab: ImmutableMap<String, PillButtonUiModel> = persistentMapOf(),
    val inactiveFilterPill: PillButtonUiModel = filterPillButton(active = false),
    val selectPill: PillButtonUiModel = selectPillButton(),

    /** The selection header's pills, mapped by `PillButtonsCollector` off [bulkActionInProgress]. */
    val selectionPills: LibrarySelectionPills = LibrarySelectionPills.of(bulkActionInProgress = false),

    /**
     * Search + filter-chip results per tab for book shelves (year-finished narrowing lives inside
     * [LibraryFilters.readYear]), precomputed off the main thread by [DisplayListsCollector] so
     * composition just does a map lookup on every frame.
     */
    val displayBooksByTab: ImmutableMap<String, ImmutableList<Book>> = persistentMapOf(),

    /**
     * Search + sort + filter results per tab for custom-list (edition) shelves, precomputed off
     * the main thread by [DisplayListsCollector].
     */
    val displayEditionsByTab: ImmutableMap<String, ImmutableList<BookEdition>> = persistentMapOf(),

    /**
     * Tab-level stats (count + pages) keyed by tab id, precomputed off the main thread by
     * [DisplayListsCollector].
     */
    val tabStatsByTab: ImmutableMap<String, LibraryTabStats> = persistentMapOf(),

    /**
     * Masthead copy per visible tab, mapped by `MastheadCollector` off each tab's stats and
     * [isLoading]. Resolve through [mastheadFor] rather than indexing directly — a tab id absent
     * here (cold start, before `VisibleTabsCollector` populates [visibleTabs]) falls back to
     * [fallbackMasthead].
     */
    val mastheads: ImmutableMap<String, PageMastheadUiModel> = persistentMapOf(),

    /** [mastheadFor]'s result when there is no tab yet, mapped by `MastheadCollector` off [isLoading]. */
    val fallbackMasthead: PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = "Refreshing your shelf…",
        title = "Library",
        subtitle = MastheadSubtitleStandIn,
        isLoading = true,
    ),

    /** The masthead's trailing refresh action, mapped by `MastheadCollector` off [isLoading]. */
    val mastheadRefresh: MastheadRefreshUiModel = mastheadRefreshFor(isLoading = true),

    val deadlines: ImmutableMap<Int, BookDeadline> = persistentMapOf(),
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,

    val searchQuery: String = "",

    /**
     * Bulk-select mode. Entered by long-pressing a cover on a built-in shelf tab (All or Status);
     * exits when the user taps the close button, presses back, switches tabs, or deselects the
     * last book. Custom-list tabs do not support selection — their entries are editions, which
     * don't have shelf semantics.
     */
    val selectionMode: Boolean = false,
    val selectedBookIds: ImmutableSet<Int> = persistentSetOf(),

    val selectionCount: StatNumberUiModel = selectionCountOf(count = 0),

    val bookCardsByTab: ImmutableMap<String, LibraryTabCards<Book>> = persistentMapOf(),

    /** Edition cards per custom-list tab id in the order the tab shows them; same shape as [bookCardsByTab]. */
    val editionCardsByTab: ImmutableMap<String, LibraryTabCards<BookEdition>> = persistentMapOf(),

    /** Stand-in cards in [gridLayout], rendered by a tab with no entry in [bookCardsByTab] or [editionCardsByTab] yet. */
    val loadingCards: LibraryTabCards<Nothing> = LibraryTabCards(
        layout = gridLayout,
        set = BookCardSet.standIns(gridLayout.standInCards()),
    ),

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
    val bulkRemoveSheet: SheetSession<SheetScaffoldUiModel, Nothing> = SheetSession.Closed,

    val isBulkRemoveSheetRequested: Boolean = false,

    val activeSessionBookId: Int? = null,

    /** The books a bulk "Move to Paused" wants to pause while their session is running; non-empty requests the confirm. */
    val endSessionPauseBookIds: ImmutableSet<Int> = persistentSetOf(),

    val endSessionPauseSheet: SheetSession<SheetScaffoldUiModel, ConfirmedPause> = SheetSession.Closed,

    /**
     * The user's full set of custom lists (unfiltered by the enabled-tabs setting), populated by
     * [BookListsCollector] so the bulk "Add to list" sheet can offer every list the user owns —
     * not just the ones surfaced as Library tabs.
     */
    val customLists: ImmutableList<BookList> = persistentListOf(),

    /** List ids whose bulk add/remove mutation is in flight; rows render a spinner instead of the indicator. */
    val listsBeingMutated: ImmutableSet<Int> = persistentSetOf(),

    val chooseListsSheet: SheetSession<ChooseListsUiModel, CreateList> = SheetSession.Closed,

    val isChooseListsSheetRequested: Boolean = false,

    val recommendedBooks: ImmutableList<Book> = persistentListOf(),
    val loadingRecommendedBooks: Boolean = true,
    val recommendedHeader: SectionHeaderUiModel = SystemVibe.Recommendations.toRecommendedHeader(isLoading = true),

    val recommendedRailByTab: ImmutableMap<String, BookCardSet<Book>> = persistentMapOf(),
) : UiState {
    fun sortModeFor(tabId: String): LibrarySortMode =
        resolveSortMode(
            sortModeByTab = sortModeByTab,
            tabId = tabId,
        )

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
     * Filter sheet chip models for [tabId]. Falls back to an empty [LibraryFilterChips] for the same
     * cold-start/not-yet-collected reasons as [availableFilterOptionsFor].
     */
    fun filterChipsFor(tabId: String): LibraryFilterChips =
        filterChipsByTab[tabId] ?: LibraryFilterChips()

    fun activeFilterChipsFor(tabId: String): LibraryActiveFilterChips =
        activeFilterChipsByTab[tabId] ?: LibraryActiveFilterChips()

    fun rearrangeChipFor(tabId: String): ChipUiModel? =
        rearrangeChipByTab[tabId]

    fun filterPillFor(tabId: String): PillButtonUiModel =
        filterPillByTab[tabId] ?: inactiveFilterPill

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

    fun mastheadFor(tabId: String?): PageMastheadUiModel = tabId?.let(mastheads::get) ?: fallbackMasthead

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
}

/** The only writer of [LibraryUiState.selectedBookIds]; keeps [LibraryUiState.selectionCount] in the same transition. */
internal fun LibraryUiState.withSelectedBookIds(ids: Set<Int>): LibraryUiState =
    copy(
        selectedBookIds = ids.toImmutableSet(),
        selectionCount = selectionCountOf(count = ids.size),
    )
