package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus

class MarkBookAsReadUseCase(
    private val repository: BooksRepository,
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
    }
}
