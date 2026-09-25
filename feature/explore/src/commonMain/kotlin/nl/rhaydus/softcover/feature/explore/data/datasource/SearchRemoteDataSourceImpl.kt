package nl.rhaydus.softcover.feature.explore.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.FetchPolicy
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.GetBooksByGenreTagQuery
import nl.rhaydus.softcover.GetBooksByGenreTagQuery.Data.Taggable_count.Book.Companion.bookDetailFragment as genreBookDetailFragment
import nl.rhaydus.softcover.GetBooksByIdsQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery.Data.Book.Companion.bookDetailFragment
import nl.rhaydus.softcover.GetBooksByMoodTagQuery
import nl.rhaydus.softcover.GetBooksByMoodTagQuery.Data.Taggable_count.Book.Companion.bookDetailFragment as moodBookDetailFragment
import nl.rhaydus.softcover.GetFeaturedUpcomingReleaseQuery
import nl.rhaydus.softcover.GetFeaturedUpcomingReleaseQuery.Data.Book.Companion.bookDetailFragment as featuredBookDetailFragment
import nl.rhaydus.softcover.GetIdsForQuery
import nl.rhaydus.softcover.GetMoodTagsQuery
import nl.rhaydus.softcover.GetNextBookInSeriesQuery
import nl.rhaydus.softcover.GetNextBookInSeriesQuery.Data.Book_series.Book.Companion.bookDetailFragment as nextInSeriesBookDetailFragment
import nl.rhaydus.softcover.GetNextBooksInSeriesQuery
import nl.rhaydus.softcover.core.book.data.mapper.toBook
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.feature.explore.data.mapper.toMoodTag
import nl.rhaydus.softcover.feature.explore.data.mapper.toTypesenseSort
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.FEATURED_RELEASE_WINDOW_DAYS
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.model.SeriesContinuationSeed
import nl.rhaydus.softcover.type.Book_series_bool_exp
import nl.rhaydus.softcover.type.Float8_comparison_exp
import nl.rhaydus.softcover.type.Int_comparison_exp

private const val MOOD_BOOKS_LIMIT = 25

// Must match GetIdsForQuery.graphql's $perPage default - a page shorter than this signals the
// end of the Typesense result set, since `search` returns no total hit count to compare against.
private const val SEARCH_RESULTS_PAGE_SIZE = 25

internal class SearchRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : SearchRemoteDataSource {
    private val _queriedBooks = MutableStateFlow<List<Book>>(emptyList())
    override val queriedBooks: Flow<List<Book>> = _queriedBooks.asStateFlow()

    private val _queriedBooksHasMore = MutableStateFlow(true)
    override val queriedBooksHasMore: Flow<Boolean> = _queriedBooksHasMore.asStateFlow()

    override suspend fun searchForName(
        name: String,
        userId: Int,
        sortMode: ExploreSortMode,
        page: Int,
    ) {
        if (page <= 1) {
            _queriedBooks.update { emptyList() }
            _queriedBooksHasMore.update { true }
        }

        val matchingIds: List<Int> = apolloClient
            .safeQuery(
                query = GetIdsForQuery(
                    query = name,
                    sort = Optional.Present(sortMode.toTypesenseSort()),
                    page = if (page > 1) Optional.Present(page) else Optional.Absent,
                ),
            )
            .search
            ?.ids
            ?.mapNotNull { it } ?: throw Exception("No ids were found for given query")

        _queriedBooksHasMore.update { matchingIds.size >= SEARCH_RESULTS_PAGE_SIZE }

        if (matchingIds.isEmpty()) {
            return
        }

        val idOrdered = matchingIds.withIndex().associate { it.value to it.index }

        val books = apolloClient
            .safeQuery(
                query = GetBooksByIdsQuery(ids = matchingIds),
                fetchPolicy = FetchPolicy.CacheFirst,
            )
            .books
            .mapNotNull { it.bookDetailFragment()?.toBook() }
            .sortedBy { book -> idOrdered[book.id] }

        _queriedBooks.update { previous ->
            (if (page > 1) previous + books else books).distinctBy { it.id }
        }
    }

    override suspend fun fetchNextInSeries(
        seriesId: Int,
        afterPosition: Double,
    ): Book? {
        val response = apolloClient.safeQuery(
            query = GetNextBookInSeriesQuery(
                seriesId = seriesId,
                afterPosition = afterPosition,
            ),
            fetchPolicy = FetchPolicy.CacheFirst,
        )

        val book = response.book_series.firstOrNull()?.book ?: return null

        return book.nextInSeriesBookDetailFragment()?.toBook()
    }

    override suspend fun fetchNextBooksInSeries(seeds: List<SeriesContinuationSeed>): List<Book> {
        if (seeds.isEmpty()) return emptyList()

        val where = Book_series_bool_exp(
            _or = Optional.Present(
                seeds.map { seed ->
                    Book_series_bool_exp(
                        series_id = Optional.Present(
                            Int_comparison_exp(_eq = Optional.Present(seed.seriesId)),
                        ),
                        position = Optional.Present(
                            Float8_comparison_exp(_gt = Optional.Present(seed.afterPosition)),
                        ),
                    )
                },
            ),
        )

        val response = apolloClient.safeQuery(
            query = GetNextBooksInSeriesQuery(where = where),
            fetchPolicy = FetchPolicy.CacheFirst,
        )

        // Hasura can't limit per group, so the response holds every remaining book in every
        // requested series. Taking the first row per series_id reproduces the single-series
        // query's `limit: 1` semantics, relying on the query's ordering (position asc, then
        // users_count desc) - re-sorting by position alone here would drop that tiebreak.
        val nextBookIdBySeriesId = response.book_series
            .groupBy { it.series_id }
            .mapValues { (_, rows) -> rows.first().book_id }

        // Seeds arrive already sorted by recency, and that ordering is the shelf's ordering -
        // preserve it rather than the where-clause's undefined response order.
        val bookIds = seeds.mapNotNull { nextBookIdBySeriesId[it.seriesId] }

        if (bookIds.isEmpty()) return emptyList()

        val idOrdered = bookIds.withIndex().associate { it.value to it.index }

        return apolloClient
            .safeQuery(
                query = GetBooksByIdsQuery(ids = bookIds),
                fetchPolicy = FetchPolicy.CacheFirst,
            )
            .books
            .mapNotNull { it.bookDetailFragment()?.toBook() }
            .sortedBy { book -> idOrdered[book.id] }
    }

    override suspend fun fetchFeaturedUpcomingRelease(): Book? {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val maxDate = today.plus(
            FEATURED_RELEASE_WINDOW_DAYS,
            DateTimeUnit.DAY,
        )

        val response = apolloClient.safeQuery(
            query = GetFeaturedUpcomingReleaseQuery(
                today = today.toString(),
                maxDate = maxDate.toString(),
            ),
        )

        return response.books.firstOrNull()?.featuredBookDetailFragment()?.toBook()
    }

    override suspend fun fetchBooksByGenre(
        genre: String,
        limit: Int,
    ): List<Book> {
        val response = apolloClient.safeQuery(
            query = GetBooksByGenreTagQuery(
                genre = genre,
                limit = limit,
            ),
        )

        // A book can carry the same genre tag through more than one taggable_counts row, so this
        // can return duplicate books - dedupe here rather than pushing that onto every caller.
        return response.taggable_counts
            .mapNotNull { it.book?.genreBookDetailFragment()?.toBook() }
            .distinctBy { it.id }
    }

    override suspend fun fetchMoodTags(limit: Int): List<MoodTag> {
        val response = apolloClient.safeQuery(query = GetMoodTagsQuery(limit = limit))

        return response.tags.map { it.toMoodTag() }
    }

    override suspend fun searchByMood(
        mood: MoodTag,
        page: Int,
    ) {
        if (page <= 1) {
            _queriedBooks.update { emptyList() }
            _queriedBooksHasMore.update { true }
        }

        val offset = (page - 1).coerceAtLeast(0) * MOOD_BOOKS_LIMIT

        val response = apolloClient.safeQuery(
            query = GetBooksByMoodTagQuery(
                slug = mood.slug,
                limit = MOOD_BOOKS_LIMIT,
                offset = if (offset > 0) Optional.Present(offset) else Optional.Absent,
            ),
        )

        val books = response.taggable_counts.mapNotNull { it.book?.moodBookDetailFragment()?.toBook() }

        // hasMore is derived from the raw taggable_counts count, not the deduped books list: a
        // book can carry the same mood tag through more than one taggable_counts row, so dedupe
        // can shrink a full page below MOOD_BOOKS_LIMIT and would otherwise signal end-of-results
        // one page too early.
        _queriedBooksHasMore.update { response.taggable_counts.size >= MOOD_BOOKS_LIMIT }
        _queriedBooks.update { previous ->
            (if (page > 1) previous + books else books).distinctBy { it.id }
        }
    }

    override suspend fun clearSearchResults() {
        _queriedBooks.update { emptyList() }
        _queriedBooksHasMore.update { true }
    }
}
