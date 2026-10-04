package nl.rhaydus.softcover.feature.explore.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
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
import nl.rhaydus.softcover.feature.explore.domain.model.FEATURED_RELEASE_WINDOW_DAYS
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toSortPillButton
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesEntryUiModel
import nl.rhaydus.toad.UiState

internal data class ExploreScreenUiState(
    val previousSearchQueries: List<String> = emptyList(),
    // Built off `previousSearchQueries` by `RecentSearchChipsCollector` (`component-contract.md`
    // § 7.2 R9).
    val recentSearchChips: ChipSet<String> = ChipSet(),
    val queriedBooks: List<Book> = emptyList(),
    // Cover UI models, keyed by book id, mapped off composition by CoverModelsCollector
    // (`component-contract.md` § 7.2 R9). Five separate maps rather than one keyed by book id:
    // the same book can appear in more than one rail at once (e.g. trending *and* because-you-
    // read), each rail stamping a different `sharedTransitionKey` surface — see ExploreShelf.kt's
    // SURFACE_* constants — and a collapsed map would hand the wrong transition key to one of the
    // two rails.
    val featuredCover: CoverUiModel? = null,
    val trendingCovers: Map<Int, CoverUiModel> = emptyMap(),
    val becauseYouReadCovers: Map<Int, CoverUiModel> = emptyMap(),
    val continueSeriesCovers: Map<Int, CoverUiModel> = emptyMap(),
    val queriedBookCovers: Map<Int, CoverUiModel> = emptyMap(),
    // Unreleased-cover-overlay UI models, mapped off composition by UnreleasedCoverOverlaysCollector
    // (`component-contract.md` § 7.2 R9). One map keyed by book id, unlike the five cover maps
    // above: an overlay carries no per-rail `sharedTransitionKey` surface, so the same book yields
    // an identical overlay everywhere it appears, and one id-keyed map is the single source of
    // truth. `featuredReleaseBadge` does not gate on `isUnreleased` - the featured hero is by
    // construction the upcoming-release card, so it badges on `effectiveReleaseDate` alone,
    // reproducing today's ExploreShelf.kt:202,236 behaviour.
    val unreleasedCoverOverlays: Map<Int, CoverOverlayUiModel> = emptyMap(),
    val featuredReleaseBadge: BadgeUiModel? = null,
    // `null` until the surface has loaded.
    val trendingCards: ImmutableList<BookCardUiModel>? = null,
    val becauseYouReadCards: ImmutableList<BookCardUiModel>? = null,
    val upNextCards: ImmutableList<BookCardUiModel>? = null,
    // `null` while a search runs or its first result's cover is pending.
    val queriedBookCards: ImmutableList<BookCardUiModel>? = null,
    // Search paging (explore-3a feedback item 7). The Typesense `search` endpoint returns no
    // total hit count, so `queriedBooksHasMore` is a "maybe more" signal derived from whether the
    // last fetched page came back full, not an authoritative total. `loadingMoreQueriedBooks` is
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
    val continueSeriesEntries: List<ContinueSeriesEntryUiModel> = emptyList(),
    /** `null` until the library's offline copy has resolved. */
    val offlineEmptyState: EmptyStateUiModel? = null,
    /** The desktop masthead and its refresh action, derived off [isRefreshing] by `MastheadCollector` (R9). */
    val masthead: PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = "Discover",
        title = "Explore",
        subtitle = "Trending books, the next in your series, and anything you search for.",
    ),
    val mastheadRefresh: MastheadRefreshUiModel = mastheadRefreshFor(isLoading = false),
    val searchText: String = "",
    /** The search chrome, derived off the four fields below by `SearchTopBarCollector` (R9). */
    val searchTopBar: SearchTopBarUiModel = SearchTopBarUiModel(
        query = "",
        active = false,
        focused = false,
        isLoading = false,
    ),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchError: String? = null,
    // Search chrome / focus state (explore-3a §4 "Search chrome" + "Search focus state").
    val searchFocused: Boolean = false,
    val sortMode: ExploreSortMode = ExploreSortMode.RELEVANCE,
    // The sort control, mapped by `SortPillCollector` off [sortMode]; [isSortMenuExpanded] is the menu it anchors.
    val sortPill: PillButtonUiModel = ExploreSortMode.RELEVANCE.toSortPillButton(),
    val isSortMenuExpanded: Boolean = false,
    // Featured upcoming release card (explore-3a §4 "Featured card").
    val featuredUpcomingRelease: Book? = null,
    val loadingFeaturedUpcomingRelease: Boolean = true,
    // The eyebrow states the whole pick rule: the window comes from the same domain constant the
    // query is bounded by, and the "N readers waiting" line on the card below supplies the ranking
    // basis, so the two together say "the most-shelved book out in the next 30 days" without
    // spending a sentence on it.
    val featuredHeader: SectionHeaderUiModel = SectionHeaderUiModel.Inline(
        eyebrow = "Most anticipated · next $FEATURED_RELEASE_WINDOW_DAYS days",
    ),
    // "Because you read {genre}" rail (explore-3a deviation 2, made user-selectable by feedback
    // item 10). `becauseYouReadGenre` is the *effective* genre - the user's persisted choice when
    // one exists, otherwise the auto-derived most-read genre - and null hides the section.
    // `becauseYouReadGenreOptions` is the picker's choice list: the distinct Genre-category tags
    // across the user's own books, same derivation as Library's `LibraryFilterOptionsBuilder`.
    val becauseYouReadGenre: String? = null,
    /** `null` hides the because-you-read rail, as a `null` [becauseYouReadGenre] does. */
    val becauseYouReadHeader: SectionHeaderUiModel? = null,
    val becauseYouReadGenreOptions: List<String> = emptyList(),
    val becauseYouReadGenreSheet: SheetScaffoldUiModel = becauseYouReadGenreSheetScaffold(),
    /** A `null` key is the "Auto (most-read)" row. */
    val becauseYouReadGenreGroup: SheetRowGroupUiModel<String?> = SheetRowGroupUiModel(rows = persistentListOf()),
    val becauseYouReadBooks: List<Book> = emptyList(),
    val loadingBecauseYouReadBooks: Boolean = true,
    // Browse-by-mood grid + the active mood filter, when a mood tile/chip drove the current
    // results (see the "Modelling choice" note on [ExploreSearchPhase]).
    val moodTags: List<MoodTag> = emptyList(),
    val moodGridHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Browse by mood",
        headline = "How do you want to feel?",
    ),
    // Built off `moodTags` by `MoodChipsCollector` (`component-contract.md` § 7.2 R9).
    val moodChips: ChipSet<MoodTag> = ChipSet(),
    // Built off `moodTags` by `MoodTilesCollector`.
    val moodTiles: ImmutableList<MoodTileUiModel> = persistentListOf(),
    val loadingMoodTags: Boolean = true,
    val activeMoodFilter: MoodTag? = null,
) : UiState {
    /**
     * Modelling choice (explore-3a §4 "Mood grid" + "Search results"): a mood tap reuses the same
     * results surface as a text search rather than a parallel state. [activeMoodFilter] and
     * [searchText] are mutually exclusive - starting a text search or clearing it always resets
     * [activeMoodFilter] to null (see `OnQueryChangeAction`) - so [searchPhase] only ever needs to
     * check "is *a* search active", not which kind. `OnMoodChipClickAction` also writes [searchText]
     * (the tapped mood's label, feedback item 6) but does so directly and never through
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
