package nl.rhaydus.softcover.feature.explore.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nl.rhaydus.softcover.GetBooksByIdsQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery.Data.Book.Companion.bookDetailFragment
import nl.rhaydus.softcover.GetIdsForQuery
import nl.rhaydus.softcover.GetNextBookInSeriesQuery
import nl.rhaydus.softcover.GetNextBookInSeriesQuery.Data.Book_series.Book.Companion.bookDetailFragment as nextInSeriesBookDetailFragment
import nl.rhaydus.softcover.core.book.data.mapper.toBook
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.network.helper.safeQuery

internal class SearchRemoteDataSourceImpl(
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
