package nl.rhaydus.softcover.feature.book_detail.domain.repository

import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview

interface BookReviewsRepository {
    suspend fun getTopReviewsForBook(bookId: Int): List<BookReview>
}
