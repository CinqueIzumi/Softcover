package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

class UpdateBookProgressUseCase(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
        actionAt: String? = null,
    ): Result<Unit> = runCatchingLogged {
        repository.updateBookProgress(
            book = book,
            newPage = newPage,
            newSeconds = newSeconds,
            actionAt = actionAt,
        )

        Unit
    }
}
