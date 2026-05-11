package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class MarkBookAsWantToReadUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(book: Book): Result<Unit> = runCatching {
        booksRepository.markBookAsWantToRead(book = book)
    }
}
