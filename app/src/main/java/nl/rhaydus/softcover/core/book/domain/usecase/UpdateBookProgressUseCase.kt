package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

class UpdateBookProgressUseCase(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
    ): Result<Unit> = runCatching {
        val updatedBook = repository.updateBookProgress(
            book = book,
            newPage = newPage,
            newSeconds = newSeconds,
        )

        repository.cacheBook(book = updatedBook)
    }
}
