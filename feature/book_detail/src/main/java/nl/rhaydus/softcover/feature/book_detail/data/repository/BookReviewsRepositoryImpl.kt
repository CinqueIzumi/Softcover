package nl.rhaydus.softcover.feature.book_detail.data.repository

import nl.rhaydus.softcover.feature.book_detail.data.datasource.BookReviewsRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.repository.BookReviewsRepository

class BookReviewsRepositoryImpl(
    private val bookReviewsRemoteDataSource: BookReviewsRemoteDataSource,
) : BookReviewsRepository {
    override suspend fun getTopReviewsForBook(bookId: Int): List<BookReview> =
        bookReviewsRemoteDataSource.getTopReviewsForBook(bookId = bookId)
}
