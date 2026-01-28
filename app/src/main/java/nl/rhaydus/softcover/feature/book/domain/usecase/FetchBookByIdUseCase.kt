package nl.rhaydus.softcover.feature.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase

class FetchBookByIdUseCase(
    private val bookDetailRepository: BookDetailRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(id: Int): Result<Book> {
        return runCatching {
            val userId = getUserIdUseCase().getOrThrow()

            bookDetailRepository.fetchBookById(
                id = id,
                userId = userId,
            )
        }
    }
}