package nl.rhaydus.softcover.feature.books.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class UpdateBookProgressUseCase(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        newPage: Int,
    ): Result<Unit> = runCatching {
        val updatedBook = repository.updateBookProgress(
            book = book,
            newPage = newPage,
        )

        repository.cacheBook(book = updatedBook)
    }
}