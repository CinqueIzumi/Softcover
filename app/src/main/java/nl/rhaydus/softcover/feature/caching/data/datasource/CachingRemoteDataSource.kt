package nl.rhaydus.softcover.feature.caching.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetUserBooksQuery
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.data.mapper.toBook

interface CachingRemoteDataSource {
    suspend fun initializeBooks(userId: Int): List<Book>
}

class CachingRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : CachingRemoteDataSource {
    override suspend fun initializeBooks(userId: Int): List<Book> {
        val result = apolloClient
            .query(query = GetUserBooksQuery(userId = userId))
            .execute()
            .dataOrThrow()

        return result.user_books.map {
            it.userBookFragment.toBook()
        }
    }
}