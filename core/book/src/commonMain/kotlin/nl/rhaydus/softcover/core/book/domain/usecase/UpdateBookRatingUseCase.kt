package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

class UpdateBookRatingUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        rating: Double,
    ): Result<Unit> = runCatchingLogged {
        val userBook = book.userBook ?: return@runCatchingLogged

        if (userBook.rating == rating) return@runCatchingLogged

        val updatedBook: Book = booksRepository.updateBookRating(
            book = book,
            rating = rating,
        )

        booksRepository.cacheBook(book = updatedBook)
    }
}
