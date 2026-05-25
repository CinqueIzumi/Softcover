package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class MarkBookAsWantToReadUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(book: Book): Result<ShelfMutationOutcome> = runCatching {
        if (book.status == BookStatus.WantToRead) return@runCatching ShelfMutationOutcome.NoChange

        booksRepository.markBookAsWantToRead(book = book)

        ShelfMutationOutcome.Applied
    }
}
