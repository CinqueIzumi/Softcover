package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class GetEditionsByBookIdUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<List<BookEdition>> = runCatching {
        booksRepository.getEditionsByBookId(bookId = bookId)
    }
}
