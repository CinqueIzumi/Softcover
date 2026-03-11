package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class RemoveListBookUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(book: ListBook) = runCatching {
        booksRepository.removeListBook(book = book)
    }
}