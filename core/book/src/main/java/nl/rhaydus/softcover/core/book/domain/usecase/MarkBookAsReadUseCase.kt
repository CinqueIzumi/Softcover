package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus

class MarkBookAsReadUseCase(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        editionId: Int? = null,
    ): Result<ShelfMutationOutcome> = runCatching {
        if (book.status == BookStatus.Read) return@runCatching ShelfMutationOutcome.NoChange

        val updatedBook = repository.markBookAsRead(
            book = book,
            editionId = editionId,
        )

        repository.cacheBook(book = updatedBook)

        ShelfMutationOutcome.Applied
    }
}
