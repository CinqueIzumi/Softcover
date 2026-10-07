package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.model.StagedBookRemoval
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

class CommitBookRemovalUseCase(
    private val booksRepository: BooksRepository,
) {
    /** Resolves to the books whose remote delete failed; those were restored locally. */
    suspend operator fun invoke(staged: StagedBookRemoval): Result<List<Book>> = runCatchingLogged {
        booksRepository.commitBookRemoval(staged = staged)
    }
}
