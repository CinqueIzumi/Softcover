package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.activity.MarkReadingActivityUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus

class MarkBookAsReadUseCase(
    private val repository: BooksRepository,
    private val markReadingActivityUseCase: MarkReadingActivityUseCase,
) {
    suspend operator fun invoke(
        book: Book,
        editionId: Int? = null,
        actionAt: String? = null,
    ): Result<ShelfMutationOutcome> = runCatchingLogged {
        if (book.status == BookStatus.Read) return@runCatchingLogged ShelfMutationOutcome.NoChange

        repository.markBookAsRead(
            book = book,
            editionId = book.statusChangeEditionId(requestedEditionId = editionId),
            actionAt = actionAt,
        )

        ShelfMutationOutcome.Applied
    }.onSuccess { outcome ->
        // Only a real mark counts as reading activity (skip the already-read no-op). Kept out of
        // the runCatchingLogged so a cancellation in the mark propagates instead of becoming a failure.
        if (outcome == ShelfMutationOutcome.Applied) markReadingActivityUseCase(actionAt = actionAt)
    }
}
