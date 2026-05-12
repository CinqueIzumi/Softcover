package nl.rhaydus.softcover.feature.explore.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nl.rhaydus.softcover.GetBooksByIdsQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery.Data.Book.Companion.bookDetailFragment
import nl.rhaydus.softcover.GetIdsForQuery
import nl.rhaydus.softcover.GetNextBookInSeriesQuery
import nl.rhaydus.softcover.GetNextBookInSeriesQuery.Data.Book_series.Book.Companion.bookDetailFragment as nextInSeriesBookDetailFragment
import nl.rhaydus.softcover.GetTrendingBookIdsQuery
import nl.rhaydus.softcover.core.data.network.helper.safeQuery
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.data.mapper.toBook

class SearchRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : SearchRemoteDataSource {
    private val _queriedBooks = MutableStateFlow<List<Book>>(emptyList())
    override val queriedBooks: Flow<List<Book>> = _queriedBooks.asStateFlow()

    override suspend fun searchForName(
        name: String,
        userId: Int,
    ) {
        val matchingIds: List<Int> = apolloClient.safeQuery(query = GetIdsForQuery(query = name))
            .search
            ?.ids
            ?.mapNotNull { it } ?: throw Exception("No ids were found for given query")

        val idOrdered = matchingIds.withIndex().associate { it.value to it.index }

        val books = apolloClient
            .safeQuery(
                query = GetBooksByIdsQuery(ids = matchingIds),
                fetchPolicy = FetchPolicy.CacheFirst,
            )
            .books
            .mapNotNull { it.bookDetailFragment()?.toBook() }
            .sortedBy { book -> idOrdered[book.id] }

        _queriedBooks.update { books }
    }

    override suspend fun fetchTrendingBooks(
        from: String,
        to: String,
        limit: Int,
        offset: Int,
    ): List<Book> {
        val trendingIds: List<Int> = apolloClient.safeQuery(
            query = GetTrendingBookIdsQuery(
                from = from,
                to = to,
                limit = limit,
                offset = offset,
            )
        )
            .books_trending
            ?.ids
            ?.mapNotNull { it }
            ?: return emptyList()

        if (trendingIds.isEmpty()) return emptyList()

        val idOrdered = trendingIds.withIndex().associate { it.value to it.index }

        return apolloClient
            .safeQuery(
                query = GetBooksByIdsQuery(ids = trendingIds),
                fetchPolicy = FetchPolicy.CacheFirst,
            )
            .books
            .mapNotNull { it.bookDetailFragment()?.toBook() }
            .sortedBy { book -> idOrdered[book.id] }
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
}