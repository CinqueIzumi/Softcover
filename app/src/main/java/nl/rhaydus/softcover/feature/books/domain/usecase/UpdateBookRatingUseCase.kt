package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class UpdateBookRatingUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        rating: Double,
    ): Result<Unit> = runCatching {
        val userBook = book.userBook ?: return@runCatching

        if (userBook.rating == rating) return@runCatching

        val updatedBook: Book = booksRepository.updateBookRating(book = book, rating = rating)

        booksRepository.cacheBook(book = updatedBook)
    }
}
