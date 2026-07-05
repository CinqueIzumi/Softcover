package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.BookEdition

class GetEditionsByBookIdUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<List<BookEdition>> = runCatchingLogged {
        booksRepository.getEditionsByBookId(bookId = bookId)
    }
}
