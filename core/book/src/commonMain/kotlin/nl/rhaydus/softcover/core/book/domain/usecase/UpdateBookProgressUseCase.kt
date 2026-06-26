package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.activity.MarkReadingActivityTodayUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.result.runCatchingLogged

class UpdateBookProgressUseCase(
    private val repository: BooksRepository,
    private val markReadingActivityTodayUseCase: MarkReadingActivityTodayUseCase,
) {
    suspend operator fun invoke(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
    ): Result<Unit> = runCatchingLogged {
        val updatedBook = repository.updateBookProgress(
            book = book,
            newPage = newPage,
            newSeconds = newSeconds,
        )

        repository.cacheBook(book = updatedBook)
    }.onSuccess {
        // A follow-on side-effect, kept out of the runCatchingLogged so a cancellation in the mark
        // propagates instead of being captured as a Result.failure of the committed write.
        markReadingActivityTodayUseCase()
    }
}
