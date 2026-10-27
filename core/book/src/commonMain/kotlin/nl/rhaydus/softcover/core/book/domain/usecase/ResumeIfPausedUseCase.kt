package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus

class ResumeIfPausedUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(book: Book): Result<Book> = runCatchingLogged {
        if (book.status != BookStatus.Paused) return@runCatchingLogged book

        booksRepository.markBookAsReading(book = book)
    }
}
