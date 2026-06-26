package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.result.runCatchingLogged

/**
 * Persists a user-defined manual order for the **prefix** of a built-in shelf.
 * [prefixOrderedBookIds] is the top-down visible order of the books the user just dragged through;
 * positions beyond the prefix are intentionally left intact so untouched books (including newcomers
 * fetched from the API) keep their natural ordering at the tail.
 */
class ReorderShelfBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        status: UserBookStatus,
        prefixOrderedBookIds: List<Int>,
    ): Result<Unit> = runCatchingLogged {
        booksRepository.applyShelfManualOrderPrefix(
            status = status,
            prefixBookIds = prefixOrderedBookIds,
        )
    }
}
