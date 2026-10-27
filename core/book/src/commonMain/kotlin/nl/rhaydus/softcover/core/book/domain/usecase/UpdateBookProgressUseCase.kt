package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.activity.MarkReadingActivityUseCase
import nl.rhaydus.softcover.core.domain.model.Book

class UpdateBookProgressUseCase(
    private val repository: BooksRepository,
    private val markReadingActivityUseCase: MarkReadingActivityUseCase,
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
    }.onSuccess {
        // A follow-on side-effect, kept out of the runCatchingLogged so a cancellation in the mark
        // propagates instead of being captured as a Result.failure of the committed write.
        markReadingActivityUseCase(actionAt = actionAt)
    }
}
