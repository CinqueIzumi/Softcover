package nl.rhaydus.softcover.feature.caching.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetUserBooksQuery
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.data.mapper.toBook
import timber.log.Timber

interface CachingRemoteDataSource {
    suspend fun initializeBooks(userId: Int): List<Book>
}

class CachingRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : CachingRemoteDataSource {
    override suspend fun initializeBooks(userId: Int): List<Book> {
        val result = apolloClient
            .query(query = GetUserBooksQuery())
            .execute()
            .dataOrThrow()

        Timber.d("-=- got a result")

        val userBooks = result.me.firstOrNull()?.user_books
            ?: throw Exception("No books were found")

        return userBooks.map { it.userBookFragment.toBook() }
    }
}