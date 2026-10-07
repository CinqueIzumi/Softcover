package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.model.StagedBookRemoval
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository

class UndoBookRemovalUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(staged: StagedBookRemoval): Result<Unit> = runCatchingLogged {
        booksRepository.undoBookRemoval(staged = staged)
    }
}
