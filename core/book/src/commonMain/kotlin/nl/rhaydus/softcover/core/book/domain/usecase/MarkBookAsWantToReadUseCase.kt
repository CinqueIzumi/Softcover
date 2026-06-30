package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.result.runCatchingLogged

class MarkBookAsWantToReadUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        editionId: Int? = null,
    ): Result<ShelfMutationOutcome> = runCatchingLogged {
        if (book.status == BookStatus.WantToRead) return@runCatchingLogged ShelfMutationOutcome.NoChange

        booksRepository.markBookAsWantToRead(
            book = book,
            editionId = editionId,
        )

        ShelfMutationOutcome.Applied
    }
}
