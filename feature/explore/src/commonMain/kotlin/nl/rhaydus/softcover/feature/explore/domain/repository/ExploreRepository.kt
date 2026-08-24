package nl.rhaydus.softcover.feature.explore.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.model.SeriesContinuationSeed

interface ExploreRepository {
    val previousSearchQueries: Flow<List<String>>
    val queriedBooks: Flow<List<Book>>

    /** Whether a further page might exist for the search currently backing [queriedBooks]. */
    val queriedBooksHasMore: Flow<Boolean>
    val dismissedContinueSeriesIds: Flow<List<Int>>
    val dismissedContinueSeriesBooks: Flow<List<DismissedSeriesBook>>
    val dismissedContinueSeries: Flow<List<DismissedSeries>>

    suspend fun fetchNextInSeries(
        seriesId: Int,
        afterPosition: Double,
    ): Book?

    /**
     * Batched sibling of [fetchNextInSeries] used by `GetContinueSeriesBooksUseCase`: resolves
     * every seed's next-unread-book cursor in a single request. Returns an empty list without
     * issuing a request when [seeds] is empty.
     */
    suspend fun fetchNextBooksInSeries(seeds: List<SeriesContinuationSeed>): List<Book>

    /** [page] is 1-based; page 1 replaces [queriedBooks], a later page appends to it. */
    suspend fun searchForName(
        name: String,
        userId: Int,
        sortMode: ExploreSortMode,
        page: Int = 1,
    )

    /**
     * The single highest-anticipated not-yet-released book, or null when none qualifies.
     *
     * Served from a process-lifetime session cache: this is editorial content that turns over at most
     * daily, so it costs its request once per session rather than once per Explore mount. `null` is
     * itself a cacheable answer, not a miss. Caching is success-only, so a failed fetch retries.
     */
    suspend fun fetchFeaturedUpcomingRelease(): Book?

    /**
     * Books carrying [genre], ranked by genre-specific strength (see `GetBooksByGenreTag.graphql`).
     * [limit] should overfetch past the rail's display size, since the caller
     * (`GetBecauseYouReadBooksUseCase`) excludes the user's own shelved/listed books client-side
     * afterward - this no longer happens server-side.
     *
     * Served from a process-lifetime session cache keyed by [genre], not a single session flag: the
     * effective genre changes mid-session via the picker, so a blanket once-per-session cache would
     * break switching. Re-selecting a genre already seen this session is therefore free, while a new
     * genre still fetches. Caching is success-only, so a failed fetch for a genre retries on the next
     * selection of it.
     */
    suspend fun fetchBooksByGenre(
        genre: String,
        limit: Int,
    ): List<Book>

    /**
     * The top Mood tags by popularity, for the "Browse by mood" grid.
     *
     * Served from a process-lifetime session cache: editorial content that turns over at most daily,
     * so it costs its request once per session rather than once per Explore mount. Unkeyed - the query
     * takes no parameter that would vary the result. Caching is success-only, so a failed fetch retries.
     */
    suspend fun fetchMoodTags(): List<MoodTag>

    /**
     * Runs a mood-filtered book search, publishing its results through [queriedBooks]. [page] is
     * 1-based, same replace/append contract as [searchForName].
     */
    suspend fun searchByMood(
        mood: MoodTag,
        page: Int = 1,
    )

    /** Resets [queriedBooks] to empty and [queriedBooksHasMore] to true. */
    suspend fun clearSearchResults()

    suspend fun saveSearchQuery(name: String)

    suspend fun removeSearchQuery(name: String)

    suspend fun removeAllSearchQueries()

    suspend fun dismissContinueSeriesBook(book: DismissedSeriesBook)

    suspend fun dismissContinueSeries(
        seriesId: Int,
        seriesName: String? = null,
        coverUrl: String? = null,
        authorText: String? = null,
        bookCount: Int? = null,
    )

    suspend fun undoContinueSeriesBookDismissal(bookId: Int)

    suspend fun undoContinueSeriesDismissal(seriesId: Int)
}
