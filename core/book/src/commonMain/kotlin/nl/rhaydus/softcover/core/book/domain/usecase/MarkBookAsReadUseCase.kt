package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.activity.MarkReadingActivityTodayUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus

class MarkBookAsReadUseCase(
    private val repository: BooksRepository,
    private val markReadingActivityTodayUseCase: MarkReadingActivityTodayUseCase,
) {
    suspend operator fun invoke(
        book: Book,
        editionId: Int? = null,
    ): Result<ShelfMutationOutcome> = runCatchingLogged {
        if (book.status == BookStatus.Read) return@runCatchingLogged ShelfMutationOutcome.NoChange

        val updatedBook = repository.markBookAsRead(
            book = book,
            editionId = editionId,
        )

        repository.cacheBook(book = updatedBook)

        ShelfMutationOutcome.Applied
    }.onSuccess { outcome ->
        // Only a real mark counts as reading activity (skip the already-read no-op). Kept out of
        // the runCatchingLogged so a cancellation in the mark propagates instead of becoming a failure.
        if (outcome == ShelfMutationOutcome.Applied) markReadingActivityTodayUseCase()
    }
}
