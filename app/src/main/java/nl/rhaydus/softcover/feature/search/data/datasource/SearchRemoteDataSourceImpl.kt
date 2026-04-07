package nl.rhaydus.softcover.feature.search.data.datasource

import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nl.rhaydus.softcover.GetBooksByIdsQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery.Data.Book.Companion.bookFragment
import nl.rhaydus.softcover.GetIdsForQuery
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
                query = GetBooksByIdsQuery(ids = matchingIds)
            )
            .books
            .mapNotNull { it.bookFragment()?.toBook() }
            .sortedBy { book -> idOrdered[book.id] }

        _queriedBooks.update { books }
    }
}