package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument

class UpdateBookReviewUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        review: ReviewDocument,
        hasSpoilers: Boolean,
    ): Result<Unit> = runCatchingLogged {
        book.userBook ?: return@runCatchingLogged

        val updatedBook: Book = booksRepository.updateBookReview(
            book = book,
            review = review,
            hasSpoilers = hasSpoilers,
        )

        booksRepository.cacheBook(book = updatedBook)
    }
}
