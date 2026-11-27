package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus

class DidNotFinishBookUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        editionId: Int? = null,
    ): Result<ShelfMutationOutcome> = runCatchingLogged {
        if (book.status == BookStatus.DidNotFinish) return@runCatchingLogged ShelfMutationOutcome.NoChange

        booksRepository.markBookAsDidNotFinish(
            book = book,
            editionId = book.statusChangeEditionId(requestedEditionId = editionId),
        )

        ShelfMutationOutcome.Applied
    }
}
