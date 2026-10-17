package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.model.StagedBookRemoval
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

class StageBookRemovalUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(books: List<Book>): Result<StagedBookRemoval> = runCatchingLogged {
        booksRepository.stageBookRemoval(books = books)
    }
}
