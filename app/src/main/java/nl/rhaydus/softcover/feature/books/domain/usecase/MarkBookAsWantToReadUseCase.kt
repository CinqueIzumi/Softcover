package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class MarkBookAsWantToReadUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatching {
        val updatedBook = booksRepository.markBookAsWantToRead(bookId = bookId)

        booksRepository.cacheBook(book = updatedBook)
    }
}