package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import nl.rhaydus.softcover.core.domain.result.runCatchingLogged
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.repository.BookReviewsRepository

class GetTopBookReviewsUseCase(
    private val bookReviewsRepository: BookReviewsRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<List<BookReview>> = runCatchingLogged {
        bookReviewsRepository.getTopReviewsForBook(bookId = bookId)
    }
}
