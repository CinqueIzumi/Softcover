package nl.rhaydus.softcover.feature.explore.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.model.SeriesContinuationSeed

interface SearchRemoteDataSource {
    val queriedBooks: Flow<List<Book>>

    /**
     * Whether a further page might exist for the search currently backing [queriedBooks] - the
     * best signal available, since the Typesense `search` endpoint returns no total hit count
     * (see [searchForName]'s doc). True until a fetch comes back short of a full page.
     */
    val queriedBooksHasMore: Flow<Boolean>

    /**
     * [page] is 1-based. Page 1 replaces [queriedBooks]; any later page appends to it. The
     * Typesense-backed `search` endpoint returns no total count, so [queriedBooksHasMore] is
     * derived from whether the page came back full, not from an authoritative total.
     */
    suspend fun searchForName(
        name: String,
        userId: Int,
        sortMode: ExploreSortMode,
        page: Int = 1,
    )

    suspend fun fetchNextInSeries(
        seriesId: Int,
        afterPosition: Double,
    ): Book?

    /**
     * Batched sibling of [fetchNextInSeries]: resolves the next-unread-book cursor for every
     * [seeds] entry in a single request instead of one request per seed, at the cost of failing
     * (or succeeding) as a whole rather than per series - see the call site in
     * `GetContinueSeriesBooksUseCase.fetchNextBooks` for why that trade is worth it. Returns at
     * most one [Book] per seed, in the same order as [seeds]; a seed whose series has no book past
     * its cursor is simply absent from the result.
     */
    suspend fun fetchNextBooksInSeries(seeds: List<SeriesContinuationSeed>): List<Book>

    suspend fun fetchFeaturedUpcomingRelease(): Book?

    /**
     * Books carrying [genre], ranked by genre-specific strength rather than global popularity
     * (see `GetBooksByGenreTag.graphql`). [limit] should overfetch past the caller's display size,
     * since the caller excludes the user's own shelved/listed books client-side afterward. Can
     * return the same book more than once (a book can carry a genre tag through more than one
     * `taggable_counts` row), so callers should dedupe by id - this already does.
     */
    suspend fun fetchBooksByGenre(
        genre: String,
        limit: Int,
    ): List<Book>

    suspend fun fetchMoodTags(limit: Int): List<MoodTag>

    /** [page] is 1-based, same replace/append contract as [searchForName]. */
    suspend fun searchByMood(
        mood: MoodTag,
        page: Int = 1,
    )

    /**
     * Resets [queriedBooks] to empty and [queriedBooksHasMore] to true, independent of any
     * in-flight search. Without this, a cleared search leaves the previous result list sitting in
     * [queriedBooks] - so an identical subsequent search (e.g. re-tapping the same mood) can
     * recompute an equal list, which a `StateFlow` dedups, and the result never reaches the UI.
     */
    suspend fun clearSearchResults()
}
