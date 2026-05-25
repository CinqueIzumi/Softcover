package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class MarkBookAsReadUseCase(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(book: Book): Result<ShelfMutationOutcome> = runCatching {
        if (book.status == BookStatus.Read) return@runCatching ShelfMutationOutcome.NoChange

        val updatedBook = repository.markBookAsRead(book = book)

        repository.cacheBook(book = updatedBook)

        ShelfMutationOutcome.Applied
    }
}
