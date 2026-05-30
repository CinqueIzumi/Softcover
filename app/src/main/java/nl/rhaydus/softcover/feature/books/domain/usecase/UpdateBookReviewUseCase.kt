package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class UpdateBookReviewUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        body: String,
        hasSpoilers: Boolean,
    ): Result<Unit> = runCatching {
        book.userBook ?: return@runCatching

        val updatedBook: Book = booksRepository.updateBookReview(
            book = book,
            body = body,
            hasSpoilers = hasSpoilers,
        )

        booksRepository.cacheBook(book = updatedBook)
    }
}
