package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class UpdateBookReviewUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        review: ReviewDocument,
        hasSpoilers: Boolean,
    ): Result<Unit> = runCatching {
        book.userBook ?: return@runCatching

        val updatedBook: Book = booksRepository.updateBookReview(
            book = book,
            review = review,
            hasSpoilers = hasSpoilers,
        )

        booksRepository.cacheBook(book = updatedBook)
    }
}
