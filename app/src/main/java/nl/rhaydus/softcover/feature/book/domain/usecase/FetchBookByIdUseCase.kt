package nl.rhaydus.softcover.feature.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository

class FetchBookByIdUseCase(
    private val bookDetailRepository: BookDetailRepository,
) {
    suspend operator fun invoke(id: Int): Result<Book> {
        return runCatching {
            bookDetailRepository.fetchBookById(id = id)
        }
    }
}