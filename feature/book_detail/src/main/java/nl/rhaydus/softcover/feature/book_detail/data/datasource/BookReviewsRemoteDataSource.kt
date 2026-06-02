package nl.rhaydus.softcover.feature.book_detail.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import nl.rhaydus.softcover.GetTopBookReviewsQuery
import nl.rhaydus.softcover.GetTopBookReviewsQuery.Data.User_book.Companion.bookReviewFragment
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.feature.book_detail.data.mapper.toBookReview
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview

interface BookReviewsRemoteDataSource {
    suspend fun getTopReviewsForBook(bookId: Int): List<BookReview>
}

class BookReviewsRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : BookReviewsRemoteDataSource {
    override suspend fun getTopReviewsForBook(bookId: Int): List<BookReview> {
        val result = apolloClient.safeQuery(
            query = GetTopBookReviewsQuery(bookId = bookId),
            fetchPolicy = FetchPolicy.CacheFirst,
        )

        return result.user_books.mapNotNull { it.bookReviewFragment()?.toBookReview() }
    }
}
