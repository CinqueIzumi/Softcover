package nl.rhaydus.softcover.feature.explore.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.mood.MoodTileUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.topbar.SearchTopBarUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.masthead.mastheadRefreshFor
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.mapper.BECAUSE_YOU_READ_HEADER_STAND_IN
import nl.rhaydus.softcover.feature.explore.presentation.mapper.BECAUSE_YOU_READ_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.FEATURED_CARD_STAND_IN
import nl.rhaydus.softcover.feature.explore.presentation.mapper.FEATURED_HEADER
import nl.rhaydus.softcover.feature.explore.presentation.mapper.SEARCH_RESULT_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.TRENDING_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.UP_NEXT_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.noSearchResultsEmptyState
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toSortPillButton
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesMenuSheetUiModel
import nl.rhaydus.softcover.feature.explore.presentation.model.FeaturedCardUiModel
import nl.rhaydus.toad.UiState

internal data class ExploreScreenUiState(
    val previousSearchQueries: List<String> = emptyList(),
    val recentSearchChips: ChipSet<String> = ChipSet(),
    val queriedBooks: List<Book> = emptyList(),
    val featuredCover: CoverUiModel? = null,
    // One map keyed by book id: an overlay carries no per-rail `sharedTransitionKey` surface, so the
    // same book yields an identical overlay everywhere it appears. `featuredReleaseBadge` does not
    // gate on `isUnreleased` - the featured hero is by construction the upcoming-release card, so it
    // badges on `effectiveReleaseDate` alone.
    val unreleasedCoverOverlays: Map<Int, CoverOverlayUiModel> = emptyMap(),
    val featuredReleaseBadge: BadgeUiModel? = null,
    // Loading stand-ins until the surface has loaded.
    val trending: BookCardSet<Book> = BookCardSet.standIns(TRENDING_STAND_IN_CARDS),
    val becauseYouRead: BookCardSet<Book> = BookCardSet.standIns(BECAUSE_YOU_READ_STAND_IN_CARDS),
    val upNext: BookCardSet<Book> = BookCardSet.standIns(UP_NEXT_STAND_IN_CARDS),
    // Loading stand-ins while a search runs or its first result's cover is pending.
    val searchResults: BookCardSet<Book> = BookCardSet.standIns(SEARCH_RESULT_STAND_IN_CARDS),
    // The Typesense `search` endpoint returns no total hit count, so `queriedBooksHasMore` is a
    // "maybe more" signal derived from whether the last fetched page came back full, not an
    // authoritative total. `loadingMoreQueriedBooks` is
    // the append-spinner flag, kept separate from `isLoading` (the full-screen/replace spinner) so
    // the UI can tell an initial search from a "load more" tail fetch.
    val queriedBooksHasMore: Boolean = true,
    val loadingMoreQueriedBooks: Boolean = false,
    val trendingBooks: List<Book> = emptyList(),
    val loadingTrendingBooks: Boolean = true,
    val trendingHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Trending this week",
        headline = "What everyone's reading",
    ),
    val continueSeriesBooks: List<Book> = emptyList(),
    val loadingContinueSeriesBooks: Boolean = true,
    val upNextHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Pick up where you left off",
        headline = "Up next in your series",
    ),
    val continueSeriesMenuSheets: ImmutableMap<Int, ContinueSeriesMenuSheetUiModel> = persistentMapOf(),
    /** `null` until the library's offline copy has resolved. */
    val offlineEmptyState: EmptyStateUiModel? = null,
    val masthead: PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = "Discover",
        title = "Explore",
        subtitle = "Trending books, the next in your series, and anything you search for.",
    ),
    val mastheadRefresh: MastheadRefreshUiModel = mastheadRefreshFor(isLoading = false),
    val searchText: String = "",
    val searchTopBar: SearchTopBarUiModel = SearchTopBarUiModel(
        query = "",
        active = false,
        focused = false,
        isLoading = false,
    ),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchError: String? = null,
    val noSearchResultsEmptyState: EmptyStateUiModel = noSearchResultsEmptyState(),
    val searchFocused: Boolean = false,
    val sortMode: ExploreSortMode = ExploreSortMode.RELEVANCE,
    // The sort control, mapped by `SortPillCollector` off [sortMode]; [isSortMenuExpanded] is the menu it anchors.
    val sortPill: PillButtonUiModel = ExploreSortMode.RELEVANCE.toSortPillButton(),
    val isSortMenuExpanded: Boolean = false,
    val featuredUpcomingRelease: Book? = null,
    val loadingFeaturedUpcomingRelease: Boolean = true,
    val featuredHeader: SectionHeaderUiModel = FEATURED_HEADER,
    // Built off the featured fields above by `FeaturedCardCollector`; `null` collapses the card.
    val featuredCard: FeaturedCardUiModel? = FEATURED_CARD_STAND_IN,
    // `becauseYouReadGenre` is the *effective* genre - the user's persisted choice when
    // one exists, otherwise the auto-derived most-read genre - and null hides the section.
    // `becauseYouReadGenreOptions` is the picker's choice list: the distinct Genre-category tags
    // across the user's own books, same derivation as Library's `LibraryFilterOptionsBuilder`.
    val becauseYouReadGenre: String? = null,
    /** A loading stand-in until a genre resolves; the rail is hidden by a `null` [becauseYouReadGenre], not by this. */
    val becauseYouReadHeader: SectionHeaderUiModel = BECAUSE_YOU_READ_HEADER_STAND_IN,
    val becauseYouReadGenreOptions: List<String> = emptyList(),
    val becauseYouReadGenreSheet: SheetScaffoldUiModel = becauseYouReadGenreSheetScaffold(),
    /** A `null` key is the "Auto (most-read)" row. */
    val becauseYouReadGenreGroup: SheetRowGroupUiModel<String?> = SheetRowGroupUiModel(rows = persistentListOf()),
    val becauseYouReadBooks: List<Book> = emptyList(),
    val loadingBecauseYouReadBooks: Boolean = true,
    // Browse-by-mood grid + the active mood filter, when a mood tile/chip drove the current
    // results (see [searchPhase]).
    val moodTags: List<MoodTag> = emptyList(),
    val moodGridHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Browse by mood",
        headline = "How do you want to feel?",
    ),
    val moodChips: ChipSet<MoodTag> = ChipSet(),
    // Built off `moodTags` by `MoodTilesCollector`.
    val moodTiles: ImmutableList<MoodTileUiModel> = persistentListOf(),
    val loadingMoodTags: Boolean = true,
    val activeMoodFilter: MoodTag? = null,
) : UiState {
    /**
     * A mood tap reuses the same
     * results surface as a text search rather than a parallel state. [activeMoodFilter] and
     * [searchText] are mutually exclusive - starting a text search or clearing it always resets
     * [activeMoodFilter] to null (see `OnQueryChangeAction`) - so [searchPhase] only ever needs to
     * check "is *a* search active", not which kind. `OnMoodChipClickAction` also writes [searchText]
     * (the tapped mood's label) but does so directly and never through
     * `OnQueryChangeAction`, so that seed can never trip its own mutual-exclusion clearing - see
     * `OnMoodChipClickAction`'s doc for the full mechanism.
     */
    val searchPhase: ExploreSearchPhase
        get() = when {
            isLoading -> ExploreSearchPhase.LOADING
            searchText.isNotEmpty() || activeMoodFilter != null -> ExploreSearchPhase.RESULTS
            searchFocused -> ExploreSearchPhase.FOCUS
            else -> ExploreSearchPhase.FEED
        }

    val showsNoSearchResults: Boolean
        get() = searchPhase == ExploreSearchPhase.RESULTS &&
            searchError == null &&
            queriedBooks.isEmpty() &&
            searchResults.cards.isEmpty()

    /**
     * True while a text or mood search occupies the screen - [ExploreSearchPhase.LOADING] and
     * [ExploreSearchPhase.RESULTS] together, the two phases that have something to clear. Derived
     * from [searchPhase] rather than re-testing the underlying fields so "a search is running" has
     * one definition: mobile's back rung, desktop's Esc rung, and desktop's results-vs-discovery
     * branch all read this.
     */
    val hasActiveSearch: Boolean
        get() = searchPhase == ExploreSearchPhase.LOADING || searchPhase == ExploreSearchPhase.RESULTS
}
