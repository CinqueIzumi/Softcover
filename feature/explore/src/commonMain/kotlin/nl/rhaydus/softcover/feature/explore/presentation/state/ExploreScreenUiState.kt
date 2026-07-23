package nl.rhaydus.softcover.feature.explore.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.toad.UiState

internal data class ExploreScreenUiState(
    val previousSearchQueries: List<String> = emptyList(),
    val queriedBooks: List<Book> = emptyList(),
    // Search paging (explore-3a feedback item 7). The Typesense `search` endpoint returns no
    // total hit count, so `queriedBooksHasMore` is a "maybe more" signal derived from whether the
    // last fetched page came back full, not an authoritative total. `loadingMoreQueriedBooks` is
    // the append-spinner flag, kept separate from `isLoading` (the full-screen/replace spinner) so
    // the UI can tell an initial search from a "load more" tail fetch.
    val queriedBooksHasMore: Boolean = true,
    val loadingMoreQueriedBooks: Boolean = false,
    val trendingBooks: List<Book> = emptyList(),
    val loadingTrendingBooks: Boolean = true,
    val continueSeriesBooks: List<Book> = emptyList(),
    val loadingContinueSeriesBooks: Boolean = true,
    val searchText: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchError: String? = null,
    // Search chrome / focus state (explore-3a §4 "Search chrome" + "Search focus state").
    val searchFocused: Boolean = false,
    val sortMode: ExploreSortMode = ExploreSortMode.RELEVANCE,
    // Featured upcoming release card (explore-3a §4 "Featured card").
    val featuredUpcomingRelease: Book? = null,
    val loadingFeaturedUpcomingRelease: Boolean = true,
    // "Because you read {genre}" rail (explore-3a deviation 2, made user-selectable by feedback
    // item 10). `becauseYouReadGenre` is the *effective* genre - the user's persisted choice when
    // one exists, otherwise the auto-derived most-read genre - and null hides the section.
    // `becauseYouReadGenreOptions` is the picker's choice list: the distinct Genre-category tags
    // across the user's own books, same derivation as Library's `LibraryFilterOptionsBuilder`.
    val becauseYouReadGenre: String? = null,
    val becauseYouReadGenreOptions: List<String> = emptyList(),
    val becauseYouReadBooks: List<Book> = emptyList(),
    val loadingBecauseYouReadBooks: Boolean = true,
    // Browse-by-mood grid + the active mood filter, when a mood tile/chip drove the current
    // results (see the "Modelling choice" note on [ExploreSearchPhase]).
    val moodTags: List<MoodTag> = emptyList(),
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
