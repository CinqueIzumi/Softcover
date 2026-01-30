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
            // TODO: Query could potentially be done using the 'me' query?
            .query(query = GetUserBooksQuery(userId = userId))
            .execute()
            .dataOrThrow()

        return result.user_books.map {
            it.book.bookFragment.toBook(
                userBookFragment = it.userBookFragment,
                userBookReadFragment = it.user_book_reads.firstOrNull()?.userBookReadFragment,
            )
        }
    }
}