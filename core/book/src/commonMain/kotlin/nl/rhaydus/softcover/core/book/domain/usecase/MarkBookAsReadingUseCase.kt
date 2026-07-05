package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus

class MarkBookAsReadingUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(book: Book): Result<ShelfMutationOutcome> = runCatchingLogged {
        if (book.status == BookStatus.Reading) return@runCatchingLogged ShelfMutationOutcome.NoChange

        val updatedBook: Book = booksRepository.markBookAsReading(book = book)

        booksRepository.cacheBook(book = updatedBook)

        ShelfMutationOutcome.Applied
    }
}
