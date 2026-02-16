package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class MarkBookAsReadingUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(book: Book): Result<Unit> = runCatching {
        val updatedBook: Book = booksRepository.markBookAsReading(book = book)

        booksRepository.cacheBook(book = updatedBook)
    }
}